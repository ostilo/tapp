'use strict';

const path = require('path');
const fs = require('fs');

const specPath = path.join(__dirname, '..', 'src', 'specs', 'SpinWheelNativeComponent.ts');
const outPath = path.join(__dirname, '..', 'lib', 'specs', 'SpinWheelNativeComponent.js');
const libRoot = path.join(__dirname, '..', 'lib');

const code = fs.readFileSync(specPath, 'utf8');

const TypeScriptParser = require('@react-native/codegen/lib/parsers/typescript/parser').TypeScriptParser;
const RNCodegen = require('@react-native/codegen/lib/generators/RNCodegen');

const parser = new TypeScriptParser();
const schema = parser.parseString(code);
const libraryName = 'SpinWheelNativeComponent';
const viewConfigJs = RNCodegen.generateViewConfig({ libraryName, schema });

fs.mkdirSync(path.dirname(outPath), { recursive: true });
fs.writeFileSync(outPath, viewConfigJs, 'utf8');

const dtsPath = outPath.replace(/\.js$/, '.d.ts');
const dts = `import type { ComponentType } from 'react';
import type { ViewProps } from 'react-native';

export interface NativeProps extends ViewProps {
  configUrl: string;
  onSpinEnd?: ((event: { angle: number }) => void) | null;
}

declare const SpinWheelNativeComponent: ComponentType<NativeProps>;
export default SpinWheelNativeComponent;
`;
fs.writeFileSync(dtsPath, dts, 'utf8');

// Also generate package entrypoints so consumers never need to compile TS.
fs.mkdirSync(libRoot, { recursive: true });
fs.writeFileSync(
  path.join(libRoot, 'index.js'),
  "export { default as SpinWheelView } from './specs/SpinWheelNativeComponent';\n",
  'utf8',
);
fs.writeFileSync(
  path.join(libRoot, 'index.d.ts'),
  "export { default as SpinWheelView } from './specs/SpinWheelNativeComponent';\nexport type { NativeProps as SpinWheelProps } from './specs/SpinWheelNativeComponent';\n",
  'utf8',
);

console.log('Generated', outPath, 'and', dtsPath);
