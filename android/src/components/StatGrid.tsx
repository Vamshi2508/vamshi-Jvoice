/**
 * Ported from NavigationShells.kt's StatGrid — shared dashboard stat tiles.
 */
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors } from '../theme/colors';

export function StatGrid({ stats }: { stats: Array<[string, string | number]> }) {
  return (
    <View style={styles.grid}>
      {stats.map(([label, value]) => (
        <View key={label} style={styles.cell}>
          <Text style={styles.value}>{value}</Text>
          <Text style={styles.label}>{label}</Text>
        </View>
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  grid: { flexDirection: 'row', flexWrap: 'wrap', gap: 10, marginBottom: 16 },
  cell: {
    flexBasis: '31%',
    flexGrow: 1,
    backgroundColor: colors.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    paddingVertical: 12,
    paddingHorizontal: 10,
    alignItems: 'center',
  },
  value: { fontSize: 20, fontWeight: '800', color: colors.royalBlue },
  label: { fontSize: 11, color: colors.textDim, marginTop: 2, textAlign: 'center' },
});
