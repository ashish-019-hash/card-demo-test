import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import type { User } from '../lib/types';

const items = [
  ['/admin', 'Admin menu'],
  ['/users/add', 'Add user'],
  ['/users/update', 'Update user'],
  ['/users/delete', 'Delete user'],
] as const;

export function AdminLayout({
  user,
  onSignOut,
  children,
}: {
  user: User;
  onSignOut: () => Promise<void>;
  children: React.ReactNode;
}) {
  const navigate = useNavigate();
  const [signOutError, setSignOutError] = useState<string | null>(null);
  const [signingOut, setSigningOut] = useState(false);

  async function signOut() {
    setSigningOut(true);
    setSignOutError(null);

    try {
      await onSignOut();
      navigate('/sign-in', { replace: true });
    } catch (reason) {
      setSignOutError(reason instanceof Error ? reason.message : 'Unable to sign out.');
    } finally {
      setSigningOut(false);
    }
  }

  return <div className="shell">
    <aside className="side">
      <div className="brand"><span className="brandmark">C</span><span>CardDemo<br /><small>Administration</small></span></div>
      <nav className="nav" aria-label="Administration">
        {items.map(([to, label]) => <NavLink key={to} to={to} end={to === '/admin'}><span className="dot" />{label}</NavLink>)}
      </nav>
      <div className="sidefoot">
        <strong>{user.userId}</strong><br /><small>Administrator</small>
        {signOutError && <p className="side-error" role="alert">{signOutError}</p>}
        <button className="link-button side-sign-out" onClick={signOut} disabled={signingOut}>
          {signingOut ? 'Signing out…' : 'Sign out'}
        </button>
      </div>
    </aside>
    <main className="main">
      <header className="top"><div className="crumb">CardDemo / User administration</div><div className="meta"><span>Bounded administration</span></div></header>
      {children}
    </main>
  </div>;
}

export function PageHeader({ title, description }: { title: string; description: string }) {
  return <div className="heading"><div><div className="eyebrow">User administration</div><h1>{title}</h1><p className="sub">{description}</p></div><span className="proposal">Modern UI proposal</span></div>;
}

export function Notice({ kind = 'info', children }: { kind?: 'info' | 'success' | 'danger'; children: React.ReactNode }) {
  return <div className={`alert ${kind}`} role={kind === 'danger' ? 'alert' : 'status'}>{children}</div>;
}
