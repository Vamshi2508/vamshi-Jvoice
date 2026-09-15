/**
 * Ported from news/ui/editor/EditorScreens.kt's ReviewQueueScreen.
 */
import React from 'react';
import { FlatList, StyleSheet, Text } from 'react-native';
import { CompositeNavigationProp, useNavigation } from '@react-navigation/native';
import { NativeStackNavigationProp } from '@react-navigation/native-stack';
import { useNewsStore } from '../../data/news/useNewsStore';
import { WorkflowNewsRow } from '../../components/WorkflowNewsRow';
import { colors } from '../../theme/colors';
import { RootStackParamList, EditorDrawerParamList } from '../../navigation/types';

type Nav = CompositeNavigationProp<
  NativeStackNavigationProp<EditorDrawerParamList, 'ReviewQueue'>,
  NativeStackNavigationProp<RootStackParamList>
>;

export function ReviewQueueScreen() {
  const navigation = useNavigation<Nav>();
  const queue = useNewsStore(s => s.reviewQueue());
  const categoryName = useNewsStore(s => s.categoryName);

  return (
    <FlatList
      style={styles.container}
      contentContainerStyle={styles.content}
      data={queue}
      keyExtractor={item => item.id}
      renderItem={({ item }) => (
        <WorkflowNewsRow article={item} categoryName={categoryName(item.categoryId)} onPress={() => navigation.navigate('ArticleReview', { articleId: item.id })} />
      )}
      ListEmptyComponent={<Text style={styles.empty}>Nothing waiting for review.</Text>}
    />
  );
}

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: colors.background },
  content: { padding: 16 },
  empty: { textAlign: 'center', color: colors.textDim, marginTop: 40 },
});
