#!/usr/bin/env sh
set -eu

scripts/compile.sh
java -cp out codes.Main --self-test
java -cp out tests.UnoTests