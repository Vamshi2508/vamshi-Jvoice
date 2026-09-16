/**
 * Ported from core/auth/StaffLoginScreen.kt.
 */
import React, { useEffect, useState } from 'react';
import { StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useAuthStore } from '../../core/auth/useAuthStore';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';
import { routeForRole } from '../../navigation/roleRouting';
import { newsRoleForCode, jvRoleFromCode } from '../../core/auth/JvRole';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function StaffLoginScreen() {
  const navigation = useNavigation<Nav>();
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const loginState = useAuthStore(s => s.loginState);
  const signIn = useAuthStore(s => s.signIn);

  useEffect(() => {
    if (loginState.status === 'success') {
      const role = newsRoleForCode(jvRoleFromCode(loginState.session.role));
      navigation.reset({ index: 0, routes: [{ name: role ? routeForRole(role) : 'ReaderTabs' }] });
    }
  }, [loginState, navigation]);

  return (
    <View style={styles.container}>
      <Text style={styles.title}>Staff sign-in</Text>

      <View style={styles.field}>
        <TextInput
          style={styles.input}
          placeholder="Login ID"
          autoCapitalize="none"
          value={loginId}
          onChangeText={setLoginId}
        />
        <Text style={styles.suffix}>@jvoicenews.com</Text>
      </View>

      <TextInput
        style={[styles.input, styles.fullInput]}
        placeholder="Password"
        secureTextEntry
        value={password}
        onChangeText={setPassword}
      />

      {loginState.status === 'error' && <Text style={styles.error}>{loginState.message}</Text>}

      <TouchableOpacity
        style={[styles.button, loginState.status === 'loading' && styles.buttonDisabled]}
        disabled={loginState.status === 'loading'}
        onPress={() => signIn(loginId, password)}>
        <Text style={styles.buttonText}>{loginState.status === 'loading' ? 'Signing in…' : 'Sign in'}</Text>
      </TouchableOpacity>

      <TouchableOpacity onPress={() => navigation.goBack()}>
        <Text style={styles.back}>Continue as reader</Text>
      </TouchableOpacity>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background, padding: 24, justifyContent: 'center' },
  title: { fontSize: 22, fontWeight: '800', color: colors.royalBlue, marginBottom: 24, textAlign: 'center' },
  field: { flexDirection: 'row', alignItems: 'center', marginBottom: 12 },
  input: { flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 12, backgroundColor: colors.surface },
  fullInput: { marginBottom: 12 },
  suffix: { marginLeft: 8, color: colors.textDim, fontSize: 12 },
  error: { color: colors.scarlet, marginBottom: 12 },
  button: { backgroundColor: colors.royalBlue, borderRadius: 8, paddingVertical: 14, alignItems: 'center', marginTop: 8 },
  buttonDisabled: { opacity: 0.6 },
  buttonText: { color: colors.white, fontWeight: '700' },
  back: { textAlign: 'center', marginTop: 20, color: colors.textDim, textDecorationLine: 'underline' },
});
