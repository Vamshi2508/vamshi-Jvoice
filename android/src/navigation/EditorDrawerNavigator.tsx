import React from 'react';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { EditorDrawerParamList } from './types';
import { EditorDashboardScreen } from '../screens/editor/EditorDashboardScreen';
import { ReviewQueueScreen } from '../screens/editor/ReviewQueueScreen';
import { DeskDrawerContent } from './DeskDrawerContent';
import { colors } from '../theme/colors';

const Drawer = createDrawerNavigator<EditorDrawerParamList>();

export function EditorDrawerNavigator() {
  return (
    <Drawer.Navigator
      drawerContent={DeskDrawerContent}
      screenOptions={{ headerStyle: { backgroundColor: colors.royalBlue }, headerTintColor: colors.white }}>
      <Drawer.Screen name="EditorDashboard" component={EditorDashboardScreen} options={{ title: 'Dashboard' }} />
      <Drawer.Screen name="ReviewQueue" component={ReviewQueueScreen} options={{ title: 'Review Queue' }} />
    </Drawer.Navigator>
  );
}
