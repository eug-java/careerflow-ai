import { describe, expect, it, beforeEach } from 'vitest';
import { storeAuthTokens } from './authApi';

describe('storeAuthTokens', () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it('stores access and refresh tokens', () => {
    storeAuthTokens({
      accessToken: 'access-token',
      refreshToken: 'refresh-token',
      tokenType: 'Bearer',
      expiresInSeconds: 7200,
    });

    expect(localStorage.getItem('accessToken')).toBe('access-token');
    expect(localStorage.getItem('refreshToken')).toBe('refresh-token');
  });
});
