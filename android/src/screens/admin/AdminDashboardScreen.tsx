/**
 * Ported from news/ui/admin/AdminScreens.kt's AdminDashboardScreen.
 */
import React, { useMemo } from 'react';
import { ScrollView, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { CompositeNavigationProp, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { useStaffStore } from '../../core/auth/useStaffStore';
import { StatGrid } from '../../components/StatGrid';
import { colors } from '../../theme/colors';
import { RootStackParamList, NewsAdminDrawerParamList } from '../../navigation/types';

type Nav = CompositeNavigationProp<
  NativeStackNavigationProp<NewsAdminDrawerParamList, 'AdminDashboard'>,
  NativeStackNavigationProp<RootStackParamList>
>;

export function AdminDashboardScreen() {
  const navigation = useNavigation<Nav>();
  const articles = useNewsStore(s => s.articles);
  const categories = useNewsStore(s => s.categories);
  const staff = useStaffStore(s => s.staff);
  const permissionDenied = useStaffStore(s => s.permissionDenied);

  const peopleStats = useMemo(
    () => [
      ['Reporters', staff.filter(u => u.role === 'REPORTER').length] as [string, number],
      ['Editors', staff.filter(u => u.role === 'EDITOR').length] as [string, number],
      ['Total staff', staff.length] as [string, number],
    ],
    [staff],
  );

  const contentStats = useMemo(
    () => [
      ['Total news', articles.length] as [string, number],
      ['Published', articles.filter(a => a.status === 'PUBLISHED').length] as [string, number],
      ['Awaiting review', articles.filter(a => a.status === 'SUBMITTED' || a.status === 'UNDER_REVIEW').length] as [string, number],
      ['Categories', categories.length] as [string, number],
    ],
    [articles, categories],
  );

  const approvedNotLive = articles.filter(a => a.status === 'APPROVED').length;

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.title}>News Admin</Text>

      <Text style={styles.sectionTitle}>Content</Text>
      <StatGrid stats={contentStats} />

      <Text style={styles.sectionTitle}>People</Text>
      <StatGrid stats={peopleStats} />
      {permissionDenied && (
        <Text style={styles.warning}>
          Staff list couldn't be loaded — the Realtime Database rules only grant per-account reads today, not a full listing. See docs/audit-report.html.
        </Text>
      )}

      {approvedNotLive > 0 && (
        <View style={styles.callout}>
          <Text style={styles.calloutText}>
            {approvedNotLive} approved {approvedNotLive === 1 ? 'story is' : 'stories are'} not live yet.
          </Text>
        </View>
      )}

      <TouchableOpacity style={styles.link} onPress={() => navigation.navigate('NewsManagement')}>
        <Text style={styles.linkText}>News Management →</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.link} onPress={() => navigation.navigate('CategoryManagement')}>
        <Text style={styles.linkText}>Category Management →</Text>
      </TouchableOpacity>
      <TouchableOpacity style={styles.link} onPress={() => navigation.navigate('ReporterManagement')}>
        <Text style={styles.linkText}>Reporter Management →</Text>
      </TouchableOpacity>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  title: { fontSize: 20, fontWeight: '800', color: colors.royalBlue, marginBottom: 12 },
  sectionTitle: { fontWeight: '700', color: colors.textPrimary, marginBottom: 8 },
  warning: { color: colors.textDim, fontSize: 11, marginBottom: 12, fontStyle: 'italic' },
  callout: { backgroundColor: colors.silver, borderRadius: 10, padding: 12, marginBottom: 16 },
  calloutText: { color: colors.royalBlue, fontWeight: '600' },
  link: { paddingVertical: 12, borderBottomWidth: 1, borderBottomColor: colors.border },
  linkText: { color: colors.royalBlue, fontWeight: '700' },
});
