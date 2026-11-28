FROM node:22-alpine AS build
WORKDIR /app

COPY web-components ./web-components
WORKDIR /app/web-components
RUN npm ci && npm run build

WORKDIR /app
COPY activityual-web ./activityual-web
WORKDIR /app/activityual-web
RUN npm ci && npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production
ENV HOST=0.0.0.0
ENV PORT=3000
COPY --from=build /app/activityual-web/build ./build
COPY --from=build /app/activityual-web/node_modules ./node_modules
COPY --from=build /app/activityual-web/package.json ./package.json
COPY --from=build /app/web-components ./web-components
EXPOSE 3000
CMD ["node", "build"]
