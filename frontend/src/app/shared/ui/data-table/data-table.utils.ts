import { TableFilterValue } from './data-table.models';

export function hasTableFilter(filter: TableFilterValue | undefined): boolean {
  if (!filter) {
    return false;
  }

  return !!(
    filter.text?.trim() ||
    filter.values?.length ||
    filter.from ||
    filter.to ||
    filter.min != null ||
    filter.max != null
  );
}
