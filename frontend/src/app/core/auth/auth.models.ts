export interface LoginRequest {
  email: string;
  password: string;
}

export type UserRole =
  | 'ADMIN'
  | 'OPTICIAN'
  | 'SELLER'
  | 'RECEPTIONIST';

export interface LoginResponse {
  token: string;
  tokenType: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
  role: UserRole;
}

export type AuthUser = Omit<
  LoginResponse,
  'token' | 'tokenType'
>;
