import type { HostComponent, ViewProps } from 'react-native';
import type {
  BubblingEventHandler,
  Double,
} from 'react-native/Libraries/Types/CodegenTypes';
import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';

type SpinEndEvent = Readonly<{
  angle: Double;
}>;

export interface NativeProps extends ViewProps {
  configUrl: string;
  onSpinEnd?: BubblingEventHandler<SpinEndEvent> | null;
}

export default codegenNativeComponent<NativeProps>('RCTSpinWheel') as HostComponent<NativeProps>;
