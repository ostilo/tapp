import React from 'react';
import {SafeAreaView, StyleSheet, View} from 'react-native';
import {SpinWheelView} from 'react-native-spin-wheel';

const CONFIG_URL =
  'https://raw.githubusercontent.com/ostilo/tapp/refs/heads/main/tapp_widget_config.json';

const App = () => {
  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.stage}>
        <SpinWheelView style={styles.wheel} configUrl={CONFIG_URL} />
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  stage: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  wheel: {
    width: '100%',
    height: '100%',
  },
});

export default App;

