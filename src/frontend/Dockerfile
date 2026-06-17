FROM node:20-alpine AS build
ARG VITE_BASE_URL=/tg-quiz/
ENV VITE_BASE_URL=$VITE_BASE_URL
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npx vite build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /tmp/nginx.conf.template
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh
EXPOSE 80
ENTRYPOINT ["/entrypoint.sh"]
