#!/bin/bash

# Login once (via Python — the slim image has requests but no curl) and share
# the token across both scripts via ETL_TOKEN, so the rate limit
# (20 POST/min/IP) is not exhausted by repeated /auth/login calls.
echo "🔐 Autenticando..."
export ETL_TOKEN=$(python3 -c "
import os, requests
resp = requests.post(
    f\"{os.getenv('API_URL', 'http://santander-api:8080')}/auth/login\",
    json={'username': os.getenv('ETL_USERNAME', 'devweekerson'),
          'password': os.getenv('ETL_PASSWORD', 'admin123')},
)
resp.raise_for_status()
print(resp.json()['token'])
" 2>&1 | tail -1)

if [ -z "$ETL_TOKEN" ]; then
  echo "❌ Falha ao autenticar no /auth/login"
  exit 1
fi

echo "🌱 Populando o banco de dados..."
python populate_api.py

echo "🔄 Iniciando o processamento ETL..."
python main.py

echo "✅ Todos os processos finalizados!"
