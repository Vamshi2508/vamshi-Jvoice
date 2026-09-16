/**
 * Ported from news/ui/reader/SearchScreen.kt.
 */
import React, { useMemo, useState } from 'react';
import { FlatList, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { NewsCard } from '../../components/NewsCard';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function SearchScreen() {
  const navigation = useNavigation<Nav>();
  const [query, setQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<string | null>(null);
  const categories = useNewsStore(s => s.categories.filter(c => c.isEnabled));
  const search = useNewsStore(s => s.search);
  const isSaved = useNewsStore(s => s.isSaved);
  const toggleSaved = useNewsStore(s => s.toggleSaved);

  const results = useMemo(() => search(query, categoryFilter, null), [query, categoryFilter, search]);

  return (
    <View style={styles.container}>
      <TextInput
        style={styles.input}
        placeholder="Search news…"
        value={query}
        onChangeText={setQuery}
        autoFocus
      />
      <FlatList
        horizontal
        showsHorizontalScrollIndicator={false}
        data={categories}
        keyExtractor={c => c.id}
        style={styles.chipRow}
        renderItem={({ item }) => (
          <TouchableOpacity
            style={[styles.chip, categoryFilter === item.id && styles.chipActive]}
            onPress={() => setCategoryFilter(categoryFilter === item.id ? null : item.id)}>
            <Text style={[styles.chipText, categoryFilter === item.id && styles.chipTextActive]}>
              {item.emoji} {item.name.en}
            </Text>
          </TouchableOpacity>
        )}
      />
      <FlatList
        contentContainerStyle={styles.results}
        data={results}
        keyExtractor={item => item.id}
        renderItem={({ item }) => (
          <NewsCard
            article={item}
            categoryName={categories.find(c => c.id === item.categoryId)?.name.en ?? ''}
            saved={isSaved(item.id)}
            onPress={() => navigation.navigate('NewsDetail', { articleId: item.id })}
            onToggleSave={() => toggleSaved(item.id)}
          />
        )}
        ListEmptyComponent={query.length > 0 ? <Text style={styles.empty}>No results.</Text> : undefined}
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, paddingTop: 12 },
  input: { marginHorizontal: 16, borderWidth: 1, borderColor: colors.border, borderRadius: 10, padding: 12, backgroundColor: colors.surface },
  chipRow: { marginTop: 12, paddingHorizontal: 16, maxHeight: 40 },
  chip: { backgroundColor: colors.silver, borderRadius: 16, paddingHorizontal: 12, paddingVertical: 6, marginRight: 8 },
  chipActive: { backgroundColor: colors.royalBlue },
  chipText: { fontSize: 12, color: colors.textPrimary },
  chipTextActive: { color: colors.white },
  results: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
});
