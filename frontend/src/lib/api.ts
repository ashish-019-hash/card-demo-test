import { ApiError, type ApiErrorBody, type SessionResponse, type User } from './types';

function csrfToken(): string | undefined {
  return document.cookie
    .split('; ')
    .find((cookie) => cookie.startsWith('XSRF-TOKEN='))
    ?.split('=')[1];
}

function requiresCsrfToken(method: string | undefined, path: string): boolean {
  return method !== undefined
    && !['GET', 'HEAD'].includes(method)
    && !(method === 'POST' && path === '/session');
}

async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const headers = new Headers(init.headers);
  const method = init.method?.toUpperCase();

  if (init.body) headers.set('Content-Type', 'application/json');
  if (requiresCsrfToken(method, path)) {
    const token = csrfToken();
    if (token) headers.set('X-XSRF-TOKEN', decodeURIComponent(token));
  }

  const response = await fetch(`/api${path}`, {
    ...init,
    headers,
    credentials: 'include',
  });

  if (!response.ok) throw await apiError(response);
  return response.status === 204 ? undefined as T : response.json() as Promise<T>;
}

async function apiError(response: Response): Promise<ApiError> {
  let body: ApiErrorBody = {
    code: 'INTERNAL_ERROR',
    message: 'An unexpected error occurred.',
  };

  try {
    body = await response.json() as ApiErrorBody;
  } catch {
    // Gateway responses may not use the API's JSON error shape.
  }

  return new ApiError(response.status, body);
}

export const api = {
  signIn: (userId: string, password: string) => request<SessionResponse>('/session', {
    method: 'POST',
    body: JSON.stringify({ userId, password }),
  }),
  currentSession: () => request<SessionResponse>('/session'),
  signOut: () => request<void>('/session', { method: 'DELETE' }),
  getUser: (userId: string) => request<User>(`/users/${encodeURIComponent(userId.toUpperCase())}`),
  createUser: (user: Omit<User, 'version'> & { password: string }) => request<User>('/users', {
    method: 'POST',
    body: JSON.stringify({ ...user, userId: user.userId.toUpperCase() }),
  }),
  updateUser: (
    userId: string,
    user: Pick<User, 'firstName' | 'lastName' | 'role' | 'version'> & { password?: string },
  ) => request<User>(`/users/${encodeURIComponent(userId.toUpperCase())}`, {
    method: 'PUT',
    body: JSON.stringify(user),
  }),
  deleteUser: (userId: string, version: number) => request<void>(
    `/users/${encodeURIComponent(userId.toUpperCase())}?version=${version}`,
    { method: 'DELETE' },
  ),
};
