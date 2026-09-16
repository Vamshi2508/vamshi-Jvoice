/**
 * Ported from news/ui/reporter/CreateNewsScreen.kt. Reachable as the root
 * "ArticleEditor" screen from both the Reporter dashboard/My News (reporter
 * mode: only Draft/Submit, no breaking/featured switches) and, later, admin
 * compose flows.
 */
import React, { useEffect, useState } from 'react';
import { Alert, ScrollView, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { RouteProp, useNavigation, useRoute } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useAuthStore } from '../../core/auth/useAuthStore';
import { useNewsStore } from '../../data/news/useNewsStore';
import { LocalizedFormField } from '../../components/LocalizedFormField';
import { EMPTY_LOCALIZED, LocalizedText } from '../../core/i18n/LocalizedText';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;
type Route = RouteProp<RootStackParamList, 'ArticleEditor'>;

export function CreateNewsScreen() {
  const navigation = useNavigation<Nav>();
  const { params } = useRoute<Route>();
  const articleId = params?.articleId;
  const user = useAuthStore(s => s.currentUser());
  const categories = useNewsStore(s => s.categories.filter(c => c.isEnabled));
  const articleById = useNewsStore(s => (articleId ? s.articleById(articleId) : undefined));
  const createOrUpdateArticle = useNewsStore(s => s.createOrUpdateArticle);

  const [headline, setHeadline] = useState<LocalizedText>(EMPTY_LOCALIZED);
  const [shortDescription, setShortDescription] = useState<LocalizedText>(EMPTY_LOCALIZED);
  const [content, setContent] = useState<LocalizedText>(EMPTY_LOCALIZED);
  const [categoryId, setCategoryId] = useState<string>('');
  const [location, setLocation] = useState(user?.location ?? 'Hyderabad');
  const [tagsText, setTagsText] = useState('');
  const [imageUrl, setImageUrl] = useState('');

  useEffect(() => {
    if (articleById) {
      setHeadline(articleById.headline);
      setShortDescription(articleById.shortDescription);
      setContent(articleById.content);
      setCategoryId(articleById.categoryId);
      setLocation(articleById.location);
      setTagsText(articleById.tags.map(t => t.en).join(', '));
      setImageUrl(articleById.imageUrl);
    } else if (categories.length > 0 && !categoryId) {
      setCategoryId(categories[0].id);
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [articleId]);

  const save = async (status: 'DRAFT' | 'SUBMITTED') => {
    if (!user) return;
    if (headline.en.trim().length === 0 && headline.te.trim().length === 0) {
      Alert.alert('Headline required');
      return;
    }
    if (!categoryId) {
      Alert.alert('Choose a category');
      return;
    }
    const tags = tagsText
      .split(',')
      .map(t => t.trim())
      .filter(Boolean)
      .map(t => ({ en: t, te: '' }));
    await createOrUpdateArticle(
      {
        headline,
        shortDescription,
        content,
        categoryId,
        location,
        imageUrl,
        tags,
        reporterId: user.id,
        reporterName: user.name,
        reporterAvatarUrl: user.avatarUrl,
        status,
      },
      articleId,
    );
    navigation.goBack();
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <LocalizedFormField label="Headline" value={headline} onChange={setHeadline} placeholder="What happened?" />
      <LocalizedFormField label="Short description" value={shortDescription} onChange={setShortDescription} placeholder="One or two lines" />
      <LocalizedFormField label="Full story" value={content} onChange={setContent} multiline placeholder="The full article" />

      <Text style={styles.label}>Category</Text>
      <View style={styles.chipRow}>
        {categories.map(c => (
          <TouchableOpacity key={c.id} style={[styles.chip, categoryId === c.id && styles.chipActive]} onPress={() => setCategoryId(c.id)}>
            <Text style={[styles.chipText, categoryId === c.id && styles.chipTextActive]}>
              {c.emoji} {c.name.en}
            </Text>
          </TouchableOpacity>
        ))}
      </View>

      <Text style={styles.label}>Location</Text>
      <TextInput style={styles.input} value={location} onChangeText={setLocation} />

      <Text style={styles.label}>Tags (comma separated)</Text>
      <TextInput style={styles.input} value={tagsText} onChangeText={setTagsText} placeholder="politics, weather" />

      <Text style={styles.label}>Image URL</Text>
      <TextInput style={styles.input} value={imageUrl} onChangeText={setImageUrl} placeholder="https://…" autoCapitalize="none" />

      <View style={styles.actions}>
        <TouchableOpacity style={styles.draftButton} onPress={() => save('DRAFT')}>
          <Text style={styles.draftButtonText}>Save draft</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.submitButton} onPress={() => save('SUBMITTED')}>
          <Text style={styles.submitButtonText}>Send to my editor</Text>
        </TouchableOpacity>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  label: { fontWeight: '700', color: colors.textPrimary, fontSize: 13, marginBottom: 6, marginTop: 6 },
  input: { borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 10, backgroundColor: colors.surface, marginBottom: 14 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 14 },
  chip: { backgroundColor: colors.silver, borderRadius: 16, paddingHorizontal: 12, paddingVertical: 6 },
  chipActive: { backgroundColor: colors.royalBlue },
  chipText: { fontSize: 12, color: colors.textPrimary },
  chipTextActive: { color: colors.white },
  actions: { marginTop: 12, gap: 10, paddingBottom: 30 },
  draftButton: { borderWidth: 1, borderColor: colors.royalBlue, borderRadius: 10, paddingVertical: 12, alignItems: 'center' },
  draftButtonText: { color: colors.royalBlue, fontWeight: '700' },
  submitButton: { backgroundColor: colors.scarlet, borderRadius: 10, paddingVertical: 12, alignItems: 'center' },
  submitButtonText: { color: colors.white, fontWeight: '700' },
});
