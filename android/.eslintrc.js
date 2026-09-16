module.exports = {
  root: true,
  extends: '@react-native',
  overrides: [
    {
      // Jest globals (jest, describe, expect) are injected by the test runner.
      files: ['jest.setup.js', 'jest.config.js', '__tests__/**/*'],
      env: { jest: true },
    },
  ],
};
