import { afterEach, describe, expect, it, vi } from 'vitest';
import { api } from '../lib/api';

function response(status = 204, body?: unknown): Response {
  return new Response(body === undefined ? null : JSON.stringify(body), {
    status,
    headers: { 'Content-Type': 'application/json' },
  });
}

describe('API client CSRF handling', () => {
  afterEach(() => {
    vi.unstubAllGlobals();
    document.cookie = 'XSRF-TOKEN=; Max-Age=0; path=/';
  });

  it('exempts only the initial sign-in POST from CSRF', async () => {
    document.cookie = 'XSRF-TOKEN=token%20value; path=/';
    const fetchMock = vi.fn().mockResolvedValue(response(200, { user: {} }));
    vi.stubGlobal('fetch', fetchMock);

    await api.signIn('ADMIN001', 'password');

    const [, options] = fetchMock.mock.calls[0];
    expect(options.credentials).toBe('include');
    expect(new Headers(options.headers).get('X-XSRF-TOKEN')).toBeNull();
  });

  it('sends the XSRF token when ending a browser session', async () => {
    document.cookie = 'XSRF-TOKEN=token%20value; path=/';
    const fetchMock = vi.fn().mockResolvedValue(response());
    vi.stubGlobal('fetch', fetchMock);

    await api.signOut();

    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe('/api/session');
    expect(options.method).toBe('DELETE');
    expect(options.credentials).toBe('include');
    expect(new Headers(options.headers).get('X-XSRF-TOKEN')).toBe('token value');
  });

  it('sends the XSRF token for authenticated create, update, and delete requests', async () => {
    document.cookie = 'XSRF-TOKEN=csrf-token; path=/';
    const fetchMock = vi.fn().mockImplementation(() => Promise.resolve(response(200, { userId: 'JSMITH' })));
    vi.stubGlobal('fetch', fetchMock);

    await api.createUser({ userId: 'jsmith', firstName: 'Jordan', lastName: 'Smith', password: 'secret', role: 'REGULAR' });
    await api.updateUser('jsmith', { firstName: 'Jordan', lastName: 'Smith', role: 'REGULAR', version: 1 });
    await api.deleteUser('jsmith', 1);

    for (const [, options] of fetchMock.mock.calls) {
      expect(new Headers(options.headers).get('X-XSRF-TOKEN')).toBe('csrf-token');
    }
  });
});
