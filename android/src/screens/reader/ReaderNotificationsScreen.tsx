/**
 * Ported from news/ui/reader/ReaderTabScreens.kt's ReaderNotificationsScreen.
 */
import React from 'react';
import { FlatList, Pressable, StyleSheet, Text, TouchableOpacity } from 'react-native';
import { useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { useCurrentLanguage, ltGet } from '../../core/i18n/useCurrentLanguage';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Nav = NativeStackNavigationProp<RootStackParamList>;

export function ReaderNotificationsScreen() {
  const navigation = useNavigation<Nav>();
  const notifications = useNewsStore(s => s.notifications);
  const markRead = useNewsStore(s => s.markNotificationRead);
  const markAllRead = useNewsStore(s => s.markAllNotificationsRead);
  const language = useCurrentLanguage();

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={notifications}
      keyExtractor={item => item.id}
      ListHeaderComponent={
        notifications.length > 0 ? (
          <TouchableOpacity onPress={() => markAllRead('READER')} style={styles.markAllButton}>
            <Text style={styles.markAllText}>Mark all read</Text>
          </TouchableOpacity>
        ) : undefined
      }
      renderItem={({ item }) => (
        <Pressable
          style={[styles.row, !item.isRead && styles.unread]}
          onPress={() => {
            markRead(item.id);
            if (item.articleId) navigation.navigate('NewsDetail', { articleId: item.articleId });
          }}>
          <Text style={styles.title}>{ltGet(item.title, language)}</Text>
          <Text style={styles.message} numberOfLines={2}>
            {ltGet(item.message, language)}
          </Text>
        </Pressable>
      )}
      ListEmptyComponent={<Text style={styles.empty}>No notifications yet.</Text>}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
  markAllButton: { alignSelf: 'flex-end', marginBottom: 8 },
  markAllText: { color: colors.royalBlue, fontWeight: '600' },
  row: { backgroundColor: colors.surface, borderRadius: 10, borderWidth: 1, borderColor: colors.border, padding: 12, marginBottom: 8 },
  unread: { borderColor: colors.royalBlue, borderWidth: 1.5 },
  title: { fontWeight: '700', color: colors.textPrimary },
  message: { fontSize: 12, color: colors.textDim, marginTop: 4 },
});
