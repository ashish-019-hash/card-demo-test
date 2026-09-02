import { useEffect, useState } from 'react';
import { Navigate, Outlet, Route, Routes, useLocation } from 'react-router-dom';
import { AdminLayout } from './components/Layout';
import { api } from './lib/api';
import type { User } from './lib/types';
import { AddUserPage } from './pages/AddUserPage';
import { AdminHomePage } from './pages/AdminHomePage';
import { DeleteUserPage } from './pages/DeleteUserPage';
import { SignInPage } from './pages/SignInPage';
import { UpdateUserPage } from './pages/UpdateUserPage';

function ProtectedAdmin({ user, onSignOut }: { user: User | null; onSignOut: () => Promise<void> }) {
  const location = useLocation();

  if (!user) {
    return <Navigate to="/sign-in" replace state={{
      message: 'Sign in is required to access administration.',
      from: location.pathname,
    }} />;
  }

  if (user.role !== 'ADMIN') return <Navigate to="/regular-user-not-migrated" replace />;
  return <AdminLayout user={user} onSignOut={onSignOut}><Outlet /></AdminLayout>;
}

function RegularUserBoundary({ user }: { user: User | null }) {
  return <main className="boundary">
    <span className="proposal">Bounded migration</span>
    <h1>Regular-user functions are not migrated.</h1>
    <p>The available CardDemo source only establishes the handoff to the absent regular-user module. Cards, accounts, and transactions are outside this release.</p>
    {user && <p className="sub">Signed in as {user.userId}.</p>}
    <a className="btn primary" href="/sign-in">Return to sign in</a>
  </main>;
}

export default function App() {
  const [user, setUser] = useState<User | null>(null);
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    api.currentSession()
      .then(({ user: current }) => setUser(current))
      .catch(() => setUser(null))
      .finally(() => setChecking(false));
  }, []);

  async function signOut() {
    await api.signOut();
    setUser(null);
  }

  if (checking) return <main className="loading" aria-live="polite">Checking your session…</main>;

  return <Routes>
    <Route path="/sign-in" element={<SignInPage onSignedIn={setUser} />} />
    <Route path="/regular-user-not-migrated" element={<RegularUserBoundary user={user} />} />
    <Route element={<ProtectedAdmin user={user} onSignOut={signOut} />}>
      <Route path="/admin" element={<AdminHomePage />} />
      <Route path="/users/add" element={<AddUserPage />} />
      <Route path="/users/update" element={<UpdateUserPage />} />
      <Route path="/users/delete" element={<DeleteUserPage />} />
    </Route>
    <Route path="*" element={<Navigate to={user?.role === 'ADMIN' ? '/admin' : '/sign-in'} replace />} />
  </Routes>;
}
