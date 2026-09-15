/**
 * Ported from news/ui/superadmin/SuperAdminScreens.kt's SystemSettingsScreen.
 * In-memory only (matches Android — not persisted, resets on app restart).
 */
import React from 'react';
import { ScrollView, StyleSheet, Switch, Text, View } from 'react-native';
import { useSettingsStore } from '../../data/news/useSettingsStore';
import { colors } from '../../theme/colors';

function Row({ label, value, onToggle }: { label: string; value: boolean; onToggle: (v: boolean) => void }) {
  return (
    <View style={styles.row}>
      <Text style={styles.rowLabel}>{label}</Text>
      <Switch value={value} onValueChange={onToggle} />
    </View>
  );
}

export function SystemSettingsScreen() {
  const settings = useSettingsStore(s => s.settings);
  const update = useSettingsStore(s => s.updateSettings);

  return (
    <ScrollView style={styles.container} contentContainerStyle={styles.content}>
      <Text style={styles.note}>These settings are in-memory only in this build and reset when the app restarts.</Text>
      <Row label="Telugu-first UI" value={settings.teluguFirstUi} onToggle={v => update(s => ({ ...s, teluguFirstUi: v }))} />
      <Row label="Breaking news banner" value={settings.breakingNewsEnabled} onToggle={v => update(s => ({ ...s, breakingNewsEnabled: v }))} />
      <Row label="Push notifications" value={settings.pushNotificationsEnabled} onToggle={v => update(s => ({ ...s, pushNotificationsEnabled: v }))} />
      <Row label="Email digest" value={settings.emailDigestEnabled} onToggle={v => update(s => ({ ...s, emailDigestEnabled: v }))} />
      <Row label="Reader comments" value={settings.commentsEnabled} onToggle={v => update(s => ({ ...s, commentsEnabled: v }))} />
      <Row label="Auto-moderation" value={settings.autoModerationEnabled} onToggle={v => update(s => ({ ...s, autoModerationEnabled: v }))} />
      <Row label="Profanity filter" value={settings.profanityFilterEnabled} onToggle={v => update(s => ({ ...s, profanityFilterEnabled: v }))} />
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  note: { color: colors.textDim, fontSize: 12, marginBottom: 16, fontStyle: 'italic' },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 14,
    marginBottom: 8,
  },
  rowLabel: { color: colors.textPrimary, fontWeight: '600', flex: 1, marginRight: 12 },
});
