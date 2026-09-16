module.exports = {
  preset: '@react-native/jest-preset',
  setupFiles: ['<rootDir>/jest.setup.js'],
  // These packages ship untranspiled ESM ("export { ... }"), which Jest cannot
  // parse, so they must go through Babel instead of being ignored. The
  // react-native-* wildcard also covers transitive deps such as
  // react-native-drawer-layout, pulled in by @react-navigation/drawer.
  transformIgnorePatterns: [
    'node_modules/(?!(?:@react-native|@react-native-community|@react-navigation' +
      '|@react-native-firebase|@react-native-async-storage' +
      // No backslash escapes here: on Windows, Jest rewrites "/" to "\\" in
      // this pattern and corrupts "\\w" into a literal backslash.
      '|react-native|react-native-[a-z-]+' +
      '|use-latest-callback|nanoid)/)',
  ],
};
