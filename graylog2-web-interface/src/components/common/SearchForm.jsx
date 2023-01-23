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
import PropTypes from 'prop-types';
import * as React from 'react';
import Promise from 'bluebird';
import styled from 'styled-components';
import { useState, useEffect } from 'react';

import { Button } from 'components/bootstrap';
import Spinner from 'components/common/Spinner';

const FormContent = styled.div(({ buttonLeftMargin }) => `
  > :not(:last-child) {
    margin-right: ${buttonLeftMargin}px;
  }

  > * {
    display: inline-block;
    vertical-align: top;
    margin-bottom: 5px;
  }
`);

const HelpFeedback = styled.span`
  &.form-control-feedback {
    pointer-events: auto;
  }

  .btn {
    max-width: 34px;
  }
`;

const StyledContainer = styled.div(({ topMargin }) => `
  margin-top: ${topMargin}px;
`);

const StyledInput = styled.input(({ queryWidth }) => `
  width: ${queryWidth} !important;
`);

/**
 * Component that renders a customizable search form. The component
 * supports a loading state, adding children next to the form, and
 * styles customization.
 */
const SearchForm = ({
  useLoadingState,
  queryHelpComponent,
  queryWidth,
  focusAfterMount,
  children,
  className,
  placeholder,
  resetButtonLabel,
  buttonLeftMargin,
  label,
  onReset,
  onSearch,
  wrapperClass,
  topMargin,
  searchButtonLabel,
  loadingLabel,
  searchBsStyle,
  onQueryChange,
  ...props
}) => {
  const [query, setQuery] = useState(props.query);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    setQuery(props.query);
  }, [props.query]);

  /**
   * This sets the loading state and returns a promise which gets resolved once the loading state is set.
   * Callers of this function should only continue once the promise got resolved to avoid race conditions
   * with setting the loading state. Otherwise it can happen that the loading state gets set to "false"
   * before setting it to "true" has happened and thus not resetting the state after a search request.
   * @private
   */
  const setLoadingState = () => {
    return new Promise((resolve) => {
      if (useLoadingState) {
        setIsLoading(true);
        resolve();
      } else {
        resolve();
      }
    });
  };

  const resetLoadingState = () => {
    if (useLoadingState) {
      setIsLoading(false);
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    e.stopPropagation();

    if (useLoadingState) {
      setLoadingState().then(() => {
        onSearch(query, resetLoadingState);
      });
    } else {
      onSearch(query);
    }
  };

  const handleReset = () => {
    resetLoadingState();
    setQuery(query);
    onQueryChange(query);
    onReset();
  };

  const handleQueryChange = (e) => {
    const newQuery = e.target.value;

    setQuery(newQuery);
    onQueryChange(query);
  };

  return (
    <StyledContainer className={`${wrapperClass} ${className}`} topMargin={topMargin}>
      <form className="form-inline" onSubmit={handleSearch}>
        <FormContent buttonLeftMargin={buttonLeftMargin}>
          <div className={`form-group ${queryHelpComponent ? 'has-feedback' : ''}`}>
            {label && (
            <label htmlFor="common-search-form-query-input" className="control-label">
              {label}
            </label>
            )}
            <StyledInput id="common-search-form-query-input"
                           /* eslint-disable-next-line jsx-a11y/no-autofocus */
                         autoFocus={focusAfterMount}
                         onChange={handleQueryChange}
                         value={query}
                         placeholder={placeholder}
                         type="text"
                         queryWidth={queryWidth}
                         className="query form-control"
                         autoComplete="off"
                         spellCheck="false" />
            {queryHelpComponent && (
            <HelpFeedback className="form-control-feedback">{queryHelpComponent}</HelpFeedback>
            )}
          </div>

          {onSearch && (
          <Button bsStyle={searchBsStyle}
                  type="submit"
                  disabled={isLoading}
                  className="submit-button">
            {isLoading ? <Spinner text={loadingLabel} delay={0} /> : searchButtonLabel}
          </Button>
          )}

          {onReset && (
          <Button type="reset" className="reset-button" onClick={handleReset}>
            {resetButtonLabel}
          </Button>
          )}
          {children}
        </FormContent>
      </form>
    </StyledContainer>
  );
};

SearchForm.propTypes = {
  /** The query string value. */
  query: PropTypes.string,
  /**
   * Callback that gets called on every update of the query string.
   * The first argument of the function is the query string.
   */
  onQueryChange: PropTypes.func,
  /**
   * Callback when a search was submitted. The function receives the query
   * and a callback to reset the loading state of the form as arguments.
   */
  onSearch: PropTypes.func,
  /** Callback when the input was reset. The function is called with no arguments. */
  onReset: PropTypes.func,
  /** Search field label. */
  label: PropTypes.string,
  /** The className is needed to override the component style with styled-components  */
  className: PropTypes.string,
  /** Search field placeholder. */
  placeholder: PropTypes.string,
  /** Class name for the search form container. */
  wrapperClass: PropTypes.string,
  /** Width to use in the search field. */
  queryWidth: PropTypes.any,
  /** Top margin to use in the search form container. */
  topMargin: PropTypes.number,
  /** Separation between search field and buttons. */
  buttonLeftMargin: PropTypes.number,
  /** bsStyle for search button. */
  searchBsStyle: PropTypes.string,
  /** Text to display in the search button. */
  searchButtonLabel: PropTypes.node,
  /** Text to display in the reset button. */
  resetButtonLabel: PropTypes.node,
  /**
   * Text to display in the search button while the search is loading. This
   * will only be used if `useLoadingState` is true.
   */
  loadingLabel: PropTypes.string,
  /**
   * Specifies if it should display a loading state from the moment the
   * search button is pressed until the component receives new props or
   * the callback function in the `onSearch` method is called.
   */
  useLoadingState: PropTypes.bool,
  /**
   * Specifies a component that should be render inside the search input
   * field, and is meant to act as a trigger to display help about the query.
   * You may want to enlarge `queryWidth` to give the user more room to write the
   * query if you use this prop.
   *
   * **Note:** Due to size constraints rendering this component inside the input,
   * this component should contain very little text and should be very light. For
   * instance, a `Button` component with `bsStyle="link"` and a font-awesome icon
   * inside would work just fine.
   */
  queryHelpComponent: PropTypes.element,
  /** Elements to display on the right of the search form. */
  children: PropTypes.oneOfType([
    PropTypes.arrayOf(PropTypes.element),
    PropTypes.element,
  ]),
  focusAfterMount: PropTypes.bool,
};

SearchForm.defaultProps = {
  query: '',
  className: '',
  onQueryChange: () => {},
  onSearch: null,
  onReset: null,
  label: null,
  placeholder: 'Enter search query...',
  wrapperClass: 'search',
  queryWidth: 'auto',
  topMargin: 0,
  buttonLeftMargin: 5,
  searchBsStyle: 'default',
  searchButtonLabel: 'Search',
  resetButtonLabel: 'Reset',
  useLoadingState: false,
  loadingLabel: 'Loading...',
  queryHelpComponent: null,
  children: null,
  focusAfterMount: false,
};

export default SearchForm;
