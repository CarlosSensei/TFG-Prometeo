import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ClientQuotes } from './client-quotes';

describe('ClientQuotes', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ClientQuotes],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('cancels the previous page request and resets pagination when the client changes', async () => {
    const fixture = TestBed.createComponent(ClientQuotes);
    fixture.componentRef.setInput('clientId', 1);
    await fixture.whenStable();
    http
      .expectOne((req) => req.url === '/api/quotes/client/1')
      .flush({ content: [], totalElements: 12 });

    fixture.componentInstance.changeQuotePage(2);
    await fixture.whenStable();
    const pending = http.expectOne((req) => req.params.get('page') === '2');

    fixture.componentRef.setInput('clientId', 2);
    await fixture.whenStable();
    expect(pending.cancelled).toBe(true);
    const replacement = http.expectOne(
      (req) => req.url === '/api/quotes/client/2',
    );
    expect(replacement.request.params.get('page')).toBe('0');
    expect(replacement.request.params.get('size')).toBe('5');
    replacement.flush({ content: [], totalElements: 0 });
    fixture.destroy();
  });

  it('cancels pending requests when the tab component is destroyed', async () => {
    const fixture = TestBed.createComponent(ClientQuotes);
    fixture.componentRef.setInput('clientId', 1);
    await fixture.whenStable();
    const pending = http.expectOne((req) => req.url === '/api/quotes/client/1');
    fixture.destroy();
    expect(pending.cancelled).toBe(true);
  });

  it('renders an error when loading fails', async () => {
    const fixture = TestBed.createComponent(ClientQuotes);
    fixture.componentRef.setInput('clientId', 1);
    await fixture.whenStable();
    http
      .expectOne((req) => req.url === '/api/quotes/client/1')
      .flush(null, { status: 500, statusText: 'Server error' });
    await fixture.whenStable();
    expect(
      fixture.nativeElement.querySelector('[role="alert"]').textContent,
    ).toContain('No se han podido recuperar los presupuestos.');
    expect(fixture.componentInstance.documentsLoading()).toBe(false);
    fixture.destroy();
  });
});
