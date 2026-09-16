/**
 * Ported from news/ui/superadmin/SuperAdminScreens.kt's SuperAdminDashboardScreen.
 */
import React, { useEffect, useMemo } from 'react';
import { ScrollView, StyleSheet, Text, TouchableOpacity } from 'react-native';
import { CompositeNavigationProp, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { useStaffStore } from '../../core/auth/useStaffStore';
import { StatGrid } from '../../components/StatGrid';
import { colors } from '../../theme/colors';
import { RootStackParamList, SuperAdminDrawerParamList } from '../../navigation/types';

type Nav = CompositeNavigationProp<
  NativeStackNavigationProp<SuperAdminDrawerParamList, 'SuperAdminDashboard'>,
  NativeStackNavigationProp<RootStackParamList>
>;

export function SuperAdminDashboardScreen() {
  const navigation = useNavigation<Nav>();
  const start = useStaffStore(s => s.start);
  const staff = useStaffStore(s => s.staff);
  const articles = useNewsStore(s => s.articles);
  const categories = useNewsStore(s => s.categories);

  useEffect(() => {
    start();
  }, [start]);

  const peopleStats = useMemo(
    () => [
      ['Total staff', staff.length] as [string, number],
      ['Reporters', staff.filter(u => u.role === 'REPORTER').length] as [string, number],
      ['Editors', staff.filter(u => u.role === 'EDITOR').length] as [string, number],
    ],
    [staff],
  );

  const contentStats = useMemo(
    () => [
      ['Total news', articles.length] as [string, number],
      ['Published', articles.filter(a => a.status === 'PUBLISHED').length] as [string, number],
      ['Categories', categories.length] as [string, number],
    ],
    [articles, categories],
  );

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>Super Admin</Text>
      <Text style={styles.sectionTitle}>Content</Text>
      <StatGrid stats={contentStats} />
      <Text style={styles.sectionTitle}>People</Text>
      <StatGrid stats={peopleStats} />

      <Text style={styles.sectionTitle}>System</Text>
      <TouchableOpacity style={styles.link} onPress={() => navigation.navigate('UserManagement')}>
        <Text style={styles.linkText}>User Management →</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.link} onPress={() => navigation.navigate('RoleManagement')}>
        <Text style={styles.linkText}>Role Management →</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.link} onPress={() => navigation.navigate('SystemSettings')}>
        <Text style={styles.linkText}>System Settings →</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  title: { fontSize: 20, fontWeight: '800', color: colors.royalBlue, marginBottom: 12 },
  sectionTitle: { fontWeight: '700', color: colors.textPrimary, marginBottom: 8, marginTop: 8 },
  link: { paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: colors.border },
  linkText: { color: colors.royalBlue, fontWeight: '700' },
});
