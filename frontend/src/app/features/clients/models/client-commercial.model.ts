export interface ClientStats {
    quotesCount: number;
    ordersCount: number;
    convertedQuotesCount: number;
    totalSpent: number;
    averageTicket: number;
    conversionRate: number;
    lastOrderDate: string | null;
    lastActivity: string | null;
    pendingFollowUps: number;
    pendingOrdersCount: number;
    openQuotesCount: number;
}

export type ClientTimelineType =
    | 'QUOTE'
    | 'ORDER'
    | 'FOLLOW_UP';

export interface ClientTimelineEvent {
    id: string;
    type: ClientTimelineType;
    referenceId: number;
    date: string;
    title: string;
    description: string | null;
    status: string | null;
    amount: number | null;
}
