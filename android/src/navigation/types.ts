/**
 * Ported from news/navigation/Routes.kt. Only the Reader-facing routes are
 * defined for this first vertical slice; Reporter/Editor/Admin/SuperAdmin
 * param lists are added as those phases are built.
 */
export type RootStackParamList = {
  Landing: undefined;
  StaffLogin: undefined;
  ReaderTabs: undefined;
  NewsDetail: { articleId: string };
  CategoryNews: { categoryId: string };
  Comments: { articleId: string; authorName: string };
  Search: undefined;
};

export type ReaderTabParamList = {
  ReaderHome: undefined;
  ReaderCategories: undefined;
  ReaderSaved: undefined;
  ReaderNotifications: undefined;
  ReaderProfile: undefined;
};
