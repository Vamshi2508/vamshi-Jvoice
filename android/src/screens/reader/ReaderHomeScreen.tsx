/**
 * Ported from news/ui/reader/ReaderHomeScreen.kt — the template the rest of
 * the reader (and later reporter/editor/admin) screens follow: read state via
 * the Zustand store hook, call store functions for mutations, no direct
 * Firestore access from the component.
 */
import React, { useMemo, useState } from 'react';
import { FlatList, RefreshControl, StyleSheet, Text, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { NewsCard } from '../../components/NewsCard';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';
import { NewsArticle } from '../../types/news';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function ReaderHomeScreen() {
  const navigation = useNavigation<Nav>();
  const articles = useNewsStore(s => s.articles);
  const categories = useNewsStore(s => s.categories);
  const isLoading = useNewsStore(s => s.isLoading);
  const toggleSaved = useNewsStore(s => s.toggleSaved);
  const isSaved = useNewsStore(s => s.isSaved);
  const [refreshing, setRefreshing] = useState(false);

  const published = useMemo(() => articles.filter(a => a.status === 'PUBLISHED'), [articles]);
  const breaking = useMemo(() => published.filter(a => a.isBreaking), [published]);
  const trending = useMemo(() => published.filter(a => a.isTrending), [published]);
  const latest = useMemo(() => published.slice(0, 20), [published]);

  const categoryName = (id: string) => categories.find(c => c.id === id)?.name.en ?? '';

  const openArticle = (article: NewsArticle) => navigation.navigate('NewsDetail', { articleId: article.id });

  const onRefresh = () => {
    // Firestore listeners are already live; this just gives the user a
    // familiar pull-to-refresh affordance while data settles.
    setRefreshing(true);
    setTimeout(() => setRefreshing(false), 600);
  };

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} />}
      ListHeaderComponent={
        <View>
          <Text style={styles.title}>J Voice</Text>
          {isLoading && <Text style={styles.loading}>Loading news…</Text>}
          {breaking.length > 0 && (
            <Section title="Breaking News">
              {breaking.map(a => (
                <NewsCard
                  key={a.id}
                  article={a}
                  categoryName={categoryName(a.categoryId)}
                  saved={isSaved(a.id)}
                  onPress={() => openArticle(a)}
                  onToggleSave={() => toggleSaved(a.id)}
                />
              ))}
            </Section>
          )}
          {trending.length > 0 && (
            <Section title="Trending Now">
              {trending.map(a => (
                <NewsCard
                  key={a.id}
                  article={a}
                  categoryName={categoryName(a.categoryId)}
                  saved={isSaved(a.id)}
                  onPress={() => openArticle(a)}
                  onToggleSave={() => toggleSaved(a.id)}
                />
              ))}
            </Section>
          )}
          <Text style={styles.sectionTitle}>Latest News</Text>
        </View>
      }
      data={latest}
      keyExtractor={item => item.id}
      renderItem={({ item }) => (
        <NewsCard
          article={item}
          categoryName={categoryName(item.categoryId)}
          saved={isSaved(item.id)}
          onPress={() => openArticle(item)}
          onToggleSave={() => toggleSaved(item.id)}
        />
      )}
      ListEmptyComponent={!isLoading ? <Text style={styles.empty}>No published articles yet.</Text> : undefined}
    />
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <View style={styles.section}>
      <Text style={styles.sectionTitle}>{title}</Text>
      {children}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  title: { fontSize: 24, fontWeight: '800', color: colors.royalBlue, marginBottom: 12 },
  loading: { color: colors.textDim, marginBottom: 8 },
  section: { marginBottom: 16 },
  sectionTitle: { fontSize: 15, fontWeight: '700', color: colors.textPrimary, marginBottom: 8, marginTop: 4 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
});
