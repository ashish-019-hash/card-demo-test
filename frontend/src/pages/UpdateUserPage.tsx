import { FormEvent, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ErrorSummary, UserFields } from '../components/Forms';
import { Notice, PageHeader } from '../components/Layout';
import { api } from '../lib/api';
import { ApiError, type User } from '../lib/types';
import { firstRequiredError, normalizeUserId } from '../lib/validation';

type EditValues = {
  userId: string;
  firstName: string;
  lastName: string;
  role: string;
  password: string;
};

function toEditValues(user: User): EditValues {
  return { ...user, password: '' };
}

export function UpdateUserPage() {
  const navigate = useNavigate();
  const lookupRef = useRef<HTMLInputElement>(null);
  const [lookupId, setLookupId] = useState('');
  const [user, setUser] = useState<User | null>(null);
  const [values, setValues] = useState<EditValues | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  function reset() {
    setLookupId('');
    setUser(null);
    setValues(null);
    setError(null);
    setSuccess(null);
    lookupRef.current?.focus();
  }

  async function lookup(event: FormEvent) {
    event.preventDefault();
    if (!lookupId.trim()) {
      setError('User ID is required.');
      lookupRef.current?.focus();
      return;
    }

    try {
      const found = await api.getUser(lookupId);
      setUser(found);
      setValues(toEditValues(found));
      setLookupId(found.userId);
      setError(null);
      setSuccess(null);
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to find the user.');
      setUser(null);
      setValues(null);
    }
  }

  function change(field: string, value: string) {
    setValues((current) => current ? { ...current, [field]: value } : current);
    setError(null);
    setSuccess(null);
  }

  async function refetchAfterConflict() {
    if (!user) return;

    try {
      const current = await api.getUser(user.userId);
      setUser(current);
      setValues(toEditValues(current));
      setSuccess(null);
      setError('This user changed elsewhere. The latest values are loaded; review and save again.');
    } catch (reason) {
      setError(reason instanceof Error ? reason.message : 'Unable to refresh the latest user values.');
    }
  }

  async function save(event: FormEvent) {
    event.preventDefault();
    if (!values || !user) return;

    const required = firstRequiredError([
      { label: 'User ID', value: values.userId },
      { label: 'First name', value: values.firstName },
      { label: 'Last name', value: values.lastName },
      { label: 'User type', value: values.role },
    ]);
    if (required) {
      setError(required);
      return;
    }

    try {
      const updated = await api.updateUser(user.userId, {
        firstName: values.firstName,
        lastName: values.lastName,
        role: values.role as User['role'],
        version: user.version,
        ...(values.password ? { password: values.password } : {}),
      });
      setUser(updated);
      setValues(toEditValues(updated));
      setSuccess(`${updated.userId} was updated successfully.`);
      setError(null);
    } catch (reason) {
      if (reason instanceof ApiError && reason.body.code === 'VERSION_CONFLICT') {
        await refetchAfterConflict();
        return;
      }
      setError(reason instanceof Error ? reason.message : 'Unable to update the user.');
    }
  }

  return <div className="content">
    <PageHeader
      title="Update user"
      description={user ? 'Review and change the returned user profile.' : 'Enter a user ID to retrieve the profile.'}
    />
    <section className="card">
      <div className="cardhead"><h2>{user ? 'User details' : 'Find user'}</h2><span className="sub">Program COUSR02C</span></div>
      <div className="cardbody">
        <ErrorSummary message={error} focus />
        {success && <Notice kind="success">{success}</Notice>}
        {!user ? <form onSubmit={lookup} className="lookup-form" noValidate>
          <div className="field">
            <label htmlFor="updateLookup">User ID</label>
            <input ref={lookupRef} id="updateLookup" value={lookupId} onChange={(event) => setLookupId(normalizeUserId(event.target.value))} autoFocus />
            <div className="hint">Submitted in uppercase</div>
          </div>
          <button className="btn primary">Find user</button>
        </form> : <form onSubmit={save} noValidate>
          <Notice>Stored passwords are never displayed. Leave the optional replacement password blank to preserve it.</Notice>
          <UserFields values={values!} onChange={change} includePassword passwordLabel="Replacement password (optional)" disableUserId />
          <div className="actions inline-actions">
            <button className="btn" type="button" onClick={reset}>Clear</button>
            <button className="btn" type="button" onClick={() => navigate('/admin')}>Back</button>
            <button className="btn primary">Save changes</button>
          </div>
        </form>}
      </div>
      {!user && <div className="actions">
        <button className="btn" onClick={() => navigate('/admin')}>Back</button>
        <button className="btn" onClick={reset}>Clear</button>
      </div>}
    </section>
  </div>;
}
