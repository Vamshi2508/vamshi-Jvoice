/**
 * Ported from news/ui/reader/CommentsScreen.kt.
 */
import React, { useState } from 'react';
import { FlatList, StyleSheet, Text, TextInput, TouchableOpacity, View } from 'react-native';
import { RouteProp, useRoute } from '@react-navigation/native';
import { useEngagementStore } from '../../data/news/useEngagementStore';
import { colors } from '../../theme/colors';
import { RootStackParamList } from '../../navigation/types';

type Route = RouteProp<RootStackParamList, 'Comments'>;

export function CommentsScreen() {
  const { params } = useRoute<Route>();
  const [draft, setDraft] = useState('');
  const comments = useEngagementStore(s => s.commentsFor(params.articleId));
  const addComment = useEngagementStore(s => s.addComment);
  const likeComment = useEngagementStore(s => s.likeComment);
  const deleteComment = useEngagementStore(s => s.deleteComment);
  const engagement = useEngagementStore(s => s.engagementFor(params.articleId));

  const send = () => {
    if (addComment(params.articleId, params.authorName, draft)) setDraft('');
  };

  return (
    <View style={styles.container}>
      <Text style={styles.header}>
        {comments.length} comments · {engagement.likes} likes
      </Text>
      <FlatList
        data={comments}
        keyExtractor={c => c.id}
        contentContainerStyle={styles.list}
        renderItem={({ item }) => (
          <View style={styles.row}>
            <View style={styles.rowHeader}>
              <Text style={styles.author}>{item.authorName}</Text>
              {item.isOwn && (
                <TouchableOpacity onPress={() => deleteComment(item.id)}>
                  <Text style={styles.delete}>Delete</Text>
                </TouchableOpacity>
              )}
            </View>
            <Text style={styles.text}>{item.text}</Text>
            <TouchableOpacity onPress={() => likeComment(item.id)}>
              <Text style={styles.like}>👍 {item.likes}</Text>
            </TouchableOpacity>
          </View>
        )}
        ListEmptyComponent={<Text style={styles.empty}>Be the first to comment.</Text>}
      />
      <View style={styles.composer}>
        <TextInput style={styles.input} placeholder="Add a comment…" value={draft} onChangeText={setDraft} />
        <TouchableOpacity style={styles.sendButton} onPress={send}>
          <Text style={styles.sendText}>Send</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  header: { padding: 16, fontWeight: '700', color: colors.textPrimary },
  list: { paddingHorizontal: 16 },
  row: { backgroundColor: colors.surface, borderRadius: 10, borderWidth: 1, borderColor: colors.border, padding: 12, marginBottom: 8 },
  rowHeader: { flexDirection: 'row', justifyContent: 'space-between' },
  author: { fontWeight: '700', color: colors.textPrimary },
  delete: { color: colors.scarlet, fontSize: 12 },
  text: { color: colors.textPrimary, marginTop: 4 },
  like: { color: colors.textDim, marginTop: 6, fontSize: 12 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 20 },
  composer: { flexDirection: 'row', padding: 12, borderTopWidth: 1, borderTopColor: colors.border, gap: 8 },
  input: { flex: 1, borderWidth: 1, borderColor: colors.border, borderRadius: 20, paddingHorizontal: 14, paddingVertical: 8, backgroundColor: colors.surface },
  sendButton: { backgroundColor: colors.royalBlue, borderRadius: 20, paddingHorizontal: 16, justifyContent: 'center' },
  sendText: { color: colors.white, fontWeight: '700' },
});
