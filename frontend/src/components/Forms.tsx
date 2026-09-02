import { useEffect, useRef } from 'react';

export function ErrorSummary({ message, focus }: { message: string | null; focus?: boolean }) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => { if (message && focus) ref.current?.focus(); }, [message, focus]);
  return message ? <div className="alert danger" role="alert" tabIndex={-1} ref={ref}>{message}</div> : null;
}

export function UserFields({ values, onChange, includePassword, passwordLabel = 'Password', disableUserId = false, firstNameRef }: {
  values: { userId: string; firstName: string; lastName: string; role: string; password?: string };
  onChange: (field: string, value: string) => void;
  includePassword: boolean;
  passwordLabel?: string;
  disableUserId?: boolean;
  firstNameRef?: React.RefObject<HTMLInputElement | null>;
}) {
  return <div className="formgrid">
    <div className="field"><label htmlFor="firstName">First name <span className="req">*</span></label><input ref={firstNameRef} id="firstName" value={values.firstName} onChange={(event) => onChange('firstName', event.target.value)} autoComplete="given-name" /></div>
    <div className="field"><label htmlFor="lastName">Last name <span className="req">*</span></label><input id="lastName" value={values.lastName} onChange={(event) => onChange('lastName', event.target.value)} autoComplete="family-name" /></div>
    <div className="field"><label htmlFor="userId">User ID <span className="req">*</span></label><input id="userId" value={values.userId} disabled={disableUserId} onChange={(event) => onChange('userId', event.target.value.toUpperCase())} autoCapitalize="characters" autoComplete="username" /><div className="hint">Submitted in uppercase</div></div>
    {includePassword && <div className="field"><label htmlFor="password">{passwordLabel} <span className="req">*</span></label><input id="password" type="password" value={values.password ?? ''} onChange={(event) => onChange('password', event.target.value)} autoComplete="new-password" /></div>}
    <div className={includePassword ? 'field full' : 'field'}><label htmlFor="role">User type <span className="req">*</span></label><select id="role" value={values.role} onChange={(event) => onChange('role', event.target.value)}><option value="">Select user type</option><option value="ADMIN">Administrator</option><option value="REGULAR">Standard user</option></select></div>
  </div>;
}
