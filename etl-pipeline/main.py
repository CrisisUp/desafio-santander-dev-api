import requests
import json
import os
from dotenv import load_dotenv
import time

# Carrega as variáveis do arquivo .env
load_dotenv()

# Busca as configurações das variáveis de ambiente
API_URL = os.getenv('API_URL')
ID_START = int(os.getenv('USER_ID_START', 1))
ID_END = int(os.getenv('USER_ID_END', 21))

def extract_user(user_id):
    max_retries = 5
    for i in range(max_retries):
        try:
            response = requests.get(f'{API_URL}/users/{user_id}')
            return response.json() if response.status_code == 200 else None
        except requests.exceptions.ConnectionError:
            print(f"⚠️ API ainda não disponível (Tentativa {i+1}/{max_retries}). Aguardando...")
            time.sleep(3)
    return None

def transform_user(user):
    name = user['name']
    balance = user['account']['balance']
    # Sua lógica de "IA" personalizada
    message = f"Olá {name}, seu saldo de R$ {balance} é excelente!"
    user['news'].append({"description": message})
    return user

def load_to_file(user):
    # Define o caminho da pasta de saída
    output_path = "data/output"
    os.makedirs(output_path, exist_ok=True)
    filename = f"{output_path}/user_{user['id']}_updated.json"
    with open(filename, 'w') as f:
        json.dump(user, f, indent=2)
    return True

def run_pipeline():
    print(f"🚀 Iniciando Pipeline Seguro em: {API_URL}")
    for user_id in range(ID_START, ID_END + 1):
        user_data = extract_user(user_id)
        if user_data:
            print(f"✅ Processando ID: {user_id}")
            transformed = transform_user(user_data)
            load_to_file(transformed)

if __name__ == "__main__":
    run_pipeline()