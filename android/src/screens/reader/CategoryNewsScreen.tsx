/**
 * Ported from news/ui/reader/ReaderTabScreens.kt's CategoryNewsScreen.
 */
import React from 'react';
import { FlatList, StyleSheet, Text } from 'react-native';
import { RouteProp, useNavigation, useRoute } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { NewsCard } from '../../components/NewsCard';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;
type Route = RouteProp<RootStackParamList, 'CategoryNews'>;

export function CategoryNewsScreen() {
  const navigation = useNavigation<Nav>();
  const { params } = useRoute<Route>();
  const articles = useNewsStore(s => s.articlesInCategory(params.categoryId));
  const categoryName = useNewsStore(s => s.categoryName(params.categoryId));
  const isSaved = useNewsStore(s => s.isSaved);
  const toggleSaved = useNewsStore(s => s.toggleSaved);

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={articles}
      keyExtractor={item => item.id}
      renderItem={({ item }) => (
        <NewsCard
          article={item}
          categoryName={categoryName}
          saved={isSaved(item.id)}
          onPress={() => navigation.navigate('NewsDetail', { articleId: item.id })}
          onToggleSave={() => toggleSaved(item.id)}
        />
      )}
      ListEmptyComponent={<Text style={styles.empty}>No published articles in this category yet.</Text>}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
});
