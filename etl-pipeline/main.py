import requests
import json
import os
from dotenv import load_dotenv

# Carrega as variáveis do arquivo .env
load_dotenv()

# Busca as configurações das variáveis de ambiente
API_URL = os.getenv('API_URL', 'http://localhost:8080')

PAGE_SIZE = 100

def extract_users():
    """Extrai todos os usuários da API paginada (evita carregar tudo de uma vez)."""
    users = []
    page = 0
    while True:
        response = requests.get(f'{API_URL}/users', params={'page': page, 'size': PAGE_SIZE})
        response.raise_for_status()
        data = response.json()
        users.extend(data.get('content', []))
        if page >= data.get('totalPages', 0) - 1:
            break
        page += 1
    return users

def transform_user(user):
    name = user['name']
    balance = user['account']['balance']
    # Sua lógica de "IA" personalizada
    message = f"Olá {name}, seu saldo de R$ {balance} é excelente!"
    # icon must match a registered brand SVG (see frontend BrandIconService);
    # a missing icon renders as a broken image on the statement detail panel.
    user['news'].append({"description": message, "icon": "https://digitalinnovationone.github.io/santander-dev-week-2023-api/icons/credit.svg"})
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
    users = extract_users()
    print(f"✅ Extraídos {len(users)} usuários")
    for user in users:
        print(f"✅ Processando ID: {user['id']}")
        transformed = transform_user(user)
        load_to_file(transformed)

if __name__ == "__main__":
    run_pipeline()
