/**
 * Ported from news/data/repository/EngagementRepository.kt. In-memory only —
 * matches Android behavior (comments/likes/reports are not Firestore-backed
 * there either; only the reportCount side-effect on the article itself is
 * persisted, via useNewsStore.reportArticle).
 */
import { create } from 'zustand';
import { ArticleComment, ArticleEngagement, ArticleReport, Reaction, ReportReason } from '../../types/news';
import { useNewsStore } from './useNewsStore';

let nextId = 1;
function genId(prefix: string): string {
  return `${prefix}_${nextId++}`;
}

interface EngagementState {
  engagement: Record<string, ArticleEngagement>;
  comments: ArticleComment[];
  reports: ArticleReport[];

  engagementFor: (articleId: string) => ArticleEngagement;
  commentsFor: (articleId: string) => ArticleComment[];
  commentCount: (articleId: string) => number;

  toggleLike: (articleId: string) => void;
  toggleDislike: (articleId: string) => void;
  addComment: (articleId: string, authorName: string, text: string) => boolean;
  likeComment: (commentId: string) => void;
  deleteComment: (commentId: string) => void;
  submitReport: (articleId: string, reason: ReportReason, suggestion: string, reportedBy: string) => void;
}

function emptyEngagement(articleId: string): ArticleEngagement {
  return { articleId, likes: 0, dislikes: 0, myReaction: 'NONE' };
}

function react(current: ArticleEngagement, reaction: Reaction): ArticleEngagement {
  if (current.myReaction === reaction) {
    // toggling off
    return {
      ...current,
      likes: reaction === 'LIKE' ? current.likes - 1 : current.likes,
      dislikes: reaction === 'DISLIKE' ? current.dislikes - 1 : current.dislikes,
      myReaction: 'NONE',
    };
  }
  let likes = current.likes;
  let dislikes = current.dislikes;
  if (current.myReaction === 'LIKE') likes -= 1;
  if (current.myReaction === 'DISLIKE') dislikes -= 1;
  if (reaction === 'LIKE') likes += 1;
  if (reaction === 'DISLIKE') dislikes += 1;
  return { ...current, likes, dislikes, myReaction: reaction };
}

export const useEngagementStore = create<EngagementState>((set, get) => ({
  engagement: {},
  comments: [],
  reports: [],

  engagementFor: articleId => get().engagement[articleId] ?? emptyEngagement(articleId),
  commentsFor: articleId => get().comments.filter(c => c.articleId === articleId).sort((a, b) => b.timeMillis - a.timeMillis),
  commentCount: articleId => get().comments.filter(c => c.articleId === articleId).length,

  toggleLike: articleId =>
    set(state => ({
      engagement: { ...state.engagement, [articleId]: react(state.engagement[articleId] ?? emptyEngagement(articleId), 'LIKE') },
    })),

  toggleDislike: articleId =>
    set(state => ({
      engagement: { ...state.engagement, [articleId]: react(state.engagement[articleId] ?? emptyEngagement(articleId), 'DISLIKE') },
    })),

  addComment: (articleId, authorName, text) => {
    const trimmed = text.trim();
    if (!trimmed) return false;
    const comment: ArticleComment = {
      id: genId('cmt'),
      articleId,
      authorName,
      text: trimmed,
      timeMillis: Date.now(),
      likes: 0,
      isOwn: true,
    };
    set(state => ({ comments: [comment, ...state.comments] }));
    return true;
  },

  likeComment: commentId =>
    set(state => ({
      comments: state.comments.map(c => (c.id === commentId ? { ...c, likes: c.likes + 1 } : c)),
    })),

  deleteComment: commentId => set(state => ({ comments: state.comments.filter(c => c.id !== commentId) })),

  submitReport: (articleId, reason, suggestion, reportedBy) => {
    const report: ArticleReport = { id: genId('rpt'), articleId, reason, suggestion, reportedBy, timeMillis: Date.now() };
    set(state => ({ reports: [...state.reports, report] }));
    useNewsStore.getState().reportArticle(articleId).catch(() => {});
  },
}));
