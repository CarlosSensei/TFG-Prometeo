export interface Client {
  id: number;
  name: string;
  surname: string;
  lastSurname: string | null;
  dni: string;
  phoneNumber: string;
  email: string | null;
  birthDate: string;
  address: string | null;
  city: string | null;
  postalCode: string | null;
  notes: string | null;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export type ClientPayload = Pick<
  Client,
  | 'name'
  | 'surname'
  | 'lastSurname'
  | 'dni'
  | 'phoneNumber'
  | 'email'
  | 'birthDate'
  | 'address'
  | 'city'
  | 'postalCode'
  | 'notes'
>;