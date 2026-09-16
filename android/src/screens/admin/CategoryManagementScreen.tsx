/**
 * Ported from news/ui/admin/AdminScreens.kt's CategoryManagementScreen.
 */
import React, { useState } from 'react';
import { Alert, FlatList, Modal, StyleSheet, Switch, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { useNewsStore } from '../../data/news/useNewsStore';
import { Category } from '../../types/news';
import { colors } from '../../theme/colors';

export function CategoryManagementScreen() {
  const categories = useNewsStore(s => s.categories);
  const articles = useNewsStore(s => s.articles);
  const addCategory = useNewsStore(s => s.addCategory);
  const updateCategory = useNewsStore(s => s.updateCategory);
  const toggleCategoryEnabled = useNewsStore(s => s.toggleCategoryEnabled);
  const deleteCategory = useNewsStore(s => s.deleteCategory);

  const [editing, setEditing] = useState<Category | null>(null);
  const [nameEn, setNameEn] = useState('');
  const [nameTe, setNameTe] = useState('');
  const [emoji, setEmoji] = useState('📰');
  const [modalVisible, setModalVisible] = useState(false);

  const openNew = () => {
    setEditing(null);
    setNameEn('');
    setNameTe('');
    setEmoji('📰');
    setModalVisible(true);
  };

  const openEdit = (c: Category) => {
    setEditing(c);
    setNameEn(c.name.en);
    setNameTe(c.name.te);
    setEmoji(c.emoji);
    setModalVisible(true);
  };

  const save = async () => {
    if (!nameEn.trim()) return;
    if (editing) await updateCategory(editing.id, { en: nameEn, te: nameTe }, emoji);
    else await addCategory({ en: nameEn, te: nameTe }, emoji);
    setModalVisible(false);
  };

  const confirmDelete = async (c: Category) => {
    const ok = await deleteCategory(c.id);
    if (!ok) Alert.alert('Cannot delete', 'This category is still used by one or more articles.');
  };

  return (
    <View style={styles.container}>
      <FlatList
        contentContainerStyle={styles.content}
        data={categories}
        keyExtractor={c => c.id}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <View style={styles.rowInfo}>
              <Text style={styles.rowTitle}>
                {item.emoji} {item.name.en} <Text style={styles.rowTe}>({item.name.te || '—'})</Text>
              </Text>
              <Text style={styles.rowCount}>{articles.filter(a => a.categoryId === item.id).length} articles</Text>
            </View>
            <Switch value={item.isEnabled} onValueChange={() => toggleCategoryEnabled(item.id)} />
            <TouchableOpacity onPress={() => openEdit(item)}>
              <Text style={styles.editText}>Edit</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => confirmDelete(item)}>
              <Text style={styles.deleteText}>Delete</Text>
            </TouchableOpacity>
          </View>
        )}
      />
      <TouchableOpacity style={styles.fab} onPress={openNew}>
        <Text style={styles.fabText}>+ Add category</Text>
      </TouchableOpacity>

      <Modal visible={modalVisible} transparent animationType="fade">
        <View style={styles.modalBackdrop}>
          <View style={styles.modalCard}>
            <Text style={styles.modalTitle}>{editing ? 'Edit category' : 'New category'}</Text>
            <TextInput style={styles.input} placeholder="Name (English)" value={nameEn} onChangeText={setNameEn} />
            <TextInput style={styles.input} placeholder="పేరు (Telugu)" value={nameTe} onChangeText={setNameTe} />
            <TextInput style={styles.input} placeholder="Emoji" value={emoji} onChangeText={setEmoji} />
            <View style={styles.modalActions}>
              <TouchableOpacity onPress={() => setModalVisible(false)}>
                <Text style={styles.modalCancel}>Cancel</Text>
              </TouchableOpacity>
              <TouchableOpacity onPress={save}>
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
  content: { padding: 16 },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    backgroundColor: colors.surface,
    borderRadius: 10,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 12,
    marginBottom: 8,
  },
  rowInfo: { flex: 1 },
  rowTitle: { fontWeight: '700', color: colors.textPrimary },
  rowTe: { fontWeight: '400', color: colors.textDim },
  rowCount: { fontSize: 11, color: colors.textDim, marginTop: 2 },
  editText: { color: colors.royalBlue, fontSize: 12, fontWeight: '700' },
  deleteText: { color: colors.scarlet, fontSize: 12, fontWeight: '700' },
  fab: { position: 'absolute', bottom: 20, right: 20, backgroundColor: colors.scarlet, borderRadius: 24, paddingVertical: 12, paddingHorizontal: 20 },
  fabText: { color: colors.white, fontWeight: '700' },
  modalBackdrop: { flex: 1, backgroundColor: 'rgba(0,0,0,0.4)', alignItems: 'center', justifyContent: 'center', padding: 24 },
  modalCard: { width: '100%', backgroundColor: colors.surface, borderRadius: 12, padding: 16 },
  modalTitle: { fontWeight: '700', color: colors.textPrimary, marginBottom: 10 },
  input: { borderWidth: 1, borderColor: colors.border, borderRadius: 8, padding: 10, marginBottom: 10 },
  modalActions: { flexDirection: 'row', justifyContent: 'flex-end', gap: 20, marginTop: 6 },
  modalCancel: { color: colors.textDim, fontWeight: '600' },
  modalConfirm: { color: colors.scarlet, fontWeight: '700' },
});
