import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ClientFollowups } from './client-followups';

const pendingFollowUp = {
  id: 7,
  quoteId: 4,
  type: 'CALL',
  status: 'PENDING',
  scheduledAt: '2026-11-06T10:00:00',
  completedAt: null,
  notes: null,
  result: null,
  automated: false,
  active: true,
  createdAt: '2026-11-06T10:00:00',
  updatedAt: '2026-11-06T10:00:00',
};

describe('ClientFollowups', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [ClientFollowups],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('completes a pending followup once and reloads the list', async () => {
    const fixture = TestBed.createComponent(ClientFollowups);
    fixture.componentRef.setInput('clientId', 1);
    await fixture.whenStable();
    http
      .expectOne((req) => req.url === '/api/clients/1/followups')
      .flush({ content: [pendingFollowUp], totalElements: 1 });
    const component = fixture.componentInstance;
    component.openCompleteFollowUp(7);
    component.completionResult.set(' Contactado ');
    component.completionNotes.set('  ');
    component.completeFollowUp();
    component.completeFollowUp();
    const completion = http.expectOne('/api/followups/7/complete');
    expect(completion.request.method).toBe('PATCH');
    expect(completion.request.body).toEqual({
      result: 'Contactado',
      notes: null,
    });
    completion.flush({ ...pendingFollowUp, status: 'COMPLETED' });
    await fixture.whenStable();
    expect(component.completingFollowUpId()).toBeNull();
    http
      .expectOne((req) => req.url === '/api/clients/1/followups')
      .flush({
        content: [{ ...pendingFollowUp, status: 'COMPLETED' }],
        totalElements: 1,
      });
    await fixture.whenStable();
    component.openCompleteFollowUp(7);
    expect(component.completingFollowUpId()).toBeNull();
    fixture.destroy();
  });

  it('keeps the form available after a completion error', async () => {
    const fixture = TestBed.createComponent(ClientFollowups);
    fixture.componentRef.setInput('clientId', 1);
    await fixture.whenStable();
    http
      .expectOne((req) => req.url === '/api/clients/1/followups')
      .flush({ content: [pendingFollowUp], totalElements: 1 });
    const component = fixture.componentInstance;
    component.openCompleteFollowUp(7);
    component.completeFollowUp();
    http
      .expectOne('/api/followups/7/complete')
      .flush(null, { status: 500, statusText: 'Server error' });
    await fixture.whenStable();
    expect(component.completing()).toBe(false);
    expect(component.completingFollowUpId()).toBe(7);
    expect(
      fixture.nativeElement.querySelector('[role="alert"]').textContent,
    ).toContain('No se ha podido completar el seguimiento.');
    fixture.destroy();
  });
});
