/**
 * Ported from news/ui/reader/ReaderTabScreens.kt's ReaderSavedScreen.
 */
import React, { useMemo } from 'react';
import { Alert, FlatList, StyleSheet, Text, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { NewsCard } from '../../components/NewsCard';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function ReaderSavedScreen() {
  const navigation = useNavigation<Nav>();
  const articles = useNewsStore(s => s.articles);
  const savedIds = useNewsStore(s => s.savedArticleIds);
  const categories = useNewsStore(s => s.categories);
  const toggleSaved = useNewsStore(s => s.toggleSaved);
  const clearSaved = useNewsStore(s => s.clearSaved);

  const saved = useMemo(() => articles.filter(a => savedIds.has(a.id)), [articles, savedIds]);

  const confirmClear = () => {
    Alert.alert('Clear saved articles?', 'This removes all your saved articles.', [
      { text: 'Cancel', style: 'cancel' },
      { text: 'Clear all', style: 'destructive', onPress: clearSaved },
    ]);
  };

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={saved}
      keyExtractor={item => item.id}
      ListHeaderComponent={
        saved.length > 0 ? (
          <TouchableOpacity onPress={confirmClear} style={styles.clearButton}>
            <Text style={styles.clearText}>Clear all</Text>
          </TouchableOpacity>
        ) : undefined
      }
      renderItem={({ item }) => (
        <NewsCard
          article={item}
          categoryName={categories.find(c => c.id === item.categoryId)?.name.en ?? ''}
          saved
          onPress={() => navigation.navigate('NewsDetail', { articleId: item.id })}
          onToggleSave={() => toggleSaved(item.id)}
        />
      )}
      ListEmptyComponent={<Text style={styles.empty}>Nothing saved yet — tap the star on any article.</Text>}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
  clearButton: { alignSelf: 'flex-end', marginBottom: 8 },
  clearText: { color: colors.scarlet, fontWeight: '600' },
});
