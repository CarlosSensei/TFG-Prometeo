export type NotificationType =
  | 'FOLLOW_UP_DUE'
  | 'FOLLOW_UP_OVERDUE'
  | 'QUOTE_EXPIRING'
  | 'ORDER_READY';

export interface AppNotification {
  id: number;
  type: NotificationType;
  title: string;
  message: string;
  followUpId: number | null;
  quoteId: number | null;
  read: boolean;
  readAt: string | null;
  createdAt: string;
}

export interface NotificationPage {
  content: AppNotification[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
}
