import { Component, computed, input, output } from '@angular/core';

@Component({
  selector: 'app-pagination',
  templateUrl: './pagination.html',
  styleUrl: './pagination.scss'
})
export class Pagination {
  readonly page = input(0);
  readonly size = input(10);
  readonly totalElements = input(0);

  readonly pageChange = output<number>();

  readonly totalPages = computed(() =>
    Math.ceil(this.totalElements() / Math.max(1, this.size()))
  );

  readonly firstItem = computed(() =>
    this.totalElements() === 0
      ? 0
      : this.page() * this.size() + 1
  );

  readonly lastItem = computed(() =>
    Math.min(
      (this.page() + 1) * this.size(),
      this.totalElements()
    )
  );

  previous(): void {
    if (this.page() > 0) {
      this.pageChange.emit(this.page() - 1);
    }
  }

  next(): void {
    if (this.page() + 1 < this.totalPages()) {
      this.pageChange.emit(this.page() + 1);
    }
  }
}
