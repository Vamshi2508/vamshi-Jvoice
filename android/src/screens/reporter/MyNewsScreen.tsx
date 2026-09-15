/**
 * Ported from news/ui/reporter/ReporterScreens.kt's MyNewsScreen.
 */
import React, { useMemo, useState } from 'react';
import { Alert, FlatList, StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { CompositeNavigationProp, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useAuthStore } from '../../core/auth/useAuthStore';
import { useNewsStore } from '../../data/news/useNewsStore';
import { WorkflowNewsRow } from '../../components/WorkflowNewsRow';
import { NewsStatus, isEditableByReporter } from '../../types/news';
import { colors } from '../../theme/colors';
import { RootStackParamList, ReporterStackParamList } from '../../navigation/types';

type Nav = CompositeNavigationProp<
  NativeStackNavigationProp<ReporterStackParamList, 'MyNews'>,
  NativeStackNavigationProp<RootStackParamList>
>;

const FILTERS: Array<NewsStatus | 'ALL'> = ['ALL', 'DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'PUBLISHED', 'REJECTED', 'SENT_BACK'];

export function MyNewsScreen() {
  const navigation = useNavigation<Nav>();
  const user = useAuthStore(s => s.currentUser());
  const articles = useNewsStore(s => s.articles);
  const categoryName = useNewsStore(s => s.categoryName);
  const deleteDraft = useNewsStore(s => s.deleteDraft);
  const [filter, setFilter] = useState<NewsStatus | 'ALL'>('ALL');

  const mine = useMemo(() => {
    const list = articles.filter(a => a.reporterId === user?.id);
    return (filter === 'ALL' ? list : list.filter(a => a.status === filter)).sort((a, b) => b.createdAt - a.createdAt);
  }, [articles, user, filter]);

  const confirmDelete = (id: string) => {
    Alert.alert('Delete draft?', 'This cannot be undone.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Delete', style: 'destructive', onPress: () => deleteDraft(id) },
    ]);
  };

  return (
    <View style={styles.container}>
      <FlatList
        horizontal
        showsHorizontalScrollIndicator={false}
        data={FILTERS}
        keyExtractor={f => f}
        style={styles.chipRow}
        renderItem={({ item }) => (
          <TouchableOpacity style={[styles.chip, filter === item && styles.chipActive]} onPress={() => setFilter(item)}>
            <Text style={[styles.chipText, filter === item && styles.chipTextActive]}>{item}</Text>
          </TouchableOpacity>
        )}
      />
      <FlatList
        contentContainerStyle={styles.content}
        data={mine}
        keyExtractor={item => item.id}
        renderItem={({ item }) => (
          <WorkflowNewsRow
            article={item}
            categoryName={categoryName(item.categoryId)}
            onPress={() => isEditableByReporter(item.status) && navigation.navigate('ArticleEditor', { articleId: item.id })}
            trailing={
              item.status === 'DRAFT' ? (
                <TouchableOpacity onPress={() => confirmDelete(item.id)}>
                  <Text style={styles.deleteText}>Delete</Text>
                </TouchableOpacity>
              ) : undefined
            }
          />
        )}
        ListEmptyComponent={<Text style={styles.empty}>No stories in this filter.</Text>}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  chipRow: { paddingHorizontal: 16, paddingTop: 12, maxHeight: 44 },
  chip: { backgroundColor: colors.silver, borderRadius: 16, paddingHorizontal: 12, paddingVertical: 6, marginRight: 8 },
  chipActive: { backgroundColor: colors.royalBlue },
  chipText: { fontSize: 11, color: colors.textPrimary },
  chipTextActive: { color: colors.white },
  content: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
  deleteText: { color: colors.scarlet, fontSize: 11, fontWeight: '600' },
});
