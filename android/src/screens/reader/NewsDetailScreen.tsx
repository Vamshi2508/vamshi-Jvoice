/**
 * Ported from news/ui/reader/NewsDetailScreen.kt.
 */
import React, { useEffect } from 'react';
import { Image, ScrollView, StyleSheet, Text, View } from 'react-native';
import { RouteProp, useRoute } from '@react-navigation/native';
import { useNewsStore } from '../../data/news/useNewsStore';
import { useCurrentLanguage, ltGet } from '../../core/i18n/useCurrentLanguage';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Route = RouteProp<RootStackParamList, 'NewsDetail'>;

export function NewsDetailScreen() {
  const { params } = useRoute<Route>();
  const article = useNewsStore(s => s.articleById(params.articleId));
  const categories = useNewsStore(s => s.categories);
  const registerView = useNewsStore(s => s.registerView);
  const language = useCurrentLanguage();

  useEffect(() => {
    registerView(params.articleId).catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.articleId]);

  if (!article) {
    return (
      <View style={styles.center}>
        <Text style={styles.missing}>This article is no longer available.</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.metaRow}>
        {article.isBreaking && (
          <View style={styles.breakingBadge}>
            <Text style={styles.breakingBadgeText}>BREAKING</Text>
          </View>
        )}
        <Text style={styles.category}>{categories.find(c => c.id === article.categoryId)?.name.en ?? ''}</Text>
      </View>
      <Text style={styles.headline}>{ltGet(article.headline, language)}</Text>
      <Text style={styles.description}>{ltGet(article.shortDescription, language)}</Text>
      {article.imageUrl ? <Image source={{ uri: article.imageUrl }} style={styles.hero} /> : null}
      <View style={styles.reporterRow}>
        <Text style={styles.reporterName}>{article.reporterName || 'J Voice Desk'}</Text>
        <Text style={styles.reporterMeta}>
          {article.location} · {article.views} views
        </Text>
      </View>
      <Text style={styles.body}>{ltGet(article.content, language)}</Text>
      {article.tags.length > 0 && (
        <View style={styles.tagRow}>
          {article.tags.map((tag, idx) => (
            <View key={idx} style={styles.tagChip}>
              <Text style={styles.tagText}>{ltGet(tag, language)}</Text>
            </View>
          ))}
        </View>
      )}
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  missing: { color: colors.textDim },
  metaRow: { flexDirection: 'row', alignItems: 'center', gap: 8, marginBottom: 8 },
  breakingBadge: { backgroundColor: colors.scarlet, borderRadius: 4, paddingHorizontal: 6, paddingVertical: 2 },
  breakingBadgeText: { color: colors.white, fontSize: 10, fontWeight: '700' },
  category: { color: colors.royalBlue, fontSize: 12, fontWeight: '700' },
  headline: { fontSize: 22, fontWeight: '800', color: colors.textPrimary, marginBottom: 6 },
  description: { fontSize: 14, color: colors.textDim, marginBottom: 12 },
  hero: { width: '100%', height: 200, borderRadius: 12, marginBottom: 12, backgroundColor: colors.silver },
  reporterRow: { marginBottom: 12 },
  reporterName: { fontWeight: '700', color: colors.textPrimary },
  reporterMeta: { color: colors.textDim, fontSize: 12, marginTop: 2 },
  body: { fontSize: 15, lineHeight: 22, color: colors.textPrimary },
  tagRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 16 },
  tagChip: { backgroundColor: colors.silver, borderRadius: 14, paddingHorizontal: 10, paddingVertical: 4 },
  tagText: { fontSize: 12, color: colors.royalBlue },
});
