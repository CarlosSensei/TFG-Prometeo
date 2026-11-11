export type FilterType = 'text' | 'multi' | 'date' | 'number';

export interface FilterOption {
  value: string;
  label: string;
}

export interface TableFilterValue {
  text?: string;
  values?: string[];
  from?: string;
  to?: string;
  min?: number | null;
  max?: number | null;
}

export interface TableColumn {
  key: string;
  label: string;
  sortable?: boolean;
  filter?: {
    type: FilterType;
    options?: FilterOption[];
    placeholder?: string;
  };
}

export type TableRow = {
  id: string | number;
  [key: string]: string | number | boolean | null | undefined;
};

export interface TableActionEvent {
  action: string;
  row: TableRow;
}

export interface TableFilterEvent {
  key: string;
  value: TableFilterValue;
}

export type SortDirection = 'asc' | 'desc';

export interface TableSort {
  key: string;
  direction: SortDirection;
}
