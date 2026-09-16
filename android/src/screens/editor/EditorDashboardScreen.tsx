/**
 * Ported from news/ui/editor/EditorScreens.kt's EditorDashboardScreen.
 */
import React, { useMemo } from 'react';
import { FlatList, StyleSheet, Text, View } from 'react-native';
import { CompositeNavigationProp, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { StatGrid } from '../../components/StatGrid';
import { WorkflowNewsRow } from '../../components/WorkflowNewsRow';
import { colors } from '../../theme/colors';
import { RootStackParamList, EditorDrawerParamList } from '../../navigation/types';

type Nav = CompositeNavigationProp<
  NativeStackNavigationProp<EditorDrawerParamList, 'EditorDashboard'>,
  NativeStackNavigationProp<RootStackParamList>
>;

function isToday(millis: number): boolean {
  const d = new Date(millis);
  const now = new Date();
  return d.toDateString() === now.toDateString();
}

export function EditorDashboardScreen() {
  const navigation = useNavigation<Nav>();
  const articles = useNewsStore(s => s.articles);
  const categoryName = useNewsStore(s => s.categoryName);
  const queue = useNewsStore(s => s.reviewQueue());

  const stats = useMemo(
    () => [
      ['Awaiting review', queue.length] as [string, number],
      ['Approved today', articles.filter(a => a.status !== 'REJECTED' && a.updatedAt && isToday(a.updatedAt) && (a.status === 'APPROVED' || a.status === 'PUBLISHED')).length] as [string, number],
      ['Published today', articles.filter(a => a.status === 'PUBLISHED' && a.publishedAt && isToday(a.publishedAt)).length] as [string, number],
    ],
    [articles, queue],
  );

  const recentlyHandled = useMemo(
    () =>
      articles
        .filter(a => ['PUBLISHED', 'APPROVED', 'REJECTED', 'SENT_BACK'].includes(a.status))
        .sort((a, b) => b.updatedAt - a.updatedAt)
        .slice(0, 8),
    [articles],
  );

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={queue.slice(0, 6)}
      keyExtractor={item => item.id}
      ListHeaderComponent={
        <View>
          <Text style={styles.title}>Editor desk</Text>
          <StatGrid stats={stats} />
          <Text style={styles.sectionTitle}>Review queue</Text>
        </View>
      }
      renderItem={({ item }) => (
        <WorkflowNewsRow article={item} categoryName={categoryName(item.categoryId)} onPress={() => navigation.navigate('ArticleReview', { articleId: item.id })} />
      )}
      ListFooterComponent={
        <View>
          <Text style={styles.sectionTitle}>Recently handled</Text>
          {recentlyHandled.map(a => (
            <WorkflowNewsRow key={a.id} article={a} categoryName={categoryName(a.categoryId)} />
          ))}
        </View>
      }
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  title: { fontSize: 20, fontWeight: '800', color: colors.royalBlue, marginBottom: 12 },
  sectionTitle: { fontWeight: '700', color: colors.textPrimary, marginBottom: 8, marginTop: 8 },
});
