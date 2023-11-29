package org.graylog.storage.opensearch2;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.inject.assistedinject.Assisted;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.graylog.shaded.opensearch2.org.opensearch.common.xcontent.json.JsonXContent;
import org.graylog.shaded.opensearch2.org.opensearch.core.common.bytes.BytesReference;
import org.graylog.shaded.opensearch2.org.opensearch.core.xcontent.ToXContent;
import org.graylog.shaded.opensearch2.org.opensearch.core.xcontent.XContentBuilder;
import org.graylog.shaded.opensearch2.org.opensearch.index.reindex.ReindexRequest;
import org.graylog.shaded.opensearch2.org.opensearch.index.reindex.RemoteInfo;
import org.graylog2.cluster.nodes.DataNodeDto;
import org.graylog2.cluster.nodes.NodeDto;
import org.graylog2.cluster.nodes.NodeService;
import org.graylog2.datanode.RemoteReindexAllowlistEvent;
import org.graylog2.events.ClusterEventBus;
import org.graylog2.indexer.IndexSet;
import org.graylog2.indexer.NoTargetIndexException;
import org.graylog2.indexer.datanode.RemoteReindexingMigrationAdapter;
import org.graylog2.indexer.indexset.CustomFieldMappings;
import org.graylog2.indexer.indexset.IndexSetConfig;
import org.graylog2.indexer.indices.Indices;
import org.graylog2.indexer.indices.TooManyAliasesException;
import org.graylog2.plugin.indexer.retention.RetentionStrategyConfig;
import org.graylog2.plugin.indexer.rotation.RotationStrategyConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.graylog.shaded.opensearch2.org.opensearch.index.query.QueryBuilders.matchAllQuery;

public class RemoteReindexingMigrationAdapterOS2 implements RemoteReindexingMigrationAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(RemoteReindexingMigrationAdapterOS2.class);

    private static final int CONNECTION_ATTEMPTS = 40;
    private static final int WAIT_BETWEEN_CONNECTION_ATTEMPTS = 3;

    private final OpenSearchClient client;
    private final OkHttpClient httpClient;
    private final NodeService<DataNodeDto> nodeService;
    private final Indices indicesService;
    private final ClusterEventBus eventBus;

    @Inject
    public RemoteReindexingMigrationAdapterOS2(final OpenSearchClient client,
                                               final OkHttpClient httpClient,
                                               final NodeService<DataNodeDto> nodeService,
                                               final Indices indicesService,
                                               final ClusterEventBus eventBus) {
        this.client = client;
        this.httpClient = httpClient;
        this.nodeService = nodeService;
        this.indicesService = indicesService;
        this.eventBus = eventBus;
    }

    @Override
    public void start(final URI uri, final String username, final String password, final List<String> indices) {
        try {
            final var toReindex = isAllIndices(indices) ? getAllIndicesFrom(uri, username, password) : indices;

            final var activeNodes = getActiveNodes();

            allowReindexingFrom(uri.getHost() + ":" + uri.getPort());

            waitForClusterRestart(activeNodes);

            reindex(uri, username, password, toReindex);
//            removeAllowlist(uri.getHost() + ":" + uri.getPort());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Status status() {
        return Status.STARTING;
    }

    Set<String> getActiveNodes() {
        return nodeService.allActive().values().stream().map(NodeDto::getNodeId).collect(Collectors.toSet());
    }

    void waitForClusterRestart(final Set<String> activeNodes) {
        // sleeping for some time to let the cluster stop so we can wait for the restart
        try {
            Thread.sleep(10 * 1000);
        } catch (InterruptedException e) {
            LOG.warn("Could not sleep...");
        }

        final var retryer = RetryerBuilder.<Boolean>newBuilder()
                .withWaitStrategy(WaitStrategies.fixedWait(WAIT_BETWEEN_CONNECTION_ATTEMPTS, TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(CONNECTION_ATTEMPTS))
                .retryIfResult(response -> !response)
                .retryIfException()
                .build();

        final Callable<Boolean> callable = () -> activeNodes.containsAll(getActiveNodes());

        try {
            var successful = retryer.call(callable);
            if (!successful) {
                LOG.error("Cluster failed to restart after " + CONNECTION_ATTEMPTS * WAIT_BETWEEN_CONNECTION_ATTEMPTS + " seconds.");
            }
        } catch (ExecutionException | RetryException e) {
            LOG.error("Cluster failed to restart: " + e.getMessage(), e);
        }

    }

    void removeAllowlist(final String host) {
        eventBus.post(new RemoteReindexAllowlistEvent(host, RemoteReindexAllowlistEvent.ACTION.REMOVE));
    }

    void allowReindexingFrom(final String host) {
        eventBus.post(new RemoteReindexAllowlistEvent(host, RemoteReindexAllowlistEvent.ACTION.ADD));
    }

    List<String> getAllIndicesFrom(final URI uri, final String username, final String password) throws MalformedURLException {
        final var host = uri.toURL().toString();
        var url = (host.endsWith("/") ? host : host + "/") + "_cat/indices?h=index";
        try (var response = httpClient.newCall(new Request.Builder().url(url).header("Authorization", Credentials.basic(username, password)).build()).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return new BufferedReader(new StringReader(response.body().string())).lines().toList();
            } else {
                throw new RuntimeException("Could not read list of indices from " + host);
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not read list of indices from " + host + ", " + e.getMessage(), e);
        }
    }

    boolean isAllIndices(final List<String> indices) {
        return indices.isEmpty() || (indices.size() == 1 && "*".equals(indices.get(0)));
    }

    void reindex(final URI uri, final String username, final String password, final List<String> indices) {
        try (XContentBuilder builder = JsonXContent.contentBuilder().prettyPrint()) {
            BytesReference query = BytesReference.bytes(matchAllQuery().toXContent(builder, ToXContent.EMPTY_PARAMS));

            for (var index : indices) {
                // TODO: copy existing index templates
                // create indices based on our defaults/mongo index sets, using the defaults manually for now
                indicesService.createSimpleIndexForMigration(index, 4, 0);

                final ReindexRequest reindexRequest = new ReindexRequest();
                reindexRequest
                        .setRemoteInfo(new RemoteInfo(uri.getScheme(), uri.getHost(), uri.getPort(), uri.getPath(), query, username, password, Map.of(), RemoteInfo.DEFAULT_SOCKET_TIMEOUT, RemoteInfo.DEFAULT_CONNECT_TIMEOUT))
                        .setSourceIndices(index).setDestIndex(index);

                var reindexResult = client.execute((c, requestOptions) -> c.reindex(reindexRequest, requestOptions));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
