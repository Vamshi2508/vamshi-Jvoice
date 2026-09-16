module.exports = {
  presets: ['module:@react-native/babel-preset'],
  // Reanimated 4 moved its worklet transform into react-native-worklets.
  // Without this plugin the worklet unpackers never get their __initData and
  // NativeWorklets throws at import time. Must stay last in the plugin list.
  plugins: ['react-native-worklets/plugin'],
};
