###############
# Build Stage #
###############
FROM node:22.13.0-alpine as builder
WORKDIR /app

# yarn 사용 (npm 사용 시 package-lock.json 활용)
COPY package.json yarn.lock ./
RUN yarn install --frozen-lockfile

# 소스 전체 복사 후 빌드 (Vite 기본 빌드 산출물은 dist 폴더에 생성됨)
COPY . .
RUN yarn build

###########################
# Production Stage: Nginx #
###########################
FROM nginx:alpine

# 빌드 산출물을 nginx 정적 파일 위치로 복사
COPY --from=builder /app/dist /usr/share/nginx/html

# 템플릿 파일 복사
COPY nginx.conf.template /etc/nginx/conf.d/nginx.conf.template

# entrypoint 스크립트 복사 및 실행 권한 부여
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 80 443
CMD ["/entrypoint.sh"]
