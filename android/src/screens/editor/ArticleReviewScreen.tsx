/**
 * Ported from news/ui/editor/ArticleReviewScreen.kt. AI Shorts is out of
 * scope for this phase (see docs/audit-report.html), so the "Create AI
 * Short" action from the Kotlin screen is omitted.
 */
import React, { useEffect, useState } from 'react';
import { Modal, ScrollView, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { RouteProp, useNavigation, useRoute } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { LocalizedFormField } from '../../components/LocalizedFormField';
import { EMPTY_LOCALIZED, LocalizedText } from '../../core/i18n/LocalizedText';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;
type Route = RouteProp<RootStackParamList, 'ArticleReview'>;

type ReasonPurpose = null | 'reject' | 'sendBack';

export function ArticleReviewScreen() {
  const navigation = useNavigation<Nav>();
  const { params } = useRoute<Route>();
  const article = useNewsStore(s => s.articleById(params.articleId));
  const categories = useNewsStore(s => s.categories.filter(c => c.isEnabled));
  const markUnderReview = useNewsStore(s => s.markUnderReview);
  const applyEditorEdits = useNewsStore(s => s.applyEditorEdits);
  const approveArticle = useNewsStore(s => s.approveArticle);
  const rejectArticle = useNewsStore(s => s.rejectArticle);
  const sendBackForCorrection = useNewsStore(s => s.sendBackForCorrection);

  const [headline, setHeadline] = useState<LocalizedText>(EMPTY_LOCALIZED);
  const [shortDescription, setShortDescription] = useState<LocalizedText>(EMPTY_LOCALIZED);
  const [content, setContent] = useState<LocalizedText>(EMPTY_LOCALIZED);
  const [categoryId, setCategoryId] = useState('');
  const [reasonPurpose, setReasonPurpose] = useState<ReasonPurpose>(null);
  const [reasonText, setReasonText] = useState('');

  useEffect(() => {
    markUnderReview(params.articleId).catch(() => {});
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [params.articleId]);

  useEffect(() => {
    if (article) {
      setHeadline(article.headline);
      setShortDescription(article.shortDescription);
      setContent(article.content);
      setCategoryId(article.categoryId);
    }
  }, [article]);

  if (!article) {
    return (
      <View style={styles.center}>
        <Text style={styles.missing}>Article not found.</Text>
      </View>
    );
  }

  const saveEdits = () => applyEditorEdits(article.id, { headline, shortDescription, content, categoryId });

  const submitReason = async () => {
    const reason: LocalizedText = { en: reasonText, te: '' };
    if (reasonPurpose === 'reject') await rejectArticle(article.id, reason);
    else if (reasonPurpose === 'sendBack') await sendBackForCorrection(article.id, reason);
    setReasonPurpose(null);
    setReasonText('');
    navigation.goBack();
  };

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <View style={styles.metaRow}>
        <Text style={styles.status}>{article.status}</Text>
        <Text style={styles.reporter}>by {article.reporterName || 'Reporter'}</Text>
      </View>

      <LocalizedFormField label="Headline" value={headline} onChange={setHeadline} />
      <LocalizedFormField label="Short description" value={shortDescription} onChange={setShortDescription} />
      <LocalizedFormField label="Full story" value={content} onChange={setContent} multiline />

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

      <TouchableOpacity style={styles.saveButton} onPress={saveEdits}>
        <Text style={styles.saveButtonText}>Save edits</Text>
      </TouchableOpacity>

      <View style={styles.actionsRow}>
        <TouchableOpacity style={styles.rejectButton} onPress={() => setReasonPurpose('reject')}>
          <Text style={styles.rejectButtonText}>Reject</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.sendBackButton} onPress={() => setReasonPurpose('sendBack')}>
          <Text style={styles.sendBackButtonText}>Send back</Text>
        </TouchableOpacity>
      </View>
      <View style={styles.actionsRow}>
        <TouchableOpacity
          style={styles.approveButton}
          onPress={async () => {
            await saveEdits();
            await approveArticle(article.id, false);
            navigation.goBack();
          }}>
          <Text style={styles.approveButtonText}>Approve only</Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.publishButton}
          onPress={async () => {
            await saveEdits();
            await approveArticle(article.id, true);
            navigation.goBack();
          }}>
          <Text style={styles.publishButtonText}>Approve &amp; publish</Text>
        </TouchableOpacity>
      </View>

      <Modal visible={reasonPurpose !== null} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>{reasonPurpose === 'reject' ? 'Reason for rejection' : 'What needs to change?'}</Text>
            <TextInput style={styles.modalInput} value={reasonText} onChangeText={setReasonText} multiline placeholder="Explain to the reporter…" />
            <View style={styles.modalActions}>
              <TouchableOpacity onPress={() => setReasonPurpose(null)}>
                <Text style={styles.modalCancel}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={submitReason}>
                <Text style={styles.modalConfirm}>Submit</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16, paddingBottom: 40 },
  center: { flex: 1, alignItems: 'center', justifyContent: 'center' },
  missing: { color: colors.textDim },
  metaRow: { flexDirection: 'row', justifyContent: 'space-between', marginBottom: 12 },
  status: { fontWeight: '700', color: colors.royalBlue },
  reporter: { color: colors.textDim, fontSize: 12 },
  label: { fontWeight: '700', color: colors.textPrimary, fontSize: 13, marginBottom: 6 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 16 },
  chip: { backgroundColor: colors.silver, borderRadius: 16, paddingHorizontal: 12, paddingVertical: 6 },
  chipActive: { backgroundColor: colors.royalBlue },
  chipText: { fontSize: 12, color: colors.textPrimary },
  chipTextActive: { color: colors.white },
  saveButton: { borderWidth: 1, borderColor: colors.royalBlue, borderRadius: 10, paddingVertical: 10, alignItems: 'center', marginBottom: 16 },
  saveButtonText: { color: colors.royalBlue, fontWeight: '700' },
  actionsRow: { flexDirection: 'row', gap: 10, marginBottom: 10 },
  rejectButton: { flex: 1, backgroundColor: colors.scarlet, borderRadius: 10, paddingVertical: 12, alignItems: 'center' },
  rejectButtonText: { color: colors.white, fontWeight: '700' },
  sendBackButton: { flex: 1, borderWidth: 1, borderColor: colors.scarlet, borderRadius: 10, paddingVertical: 12, alignItems: 'center' },
  sendBackButtonText: { color: colors.scarlet, fontWeight: '700' },
  approveButton: { flex: 1, borderWidth: 1, borderColor: colors.royalBlue, borderRadius: 10, paddingVertical: 12, alignItems: 'center' },
  approveButtonText: { color: colors.royalBlue, fontWeight: '700' },
  publishButton: { flex: 1, backgroundColor: colors.royalBlue, borderRadius: 10, paddingVertical: 12, alignItems: 'center' },
  publishButtonText: { color: colors.white, fontWeight: '700' },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', alignItems: 'center', justifyContent: 'center', padding: 24 },
  modalCard: { width: '100%', backgroundColor: colors.surface, borderRadius: 12, padding: 16 },
  modalTitle: { fontWeight: '700', color: colors.textPrimary, marginBottom: 10 },
  modalInput: { borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 10, minHeight: 80, textAlignVertical: 'top' },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 20, marginTop: 14 },
  modalCancel: { color: colors.textDim, fontWeight: '600' },
  modalConfirm: { color: colors.scarlet, fontWeight: '700' },
});
