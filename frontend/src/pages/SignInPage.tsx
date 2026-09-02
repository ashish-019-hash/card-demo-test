import { FormEvent, useRef, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api } from '../lib/api';
import type { User } from '../lib/types';
import { firstRequiredError, normalizeUserId } from '../lib/validation';
import { ErrorSummary } from '../components/Forms';

export function SignInPage({ onSignedIn }: { onSignedIn: (user: User) => void }) {
  const navigate = useNavigate();
  const location = useLocation();
  const [userId, setUserId] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(location.state?.message ?? null);
  const [busy, setBusy] = useState(false);
  const userIdRef = useRef<HTMLInputElement>(null);

  async function submit(event: FormEvent) {
    event.preventDefault();
    const required = firstRequiredError([{ label: 'User ID', value: userId }, { label: 'Password', value: password }]);
    if (required) { setError(required); userIdRef.current?.focus(); return; }
    setBusy(true); setError(null);
    try {
      const { user } = await api.signIn(normalizeUserId(userId), password);
      onSignedIn(user);
      if (user.role === 'ADMIN') navigate('/admin', { replace: true });
      else navigate('/regular-user-not-migrated', { replace: true });
    } catch (reason) { setError(reason instanceof Error ? reason.message : 'Unable to sign in.'); }
    finally { setBusy(false); }
  }

  return <div className="sign-in-shell"><section className="sign-in-intro"><div className="brand"><span className="brandmark">C</span><span>CardDemo</span></div><div><div className="eyebrow light">Modernized administration</div><h1>Secure access to card operations.</h1><p>Sign in with your existing CardDemo credentials to continue to the appropriate workspace.</p></div><small>Bounded identity and administration slice</small></section>
    <main className="sign-in-main"><form className="sign-in-form" onSubmit={submit} noValidate><span className="proposal">Modern UI proposal</span><h1>Welcome back</h1><p className="sub">Enter your user ID and password.</p><ErrorSummary message={error} focus />
      <div className="field"><label htmlFor="signInUserId">User ID</label><input ref={userIdRef} id="signInUserId" value={userId} onChange={(event) => setUserId(normalizeUserId(event.target.value))} autoCapitalize="characters" autoComplete="username" autoFocus /><div className="hint">Submitted in uppercase</div></div>
      <div className="field"><label htmlFor="signInPassword">Password</label><input id="signInPassword" type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" /></div>
      <button className="btn primary full-button" disabled={busy}>{busy ? 'Signing in…' : 'Sign in'}</button></form></main>
  </div>;
}
