import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { RootStackParamList } from './types';
import { LandingScreen } from '../screens/reader/LandingScreen';
import { StaffLoginScreen } from '../screens/reader/StaffLoginScreen';
import { ReaderTabNavigator } from './ReaderTabNavigator';
import { NewsDetailScreen } from '../screens/reader/NewsDetailScreen';
import { colors } from '../theme/colors';

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator() {
  return (
    <Stack.Navigator
      initialRouteName="Landing"
      screenOptions={{
        headerStyle: { backgroundColor: colors.royalBlue },
        headerTintColor: colors.white,
      }}>
      <Stack.Screen name="Landing" component={LandingScreen} options={{ headerShown: false }} />
      <Stack.Screen name="StaffLogin" component={StaffLoginScreen} options={{ headerShown: false }} />
      <Stack.Screen name="ReaderTabs" component={ReaderTabNavigator} options={{ headerShown: false }} />
      <Stack.Screen name="NewsDetail" component={NewsDetailScreen} options={{ title: 'Article' }} />
    </Stack.Navigator>
  );
}
