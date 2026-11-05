# CareerFlow AI — Web App

React 19 + TypeScript + Vite + Tailwind. Talks to the API through the gateway at `VITE_API_BASE_URL` (default `http://localhost:8080`).

## Scripts

| Command | Purpose |
|---------|---------|
| `npm run dev` | Local dev server (port 5173) |
| `npm run build` | Production build |
| `npm run lint` | ESLint |
| `npm test` | Vitest unit tests |
| `npm run test:e2e` | Playwright (UI specs) |
| `npm run test:e2e:fullstack` | Playwright against real auth/gateway |

## Docker

The root `compose.yaml` builds this app with `VITE_API_BASE_URL=http://localhost:8080` and serves it on http://localhost:5173.

Project-wide setup, demo login, and architecture: [../../README.md](../../README.md).
