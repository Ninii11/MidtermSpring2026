#!/usr/bin/env sh
set -eu

scripts/compile.sh
java -cp out codes.Main "$@"