/**
 * Ported from news/ui/admin/AdminScreens.kt's ReporterManagementScreen.
 * See useStaffStore.ts's header comment for the known RTDB rules limitation
 * that can make this list empty even for admins today.
 */
import React, { useEffect, useMemo } from 'react';
import { FlatList, StyleSheet, Switch, Text, View } from 'react-native';
import { useStaffStore } from '../../core/auth/useStaffStore';
import { useNewsStore } from '../../data/news/useNewsStore';
import { colors } from '../../theme/colors';

export function ReporterManagementScreen() {
  const start = useStaffStore(s => s.start);
  const reporters = useStaffStore(s => s.reporters);
  const permissionDenied = useStaffStore(s => s.permissionDenied);
  const setActive = useStaffStore(s => s.setActive);
  const articles = useNewsStore(s => s.articles);

  useEffect(() => {
    start();
  }, [start]);

  const statsFor = useMemo(
    () => (userId: string) => {
      const mine = articles.filter(a => a.reporterId === userId);
      return {
        total: mine.length,
        approved: mine.filter(a => a.status === 'APPROVED' || a.status === 'PUBLISHED').length,
        rejected: mine.filter(a => a.status === 'REJECTED').length,
        pending: mine.filter(a => a.status === 'SUBMITTED' || a.status === 'UNDER_REVIEW').length,
      };
    },
    [articles],
  );

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={reporters}
      keyExtractor={r => r.userId}
      ListEmptyComponent={
        <Text style={styles.empty}>
          {permissionDenied
            ? "Can't load the reporter list — the Realtime Database rules only grant per-account reads today, not a full listing (see docs/audit-report.html)."
            : 'No reporters found.'}
        </Text>
      }
      renderItem={({ item }) => {
        const stats = statsFor(item.userId);
        return (
          <View style={styles.row}>
            <View style={styles.info}>
              <Text style={styles.name}>{item.name}</Text>
              <Text style={styles.meta}>{item.assignedLocation}</Text>
              <Text style={styles.stats}>
                {stats.total} total · {stats.approved} approved · {stats.rejected} rejected · {stats.pending} pending
              </Text>
            </View>
            <Switch value={item.isActive} onValueChange={v => setActive(item.userId, v)} />
          </View>
        );
      }}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40, paddingHorizontal: 20 },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 12,
    marginBottom: 8,
  },
  info: { flex: 1 },
  name: { fontWeight: '700', color: colors.textPrimary },
  meta: { fontSize: 12, color: colors.textDim, marginTop: 2 },
  stats: { fontSize: 11, color: colors.textDim, marginTop: 4 },
});
