import { describe, expect, it } from 'vitest';
import { isTokenExpired } from './tokenUtils';

describe('isTokenExpired', () => {
  it('returns true for null token', () => {
    expect(isTokenExpired(null)).toBe(true);
  });

  it('returns false for token without exp claim', () => {
    const payload = btoa(JSON.stringify({ sub: 'demo' }));
    expect(isTokenExpired(`header.${payload}.sig`)).toBe(false);
  });

  it('returns true for expired token', () => {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) - 60 }));
    expect(isTokenExpired(`header.${payload}.sig`)).toBe(true);
  });

  it('returns false for valid token', () => {
    const payload = btoa(JSON.stringify({ exp: Math.floor(Date.now() / 1000) + 3600 }));
    expect(isTokenExpired(`header.${payload}.sig`)).toBe(false);
  });
});
