/**
 * Ported from NavigationShells.kt's ReaderBottomBar (Clips tab intentionally
 * omitted — see docs/audit-report.html: it was never wired to a backend on
 * Android and is out of scope for this phase).
 */
import React from 'react';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Text } from 'react-native';
import { ReaderTabParamList } from './types';
import { ReaderHomeScreen } from '../screens/reader/ReaderHomeScreen';
import {
  ReaderCategoriesScreen,
  ReaderNotificationsScreen,
  ReaderProfileScreen,
  ReaderSavedScreen,
} from '../screens/reader/StubScreens';
import { colors } from '../theme/colors';

const Tab = createBottomTabNavigator<ReaderTabParamList>();

function icon(symbol: string) {
  return ({ color }: { color: string }) => <Text style={{ fontSize: 20, color }}>{symbol}</Text>;
}

export function ReaderTabNavigator() {
  return (
    <Tab.Navigator
      screenOptions={{
        headerShown: false,
        tabBarActiveTintColor: colors.scarlet,
        tabBarInactiveTintColor: colors.textDim,
      }}>
      <Tab.Screen name="ReaderHome" component={ReaderHomeScreen} options={{ title: 'News', tabBarIcon: icon('📰') }} />
      <Tab.Screen name="ReaderCategories" component={ReaderCategoriesScreen} options={{ title: 'Categories', tabBarIcon: icon('🗂️') }} />
      <Tab.Screen name="ReaderSaved" component={ReaderSavedScreen} options={{ title: 'Saved', tabBarIcon: icon('★') }} />
      <Tab.Screen name="ReaderNotifications" component={ReaderNotificationsScreen} options={{ title: 'Alerts', tabBarIcon: icon('🔔') }} />
      <Tab.Screen name="ReaderProfile" component={ReaderProfileScreen} options={{ title: 'Profile', tabBarIcon: icon('👤') }} />
    </Tab.Navigator>
  );
}
