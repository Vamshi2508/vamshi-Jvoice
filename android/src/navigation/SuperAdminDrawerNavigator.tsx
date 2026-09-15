import React from 'react';
import { createDrawerNavigator } from '@react-navigation/drawer';
import { SuperAdminDrawerParamList } from './types';
import { SuperAdminDashboardScreen } from '../screens/superadmin/SuperAdminDashboardScreen';
import { UserManagementScreen } from '../screens/superadmin/UserManagementScreen';
import { RoleManagementScreen } from '../screens/superadmin/RoleManagementScreen';
import { SystemSettingsScreen } from '../screens/superadmin/SystemSettingsScreen';
import { DeskDrawerContent } from './DeskDrawerContent';
import { colors } from '../theme/colors';

const Drawer = createDrawerNavigator<SuperAdminDrawerParamList>();

export function SuperAdminDrawerNavigator() {
  return (
    <Drawer.Navigator
      drawerContent={DeskDrawerContent}
      screenOptions={{ headerStyle: { backgroundColor: colors.royalBlue }, headerTintColor: colors.white }}>
      <Drawer.Screen name="SuperAdminDashboard" component={SuperAdminDashboardScreen} options={{ title: 'Dashboard' }} />
      <Drawer.Screen name="UserManagement" component={UserManagementScreen} options={{ title: 'Users' }} />
      <Drawer.Screen name="RoleManagement" component={RoleManagementScreen} options={{ title: 'Roles' }} />
      <Drawer.Screen name="SystemSettings" component={SystemSettingsScreen} options={{ title: 'Settings' }} />
    </Drawer.Navigator>
  );
}
