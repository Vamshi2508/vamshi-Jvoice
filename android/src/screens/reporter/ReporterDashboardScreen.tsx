/**
 * Ported from news/ui/reporter/ReporterScreens.kt's ReporterDashboardScreen.
 */
import React, { useMemo } from 'react';
import { Alert, FlatList, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { CompositeNavigationProp } from '@react-navigation/native';
import { useAuthStore } from '../../core/auth/useAuthStore';
import { useNewsStore } from '../../data/news/useNewsStore';
import { StatGrid } from '../../components/StatGrid';
import { WorkflowNewsRow } from '../../components/WorkflowNewsRow';
import { colors } from '../../theme/colors';
import { RootStackParamList, ReporterStackParamList } from '../../navigation/types';

type Nav = CompositeNavigationProp<
  NativeStackNavigationProp<ReporterStackParamList, 'ReporterDashboard'>,
  NativeStackNavigationProp<RootStackParamList>
>;

export function ReporterDashboardScreen() {
  const navigation = useNavigation<Nav>();
  const user = useAuthStore(s => s.currentUser());
  const articles = useNewsStore(s => s.articles);
  const categoryName = useNewsStore(s => s.categoryName);
  const signOut = useAuthStore(s => s.signOut);

  const mine = useMemo(() => articles.filter(a => a.reporterId === user?.id).sort((a, b) => b.createdAt - a.createdAt), [articles, user]);

  const stats = useMemo(
    () => [
      ['Total', mine.length] as [string, number],
      ['Approved', mine.filter(a => a.status === 'APPROVED' || a.status === 'PUBLISHED').length] as [string, number],
      ['Published', mine.filter(a => a.status === 'PUBLISHED').length] as [string, number],
      ['Pending', mine.filter(a => a.status === 'SUBMITTED' || a.status === 'UNDER_REVIEW').length] as [string, number],
      ['Rejected', mine.filter(a => a.status === 'REJECTED').length] as [string, number],
      ['Sent back', mine.filter(a => a.status === 'SENT_BACK').length] as [string, number],
    ],
    [mine],
  );

  const switchRole = () => {
    Alert.alert('Switch role?', 'You will return to the landing screen.', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Switch',
        onPress: async () => {
          await signOut();
          navigation.getParent()?.reset({ index: 0, routes: [{ name: 'Landing' }] });
        },
      },
    ]);
  };

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={mine.slice(0, 10)}
      keyExtractor={item => item.id}
      ListHeaderComponent={
        <View>
          <Text style={styles.title}>Hi, {user?.name ?? 'Reporter'}</Text>
          <StatGrid stats={stats} />
          <TouchableOpacity style={styles.createButton} onPress={() => navigation.navigate('ArticleEditor', undefined)}>
            <Text style={styles.createButtonText}>+ Create News</Text>
          </TouchableOpacity>
          <Text style={styles.sectionTitle}>Recent</Text>
        </View>
      }
      renderItem={({ item }) => (
        <WorkflowNewsRow
          article={item}
          categoryName={categoryName(item.categoryId)}
          onPress={() => {
            if (item.status === 'DRAFT' || item.status === 'REJECTED' || item.status === 'SENT_BACK') {
              navigation.navigate('ArticleEditor', { articleId: item.id });
            } else {
              navigation.navigate('MyNews');
            }
          }}
        />
      )}
      ListFooterComponent={
        <TouchableOpacity style={styles.switchButton} onPress={switchRole}>
          <Text style={styles.switchButtonText}>Switch role</Text>
        </TouchableOpacity>
      }
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  title: { fontSize: 20, fontWeight: '800', color: colors.royalBlue, marginBottom: 12 },
  createButton: { backgroundColor: colors.scarlet, borderRadius: 10, paddingVertical: 12, alignItems: 'center', marginBottom: 20 },
  createButtonText: { color: colors.white, fontWeight: '700' },
  sectionTitle: { fontWeight: '700', color: colors.textPrimary, marginBottom: 8 },
  switchButton: { alignItems: 'center', paddingVertical: 20 },
  switchButtonText: { color: colors.textDim, textDecorationLine: 'underline' },
});
