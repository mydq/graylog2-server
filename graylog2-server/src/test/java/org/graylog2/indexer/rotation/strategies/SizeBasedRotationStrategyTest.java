/*
 * Copyright (C) 2020 Graylog, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the Server Side Public License, version 1,
 * as published by MongoDB, Inc.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Server Side Public License for more details.
 *
 * You should have received a copy of the Server Side Public License
 * along with this program. If not, see
 * <http://www.mongodb.com/licensing/server-side-public-license>.
 */
package org.graylog2.indexer.rotation.strategies;

import org.graylog2.audit.AuditEventSender;
import org.graylog2.indexer.IndexSet;
import org.graylog2.indexer.indexset.IndexSetConfig;
import org.graylog2.indexer.indices.Indices;
import org.graylog2.indexer.rotation.common.IndexRotator;
import org.graylog2.plugin.system.NodeId;
import org.graylog2.plugin.system.SimpleNodeId;
import jakarta.annotation.Nonnull;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Optional;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SizeBasedRotationStrategyTest {
    @Rule
    public final MockitoRule mockitoRule = MockitoJUnit.rule();
    private final NodeId nodeId = new SimpleNodeId("5ca1ab1e-0000-4000-a000-000000000000");
    @Mock
    private IndexSet indexSet;
    @Mock
    private IndexSetConfig indexSetConfig;
    @Mock
    private Indices indices;
    @Mock
    private AuditEventSender auditEventSender;

    @Test
    public void testRotate() {
        when(indices.getStoreSizeInBytes("name")).thenReturn(Optional.of(1000L));
        when(indexSet.getNewestIndex()).thenReturn("name");
        when(indexSet.getConfig()).thenReturn(indexSetConfig);
        when(indexSetConfig.rotationStrategy()).thenReturn(SizeBasedRotationStrategyConfig.create(100L));

        final SizeBasedRotationStrategy strategy = createStrategy();

        strategy.rotate(indexSet);
        verify(indexSet, times(1)).cycle();
        reset(indexSet);
    }


    @Test
    public void testDontRotate() {
        when(indices.getStoreSizeInBytes("name")).thenReturn(Optional.of(1000L));
        when(indexSet.getNewestIndex()).thenReturn("name");
        when(indexSet.getConfig()).thenReturn(indexSetConfig);
        when(indexSetConfig.rotationStrategy()).thenReturn(SizeBasedRotationStrategyConfig.create(100000L));

        final SizeBasedRotationStrategy strategy = createStrategy();

        strategy.rotate(indexSet);
        verify(indexSet, never()).cycle();
        reset(indexSet);
    }


    @Test
    public void testRotateFailed() {
        when(indices.getStoreSizeInBytes("name")).thenReturn(Optional.empty());
        when(indexSet.getNewestIndex()).thenReturn("name");
        when(indexSet.getConfig()).thenReturn(indexSetConfig);
        when(indexSetConfig.rotationStrategy()).thenReturn(SizeBasedRotationStrategyConfig.create(100L));

        final SizeBasedRotationStrategy strategy = createStrategy();

        strategy.rotate(indexSet);
        verify(indexSet, never()).cycle();
        reset(indexSet);
    }

    @Nonnull
    private SizeBasedRotationStrategy createStrategy() {
        return new SizeBasedRotationStrategy(indices, new IndexRotator(indices, auditEventSender, nodeId));
    }
}
