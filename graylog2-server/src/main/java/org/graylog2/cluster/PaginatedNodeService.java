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
package org.graylog2.cluster;

import org.graylog2.bindings.providers.MongoJackObjectMapperProvider;
import org.graylog2.database.MongoConnection;
import org.graylog2.database.PaginatedDbService;
import org.graylog2.database.PaginatedList;
import org.graylog2.search.SearchQuery;
import org.mongojack.DBQuery;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class PaginatedNodeService extends PaginatedDbService<NodeImpl> {
    private static final String NODE_COLLECTION_NAME = "nodes";

    @Inject
    public PaginatedNodeService(MongoConnection mongoConnection, MongoJackObjectMapperProvider mapper) {
        super(mongoConnection, mapper, NodeImpl.class, NODE_COLLECTION_NAME);
    }

    public PaginatedList<Node> searchPaginated(SearchQuery query,
                                               String sortByField, String sortOrder, int page, int perPage) {
        final PaginatedList<NodeImpl> results = findPaginatedWithQueryFilterAndSort(query.toDBQuery(), node -> true,
                getSortBuilder(sortOrder, sortByField), page, perPage);
        return new PaginatedList<>(results.delegate().stream().map(n -> (Node) n).toList(),
                results.pagination().total(), results.pagination().page(), results.pagination().perPage());
    }

    public PaginatedList<Node> searchPaginatedDatanodes(SearchQuery query,
                                                        String sortByField, String sortOrder, int page, int perPage) {
        final DBQuery.Query dbQuery = DBQuery.and(query.toDBQuery(),
                DBQuery.is("type", Node.Type.DATANODE));
        final PaginatedList<NodeImpl> results = findPaginatedWithQueryFilterAndSort(dbQuery, node -> true,
                getSortBuilder(sortOrder, sortByField), page, perPage);
        return new PaginatedList<>(results.delegate().stream().map(n -> (Node) n).toList(),
                results.pagination().total(), results.pagination().page(), results.pagination().perPage());
    }


}
