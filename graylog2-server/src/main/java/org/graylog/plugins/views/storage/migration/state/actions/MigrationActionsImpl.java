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
package org.graylog.plugins.views.storage.migration.state.actions;

import jakarta.inject.Inject;
import org.graylog.plugins.views.storage.migration.state.persistence.DatanodeMigrationConfiguration;
import org.graylog2.plugin.cluster.ClusterConfigService;

public class MigrationActionsImpl implements MigrationActions {

    private final ClusterConfigService clusterConfigService;

    @Inject
    public MigrationActionsImpl(ClusterConfigService clusterConfigService) {
        this.clusterConfigService = clusterConfigService;
    }

    @Override
    public void resetMigration() {
        clusterConfigService.remove(DatanodeMigrationConfiguration.class);
    }


    @Override
    public boolean runDirectoryCompatibilityCheck() {
        // TODO: add real test
        return true;
    }

    @Override
    public boolean isOldClusterStopped() {
        // TODO: add real test
        return true;
    }

    @Override
    public void rollingUpgradeSelected() {

    }

    @Override
    public boolean directoryCompatibilityCheckOk() {
        // TODO: add real test
        return true;
    }

    @Override
    public void reindexUpgradeSelected() {

    }

    @Override
    public boolean reindexingFinished() {
        // TODO: add real test
        return true;
    }

    @Override
    public void reindexOldData() {

    }

    @Override
    public void stopMessageProcessing() {

    }

    @Override
    public void startMessageProcessing() {

    }

    @Override
    public boolean caDoesNotExist() {
        // TODO: add real test
        return true;
    }

    @Override
    public boolean removalPolicyDoesNotExist() {
        // TODO: add real test
        return true;
    }

    @Override
    public boolean caAndRemovalPolicyExist() {
        // TODO: add real test
        return true;
    }
}
