#!/usr/bin/env bash
# Pemakaian: ./rename-username.sh username_baru
set -e
OLD=ifs25018; NEW="$1"; [ -z "$NEW" ] && { echo "isi username baru"; exit 1; }
grep -rlI "$OLD" . --exclude-dir=.git --exclude=rename-username.sh | xargs sed -i "s/$OLD/$NEW/g"
mv "src/main/java/org/delcom/${OLD}pbo2026springbootcashflow" "src/main/java/org/delcom/${NEW}pbo2026springbootcashflow"
mv "src/test/java/org/delcom/${OLD}pbo2026springbootcashflow" "src/test/java/org/delcom/${NEW}pbo2026springbootcashflow"
echo "Selesai. Ganti juga nama folder proyek menjadi ${NEW}-pbo2026-springboot-cashflow"
