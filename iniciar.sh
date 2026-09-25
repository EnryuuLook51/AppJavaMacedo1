#!/usr/bin/env bash
set -euo pipefail
cd -- "$(dirname -- "${BASH_SOURCE[0]}")"
if [[ "${1:-}" != "--sin-compilar" ]]; then
    ./mvnw -B verify
fi
if [[ ! -f target/acceso-1.0.0.jar ]]; then
    echo 'Primero compile con ./mvnw verify.' >&2
    exit 1
fi
first_run=1
database_url=${DB_URL:-jdbc:h2:file:./data/acceso}
if [[ "$database_url" == jdbc:h2:file:* && "$database_url" != *\;* ]]; then
    h2_jar=$(find .tools/repository/com/h2database/h2 -maxdepth 2 -type f -name 'h2-*.jar' -print -quit 2>/dev/null || true)
    if [[ -n "$h2_jar" ]]; then
        users=$(java -cp "$h2_jar" org.h2.tools.Shell -url "$database_url" \
            -user sa -password "${DB_PASSWORD:-}" -sql 'select count(*) from usuario' 2>/dev/null \
            | awk '/^[0-9]+$/ { print; exit }' || true)
        [[ "$users" =~ ^[1-9][0-9]*$ ]] && first_run=0
    fi
fi
if (( first_run )); then
    if [[ -z "${ADMIN_PASSWORD:-}" ]]; then
        read -r -s -p 'Contraseña inicial de admin (8–72 caracteres, mayúscula, minúscula, número y símbolo): ' ADMIN_PASSWORD
        echo
    fi
    password_bytes=$(LC_ALL=C printf '%s' "$ADMIN_PASSWORD" | wc -c | tr -d '[:space:]')
    if (( ${#ADMIN_PASSWORD} < 8 || ${#ADMIN_PASSWORD} > 72 || password_bytes > 72 )) \
        || [[ ! "$ADMIN_PASSWORD" =~ [A-Z] || ! "$ADMIN_PASSWORD" =~ [a-z] \
             || ! "$ADMIN_PASSWORD" =~ [0-9] || ! "$ADMIN_PASSWORD" =~ [^[:alnum:][:space:]] ]]; then
        echo 'La contraseña no cumple la política: 8–72 caracteres (72 bytes como máximo), con mayúscula, minúscula, número y símbolo sin espacios.' >&2
        echo 'Vuelva a ejecutar sin ADMIN_PASSWORD en el entorno para introducirla de forma segura.' >&2
        exit 1
    fi
    export ADMIN_PASSWORD
fi
echo "Servidor en http://localhost:${PORT:-8080}. Use Ctrl+C para detener."
exec java -jar target/acceso-1.0.0.jar
