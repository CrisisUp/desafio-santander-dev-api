import { test, expect, Page } from '@playwright/test';

/**
 * End-to-end smoke tests: auth, navigation, user list, dashboard.
 * These run against the real dev stack (Angular + proxied API + H2 seed).
 */

/** Logs in via the real login form (devweekerson/admin123 seeded admin). */
async function login(page: Page) {
  await page.goto('/login');
  await page.getByLabel('Usuário').fill('devweekerson');
  await page.getByLabel('Senha').fill('admin123');
  await page.getByRole('button', { name: 'Entrar' }).click();
  await page.waitForURL(/\/users$/);
}

test('redirects to login when unauthenticated', async ({ page }) => {
  await page.goto('/users');
  await expect(page).toHaveURL(/\/login$/);
});

test('logs in and shows the user list with seed data', async ({ page }) => {
  await login(page);
  // The user-list lazy chunk compiles on first visit in dev; allow extra time.
  await expect(page.getByText('Devweekerson').first()).toBeVisible({ timeout: 15_000 });
  await expect(page.getByText('41 usuário(s)').first()).toBeVisible();
});

test('user list shows account columns', async ({ page }) => {
  await login(page);
  await expect(page.getByRole('columnheader', { name: 'Conta' })).toBeVisible();
  await expect(page.getByRole('columnheader', { name: 'Saldo' })).toBeVisible();
  await expect(page.getByText('01.097954-4', { exact: true })).toBeVisible();
});

test('dashboard renders KPIs from the aggregated API', async ({ page }) => {
  await login(page);
  await page.goto('/dashboard');
  await expect(page.getByText('Saldo total')).toBeVisible();
  await expect(page.getByText('Saldo por faixa')).toBeVisible();
  await expect(page.getByText('Valor total por tipo')).toBeVisible();
});

test('search filters the user list by name', async ({ page }) => {
  await login(page);
  const search = page.getByPlaceholder('Ex.: Devweekerson');
  await search.fill('Bruno');
  await expect(page.getByText('Bruno Lima', { exact: true })).toBeVisible();
  await expect(page.getByText('Devweekerson', { exact: true })).toBeHidden();
});

test('logout returns to login', async ({ page }) => {
  await login(page);
  await page.getByRole('button', { name: 'Sair' }).click();
  await expect(page).toHaveURL(/\/login$/);
});

test('expired access token is transparently refreshed', async ({ page }) => {
  await login(page);
  // Corrupt the stored access token (simulate expiry) but keep the refresh token.
  await page.evaluate(() => {
    const raw = localStorage.getItem('sdw_jwt');
    if (raw) {
      const data = JSON.parse(raw);
      data.token = 'expired.invalid.token';
      localStorage.setItem('sdw_jwt', JSON.stringify(data));
    }
  });
  // Navigate to the dashboard: the 401 triggers a refresh and retries.
  await page.goto('/dashboard');
  await expect(page.getByText('Saldo total')).toBeVisible({ timeout: 15_000 });
});
