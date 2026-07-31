export function decodeJwtExpiry(token: string): number | null {
    try {
        const payload = token.split(".")[1];
        const decoded = JSON.parse(atob(payload)) as { exp?: number };
        return decoded.exp ?? null;
    } catch {
        return null;
    }
}

export function isTokenExpired(token: string | null): boolean {
    if (!token) {
        return true;
    }
    const exp = decodeJwtExpiry(token);
    if (!exp) {
        return false;
    }
    return Date.now() >= exp * 1000;
}
