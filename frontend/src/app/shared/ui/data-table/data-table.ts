
import { Component, input, output } from '@angular/core';

export interface TableColumn {
  key: string;
  label: string;
}

export type TableRow = {
  id: string | number;
  [key: string]: string | number | boolean | null | undefined;
};

export interface TableActionEvent {
  action: string;
  row: TableRow;
}

@Component({
  selector: 'app-data-table',
  templateUrl: './data-table.html',
  styleUrl: './data-table.scss'
})
export class DataTable {

  readonly columns = input.required<TableColumn[]>();
  readonly rows = input.required<TableRow[]>();
  readonly loading = input(false);
  readonly emptyMessage = input('No se encontraron registros');

  readonly rowSelected = output<TableRow>();
  readonly rowAction = output<TableActionEvent>();

  selectRow(row: TableRow): void {
    this.rowSelected.emit(row);
  }

  executeAction(action: string, row: TableRow): void {
    this.rowAction.emit({ action, row });
  }
}
