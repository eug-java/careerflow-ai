# CareerFlow Kubernetes (starter manifests)

Minimal manifests for deploying the API gateway, auth service, and frontend.
Data stores and remaining microservices are expected to be added using the same
patterns as `compose.yaml`.

## Prerequisites

- Kubernetes cluster (1.28+)
- NGINX Ingress Controller
- Container images built from this repository

## Build images

```bash
docker build -f backend/auth-service/Dockerfile -t careerflow/auth-service:latest .
docker build -f backend/api-gateway-service/Dockerfile -t careerflow/api-gateway-service:latest .
docker build -f frontend/web-app/Dockerfile \
  --build-arg VITE_API_BASE_URL=https://app.example.com \
  --build-arg VITE_WS_BASE_URL=wss://app.example.com \
  --build-arg VITE_GITHUB_OAUTH_CLIENT_ID=your_github_client_id \
  -t careerflow/frontend:latest frontend/web-app
```

Push images to your registry and update image names in the YAML files if needed.

## Deploy

1. Edit `configmap.yaml` (hostnames, service URLs, CORS origins).
2. Copy `secret.example.yaml` to `secret.yaml` and fill in secrets.
3. Apply manifests:

```bash
kubectl apply -f deploy/kubernetes/namespace.yaml
kubectl apply -f deploy/kubernetes/configmap.yaml
kubectl apply -f deploy/kubernetes/secret.yaml
kubectl apply -f deploy/kubernetes/auth-service.yaml
kubectl apply -f deploy/kubernetes/api-gateway-service.yaml
kubectl apply -f deploy/kubernetes/frontend.yaml
kubectl apply -f deploy/kubernetes/ingress.yaml
```

## GitHub OAuth

Set `GITHUB_CLIENT_ID`, `GITHUB_CLIENT_SECRET`, and `GITHUB_REDIRECT_URI` in the
ConfigMap/Secret. The frontend build must include `VITE_GITHUB_OAUTH_CLIENT_ID`
matching the GitHub OAuth app client ID.

## Notes

- `auth-service` expects PostgreSQL at `auth-postgres:5432` — provision a
  Postgres instance or StatefulSet separately.
- Remaining backend services (profile, job, matching, etc.) follow the same
  Deployment + Service pattern with env from `careerflow-config` and
  `careerflow-secrets`.
