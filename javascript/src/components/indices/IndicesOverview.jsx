import React from 'react';
import { Col, Row } from 'react-bootstrap';

import { IndexDetails, IndexSummary } from 'components/indices';

const IndicesOverview = React.createClass({
  propTypes: {
    deflector: React.PropTypes.object.isRequired,
    indexRanges: React.PropTypes.array.isRequired,
    indices: React.PropTypes.object.isRequired,
  },
  _formatIndex(indexName, index) {
    const indexRange = this.props.indexRanges.filter((indexRange) => indexRange.index_name === indexName)[0];
    index.name = indexName;
    return (
      <Row key={'index-summary-' + index.name} className="content index-description">
        <Col md={12}>
          <IndexSummary index={index} indexRange={indexRange} deflector={this.props.deflector}>
            <span>
              <IndexDetails index={index} indexRange={indexRange} />
            </span>
          </IndexSummary>
        </Col>
      </Row>
    );
  },
  render() {
    return (
      <span>
        {Object.keys(this.props.indices).map((indexName) => this._formatIndex(indexName, this.props.indices[indexName]))}
      </span>
    );
  },
});

export default IndicesOverview;
