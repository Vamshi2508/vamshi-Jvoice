/**
 * Ported from news/navigation/Routes.kt.
 */
export type RootStackParamList = {
  Landing: undefined;
  StaffLogin: undefined;
  ReaderTabs: undefined;
  NewsDetail: { articleId: string };
  CategoryNews: { categoryId: string };
  Comments: { articleId: string; authorName: string };
  Search: undefined;

  ReporterStack: undefined;
  EditorDrawer: undefined;
  NewsAdminDrawer: undefined;
  SuperAdminDrawer: undefined;

  ArticleEditor: { articleId?: string } | undefined;
  ArticleReview: { articleId: string };
};

export type ReaderTabParamList = {
  ReaderHome: undefined;
  ReaderCategories: undefined;
  ReaderSaved: undefined;
  ReaderNotifications: undefined;
  ReaderProfile: undefined;
};

export type ReporterStackParamList = {
  ReporterDashboard: undefined;
  MyNews: undefined;
};

export type EditorDrawerParamList = {
  EditorDashboard: undefined;
  ReviewQueue: undefined;
};

export type NewsAdminDrawerParamList = {
  AdminDashboard: undefined;
  NewsManagement: undefined;
  CategoryManagement: undefined;
  ReporterManagement: undefined;
};

export type SuperAdminDrawerParamList = {
  SuperAdminDashboard: undefined;
  UserManagement: undefined;
  RoleManagement: undefined;
  SystemSettings: undefined;
};
