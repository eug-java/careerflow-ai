import { test, expect } from '@playwright/test';

test.describe('Auth API through gateway @fullstack', () => {
  test('demo user can sign in and receive tokens', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Username').fill('demo');
    await page.getByLabel('Password').fill('demo');
    await page.getByRole('button', { name: 'Sign In' }).click();

    await expect(page).toHaveURL('/');

    const tokens = await page.evaluate(() => ({
      accessToken: localStorage.getItem('accessToken'),
      refreshToken: localStorage.getItem('refreshToken'),
    }));

    expect(tokens.accessToken).toBeTruthy();
    expect(tokens.refreshToken).toBeTruthy();
  });

  test('new user can register and land on dashboard route', async ({ page }) => {
    const username = `e2e_${Date.now()}`;

    await page.goto('/register');
    await page.getByLabel('Username').fill(username);
    await page.getByLabel('Password', { exact: true }).fill('password123');
    await page.getByLabel('Confirm password').fill('password123');
    await page.getByRole('button', { name: 'Create Account' }).click();

    await expect(page).toHaveURL('/');

    const accessToken = await page.evaluate(() => localStorage.getItem('accessToken'));
    expect(accessToken).toBeTruthy();
  });

  test('invalid credentials show an error', async ({ page }) => {
    await page.goto('/login');

    await page.getByLabel('Username').fill('demo');
    await page.getByLabel('Password').fill('wrong-password');
    await page.getByRole('button', { name: 'Sign In' }).click();

    await expect(page.getByText('Invalid username or password.')).toBeVisible();
    await expect(page).toHaveURL('/login');
  });
});
