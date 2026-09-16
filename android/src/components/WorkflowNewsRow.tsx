/**
 * Shared article-summary list row used across Reporter/Editor/Admin screens
 * (mirrors the repeated row pattern in ReporterScreens.kt / EditorScreens.kt /
 * AdminScreens.kt).
 */
import React from 'react';
import { Pressable, StyleSheet, Text, View } from 'react-native';
import { NewsArticle, NEWS_STATUS_LABELS } from '../types/news';
import { useCurrentLanguage, ltGet } from '../core/i18n/useCurrentLanguage';
import { colors } from '../theme/colors';

const STATUS_COLOR: Record<string, string> = {
  DRAFT: colors.textDim,
  SUBMITTED: colors.royalBlue,
  UNDER_REVIEW: colors.royalBlue,
  APPROVED: colors.royalBlueBright,
  REJECTED: colors.scarlet,
  SENT_BACK: colors.scarlet,
  PUBLISHED: '#157A4A',
};

export function WorkflowNewsRow({
  article,
  categoryName,
  onPress,
  trailing,
}: {
  article: NewsArticle;
  categoryName: string;
  onPress?: () => void;
  trailing?: React.ReactNode;
}) {
  const language = useCurrentLanguage();
  return (
    <Pressable style={styles.row} onPress={onPress}>
      <View style={styles.info}>
        <Text style={styles.headline} numberOfLines={1}>
          {ltGet(article.headline, language) || '(untitled)'}
        </Text>
        <Text style={styles.meta}>
          {categoryName} · {article.location}
        </Text>
      </View>
      <View style={styles.right}>
        <View style={[styles.statusPill, { backgroundColor: STATUS_COLOR[article.status] ?? colors.textDim }]}>
          <Text style={styles.statusText}>{NEWS_STATUS_LABELS[article.status].label}</Text>
        </View>
        {trailing}
      </View>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colors.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 12,
    marginBottom: 8,
  },
  info: { flex: 1, marginRight: 8 },
  headline: { fontWeight: '700', color: colors.textPrimary, fontSize: 14 },
  meta: { fontSize: 11, color: colors.textDim, marginTop: 2 },
  right: { alignItems: 'flex-end', gap: 4 },
  statusPill: { borderRadius: 10, paddingHorizontal: 8, paddingVertical: 3 },
  statusText: { fontSize: 10, color: colors.white, fontWeight: '700' },
});
