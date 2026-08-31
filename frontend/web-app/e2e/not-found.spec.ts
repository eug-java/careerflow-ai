import { test, expect } from '@playwright/test';

test('not found page renders', async ({ page }) => {
  await page.goto('/this-route-does-not-exist');
  await expect(page.getByRole('heading', { name: 'Page not found' })).toBeVisible();
  await expect(page.getByRole('link', { name: 'Go to dashboard' })).toBeVisible();
});
