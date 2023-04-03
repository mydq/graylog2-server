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
import * as React from 'react';
import { Grid, List } from '@mantine/core';
import styled, { css, useTheme } from 'styled-components';
import type { IconName } from '@fortawesome/fontawesome-common-types';

import Icon from 'components/common/Icon';
import useConfigurationStep from 'preflight/hooks/useConfigurationStep';
import Spinner from 'components/common/Spinner';
import { CONFIGURATION_STEPS, CONFIGURATION_STEPS_ORDER } from 'preflight/Constants';

import CAConfiguration from './CAConfiguration';

const StepIcon = styled(Icon)<{ $color: string }>(({ $color }) => css`
  color: ${$color};
`);

const stepIcon = (stepKey, activeStepKey, theme): { name: IconName, color: string } => {
  const stepIndex = CONFIGURATION_STEPS_ORDER.findIndex((key) => key === stepKey);
  const activeStepIndex = CONFIGURATION_STEPS_ORDER.findIndex((key) => key === activeStepKey);

  console.log(stepKey, activeStepKey, stepIndex, activeStepIndex);

  if (stepIndex < activeStepIndex || stepKey === CONFIGURATION_STEPS.CONFIGURATION_FINISHED) {
    return {
      name: 'circle-check',
      color: theme.colors.variant.success,
    };
  }

  if (stepKey === activeStepKey) {
    return {
      name: 'circle',
      color: theme.colors.variant.info,
    };
  }

  return {
    name: 'circle',
    color: theme.colors.input.colorDisabled,
  };
};

const ConfigurationWizard = () => {
  const { step: activeStepKey, isLoading: isLoadingConfigurationStep } = useConfigurationStep();
  const theme = useTheme();

  if (isLoadingConfigurationStep) {
    return <Spinner />;
  }

  return (
    <Grid>
      <Grid.Col span={6}>
        {activeStepKey === CONFIGURATION_STEPS.CA_CONFIGURATION.key && <CAConfiguration />}
        {activeStepKey === CONFIGURATION_STEPS.CERTIFICATE_PROVISIONING.key && '<CertificateProvisioning />'}
        {activeStepKey === CONFIGURATION_STEPS.CONFIGURATION_FINISHED.key && '<ConfigurationFinished />'}
      </Grid.Col>
      <Grid.Col span={6}>
        <List>
          {CONFIGURATION_STEPS_ORDER.map((configurationStepKey) => {
            const { description } = CONFIGURATION_STEPS[configurationStepKey];
            const { name: iconName, color: iconColor } = stepIcon(configurationStepKey, activeStepKey, theme);

            return (
              <List.Item key={configurationStepKey} icon={<StepIcon name={iconName} $color={iconColor} />}>
                {description}
              </List.Item>
            );
          })}
        </List>
      </Grid.Col>
    </Grid>
  );
};

export default ConfigurationWizard;