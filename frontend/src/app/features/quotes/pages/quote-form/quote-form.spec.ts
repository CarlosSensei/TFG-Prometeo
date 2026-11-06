import { TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import {
  ActivatedRoute,
  convertToParamMap,
  provideRouter,
  Router,
} from '@angular/router';
import { QuoteForm } from './quote-form';
import { QuoteClientSelector } from '../../components/quote-client-selector/quote-client-selector';
import { Client } from '../../../clients/models/client.model';

const client: Client = {
  id: 7,
  name: 'Ana',
  surname: 'García',
  lastSurname: null,
  dni: '12345678Z',
  phoneNumber: '600000000',
  email: null,
  birthDate: '1990-01-01',
  address: null,
  city: null,
  postalCode: null,
  notes: null,
  active: true,
  createdAt: '',
  updatedAt: '',
};
const item = {
  id: 3,
  name: 'Montura',
  sku: 'M-3',
  brand: null,
  salePrice: 100,
  taxRate: 21,
  stock: 5,
  active: true,
};

describe('QuoteForm component composition', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [QuoteForm],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        {
          provide: ActivatedRoute,
          useValue: {
            snapshot: {
              paramMap: convertToParamMap({}),
              queryParamMap: convertToParamMap({
                clientId: '7',
                returnUrl: '/quotes?page=2',
              }),
            },
          },
        },
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('displays the preselected client once and clears the form selection when typing', async () => {
    const fixture = TestBed.createComponent(QuoteForm);
    await fixture.whenStable();
    http.expectOne('/api/clients/7').flush(client);
    await fixture.whenStable();
    const input: HTMLInputElement =
      fixture.nativeElement.querySelector('#client-query');
    expect(input.value).toBe('Ana García');
    expect(
      fixture.nativeElement.querySelectorAll('.selected-record').length,
    ).toBe(1);
    input.value = 'A';
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect(fixture.componentInstance.form.controls.clientId.value).toBeNull();
    expect(fixture.nativeElement.querySelector('.selected-record')).toBeNull();
    fixture.destroy();
  });

  it('saves lines edited through child components and returns to the saved list URL', async () => {
    const navigate = vi
      .spyOn(TestBed.inject(Router), 'navigateByUrl')
      .mockResolvedValue(true);
    const fixture = TestBed.createComponent(QuoteForm);
    await fixture.whenStable();
    http.expectOne('/api/clients/7').flush(client);
    await fixture.whenStable();
    const search: HTMLInputElement =
      fixture.nativeElement.querySelector('#inventory-query');
    search.value = 'Montura';
    search.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    fixture.nativeElement
      .querySelector('app-quote-inventory-search app-button button')
      .click();
    http.expectOne('/api/inventory/search').flush({ content: [item] });
    await fixture.whenStable();
    fixture.nativeElement
      .querySelector('app-quote-inventory-search .lookup-result')
      .click();
    await fixture.whenStable();
    const quantity: HTMLInputElement =
      fixture.nativeElement.querySelector('[id^="qty-"]');
    quantity.value = '2';
    quantity.dispatchEvent(new Event('input'));
    const discount: HTMLInputElement =
      fixture.nativeElement.querySelector('[id^="discount-"]');
    discount.value = '10';
    discount.dispatchEvent(new Event('input'));
    await fixture.whenStable();
    expect(fixture.componentInstance.totals().total).toBe(229.9);
    fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { bubbles: true, cancelable: true }));
    const save = http.expectOne('/api/quotes');
    expect(save.request.method).toBe('POST');
    expect(save.request.body.clientId).toBe(7);
    expect(save.request.body.items).toEqual([
      { inventoryItemId: 3, quantity: 2, discount: 10 },
    ]);
    save.flush({ id: 1 });
    await fixture.whenStable();
    expect(navigate).toHaveBeenCalledWith('/quotes?page=2');
    fixture.destroy();
  });

  it('cancels client autocomplete when selecting a result', async () => {
    const fixture = TestBed.createComponent(QuoteForm);
    await fixture.whenStable();
    http.expectOne('/api/clients/7').flush(client);
    await fixture.whenStable();
    const selector = fixture.debugElement.query(
      By.directive(QuoteClientSelector),
    ).componentInstance as QuoteClientSelector;
    selector.updateClientQuery('Ana');
    await new Promise((resolve) => setTimeout(resolve, 350));
    const pending = http.expectOne('/api/clients/search');
    selector.selectClient(client);
    await fixture.whenStable();
    expect(pending.cancelled).toBe(true);
    expect(fixture.componentInstance.form.controls.clientId.value).toBe(7);
    expect(selector.searchingClients()).toBe(false);
    fixture.destroy();
  });
});
