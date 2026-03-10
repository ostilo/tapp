import React from 'react';
import {
  requireNativeComponent,
  ViewProps,
  NativeSyntheticEvent,
} from 'react-native';

type SpinEndEvent = NativeSyntheticEvent<{
  angle: number;
}>;

export interface SpinWheelProps extends ViewProps {
  configUrl: string;
  onSpinEnd?: (event: SpinEndEvent) => void;
}

const NativeSpinWheel =
  requireNativeComponent<SpinWheelProps>('RCTSpinWheel');

export function SpinWheelView(props: SpinWheelProps): React.ReactElement {
  return <NativeSpinWheel {...props} />;
}
