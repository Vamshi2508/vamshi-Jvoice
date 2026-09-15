/**
 * Placeholder screens for the remaining Reader tabs (Categories, Saved,
 * Notifications, Profile). These are next in line to be built out — kept as
 * simple stubs for now so navigation between all tabs already works
 * end-to-end, matching news/ui/reader/ReaderTabScreens.kt's screen set.
 */
import React from 'react';
import { StyleSheet, Text, View } from 'react-native';
import { colors } from '../../theme/colors';

function Stub({ title }: { title: string }) {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.note}>Coming soon.</Text>
    </View>
  );
}

export function ReaderCategoriesScreen() {
  return <Stub title="Categories" />;
}
export function ReaderSavedScreen() {
  return <Stub title="Saved" />;
}
export function ReaderNotificationsScreen() {
  return <Stub title="Notifications" />;
}
export function ReaderProfileScreen() {
  return <Stub title="Profile" />;
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, alignItems: 'center', justifyContent: 'center' },
  title: { fontSize: 20, fontWeight: '700', color: colors.textPrimary },
  note: { color: colors.textDim, marginTop: 8 },
});
