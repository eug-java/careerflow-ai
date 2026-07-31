# Dashboard & Frontend Pages

## Overview

After login, users land on the **Career Command Center** dashboard at `/`.

## Pages

| Route | Page | Description |
|-------|------|-------------|
| `/` | Dashboard | KPIs, top matches, activity feed, quick actions |
| `/profiles` | Profiles | Profile list with readiness score |
| `/profiles/:id` | Profile detail | Skills, experience, top matches, skill gaps |
| `/jobs` | Jobs | Job list with match scores and generation actions |
| `/jobs/:id` | Job detail | Match history, skill gap, generate documents |
| `/matches` | Match history | All match calculations with score breakdown |
| `/documents` | Documents | Preview, PDF/DOCX download, filters |
| `/email` | Recruiter inbox | Synced emails by category, reply with attachments |
| `/email/settings` | Email settings | IMAP/SMTP configuration |

## Dashboard widgets

- **KPI cards** — profiles, jobs, matches, documents, avg match %, running workflows
- **Profile readiness** — checklist for profile completeness
- **Next best action** — smart CTA (create profile → add job → match → generate)
- **Top matches** — leaderboard with skills/location/salary breakdown
- **Activity feed** — recent matches, documents, workflows
- **Active pipeline** — running document generation workflows
- **Match distribution** — histogram of match scores
- **Quick actions** — paste JD, quick match, generate documents
- **Documents snapshot** — latest generated files
- **Recruiter email** — inbox summary by category (requires email integration)

## Tech stack

- React 19 + TypeScript + Vite
- TanStack Query (data fetching & cache)
- Axios with JWT refresh interceptor
- Tailwind CSS v4
- Toast notifications

## Developer commands

```bash
cd frontend/web-app
npm ci
npm run dev      # http://localhost:5173
npm run lint
npm run test     # Vitest
npm run build
npm run test:e2e # Playwright (optional)
```
