import type { ComponentType } from 'react';
import type { ViewProps } from 'react-native';

export interface NativeProps extends ViewProps {
  configUrl: string;
  onSpinEnd?: ((event: { angle: number }) => void) | null;
}

declare const SpinWheelNativeComponent: ComponentType<NativeProps>;
export default SpinWheelNativeComponent;
