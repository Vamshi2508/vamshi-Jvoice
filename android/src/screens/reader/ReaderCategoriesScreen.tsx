/**
 * Ported from news/ui/reader/ReaderTabScreens.kt's ReaderCategoriesScreen.
 */
import React, { useMemo } from 'react';
import { FlatList, Pressable, StyleSheet, Text } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { useCurrentLanguage, ltGet } from '../../core/i18n/useCurrentLanguage';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function ReaderCategoriesScreen() {
  const navigation = useNavigation<Nav>();
  const categories = useNewsStore(s => s.categories.filter(c => c.isEnabled));
  const articles = useNewsStore(s => s.articles);
  const language = useCurrentLanguage();

  const counts = useMemo(() => {
    const map: Record<string, number> = {};
    articles.forEach(a => {
      if (a.status !== 'PUBLISHED') return;
      map[a.categoryId] = (map[a.categoryId] ?? 0) + 1;
    });
    return map;
  }, [articles]);

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={categories}
      keyExtractor={c => c.id}
      numColumns={2}
      columnWrapperStyle={styles.row}
      renderItem={({ item }) => (
        <Pressable style={styles.card} onPress={() => navigation.navigate('CategoryNews', { categoryId: item.id })}>
          <Text style={styles.emoji}>{item.emoji}</Text>
          <Text style={styles.name}>{ltGet(item.name, language)}</Text>
          <Text style={styles.count}>{counts[item.id] ?? 0} articles</Text>
        </Pressable>
      )}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  row: { gap: 12, marginBottom: 12 },
  card: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 16,
    alignItems: 'center',
  },
  emoji: { fontSize: 28, marginBottom: 6 },
  name: { fontWeight: '700', color: colors.textPrimary, textAlign: 'center' },
  count: { fontSize: 11, color: colors.textDim, marginTop: 4 },
});
