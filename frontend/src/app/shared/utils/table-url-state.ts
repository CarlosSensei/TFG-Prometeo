import { ParamMap, Params } from '@angular/router';
import { TableFilterValue, TableSort } from '../ui/data-table/data-table';

export interface TableUrlState {
  filters: Record<string, TableFilterValue>;
  sort: TableSort;
  page: number;
}

export interface TableUrlConfig {
  allowedFilters: string[];
  allowedSorts: string[];
  defaultSort: TableSort;
}

export function readTableUrlState(params: ParamMap, config: TableUrlConfig): TableUrlState {

  const filters: Record<string, TableFilterValue> = {};

  // Filtros de texto, fechas, selección múltiple...
  for (const key of config.allowedFilters) {

    const text = params.get(`${key}.text`);
    const values = params.get(`${key}.values`);
    const from = params.get(`${key}.from`);
    const to = params.get(`${key}.to`);
    const min = params.get(`${key}.min`);
    const max = params.get(`${key}.max`);

    const filter: TableFilterValue = {};

    if (text) filter.text = text;

    if (values) {
      filter.values = values
        .split(',')
        .filter(Boolean);
    }

    if (from) filter.from = from;
    if (to) filter.to = to;

    if (min !== null && min.trim() !== '' &&
        Number.isFinite(Number(min))) {
      filter.min = Number(min);
    }

    if (max !== null && max.trim() !== '' &&
        Number.isFinite(Number(max))) {
      filter.max = Number(max);
    }

    if (Object.keys(filter).length > 0) {
      filters[key] = filter;
    }
  }

  const requestedSort = params.get('sort');

  const sortKey = requestedSort && config.allowedSorts.includes(requestedSort)
      ? requestedSort : config.defaultSort.key;

  const direction = params.get('direction');

  const sort: TableSort = {
    key: sortKey,
    direction: direction === 'asc' || direction === 'desc'
      ? direction : config.defaultSort.direction
  };

  // URL: página 1, 2, 3...
  // Spring Data: página 0, 1, 2...
  const requestedPage = Number(params.get('page') ?? '1');

  const page = Number.isSafeInteger(requestedPage) && requestedPage > 0 
    ? requestedPage - 1 : 0;

  return { filters, sort, page };
}

export function writeTableUrlState(state: TableUrlState, config: TableUrlConfig): Params {

  const params: Params = {};

  for (const key of config.allowedFilters) {

    const filter = state.filters[key];

    if (!filter) continue;

    if (filter.text?.trim()) {
      params[`${key}.text`] = filter.text.trim();
    }

    if (filter.values?.length) {
      params[`${key}.values`] = filter.values.join(',');
    }

    if (filter.from) {
      params[`${key}.from`] = filter.from;
    }

    if (filter.to) {
      params[`${key}.to`] = filter.to;
    }

    if (filter.min != null) {
      params[`${key}.min`] = filter.min;
    }

    if (filter.max != null) {
      params[`${key}.max`] = filter.max;
    }
  }

  if (
    state.sort.key !== config.defaultSort.key ||
    state.sort.direction !== config.defaultSort.direction
  ) {
    params['sort'] = state.sort.key;
    params['direction'] = state.sort.direction;
  }

  if (state.page > 0) {
    params['page'] = state.page + 1;
  }

  return params;
}
