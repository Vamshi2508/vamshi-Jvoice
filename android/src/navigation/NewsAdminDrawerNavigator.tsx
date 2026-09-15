import React from 'react';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { NewsAdminDrawerParamList } from './types';
import { AdminDashboardScreen } from '../screens/admin/AdminDashboardScreen';
import { NewsManagementScreen } from '../screens/admin/NewsManagementScreen';
import { CategoryManagementScreen } from '../screens/admin/CategoryManagementScreen';
import { ReporterManagementScreen } from '../screens/admin/ReporterManagementScreen';
import { DeskDrawerContent } from './DeskDrawerContent';
import { colors } from '../theme/colors';

const Drawer = createDrawerNavigator<NewsAdminDrawerParamList>();

export function NewsAdminDrawerNavigator() {
  return (
    <Drawer.Navigator
      drawerContent={DeskDrawerContent}
      screenOptions={{ headerStyle: { backgroundColor: colors.royalBlue }, headerTintColor: colors.white }}>
      <Drawer.Screen name="AdminDashboard" component={AdminDashboardScreen} options={{ title: 'Dashboard' }} />
      <Drawer.Screen name="NewsManagement" component={NewsManagementScreen} options={{ title: 'News Management' }} />
      <Drawer.Screen name="CategoryManagement" component={CategoryManagementScreen} options={{ title: 'Categories' }} />
      <Drawer.Screen name="ReporterManagement" component={ReporterManagementScreen} options={{ title: 'Reporters' }} />
    </Drawer.Navigator>
  );
}
