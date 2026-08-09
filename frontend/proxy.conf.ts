/**
 * Dev-server proxy (Vite via @angular/build).
 *
 * GET /users is BOTH the API list endpoint AND the SPA route. A hard refresh of
 * /users must render index.html, not the API's raw JSON. This config proxies
 * only real API calls; bare GET /users (SPA navigation) is bypassed so Vite
 * serves the SPA.
 *
 * Matches (regex, same combined form as the previous JSON):
 *   /users (GET with query = list XHR; POST = create)   -> API
 *   /users/check                                        -> API
 *   /users/{id}                                         -> API
 *   /accounts/{id}/transactions, /accounts/transactions/summary -> API
 * SPA routes (/users/new, /users/{id}/edit, /users/{id}/transactions,
 * /dashboard, ...) never match the regex -> served as index.html.
 */
export default {
  '^/auth/(login|register)$': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    secure: false,
  },
  '^/audit(\\?.*)?$': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    secure: false,
  },
  '^/users(\\?.*)?$|^/users/check(\\?.*)?$|^/users/[0-9]+(\\?.*)?$|^/accounts/[0-9]+/transactions(\\?.*)?$|^/accounts/transactions/summary(\\?.*)?$': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    secure: false,
    // A bare GET /users (no query string) is SPA navigation, not an API call.
    // Serve index.html so the Angular router takes over and renders the list.
    bypass: (req: { method: string; url: string }) => {
      if (req.method === 'GET' && req.url === '/users') {
        return '/index.html';
      }
      return undefined;
    },
  },
};
