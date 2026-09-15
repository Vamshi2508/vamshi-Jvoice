/**
 * Ported from core/i18n/LocalizedFormFields.kt (simplified for React Native):
 * a bilingual text input with an [English] / [తెలుగు] tab row, binding to
 * `rawFor(language)` (no fallback) so an empty box stays empty while authoring.
 */
import React, { useState } from 'react';
import { StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { AppLanguage, LocalizedText, ltRawFor, ltWith } from '../core/i18n/LocalizedText';
import { colors } from '../theme/colors';

interface Props {
  label: string;
  value: LocalizedText;
  onChange: (next: LocalizedText) => void;
  multiline?: boolean;
  placeholder?: string;
}

export function LocalizedFormField({ label, value, onChange, multiline, placeholder }: Props) {
  const [tab, setTab] = useState<AppLanguage>('en');
  const missingOther = ltRawFor(value, tab === 'en' ? 'te' : 'en').trim().length === 0;

  return (
    <View style={styles.container}>
      <View style={styles.labelRow}>
        <Text style={styles.label}>{label}</Text>
        <View style={styles.tabs}>
          {(['en', 'te'] as AppLanguage[]).map(code => (
            <TouchableOpacity key={code} onPress={() => setTab(code)} style={[styles.tab, tab === code && styles.tabActive]}>
              <Text style={[styles.tabText, tab === code && styles.tabTextActive]}>
                {code === 'en' ? 'English' : 'తెలుగు'}
                {ltRawFor(value, code).trim().length === 0 ? ' •' : ''}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
      </View>
      <TextInput
        style={[styles.input, multiline && styles.inputMultiline]}
        multiline={multiline}
        placeholder={placeholder}
        value={ltRawFor(value, tab)}
        onChangeText={text => onChange(ltWith(value, tab, text))}
      />
      {!missingOther ? null : (
        <Text style={styles.hint}>Only {tab === 'en' ? 'English' : 'Telugu'} filled — the other language will show this text as a fallback.</Text>
      )}
    </View>
  );
}

const styles = StyleSheet.create({
  container: { marginBottom: 14 },
  labelRow: { flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 },
  label: { fontWeight: '700', color: colors.textPrimary, fontSize: 13 },
  tabs: { flexDirection: 'row', gap: 4 },
  tab: { paddingHorizontal: 8, paddingVertical: 3, borderRadius: 12, backgroundColor: colors.silver },
  tabActive: { backgroundColor: colors.royalBlue },
  tabText: { fontSize: 11, color: colors.textDim, fontWeight: '600' },
  tabTextActive: { color: colors.white },
  input: { borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 10, backgroundColor: colors.surface, fontSize: 14 },
  inputMultiline: { minHeight: 90, textAlignVertical: 'top' },
  hint: { fontSize: 11, color: colors.textDim, marginTop: 4 },
});
