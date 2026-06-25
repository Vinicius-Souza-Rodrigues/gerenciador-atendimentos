# syntax=docker/dockerfile:1

# ---- Deps stage: instala dependências ----
FROM node:20-alpine AS deps
WORKDIR /app
COPY package.json package-lock.json* ./
# npm install enquanto não há package-lock.json commitado.
# Quando o lock file existir, trocar para: RUN npm ci
RUN npm install

# ---- Build stage: compila o Next.js ----
FROM node:20-alpine AS build
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

# ---- Runtime stage: imagem enxuta com o standalone ----
FROM node:20-alpine AS runtime
WORKDIR /app
ENV NODE_ENV=production

# Usuário não-root (o node:alpine já traz o usuário "node")
USER node

COPY --from=build --chown=node:node /app/public ./public
COPY --from=build --chown=node:node /app/.next/standalone ./
COPY --from=build --chown=node:node /app/.next/static ./.next/static

EXPOSE 3000
CMD ["node", "server.js"]
