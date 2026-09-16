/**
 * Ported from NavigationShells.kt's AdminScaffold drawer header/footer:
 * shows the signed-in user's name + role, and a "Switch role" item that
 * signs out and returns to Landing.
 */
import React from 'react';
import { Alert, StyleSheet, Text, View } from 'react-native';
import { DrawerContentComponentProps, DrawerContentScrollView, DrawerItemList } from '@react-navigation/drawer';
import { CommonActions } from '@react-navigation/native';
import { useAuthStore } from '../core/auth/useAuthStore';
import { USER_ROLE_LABELS } from '../types/news';
import { colors } from '../theme/colors';

export function DeskDrawerContent(props: DrawerContentComponentProps) {
  const user = useAuthStore(s => s.currentUser());
  const signOut = useAuthStore(s => s.signOut);

  const switchRole = () => {
    Alert.alert('Switch role?', 'You will return to the landing screen.', [
      { text: 'Cancel', style: 'cancel' },
      {
        text: 'Switch',
        onPress: async () => {
          await signOut();
          props.navigation.dispatch(CommonActions.reset({ index: 0, routes: [{ name: 'Landing' }] }));
        },
      },
    ]);
  };

  return (
    <DrawerContentScrollView {...props} contentContainerStyle={styles.content}>
      <View style={styles.header}>
        <Text style={styles.appName}>J Voice</Text>
        <Text style={styles.userName}>{user?.name ?? ''}</Text>
        <Text style={styles.role}>{user ? USER_ROLE_LABELS[user.role].label : ''}</Text>
      </View>
      <DrawerItemList {...props} />
      <View style={styles.footer}>
        <Text style={styles.switchRole} onPress={switchRole}>
          Switch role
        </Text>
      </View>
    </DrawerContentScrollView>
  );
}

const styles = StyleSheet.create({
  content: { flex: 1 },
  header: { backgroundColor: colors.royalBlue, padding: 20, marginBottom: 8 },
  appName: { color: colors.white, fontWeight: '800', fontSize: 18 },
  userName: { color: colors.white, marginTop: 8, fontWeight: '600' },
  role: { color: colors.silver, fontSize: 12, marginTop: 2 },
  footer: { marginTop: 'auto', padding: 20, borderTopWidth: 1, borderTopColor: colors.border },
  switchRole: { color: colors.scarlet, fontWeight: '700' },
});
