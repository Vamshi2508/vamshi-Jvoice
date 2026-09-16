/**
 * Ported from news/ui/admin/AdminScreens.kt's NewsManagementScreen.
 */
import React, { useMemo, useState } from 'react';
import { Alert, FlatList, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { useNewsStore } from '../../data/news/useNewsStore';
import { WorkflowNewsRow } from '../../components/WorkflowNewsRow';
import { NewsStatus } from '../../types/news';
import { colors } from '../../theme/colors';

const FILTERS: Array<NewsStatus | 'ALL'> = ['ALL', 'DRAFT', 'SUBMITTED', 'UNDER_REVIEW', 'APPROVED', 'PUBLISHED', 'REJECTED', 'SENT_BACK'];

export function NewsManagementScreen() {
  const articles = useNewsStore(s => s.articles);
  const categoryName = useNewsStore(s => s.categoryName);
  const toggleBreaking = useNewsStore(s => s.toggleBreaking);
  const toggleFeatured = useNewsStore(s => s.toggleFeatured);
  const publishApproved = useNewsStore(s => s.publishApproved);
  const unpublish = useNewsStore(s => s.unpublish);
  const removeArticle = useNewsStore(s => s.removeArticle);

  const [query, setQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<NewsStatus | 'ALL'>('ALL');

  const filtered = useMemo(() => {
    const q = query.trim().toLowerCase();
    return articles
      .filter(a => statusFilter === 'ALL' || a.status === statusFilter)
      .filter(a => !q || a.headline.en.toLowerCase().includes(q) || a.headline.te.includes(q))
      .sort((a, b) => b.createdAt - a.createdAt);
  }, [articles, query, statusFilter]);

  const confirmRemove = (id: string) => {
    Alert.alert('Remove article?', 'This permanently deletes it.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Remove', style: 'destructive', onPress: () => removeArticle(id) },
    ]);
  };

  return (
    <View style={styles.container}>
      <TextInput style={styles.search} placeholder="Search headlines…" value={query} onChangeText={setQuery} />
      <FlatList
        horizontal
        showsHorizontalScrollIndicator={false}
        data={FILTERS}
        keyExtractor={f => f}
        style={styles.chipRow}
        renderItem={({ item }) => (
          <TouchableOpacity style={[styles.chip, statusFilter === item && styles.chipActive]} onPress={() => setStatusFilter(item)}>
            <Text style={[styles.chipText, statusFilter === item && styles.chipTextActive]}>{item}</Text>
          </TouchableOpacity>
        )}
      />
      <FlatList
        contentContainerStyle={styles.content}
        data={filtered}
        keyExtractor={item => item.id}
        renderItem={({ item }) => (
          <View style={styles.rowWrapper}>
            <WorkflowNewsRow article={item} categoryName={categoryName(item.categoryId)} />
            <View style={styles.actionsRow}>
              <TouchableOpacity onPress={() => toggleBreaking(item.id)}>
                <Text style={[styles.actionText, item.isBreaking && styles.actionActive]}>Breaking</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={() => toggleFeatured(item.id)}>
                <Text style={[styles.actionText, item.isFeatured && styles.actionActive]}>Featured</Text>
              </TouchableOpacity>
              {item.status === 'APPROVED' && (
                <TouchableOpacity onPress={() => publishApproved(item.id)}>
                  <Text style={styles.actionText}>Publish</Text>
                </TouchableOpacity>
              )}
              {item.status === 'PUBLISHED' && (
                <TouchableOpacity onPress={() => unpublish(item.id)}>
                  <Text style={styles.actionText}>Unpublish</Text>
                </TouchableOpacity>
              )}
              <TouchableOpacity onPress={() => confirmRemove(item.id)}>
                <Text style={styles.removeText}>Remove</Text>
              </TouchableOpacity>
            </View>
          </View>
        )}
        ListEmptyComponent={<Text style={styles.empty}>No articles match.</Text>}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  search: { margin: 16, marginBottom: 8, borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 10, backgroundColor: colors.surface },
  chipRow: { paddingHorizontal: 16, maxHeight: 44 },
  chip: { backgroundColor: colors.silver, borderRadius: 16, paddingHorizontal: 12, paddingVertical: 6, marginRight: 8 },
  chipActive: { backgroundColor: colors.royalBlue },
  chipText: { fontSize: 11, color: colors.textPrimary },
  chipTextActive: { color: colors.white },
  content: { padding: 16 },
  rowWrapper: { marginBottom: 4 },
  actionsRow: { flexDirection: 'row', gap: 16, marginTop: -4, marginBottom: 8, paddingHorizontal: 4 },
  actionText: { fontSize: 11, color: colors.textDim, fontWeight: '600' },
  actionActive: { color: colors.scarlet },
  removeText: { fontSize: 11, color: colors.scarlet, fontWeight: '700' },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
});
