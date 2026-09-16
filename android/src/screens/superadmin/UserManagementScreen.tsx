/**
 * Ported from news/ui/superadmin/SuperAdminScreens.kt's UserManagementScreen.
 */
import React, { useEffect, useState } from 'react';
import { Alert, FlatList, Modal, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { useStaffStore } from '../../core/auth/useStaffStore';
import { UserRole, USER_ROLE_LABELS } from '../../types/news';
import { JvRoleCode } from '../../core/auth/JvRole';
import { colors } from '../../theme/colors';

const NEWS_ROLE_TO_CODE: Partial<Record<UserRole, JvRoleCode>> = {
  REPORTER: 'reporter',
  EDITOR: 'editor',
  NEWS_ADMIN: 'news_admin',
  SUPER_ADMIN: 'super_admin',
};

export function UserManagementScreen() {
  const start = useStaffStore(s => s.start);
  const staff = useStaffStore(s => s.staff);
  const permissionDenied = useStaffStore(s => s.permissionDenied);
  const setActive = useStaffStore(s => s.setActive);
  const setRole = useStaffStore(s => s.setRole);
  const forceLogout = useStaffStore(s => s.forceLogout);
  const updateProfile = useStaffStore(s => s.updateProfile);

  const [query, setQuery] = useState('');
  const [editingId, setEditingId] = useState<string | null>(null);
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [location, setLocation] = useState('');

  useEffect(() => {
    start();
  }, [start]);

  const filtered = staff.filter(u => !query || u.name.toLowerCase().includes(query.toLowerCase()));
  const editingUser = staff.find(u => u.id === editingId);

  const openEdit = (id: string) => {
    const u = staff.find(x => x.id === id);
    if (!u) return;
    setEditingId(id);
    setName(u.name);
    setEmail(u.email);
    setLocation(u.location);
  };

  const saveEdit = async () => {
    if (editingId) await updateProfile(editingId, name, email, location);
    setEditingId(null);
  };

  const confirmDeactivate = (uid: string, active: boolean) => {
    if (active) {
      Alert.alert('Deactivate account?', 'They will not be able to sign in.', [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Deactivate', style: 'destructive', onPress: () => setActive(uid, false) },
      ]);
    } else {
      setActive(uid, true);
    }
  };

  return (
    <View style={styles.container}>
      <TextInput style={styles.search} placeholder="Search users…" value={query} onChangeText={setQuery} />
      <FlatList
        contentContainerStyle={styles.content}
        data={filtered}
        keyExtractor={u => u.id}
        ListEmptyComponent={
          <Text style={styles.empty}>
            {permissionDenied
              ? "Can't load the user list — the Realtime Database rules only grant per-account reads today, not a full listing (see docs/audit-report.html)."
              : 'No users found.'}
          </Text>
        }
        renderItem={({ item }) => (
          <View style={styles.row}>
            <View style={styles.info}>
              <Text style={styles.name}>{item.name}</Text>
              <Text style={styles.meta}>
                {USER_ROLE_LABELS[item.role].label} · {item.isActive ? 'Active' : 'Disabled'}
              </Text>
            </View>
            <TouchableOpacity onPress={() => openEdit(item.id)}>
              <Text style={styles.actionText}>Edit</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => confirmDeactivate(item.id, item.isActive)}>
              <Text style={styles.actionText}>{item.isActive ? 'Deactivate' : 'Activate'}</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => forceLogout(item.id)}>
              <Text style={styles.forceText}>Force logout</Text>
            </TouchableOpacity>
          </View>
        )}
      />

      <Modal visible={editingId !== null} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>Edit {editingUser?.name}</Text>
            <TextInput style={styles.input} placeholder="Name" value={name} onChangeText={setName} />
            <TextInput style={styles.input} placeholder="Email" value={email} onChangeText={setEmail} autoCapitalize="none" />
            <TextInput style={styles.input} placeholder="Location" value={location} onChangeText={setLocation} />
            {editingUser && (
              <View style={styles.roleRow}>
                {(Object.keys(NEWS_ROLE_TO_CODE) as UserRole[]).map(role => (
                  <TouchableOpacity
                    key={role}
                    style={[styles.roleChip, editingUser.role === role && styles.roleChipActive]}
                    onPress={() => setRole(editingUser.id, NEWS_ROLE_TO_CODE[role]!)}>
                    <Text style={[styles.roleChipText, editingUser.role === role && styles.roleChipTextActive]}>{USER_ROLE_LABELS[role].label}</Text>
                  </TouchableOpacity>
                ))}
              </View>
            )}
            <View style={styles.modalActions}>
              <TouchableOpacity onPress={() => setEditingId(null)}>
                <Text style={styles.modalCancel}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={saveEdit}>
                <Text style={styles.modalConfirm}>Save</Text>
              </TouchableOpacity>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  search: { margin: 16, marginBottom: 8, borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 10, backgroundColor: colors.surface },
  content: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40, paddingHorizontal: 20 },
  row: { backgroundColor: colors.surface, borderRadius: 10, borderWidth: 1, borderColor: colors.border, padding: 12, marginBottom: 8, gap: 6 },
  info: { marginBottom: 4 },
  name: { fontWeight: '700', color: colors.textPrimary },
  meta: { fontSize: 12, color: colors.textDim, marginTop: 2 },
  actionText: { color: colors.royalBlue, fontSize: 12, fontWeight: '600' },
  forceText: { color: colors.scarlet, fontSize: 12, fontWeight: '600' },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', alignItems: 'center', justifyContent: 'center', padding: 24 },
  modalCard: { width: '100%', backgroundColor: colors.surface, borderRadius: 12, padding: 16 },
  modalTitle: { fontWeight: '700', color: colors.textPrimary, marginBottom: 10 },
  input: { borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 10, marginBottom: 10 },
  roleRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginBottom: 10 },
  roleChip: { backgroundColor: colors.silver, borderRadius: 14, paddingHorizontal: 10, paddingVertical: 5 },
  roleChipActive: { backgroundColor: colors.royalBlue },
  roleChipText: { fontSize: 11, color: colors.textPrimary },
  roleChipTextActive: { color: colors.white },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 20, marginTop: 6 },
  modalCancel: { color: colors.textDim, fontWeight: '600' },
  modalConfirm: { color: colors.scarlet, fontWeight: '700' },
});
