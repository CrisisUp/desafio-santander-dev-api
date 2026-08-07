import requests
import random
import os
from dotenv import load_dotenv

load_dotenv()

# Em vez de API_URL = 'http://localhost:8080/users'
# Use a variável de ambiente definida no docker-compose
BASE_URL = os.getenv('API_URL', 'http://localhost:8080')
API_URL = f"{BASE_URL}/users"

def existing_account_numbers():
    """Números de conta já cadastrados (inclui o seed do Flyway, ex.: 01.097954-4).

    The API returns a Spring Page ({"content": [...]}), not a bare list, so we
    paginate like main.extract_users instead of iterating the JSON object.
    """
    numbers = set()
    page = 0
    while True:
        response = requests.get(API_URL, params={'page': page, 'size': 100})
        response.raise_for_status()
        data = response.json()
        numbers.update(user['account']['number'] for user in data.get('content', []))
        if page >= data.get('totalPages', 0) - 1:
            break
        page += 1
    return numbers

def create_user(i, used_cards):
    """Gera um payload com conta e cartão únicos e tenta criar o usuário."""
    card_number = None
    while card_number is None or card_number in used_cards:
        card_number = f"**** **** **** {random.randint(1000, 9999)}"
    used_cards.add(card_number)

    user_data = {
        "name": f"Usuario_Teste_{i}",
        "account": {
            "number": f"000{i}",
            "agency": "0001",
            "balance": round(random.uniform(500, 5000), 2),
            "limit": 1000.00
        },
        "card": {
            "number": card_number,
            "limit": 5000.00
        },
        "features": [],
        "news": []
    }
    return user_data

def populate(quantity):
    existing = existing_account_numbers()
    used_cards = set()

    created = 0
    for i in range(2, quantity + 2):  # Começa no 2 pois o 1 é o seed do Flyway
        if f"000{i}" in existing:
            continue  # Idempotente: re-execuções não duplicam contas

        response = requests.post(API_URL, json=create_user(i, used_cards))
        if response.status_code == 201:
            created += 1
            print(f"✅ Usuário {i} criado com sucesso!")
        elif response.status_code == 422:
            print(f"⚠️ Usuário {i} já existe (ou payload inválido): {response.text}")
        else:
            print(f"❌ Erro no usuário {i}: {response.status_code} {response.text}")

    print(f"✅ {created} usuário(s) criado(s)")

if __name__ == "__main__":
    # Vamos criar 20 usuários de uma vez
    populate(20)
