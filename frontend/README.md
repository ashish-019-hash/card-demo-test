# CardDemo frontend

React + TypeScript + Vite client for the bounded identity and administrator user-maintenance slice. It is a modern interpretation of the approved designs because the legacy BMS maps are unavailable.

## Scope

- Sign in and browser-session inspection/end.
- Administrator menu, add, update lookup/edit, and delete lookup/review/explicit confirmation.
- Regular users stop at the explicit **not migrated** boundary. Cards, accounts, transactions, and unknown admin functions are excluded.
- User IDs are uppercased. The API does not return passwords; update provides an optional replacement password only.
- Update resolves an optimistic-lock `VERSION_CONFLICT` by re-fetching the user, loading the current values, and asking the administrator to review and save again.

## Run

1. Start the backend from `../backend` with `mvn spring-boot:run` (default `http://localhost:8080`).
2. Install and start the frontend:

   ```bash
   cd frontend
   npm install
   npm run dev
   ```

The Vite dev server runs on port 5173, binds to `0.0.0.0`, accepts preview hosts, and proxies relative `/api` calls to `http://localhost:8080`. Override the proxy target if necessary:

```bash
VITE_API_PROXY_TARGET=http://localhost:8080 npm run dev
```

## Session and CSRF behavior

The API client sends `credentials: 'include'` for every request. The only CSRF-exempt request is the initial `POST /api/session` login exchange. Every other state-changing request, including `DELETE /api/session` sign-out, forwards the `XSRF-TOKEN` cookie in the `X-XSRF-TOKEN` header.

The app only clears its local session state and navigates to sign-in after the server confirms sign-out. If sign-out fails, the administrator remains on the current screen and receives an error message.

Development seed credentials are documented in `../backend/README.md`.

## Verification

```bash
npm test
npm run build
```

The tests cover ordered required validation, uppercase sign-in IDs, clear/focus behavior, API CSRF headers, successful create/update, update conflict re-fetch, explicit delete cancellation/confirmation, route guards, the regular-user boundary, and sign-out success/failure behavior.
