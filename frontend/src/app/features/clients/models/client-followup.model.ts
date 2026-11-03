
export type FollowUpType =
  | 'CALL'
  | 'EMAIL'
  | 'WHATSAPP'
  | 'SMS'
  | 'REMINDER'
  | 'OTHER';

export type FollowUpStatus =
  | 'PENDING'
  | 'COMPLETED'
  | 'CANCELLED';

export interface ClientFollowUp {
  id: number;
  quoteId: number;
  type: FollowUpType;
  status: FollowUpStatus;
  scheduledAt: string;
  completedAt: string | null;
  notes: string | null;
  result: string | null;
  automated: boolean;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CompleteFollowUpPayload {
  result: string | null;
  notes: string | null;
}
