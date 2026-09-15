/**
 * Ported from news/ui/reader/ReaderTabScreens.kt's ReaderProfileScreen.
 */
import React from 'react';
import { Alert, StyleSheet, Switch, Text, TouchableOpacity, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useAuthStore } from '../../core/auth/useAuthStore';
import { useNewsStore } from '../../data/news/useNewsStore';
import { useLanguageStore } from '../../core/i18n/useLanguageStore';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function ReaderProfileScreen() {
  const navigation = useNavigation<Nav>();
  const readerUser = useAuthStore(s => s.readerUser);
  const savedCount = useNewsStore(s => s.savedArticleIds.size);
  const location = useNewsStore(s => s.selectedLocation);
  const language = useLanguageStore(s => s.language);
  const toggleLanguage = useLanguageStore(s => s.toggle);
  const signOut = useAuthStore(s => s.signOut);

  const switchRole = () => {
    Alert.alert('Switch role?', 'You will return to the landing screen.', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Switch',
        onPress: async () => {
          await signOut();
          navigation.reset({ index: 0, routes: [{ name: 'Landing' }] });
        },
      },
    ]);
  };

  return (
    <View style={styles.container}>
      <View style={styles.avatar}>
        <Text style={styles.avatarText}>{(readerUser?.name ?? 'R')[0]}</Text>
      </View>
      <Text style={styles.name}>{readerUser?.name ?? 'Reader'}</Text>
      <Text style={styles.rolePill}>Reader</Text>

      <View style={styles.statsRow}>
        <Stat label="Saved" value={savedCount} />
        <Stat label="Location" value={location} />
      </View>

      <View style={styles.row}>
        <Text style={styles.rowLabel}>Telugu / English</Text>
        <Switch value={language === 'en'} onValueChange={toggleLanguage} />
      </View>

      <TouchableOpacity style={styles.button} onPress={switchRole}>
        <Text style={styles.buttonText}>Switch role</Text>
      </TouchableOpacity>
    </View>
  );
}

function Stat({ label, value }: { label: string; value: string | number }) {
  return (
    <View style={styles.stat}>
      <Text style={styles.statValue}>{value}</Text>
      <Text style={styles.statLabel}>{label}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: 20, alignItems: 'center' },
  avatar: { width: 72, height: 72, borderRadius: 36, backgroundColor: colors.royalBlue, alignItems: 'center', justifyContent: 'center', marginTop: 12 },
  avatarText: { color: colors.white, fontSize: 28, fontWeight: '800' },
  name: { fontSize: 18, fontWeight: '700', color: colors.textPrimary, marginTop: 10 },
  rolePill: { fontSize: 12, color: colors.royalBlue, marginTop: 4 },
  statsRow: { flexDirection: 'row', gap: 24, marginTop: 20 },
  stat: { alignItems: 'center' },
  statValue: { fontSize: 18, fontWeight: '800', color: colors.textPrimary },
  statLabel: { fontSize: 11, color: colors.textDim, marginTop: 2 },
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    width: '100%',
    backgroundColor: colors.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 14,
    marginTop: 24,
  },
  rowLabel: { color: colors.textPrimary, fontWeight: '600' },
  button: { marginTop: 24, paddingVertical: 12, paddingHorizontal: 24 },
  buttonText: { color: colors.scarlet, fontWeight: '700' },
});
