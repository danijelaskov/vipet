#!/bin/sh
HERE="$(dirname "$(readlink -f "$0")")"
exec "$HERE/usr/lib/runtime/bin/{{APP_NAME}}" "$@"