import { FormEvent, useRef, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ErrorSummary, UserFields } from '../components/Forms';
import { Notice, PageHeader } from '../components/Layout';
import { api } from '../lib/api';
import { firstRequiredError } from '../lib/validation';

const empty = { firstName: '', lastName: '', userId: '', password: '', role: '' };
export function AddUserPage() {
  const navigate = useNavigate(); const [values, setValues] = useState(empty); const [error, setError] = useState<string | null>(null); const [success, setSuccess] = useState<string | null>(null); const firstRef = useRef<HTMLInputElement>(null);
  function change(field: string, value: string) { setValues((current) => ({ ...current, [field]: value })); setError(null); setSuccess(null); }
  function clear() { setValues(empty); setError(null); setSuccess(null); firstRef.current?.focus(); }
  async function submit(event: FormEvent) {
    event.preventDefault();
    const required = firstRequiredError([{ label: 'First name', value: values.firstName }, { label: 'Last name', value: values.lastName }, { label: 'User ID', value: values.userId }, { label: 'Password', value: values.password }, { label: 'User type', value: values.role }]);
    if (required) { setError(required); firstRef.current?.focus(); return; }
    try { const user = await api.createUser(values as typeof values & { role: 'ADMIN' | 'REGULAR' }); setSuccess(`${user.userId} was added successfully.`); setValues(empty); firstRef.current?.focus(); } catch (reason) { setError(reason instanceof Error ? reason.message : 'Unable to add the user.'); }
  }
  return <div className="content"><PageHeader title="Add user" description="Create a CardDemo user profile. All fields are required." /><form className="card" onSubmit={submit} noValidate><div className="cardhead"><h2>User details</h2><span className="sub">Program COUSR01C</span></div><div className="cardbody"><ErrorSummary message={error} focus /><>{success && <Notice kind="success">{success}</Notice>}</><UserFields values={values} onChange={change} includePassword firstNameRef={firstRef} /></div><div className="actions"><div className="actionset"><button className="btn" type="button" onClick={() => navigate('/admin')}>Back</button><button className="btn" type="button" onClick={clear}>Clear</button></div><button className="btn primary">Add user</button></div></form></div>;
}
