#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TOKEN_FILE="${GITHUB_TOKEN_FILE:-$ROOT_DIR/.github-token}"
BRANCH="${1:-main}"

if [[ ! -f "$TOKEN_FILE" ]]; then
  echo "Token non trovato: $TOKEN_FILE" >&2
  echo "Copia .github-token.example in .github-token e inserisci il PAT." >&2
  exit 1
fi

TOKEN="$(sed -e 's/#.*//' "$TOKEN_FILE" | tr -d '[:space:]')"
if [[ -z "$TOKEN" ]]; then
  echo "Il file $TOKEN_FILE non contiene un token." >&2
  exit 1
fi

export GITHUB_TOKEN="$TOKEN"
git -C "$ROOT_DIR" \
  -c credential.helper='!f() { if [ "$1" = get ]; then echo username=x-access-token; echo "password=$GITHUB_TOKEN"; fi; }; f' \
  push origin "HEAD:$BRANCH"
