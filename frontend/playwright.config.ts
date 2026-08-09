import { defineConfig } from '@playwright/test';

/**
 * E2E smoke tests against the real app.
 * Requires the API on :8080 (the dev server proxies /users and /accounts to it)
 * and the Angular dev server on :4200 (npm start).
 *
 * Run: npm run e2e
 *   - starts `ng serve` on :4200 (reuseExistingServer keeps a running one)
 *   - API must be reachable at localhost:8080 (./gradlew bootRun)
 */
export default defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  retries: 0,
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    reuseExistingServer: true,
    timeout: 60_000,
  },
});
