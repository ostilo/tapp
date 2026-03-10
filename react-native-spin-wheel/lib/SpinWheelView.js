import React from 'react';
import { requireNativeComponent, } from 'react-native';
const NativeSpinWheel = requireNativeComponent('RCTSpinWheel');
export function SpinWheelView(props) {
    return <NativeSpinWheel {...props}/>;
}
