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
import React from 'react';
// eslint-disable-next-line no-restricted-imports
import createReactClass from 'create-react-class';
import PropTypes from 'prop-types';
import upperFirst from 'lodash/upperFirst';

import { LinkContainer } from 'components/common/router';
import { ButtonToolbar, MenuItem, Button } from 'components/bootstrap';
import Routes from 'routing/Routes';
import OperatingSystemIcon from 'components/sidecars/common/OperatingSystemIcon';
import { MoreActions } from 'components/common/EntityDataTable';

import CopyCollectorModal from './CopyCollectorModal';

const CollectorRow = createReactClass({
  // eslint-disable-next-line react/no-unused-class-component-methods
  propTypes: {
    collector: PropTypes.object.isRequired,
    onClone: PropTypes.func.isRequired,
    onDelete: PropTypes.func.isRequired,
    validateCollector: PropTypes.func.isRequired,
  },

  handleDelete() {
    const { onDelete, collector } = this.props;

    // eslint-disable-next-line no-alert
    if (window.confirm(`You are about to delete collector "${collector.name}". Are you sure?`)) {
      onDelete(collector);
    }
  },

  render() {
    const { collector, validateCollector, onClone } = this.props;

    return (
      <tr>
        <td>
          {collector.name}
        </td>
        <td>
          <OperatingSystemIcon operatingSystem={collector.node_operating_system} /> {upperFirst(collector.node_operating_system)}
        </td>
        <td>
          <ButtonToolbar>
            <LinkContainer to={Routes.SYSTEM.SIDECARS.EDIT_COLLECTOR(collector.id)}>
              <Button bsSize="xsmall">Edit</Button>
            </LinkContainer>
            <MoreActions>
              <CopyCollectorModal collector={collector}
                                  validateCollector={validateCollector}
                                  copyCollector={onClone} />
              <MenuItem divider />
              <MenuItem onSelect={this.handleDelete} variant="danger">Delete</MenuItem>
            </MoreActions>
          </ButtonToolbar>
        </td>
      </tr>
    );
  },
});

export default CollectorRow;
