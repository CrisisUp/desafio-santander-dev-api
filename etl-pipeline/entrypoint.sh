#!/bin/bash

echo "🌱 Populando o banco de dados..."
python populate_api.py

echo "🔄 Iniciando o processamento ETL..."
python main.py

echo "✅ Todos os processos finalizados!"