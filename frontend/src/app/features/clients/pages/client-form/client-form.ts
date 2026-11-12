import { CommonModule } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { ClientApiService } from '../../services/client-api.service';
import { ClientPayload } from '../../models/client.model';
import { Button } from '../../../../shared/ui/button/button';
import { Input } from '../../../../shared/ui/input/input';

@Component({
  selector: 'app-client-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, Button, Input],
  templateUrl: './client-form.html',
  styleUrl: './client-form.scss'
})
export class ClientForm implements OnInit {

  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ClientApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  clientId: number | null = null;
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly errorMessage = signal('');

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    surname: ['', [Validators.required, Validators.maxLength(50)]],
    lastSurname: ['', Validators.maxLength(50)],
    dni: ['', [Validators.required, Validators.maxLength(10)]],
    phoneNumber: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    birthDate: ['', Validators.required],
    address: ['', Validators.maxLength(200)],
    city: ['', Validators.maxLength(50)],
    postalCode: ['', Validators.pattern(/^\d{5}$/)],
    notes: ['', Validators.maxLength(1000)]
  });

  get isEditMode(): boolean {
    return this.clientId !== null;
  }

  get title(): string {
    return this.isEditMode ? 'Editar cliente' : 'Nuevo cliente';
  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');

    if (idParam === null) return;

    const id = Number(idParam);

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.errorMessage.set('Identificador de cliente inválido.');
      return;
    }

    this.clientId = id;
    this.loadClient(id);
  }

  private loadClient(id: number): void {

    this.loading.set(true);
    this.errorMessage.set('');

    this.api.findById(id).subscribe({
      next: client => {

        this.form.patchValue({
          name: client.name ?? '',
          surname: client.surname ?? '',
          lastSurname: client.lastSurname ?? '',
          dni: client.dni ?? '',
          phoneNumber: client.phoneNumber ?? '',
          email: client.email ?? '',
          birthDate: client.birthDate ?? '',
          address: client.address ?? '',
          city: client.city ?? '',
          postalCode: client.postalCode ?? '',
          notes: client.notes ?? ''
        });

        this.loading.set(false);
      },

      error: error => {
        console.error('Error recuperando cliente:', error);

        this.errorMessage.set(
          'No se ha podido cargar el cliente.'
        );

        this.loading.set(false);
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.loading() || this.saving()) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving.set(true);
    this.errorMessage.set('');

    const value = this.form.getRawValue();

    const optional = (text: string): string | null =>
      text.trim() || null;

    const payload: ClientPayload = {
      name: value.name.trim(),
      surname: value.surname.trim(),
      lastSurname: optional(value.lastSurname),
      dni: value.dni.trim().toUpperCase(),
      phoneNumber: value.phoneNumber.trim(),
      email: optional(value.email),
      birthDate: value.birthDate,
      address: optional(value.address),
      city: optional(value.city),
      postalCode: optional(value.postalCode),
      notes: optional(value.notes)
    };

    const request = this.clientId !== null
      ? this.api.update(this.clientId, payload)
      : this.api.create(payload);

    request.subscribe({
      next: () => {
        this.saving.set(false);
        this.router.navigate(['/clients']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving.set(false);
        this.handleError(error);
      }
    });
  }

  private handleError(error: HttpErrorResponse): void {

    if (error.status === 409) {
      this.errorMessage.set('Ya existe un cliente con este DNI/NIE o hay un conflicto de datos.');

    } else if (error.status === 400) {

      const validationErrors = error.error?.validationErrors ?? error.error?.errors;

      if (validationErrors && typeof validationErrors === 'object') {

        const messages = Object.entries(validationErrors)
          .map(([field, message]) => `${field}: ${message}`)
          .join(' · ');

        this.errorMessage.set(messages || 'Hay datos incorrectos.');

      } else {

        this.errorMessage.set(error.error?.message || 'Hay datos incorrectos. Revisa el formulario.');
      }

    } else if (error.status === 404) {
      this.errorMessage.set('El cliente ya no existe.');

    } else {
      this.errorMessage.set('No se ha podido guardar. Inténtalo de nuevo.');
    }

    console.error('Error guardando cliente:', error.error);
  }

  hasError(field: string): boolean {

    const control = this.form.get(field);

    return !!control && control.invalid && (control.dirty || control.touched);
  }

  goBack(): void {
    
    this.router.navigate(['/clients']);
  }
}
