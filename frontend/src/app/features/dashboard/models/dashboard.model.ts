export interface DashboardActivity {
  type: 'QUOTE' | 'ORDER';
  id: number;
  reference: string;
  status: string;
  occurredAt: string;
}

export interface DashboardFollowUp {
  id: number;
  quoteId: number;
  quoteNumber: string;
  type: string;
  scheduledAt: string;
  overdue: boolean;
}

export interface DashboardSummary {
  activeClients: number;
  openQuotes: number;
  activeOrders: number;
  pendingFollowUps: number;
  overdueFollowUps: number;
  convertedQuotes: number;
  decidedQuotes: number;
  conversionRate: number;
  deliveredRevenue: number;
  recentActivity: DashboardActivity[];
  upcomingFollowUps: DashboardFollowUp[];
}

export interface DashboardCalendarAction extends DashboardFollowUp {}

export interface DashboardCalendar {
  year: number;
  month: number;
  monthActions: DashboardCalendarAction[];
  nextActions: DashboardCalendarAction[];
}
