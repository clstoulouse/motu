#!/bin/bash

set -e

if [ -z "${CI_PROJECT_DIR}" ]; then
    CURRENT_DIR=$(cd "$(dirname "$(readlink -f "${BASH_SOURCE[0]}")")" && pwd)
    CI_PROJECT_DIR=$(dirname "${CURRENT_DIR}")
else
    CURRENT_DIR="${CI_PROJECT_DIR}/ci"
fi

WORKSPACE_DIR="${CI_PROJECT_DIR}"

cat "${WORKSPACE_DIR}/motu-parent/pom.xml" | grep -B 1 '<packaging>pom</packaging>' | grep '<version>' | sed 's/^[[:space:]]*<version>//g' | sed 's,</version>[[:space:]]*$,,g'