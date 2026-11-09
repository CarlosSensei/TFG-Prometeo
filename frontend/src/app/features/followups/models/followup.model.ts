import { Page } from '../../clients/models/page.model';

export type FollowUpType = 'CALL' | 'EMAIL' | 'WHATSAPP' | 'SMS' | 'REMINDER' | 'OTHER';
export type FollowUpStatus = 'PENDING' | 'COMPLETED' | 'CANCELLED';

export interface FollowUp {
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

export interface FollowUpSearch {
  quoteId?: number;
  type?: FollowUpType;
  status?: FollowUpStatus;
  types?: FollowUpType[];
  statuses?: FollowUpStatus[];
  scheduledFrom?: string;
  scheduledTo?: string;
  automated?: boolean;
  active: boolean;
  page: number;
  size: number;
  sortBy: 'scheduledAt' | 'createdAt' | 'completedAt' | 'type' | 'status';
  sortDirection: 'asc' | 'desc';
}

export interface CreateFollowUpPayload {
  type: FollowUpType;
  scheduledAt: string;
  notes?: string | null;
  automated?: boolean;
}

export interface CompleteFollowUpPayload {
  result: string | null;
  notes: string | null;
}

export type FollowUpPage = Page<FollowUp>;

export const FOLLOWUP_TYPES: { value: FollowUpType; label: string }[] = [
  { value: 'CALL', label: 'Llamada' },
  { value: 'EMAIL', label: 'Correo electrónico' },
  { value: 'WHATSAPP', label: 'WhatsApp' },
  { value: 'SMS', label: 'SMS' },
  { value: 'REMINDER', label: 'Recordatorio' },
  { value: 'OTHER', label: 'Otro' }
];

export const FOLLOWUP_STATUSES: { value: FollowUpStatus; label: string }[] = [
  { value: 'PENDING', label: 'Pendiente' },
  { value: 'COMPLETED', label: 'Completado' },
  { value: 'CANCELLED', label: 'Cancelado' }
];
