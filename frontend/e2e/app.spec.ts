import { test, expect } from '@playwright/test';

/**
 * End-to-end smoke tests: navigation, user list, dashboard.
 * These run against the real dev stack (Angular + proxied API + H2 seed).
 */

test('landing redirects to the user list and shows the seed data', async ({ page }) => {
  await page.goto('/');
  await expect(page).toHaveURL(/\/users$/);
  // The user-list lazy chunk compiles on first visit in dev; allow extra time.
  // The name cell also holds a lock icon, so match as substring (not exact).
  await expect(page.getByText('Devweekerson').first()).toBeVisible({ timeout: 15_000 });
  await expect(page.getByText('41 usuário(s)').first()).toBeVisible();
});

test('user list shows account columns', async ({ page }) => {
  await page.goto('/users');
  await expect(page.getByRole('columnheader', { name: 'Conta' })).toBeVisible();
  await expect(page.getByRole('columnheader', { name: 'Saldo' })).toBeVisible();
  await expect(page.getByText('01.097954-4', { exact: true })).toBeVisible();
});

test('dashboard renders KPIs from the aggregated API', async ({ page }) => {
  await page.goto('/dashboard');
  await expect(page.getByText('Saldo total')).toBeVisible();
  await expect(page.getByText('Saldo por faixa')).toBeVisible();
  await expect(page.getByText('Valor total por tipo')).toBeVisible();
});

test('search filters the user list by name', async ({ page }) => {
  await page.goto('/users');
  const search = page.getByPlaceholder('Ex.: Devweekerson');
  await search.fill('Bruno');
  await expect(page.getByText('Bruno Lima', { exact: true })).toBeVisible();
  await expect(page.getByText('Devweekerson', { exact: true })).toBeHidden();
});
