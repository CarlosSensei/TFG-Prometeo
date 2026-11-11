import { Component, input, output } from '@angular/core';
import { TableActionEvent, TableColumn, TableRow } from '../data-table.models';

@Component({
  selector: 'tbody[appTableBody]',
  templateUrl: './table-body.html',
  styleUrl: './table-body.scss',
})
export class TableBody {
  readonly columns = input.required<TableColumn[]>();
  readonly rows = input.required<TableRow[]>();
  readonly loading = input(false);
  readonly emptyMessage = input('No se encontraron registros');
  readonly rowSelected = output<TableRow>();
  readonly rowAction = output<TableActionEvent>();
}
