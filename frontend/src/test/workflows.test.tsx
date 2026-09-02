import { MemoryRouter } from 'react-router-dom';
import { cleanup, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { api } from '../lib/api';
import { ApiError } from '../lib/types';
import { AddUserPage } from '../pages/AddUserPage';
import { DeleteUserPage } from '../pages/DeleteUserPage';
import { SignInPage } from '../pages/SignInPage';
import { UpdateUserPage } from '../pages/UpdateUserPage';

vi.mock('../lib/api', () => ({ api: { signIn: vi.fn(), createUser: vi.fn(), getUser: vi.fn(), updateUser: vi.fn(), deleteUser: vi.fn() } }));
const mockedApi = vi.mocked(api);
const renderPage = (page: React.ReactNode) => render(<MemoryRouter>{page}</MemoryRouter>);

describe('bounded administration workflows', () => {
  beforeEach(() => vi.clearAllMocks());
  afterEach(() => cleanup());

  it('reports the first missing add field and clears the form', async () => {
    const user = userEvent.setup(); renderPage(<AddUserPage />);
    await user.click(screen.getByRole('button', { name: 'Add user' }));
    expect(screen.getByRole('alert')).toHaveTextContent('First name is required.');
    await user.type(screen.getByLabelText(/First name/), 'Ada');
    await user.click(screen.getByRole('button', { name: 'Clear' }));
    expect(screen.getByLabelText(/First name/)).toHaveValue('');
    expect(document.activeElement).toBe(screen.getByLabelText(/First name/));
  });

  it('uppercases sign-in IDs and sends credentials to the session endpoint', async () => {
    const user = userEvent.setup(); mockedApi.signIn.mockResolvedValue({ user: { userId: 'ADMIN001', firstName: 'Admin', lastName: 'User', role: 'ADMIN', version: 0 } });
    renderPage(<SignInPage onSignedIn={vi.fn()} />);
    await user.type(screen.getByLabelText('User ID'), 'admin001'); await user.type(screen.getByLabelText('Password'), 'secret');
    await user.click(screen.getByRole('button', { name: 'Sign in' }));
    await waitFor(() => expect(mockedApi.signIn).toHaveBeenCalledWith('ADMIN001', 'secret'));
  });

  it('creates a user and reports success', async () => {
    const user = userEvent.setup();
    mockedApi.createUser.mockResolvedValue({ userId: 'ADA001', firstName: 'Ada', lastName: 'Lovelace', role: 'REGULAR', version: 0 });
    renderPage(<AddUserPage />);
    await user.type(screen.getByLabelText(/First name/), 'Ada');
    await user.type(screen.getByLabelText(/Last name/), 'Lovelace');
    await user.type(screen.getByLabelText(/User ID/), 'ada001');
    await user.type(screen.getByLabelText(/Password/), 'password');
    await user.selectOptions(screen.getByLabelText(/User type/), 'REGULAR');
    await user.click(screen.getByRole('button', { name: 'Add user' }));
    await waitFor(() => expect(mockedApi.createUser).toHaveBeenCalledWith(expect.objectContaining({ userId: 'ADA001' })));
    expect(screen.getByText('ADA001 was added successfully.')).toBeInTheDocument();
  });

  it('shows no-change feedback from update API errors', async () => {
    const user = userEvent.setup(); mockedApi.getUser.mockResolvedValue({ userId: 'JSMITH', firstName: 'Jordan', lastName: 'Smith', role: 'REGULAR', version: 2 }); mockedApi.updateUser.mockRejectedValue(new Error('Please modify a value before updating.'));
    renderPage(<UpdateUserPage />); await user.type(screen.getByLabelText('User ID'), 'jsmith'); await user.click(screen.getByRole('button', { name: 'Find user' }));
    await screen.findByText('Stored passwords are never displayed. Leave the optional replacement password blank to preserve it.'); await user.click(screen.getByRole('button', { name: 'Save changes' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Please modify a value before updating.');
  });

  it('saves changed user details and reports success', async () => {
    const user = userEvent.setup();
    mockedApi.getUser.mockResolvedValue({ userId: 'JSMITH', firstName: 'Jordan', lastName: 'Smith', role: 'REGULAR', version: 2 });
    mockedApi.updateUser.mockResolvedValue({ userId: 'JSMITH', firstName: 'Jordan', lastName: 'Jones', role: 'REGULAR', version: 3 });
    renderPage(<UpdateUserPage />);
    await user.type(screen.getByLabelText('User ID'), 'jsmith');
    await user.click(screen.getByRole('button', { name: 'Find user' }));
    await screen.findByDisplayValue('Smith');
    await user.clear(screen.getByLabelText(/Last name/));
    await user.type(screen.getByLabelText(/Last name/), 'Jones');
    await user.click(screen.getByRole('button', { name: 'Save changes' }));
    await waitFor(() => expect(mockedApi.updateUser).toHaveBeenCalledWith('JSMITH', expect.objectContaining({ lastName: 'Jones', version: 2 })));
    expect(screen.getByText('JSMITH was updated successfully.')).toBeInTheDocument();
  });

  it('refetches the latest profile after a version conflict', async () => {
    const user = userEvent.setup();
    mockedApi.getUser
      .mockResolvedValueOnce({ userId: 'JSMITH', firstName: 'Jordan', lastName: 'Smith', role: 'REGULAR', version: 2 })
      .mockResolvedValueOnce({ userId: 'JSMITH', firstName: 'Jordan', lastName: 'Taylor', role: 'REGULAR', version: 3 });
    mockedApi.updateUser.mockRejectedValue(new ApiError(409, { code: 'VERSION_CONFLICT', message: 'The user was changed by another request.' }));
    renderPage(<UpdateUserPage />);
    await user.type(screen.getByLabelText('User ID'), 'jsmith');
    await user.click(screen.getByRole('button', { name: 'Find user' }));
    await screen.findByDisplayValue('Smith');
    await user.clear(screen.getByLabelText(/Last name/));
    await user.type(screen.getByLabelText(/Last name/), 'Jones');
    await user.click(screen.getByRole('button', { name: 'Save changes' }));
    expect(await screen.findByRole('alert')).toHaveTextContent('The latest values are loaded; review and save again.');
    expect(screen.getByDisplayValue('Taylor')).toBeInTheDocument();
  });

  it('requires review then allows delete confirmation or cancellation', async () => {
    const user = userEvent.setup(); mockedApi.getUser.mockResolvedValue({ userId: 'JSMITH', firstName: 'Jordan', lastName: 'Smith', role: 'REGULAR', version: 2 });
    renderPage(<DeleteUserPage />); await user.type(screen.getByLabelText('User ID'), 'jsmith'); await user.click(screen.getByRole('button', { name: 'Find user' }));
    expect(await screen.findByText('Permanently delete this user?')).toBeInTheDocument(); await user.click(screen.getByRole('button', { name: 'Cancel' }));
    expect(screen.getByRole('button', { name: 'Find user' })).toBeInTheDocument(); expect(mockedApi.deleteUser).not.toHaveBeenCalled();
  });

  it('deletes only after explicit confirmation', async () => {
    const user = userEvent.setup(); mockedApi.getUser.mockResolvedValue({ userId: 'JSMITH', firstName: 'Jordan', lastName: 'Smith', role: 'REGULAR', version: 2 }); mockedApi.deleteUser.mockResolvedValue();
    renderPage(<DeleteUserPage />); await user.type(screen.getByLabelText('User ID'), 'jsmith'); await user.click(screen.getByRole('button', { name: 'Find user' })); await screen.findByText('Permanently delete this user?'); await user.click(screen.getByRole('button', { name: 'Confirm delete' }));
    await waitFor(() => expect(mockedApi.deleteUser).toHaveBeenCalledWith('JSMITH', 2)); expect(screen.getByText('JSMITH was deleted successfully.')).toBeInTheDocument();
  });
});
