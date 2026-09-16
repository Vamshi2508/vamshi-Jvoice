import React from 'react';
import { Image, Pressable, StyleSheet, Text, View } from 'react-native';
import { NewsArticle } from '../types/news';
import { ltGet, useCurrentLanguage } from '../core/i18n/useCurrentLanguage';
import { colors } from '../theme/colors';

interface Props {
  article: NewsArticle;
  categoryName: string;
  saved: boolean;
  onPress: () => void;
  onToggleSave: () => void;
}

function whenLabel(article: NewsArticle): string {
  const millis = article.publishedAt ?? article.createdAt;
  const diffMin = Math.floor((Date.now() - millis) / 60000);
  if (diffMin < 1) return 'just now';
  if (diffMin < 60) return `${diffMin}m ago`;
  const diffHr = Math.floor(diffMin / 60);
  if (diffHr < 24) return `${diffHr}h ago`;
  return `${Math.floor(diffHr / 24)}d ago`;
}

export function NewsCard({ article, categoryName, saved, onPress, onToggleSave }: Props) {
  const language = useCurrentLanguage();
  return (
    <Pressable style={styles.card} onPress={onPress}>
      {article.imageUrl ? <Image source={{ uri: article.imageUrl }} style={styles.thumb} /> : <View style={[styles.thumb, styles.thumbFallback]} />}
      <View style={styles.body}>
        <View style={styles.metaRow}>
          {article.isBreaking && (
            <View style={styles.breakingBadge}>
              <Text style={styles.breakingBadgeText}>BREAKING</Text>
            </View>
          )}
          <Text style={styles.category}>{categoryName}</Text>
        </View>
        <Text style={styles.headline} numberOfLines={2}>
          {ltGet(article.headline, language)}
        </Text>
        <Text style={styles.description} numberOfLines={2}>
          {ltGet(article.shortDescription, language)}
        </Text>
        <View style={styles.footerRow}>
          <Text style={styles.footerText}>
            {article.location} · {whenLabel(article)}
          </Text>
          <Pressable hitSlop={12} onPress={onToggleSave}>
            <Text style={styles.saveIcon}>{saved ? '★' : '☆'}</Text>
          </Pressable>
        </View>
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  card: {
    flexDirection: 'row',
    backgroundColor: colors.surface,
    borderRadius: 12,
    marginBottom: 12,
    overflow: 'hidden',
    borderWidth: 1,
    borderColor: colors.border,
  },
  thumb: { width: 96, height: 96 },
  thumbFallback: { backgroundColor: colors.silver },
  body: { flex: 1, padding: 10, justifyContent: 'space-between' },
  metaRow: { flexDirection: 'row', alignItems: 'center', gap: 6 },
  breakingBadge: { backgroundColor: colors.scarlet, borderRadius: 4, paddingHorizontal: 6, paddingVertical: 2 },
  breakingBadgeText: { color: colors.white, fontSize: 10, fontWeight: '700' },
  category: { color: colors.royalBlue, fontSize: 12, fontWeight: '600' },
  headline: { fontSize: 15, fontWeight: '700', color: colors.textPrimary, marginTop: 4 },
  description: { fontSize: 12, color: colors.textDim, marginTop: 2 },
  footerRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginTop: 6 },
  footerText: { fontSize: 11, color: colors.textDim },
  saveIcon: { fontSize: 18, color: colors.scarlet },
});
