import { test, expect } from '@playwright/test';

test('register page renders', async ({ page }) => {
  await page.goto('/register');
  await expect(page.getByRole('heading', { name: 'Create account' })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create Account' })).toBeVisible();
});

test('login page links to register', async ({ page }) => {
  await page.goto('/login');
  await expect(page.getByRole('link', { name: 'Create an account' })).toBeVisible();
});
