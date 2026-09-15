import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { ReporterStackParamList } from './types';
import { ReporterDashboardScreen } from '../screens/reporter/ReporterDashboardScreen';
import { MyNewsScreen } from '../screens/reporter/MyNewsScreen';
import { colors } from '../theme/colors';

const Stack = createNativeStackNavigator<ReporterStackParamList>();

export function ReporterStackNavigator() {
  return (
    <Stack.Navigator screenOptions={{ headerStyle: { backgroundColor: colors.royalBlue }, headerTintColor: colors.white }}>
      <Stack.Screen name="ReporterDashboard" component={ReporterDashboardScreen} options={{ title: 'J Voice — Reporter' }} />
      <Stack.Screen name="MyNews" component={MyNewsScreen} options={{ title: 'My News' }} />
    </Stack.Navigator>
  );
}
