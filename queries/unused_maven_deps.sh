#!/usr/bin/env bash

set -euo pipefail

bazel query 'attr("visibility", "public", kind("jvm_import", @mvn//...)) - deps(//...)'
