/**
 * Jest setup for the J Voice React Native app.
 *
 * The app talks to native modules (Firebase, AsyncStorage, Reanimated) that
 * have no JS implementation under Jest, so they are mocked here. Without
 * these, importing App.tsx crashes at module load: src/core/firebase/firebase
 * calls getApp() at the top level.
 */

require('react-native-gesture-handler/jestSetup');

// Reanimated 4's own /mock entry re-exports the real module, which throws at
// import time because the native Worklets proxy only exists on a device. So
// stub the surface @react-navigation/drawer and react-native-drawer-layout
// actually use instead of loading the real package.
jest.mock('react-native-reanimated', () => {
  const { View } = require('react-native');
  const sharedValue = init => ({ value: init });

  const Animated = {
    View,
    Text: require('react-native').Text,
    Image: require('react-native').Image,
    ScrollView: require('react-native').ScrollView,
    createAnimatedComponent: c => c,
  };

  return {
    __esModule: true,
    default: Animated,
    ...Animated,
    useSharedValue: jest.fn(sharedValue),
    useDerivedValue: jest.fn(fn => ({ value: fn() })),
    useAnimatedStyle: jest.fn(fn => fn()),
    useAnimatedProps: jest.fn(fn => fn()),
    useAnimatedRef: jest.fn(() => ({ current: null })),
    useAnimatedReaction: jest.fn(),
    withSpring: jest.fn((v, _cfg, cb) => {
      cb?.(true);
      return v;
    }),
    withTiming: jest.fn((v, _cfg, cb) => {
      cb?.(true);
      return v;
    }),
    withDelay: jest.fn((_d, v) => v),
    cancelAnimation: jest.fn(),
    runOnJS: jest.fn(fn => fn),
    runOnUI: jest.fn(fn => fn),
    interpolate: jest.fn((v, _i, output) => output?.[0] ?? v),
    interpolateColor: jest.fn((_v, _i, output) => output?.[0]),
    Extrapolation: { CLAMP: 'clamp', EXTEND: 'extend', IDENTITY: 'identity' },
    ReduceMotion: { System: 'system', Always: 'always', Never: 'never' },
    Easing: new Proxy({}, { get: () => jest.fn(t => t) }),
  };
});

// async-storage v3 no longer ships a bundled jest mock, so back it with a
// plain in-memory Map.
jest.mock('@react-native-async-storage/async-storage', () => {
  const store = new Map();
  return {
    __esModule: true,
    default: {
      getItem: jest.fn(k => Promise.resolve(store.has(k) ? store.get(k) : null)),
      setItem: jest.fn((k, v) => {
        store.set(k, v);
        return Promise.resolve();
      }),
      removeItem: jest.fn(k => {
        store.delete(k);
        return Promise.resolve();
      }),
      clear: jest.fn(() => {
        store.clear();
        return Promise.resolve();
      }),
      getAllKeys: jest.fn(() => Promise.resolve([...store.keys()])),
      multiGet: jest.fn(ks =>
        Promise.resolve(ks.map(k => [k, store.has(k) ? store.get(k) : null])),
      ),
      multiSet: jest.fn(pairs => {
        pairs.forEach(([k, v]) => store.set(k, v));
        return Promise.resolve();
      }),
      multiRemove: jest.fn(ks => {
        ks.forEach(k => store.delete(k));
        return Promise.resolve();
      }),
    },
  };
});

const firebaseApp = {};

jest.mock('@react-native-firebase/app', () => ({
  getApp: jest.fn(() => firebaseApp),
}));

jest.mock('@react-native-firebase/firestore', () => {
  const unsubscribe = jest.fn();
  return {
    getFirestore: jest.fn(() => ({})),
    connectFirestoreEmulator: jest.fn(),
    collection: jest.fn(() => ({})),
    doc: jest.fn(() => ({})),
    query: jest.fn(() => ({})),
    where: jest.fn(() => ({})),
    setDoc: jest.fn(() => Promise.resolve()),
    updateDoc: jest.fn(() => Promise.resolve()),
    deleteDoc: jest.fn(() => Promise.resolve()),
    writeBatch: jest.fn(() => ({
      set: jest.fn(),
      update: jest.fn(),
      delete: jest.fn(),
      commit: jest.fn(() => Promise.resolve()),
    })),
    onSnapshot: jest.fn(() => unsubscribe),
  };
});

jest.mock('@react-native-firebase/database', () => {
  const unsubscribe = jest.fn();
  return {
    getDatabase: jest.fn(() => ({})),
    connectDatabaseEmulator: jest.fn(),
    ref: jest.fn(() => ({})),
    get: jest.fn(() => Promise.resolve({ val: () => null, exists: () => false })),
    set: jest.fn(() => Promise.resolve()),
    update: jest.fn(() => Promise.resolve()),
    onValue: jest.fn(() => unsubscribe),
    serverTimestamp: jest.fn(() => 0),
    increment: jest.fn(n => n),
  };
});

jest.mock('@react-native-firebase/auth', () => ({
  getAuth: jest.fn(() => ({ currentUser: null })),
  connectAuthEmulator: jest.fn(),
  signInWithEmailAndPassword: jest.fn(() => Promise.resolve({ user: null })),
  signOut: jest.fn(() => Promise.resolve()),
}));
