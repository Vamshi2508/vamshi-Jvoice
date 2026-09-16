/**
 * Ported from news/ui/auth/LandingScreen.kt. Reader has no credentials
 * (signInAsReader is purely local); staff sign-in goes through the real
 * Firebase Auth flow in useAuthStore.signIn via StaffLoginScreen.
 */
import React from 'react';
import { StyleSheet, Text, TouchableOpacity, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useAuthStore } from '../../core/auth/useAuthStore';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function LandingScreen() {
  const navigation = useNavigation<Nav>();
  const signInAsReader = useAuthStore(s => s.signInAsReader);

  return (
    <View style={styles.container}>
      <Text style={styles.logo}>J Voice</Text>
      <Text style={styles.tagline}>Telugu &amp; English news, right where you are.</Text>

      <TouchableOpacity
        style={styles.primaryButton}
        onPress={() => {
          signInAsReader();
          navigation.reset({ index: 0, routes: [{ name: 'ReaderTabs' }] });
        }}>
        <Text style={styles.primaryButtonText}>Continue as Reader</Text>
      </TouchableOpacity>

      <TouchableOpacity style={styles.secondaryButton} onPress={() => navigation.navigate('StaffLogin')}>
        <Text style={styles.secondaryButtonText}>Staff sign-in</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.navy, alignItems: 'center', justifyContent: 'center', padding: 24 },
  logo: { fontSize: 34, fontWeight: '800', color: colors.white },
  tagline: { fontSize: 14, color: colors.silver, marginTop: 8, marginBottom: 40, textAlign: 'center' },
  primaryButton: { backgroundColor: colors.scarlet, paddingVertical: 14, paddingHorizontal: 32, borderRadius: 24, width: '100%', alignItems: 'center' },
  primaryButtonText: { color: colors.white, fontWeight: '700', fontSize: 16 },
  secondaryButton: { marginTop: 16, paddingVertical: 10 },
  secondaryButtonText: { color: colors.silver, fontWeight: '600', textDecorationLine: 'underline' },
});
