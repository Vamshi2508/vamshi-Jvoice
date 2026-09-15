import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { RootStackParamList } from './types';
import { LandingScreen } from '../screens/reader/LandingScreen';
import { StaffLoginScreen } from '../screens/reader/StaffLoginScreen';
import { ReaderTabNavigator } from './ReaderTabNavigator';
import { NewsDetailScreen } from '../screens/reader/NewsDetailScreen';
import { CategoryNewsScreen } from '../screens/reader/CategoryNewsScreen';
import { SearchScreen } from '../screens/reader/SearchScreen';
import { CommentsScreen } from '../screens/reader/CommentsScreen';
import { ReporterStackNavigator } from './ReporterStackNavigator';
import { EditorDrawerNavigator } from './EditorDrawerNavigator';
import { NewsAdminDrawerNavigator } from './NewsAdminDrawerNavigator';
import { SuperAdminDrawerNavigator } from './SuperAdminDrawerNavigator';
import { CreateNewsScreen } from '../screens/reporter/CreateNewsScreen';
import { ArticleReviewScreen } from '../screens/editor/ArticleReviewScreen';
import { useAuthStore } from '../core/auth/useAuthStore';
import { routeForRole } from './roleRouting';
import { colors } from '../theme/colors';

const Stack = createNativeStackNavigator<RootStackParamList>();

export function RootNavigator() {
  const currentUser = useAuthStore(s => s.currentUser());
  const initialRouteName = currentUser ? routeForRole(currentUser.role) : 'Landing';

  return (
    <Stack.Navigator
      initialRouteName={initialRouteName}
      screenOptions={{
        headerStyle: { backgroundColor: colors.royalBlue },
        headerTintColor: colors.white,
      }}>
      <Stack.Screen name="Landing" component={LandingScreen} options={{ headerShown: false }} />
      <Stack.Screen name="StaffLogin" component={StaffLoginScreen} options={{ headerShown: false }} />

      <Stack.Screen name="ReaderTabs" component={ReaderTabNavigator} options={{ headerShown: false }} />
      <Stack.Screen name="NewsDetail" component={NewsDetailScreen} options={{ title: 'Article' }} />
      <Stack.Screen name="CategoryNews" component={CategoryNewsScreen} options={{ title: 'Category' }} />
      <Stack.Screen name="Search" component={SearchScreen} options={{ title: 'Search' }} />
      <Stack.Screen name="Comments" component={CommentsScreen} options={{ title: 'Comments' }} />

      <Stack.Screen name="ReporterStack" component={ReporterStackNavigator} options={{ headerShown: false }} />
      <Stack.Screen name="EditorDrawer" component={EditorDrawerNavigator} options={{ headerShown: false }} />
      <Stack.Screen name="NewsAdminDrawer" component={NewsAdminDrawerNavigator} options={{ headerShown: false }} />
      <Stack.Screen name="SuperAdminDrawer" component={SuperAdminDrawerNavigator} options={{ headerShown: false }} />

      <Stack.Screen name="ArticleEditor" component={CreateNewsScreen} options={{ title: 'Story' }} />
      <Stack.Screen name="ArticleReview" component={ArticleReviewScreen} options={{ title: 'Review' }} />
    </Stack.Navigator>
  );
}
