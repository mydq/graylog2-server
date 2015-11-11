import Reflux from 'reflux';

const IndicesActions = Reflux.createActions({
  'list': {asyncResult: true },
  'close': { asyncResult: true },
  'delete': { asyncResult: true },
});

export default IndicesActions;
