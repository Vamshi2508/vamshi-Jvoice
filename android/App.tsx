/**
 * J Voice — News module (React Native rewrite, phase 1).
 */
import React, { useEffect, useState } from 'react';
import { StatusBar, useColorScheme } from 'react-native';
import { NavigationContainer } from '@react-navigation/native';
import { GestureHandlerRootView } from 'react-native-gesture-handler';
import { SafeAreaProvider } from 'react-native-safe-area-context';
import { connectEmulatorsIfNeeded } from './src/core/firebase/firebase';
import { useLanguageStore } from './src/core/i18n/useLanguageStore';
import { useAuthStore } from './src/core/auth/useAuthStore';
import { useNewsStore } from './src/data/news/useNewsStore';
import { RootNavigator } from './src/navigation/RootNavigator';

connectEmulatorsIfNeeded();

function App() {
  const isDarkMode = useColorScheme() === 'dark';
  const [ready, setReady] = useState(false);
  const hydrateLanguage = useLanguageStore(s => s.hydrate);
  const hydrateAuth = useAuthStore(s => s.hydrate);
  const session = useAuthStore(s => s.session);
  const startNews = useNewsStore(s => s.start);
  const onAuthChanged = useNewsStore(s => s.onAuthChanged);

  useEffect(() => {
    Promise.all([hydrateLanguage(), hydrateAuth()]).then(() => setReady(true));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  useEffect(() => {
    if (!ready) return;
    startNews(Boolean(session));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ready]);

  useEffect(() => {
    if (!ready) return;
    onAuthChanged(Boolean(session));
  }, [ready, session, onAuthChanged]);

  if (!ready) return null;

  return (
    <GestureHandlerRootView style={{ flex: 1 }}>
      <SafeAreaProvider>
        <StatusBar barStyle={isDarkMode ? 'light-content' : 'dark-content'} />
        <NavigationContainer>
          <RootNavigator />
        </NavigationContainer>
      </SafeAreaProvider>
    </GestureHandlerRootView>
  );
}

export default App;
