#!/bin/sh
envsubst '${API_GATEWAY_URL}' < /tmp/nginx.conf.template > /etc/nginx/conf.d/default.conf
exec nginx -g 'daemon off;'
