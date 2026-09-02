export type Role = 'ADMIN' | 'REGULAR';

export interface User {
  userId: string;
  firstName: string;
  lastName: string;
  role: Role;
  version: number;
}

export interface SessionResponse { user: User; }

export interface ApiErrorBody {
  code: string;
  message: string;
  field?: string | null;
  traceId?: string | null;
}

export class ApiError extends Error {
  constructor(public readonly status: number, public readonly body: ApiErrorBody) {
    super(body.message);
  }
}
