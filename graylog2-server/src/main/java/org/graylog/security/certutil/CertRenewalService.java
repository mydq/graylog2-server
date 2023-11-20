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
package org.graylog.security.certutil;

import org.graylog2.cluster.DataNodeStatus;
import org.graylog2.cluster.Node;
import org.graylog2.cluster.preflight.DataNodeProvisioningConfig;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface CertRenewalService {

    record DataNode(String nodeId, Node.Type type, DataNodeStatus dataNodeStatus, String transportAddress,
                    DataNodeProvisioningConfig.State status, String errorMsg, String hostname, String shortNodeId,
                    LocalDateTime certValidUntil) {}

    void checkCertificatesForRenewal();
    void initiateRenewalForNode(String nodeId);
    List<DataNode> findNodes();

    List<DataNode> addProvisioningInformation(Collection<Node> nodes);
}
