#!/usr/bin/env sh
# Maven local al proyecto; no requiere instalación global ni privilegios de administrador.
set -eu
cd "$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
version=3.9.9
distribution="apache-maven-$version"
if [ ! -x ".tools/$distribution/bin/mvn" ]; then
    mkdir -p .tools
    archive=".tools/$distribution-bin.tar.gz"
    url="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$version/$distribution-bin.tar.gz"
    curl --fail --location "$url" -o "$archive"
    curl --fail --location "$url.sha512" -o "$archive.sha512"
    expected=$(tr -d '[:space:]' < "$archive.sha512")
    actual=$(sha512sum "$archive" | cut -d ' ' -f 1)
    [ "$actual" = "$expected" ] || { echo 'La verificación de Maven falló.' >&2; exit 1; }
    tar -xzf "$archive" -C .tools
fi
exec ".tools/$distribution/bin/mvn" -Dmaven.repo.local=.tools/repository "$@"
