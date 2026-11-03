import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
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
  loading = false;
  saving = false;
  errorMessage = '';

  readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.maxLength(50)]],
    surname: ['', [Validators.required, Validators.maxLength(50)]],
    lastSurname: ['', Validators.maxLength(50)],
    dni: ['', [Validators.required, Validators.maxLength(10)]],
    phoneNumber: ['', Validators.required],
    email: ['', Validators.email],
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
      this.errorMessage = 'Identificador de cliente inválido.';
      return;
    }

    this.clientId = id;
    this.loadClient(id);
  }

  private loadClient(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.api.getById(id).subscribe({
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

        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'No se ha podido cargar el cliente.';
        this.loading = false;
      }
    });
  }

  save(): void {
    if (this.form.invalid || this.loading || this.saving) {
      this.form.markAllAsTouched();
      return;
    }

    this.saving = true;
    this.errorMessage = '';

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
        this.saving = false;
        this.router.navigate(['/clients']);
      },
      error: (error: HttpErrorResponse) => {
        this.saving = false;
        this.handleError(error);
      }
    });
  }

  private handleError(error: HttpErrorResponse): void {
    if (error.status === 409) {
      this.errorMessage = 'Ya existe un cliente con este DNI/NIE.';
    } else if (error.status === 400) {
      this.errorMessage =
        'Hay datos incorrectos. Revisa los campos del formulario.';
    } else if (error.status === 404) {
      this.errorMessage = 'El cliente ya no existe.';
    } else {
      this.errorMessage =
        'No se ha podido guardar. Inténtalo de nuevo.';
    }
  }

  hasError(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid &&
      (control.dirty || control.touched);
  }

  goBack(): void {
    this.router.navigate(['/clients']);
  }
}
