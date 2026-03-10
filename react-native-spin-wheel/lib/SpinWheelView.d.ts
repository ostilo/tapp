import React from 'react';
import { ViewProps, NativeSyntheticEvent } from 'react-native';
type SpinEndEvent = NativeSyntheticEvent<{
    angle: number;
}>;
export interface SpinWheelProps extends ViewProps {
    configUrl: string;
    onSpinEnd?: (event: SpinEndEvent) => void;
}
export declare function SpinWheelView(props: SpinWheelProps): React.ReactElement;
export {};
