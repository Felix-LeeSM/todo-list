#!/bin/sh
envsubst '$SSL_CERTIFICATE $SSL_CERTIFICATE_KEY' </etc/nginx/conf.d/nginx.conf.template >/etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'
