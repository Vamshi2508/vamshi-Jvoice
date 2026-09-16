/**
 * Ported from news/ui/superadmin/SuperAdminScreens.kt's RoleManagementScreen —
 * "Read-only in Module 1" (matches the original app: this view has no writes).
 */
import React from 'react';
import { FlatList, StyleSheet, Text, View } from 'react-native';
import { ROLE_PERMISSIONS } from '../../data/news/useSettingsStore';
import { USER_ROLE_LABELS } from '../../types/news';
import { colors } from '../../theme/colors';

export function RoleManagementScreen() {
  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={ROLE_PERMISSIONS}
      keyExtractor={p => p.role}
      renderItem={({ item }) => (
        <View style={styles.card}>
          <Text style={styles.role}>{USER_ROLE_LABELS[item.role].label}</Text>
          <Text style={styles.roleTe}>{USER_ROLE_LABELS[item.role].teluguLabel}</Text>
          {item.permissions.map((p, i) => (
            <Text key={i} style={styles.permission}>
              • {p}
            </Text>
          ))}
        </View>
      )}
      ListHeaderComponent={<Text style={styles.note}>Read-only in this module.</Text>}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  note: { color: colors.textDim, fontSize: 12, marginBottom: 12, fontStyle: 'italic' },
  card: { backgroundColor: colors.surface, borderRadius: 10, borderWidth: 1, borderColor: colors.border, padding: 14, marginBottom: 10 },
  role: { fontWeight: '800', color: colors.royalBlue, fontSize: 15 },
  roleTe: { color: colors.textDim, fontSize: 12, marginBottom: 8 },
  permission: { color: colors.textPrimary, fontSize: 13, marginTop: 2 },
});
