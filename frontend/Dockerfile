# Multi-stage: Node build -> nginx serve (com proxy para a API).
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
# O dev server do Angular roda no Node 22 (LTS); o build também.
RUN npm run build -- --configuration=production

FROM nginx:1.27-alpine AS serve
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
# Configura o nginx como proxy reverso: /users -> santander-api:8080.
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
