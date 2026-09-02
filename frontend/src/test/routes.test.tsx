import { cleanup, render, screen, waitFor } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import App from '../App';
import { api } from '../lib/api';

vi.mock('../lib/api', () => ({
  api: {
    currentSession: vi.fn(),
    signOut: vi.fn(),
    signIn: vi.fn(),
    createUser: vi.fn(),
    getUser: vi.fn(),
    updateUser: vi.fn(),
    deleteUser: vi.fn(),
  },
}));

const mockedApi = vi.mocked(api);
const admin = { userId: 'ADMIN001', firstName: 'Admin', lastName: 'User', role: 'ADMIN' as const, version: 0 };
const regular = { userId: 'REGULAR1', firstName: 'Regular', lastName: 'User', role: 'REGULAR' as const, version: 0 };

function renderAt(path: string) {
  window.history.pushState({}, '', path);
  return render(<BrowserRouter><App /></BrowserRouter>);
}

describe('session route boundaries', () => {
  beforeEach(() => vi.clearAllMocks());
  afterEach(() => cleanup());

  it('redirects an anonymous administration request to sign-in', async () => {
    mockedApi.currentSession.mockRejectedValue(new Error('Authentication is required.'));
    renderAt('/admin');
    expect(await screen.findByText('Welcome back')).toBeInTheDocument();
    expect(screen.getByText('Sign in is required to access administration.')).toBeInTheDocument();
  });

  it('keeps regular users at the explicit not-migrated boundary', async () => {
    mockedApi.currentSession.mockResolvedValue({ user: regular });
    renderAt('/admin');
    expect(await screen.findByText('Regular-user functions are not migrated.')).toBeInTheDocument();
    expect(screen.getByText('Signed in as REGULAR1.')).toBeInTheDocument();
  });

  it('keeps the current user and route when sign-out fails', async () => {
    const user = userEvent.setup();
    mockedApi.currentSession.mockResolvedValue({ user: admin });
    mockedApi.signOut.mockRejectedValue(new Error('Session could not be ended.'));
    renderAt('/admin');
    expect(await screen.findByRole('heading', { name: 'Admin menu' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Sign out' }));

    expect(await screen.findByRole('alert')).toHaveTextContent('Session could not be ended.');
    expect(screen.getByRole('heading', { name: 'Admin menu' })).toBeInTheDocument();
  });

  it('clears local access and redirects only after server sign-out succeeds', async () => {
    const user = userEvent.setup();
    mockedApi.currentSession.mockResolvedValue({ user: admin });
    mockedApi.signOut.mockResolvedValue();
    renderAt('/admin');
    expect(await screen.findByRole('heading', { name: 'Admin menu' })).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: 'Sign out' }));

    await waitFor(() => expect(screen.getByText('Welcome back')).toBeInTheDocument());
    expect(mockedApi.signOut).toHaveBeenCalledOnce();
  });
});
