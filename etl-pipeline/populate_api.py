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
    return existing_account_and_card_numbers()[0]

def existing_account_and_card_numbers():
    """Contas E cartões já cadastrados (seed do Flyway + execuções anteriores).

    O cartão tem UNIQUE no banco, então os novos números aleatórios (apenas 9000
    combinações) colidem facilmente com cartões de runs anteriores se não forem
    pré-carregados. A API retorna um Spring Page ({"content": [...]}), então
    paginamos como main.extract_users.
    """
    account_numbers = set()
    card_numbers = set()
    page = 0
    while True:
        response = requests.get(API_URL, params={'page': page, 'size': 100})
        response.raise_for_status()
        data = response.json()
        account_numbers.update(user['account']['number'] for user in data.get('content', []))
        card_numbers.update(user['card']['number'] for user in data.get('content', []))
        if page >= data.get('totalPages', 0) - 1:
            break
        page += 1
    return account_numbers, card_numbers

def create_user(account_number, name, used_cards):
    """Gera um payload com conta e cartão únicos e tenta criar o usuário."""
    card_number = None
    while card_number is None or card_number in used_cards:
        card_number = f"**** **** **** {random.randint(1000, 9999)}"
    used_cards.add(card_number)

    user_data = {
        "name": name,
        "account": {
            "number": account_number,
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
    existing, used_cards = existing_account_and_card_numbers()

    created = 0
    # The Flyway seed uses accounts '01.097954-4' and '0002'..'0041', so numbers
    # starting at 990000 can never collide with it (V5/V6 stay in the 0002..0041
    # range). Each candidate is validated as unused before the POST — a re-run
    # re-reads `existing`, so it stays idempotent.
    for i in range(quantity):
        account_number = f"9900{i:03d}"
        if account_number in existing:
            continue  # Idempotente: re-execuções não duplicam contas

        response = requests.post(API_URL, json=create_user(account_number, f"Usuario_Teste_{account_number}", used_cards))
        if response.status_code == 201:
            created += 1
            existing.add(account_number)
            print(f"✅ Usuário {account_number} criado com sucesso!")
        elif response.status_code == 422:
            print(f"⚠️ Usuário {account_number} já existe (ou payload inválido): {response.text}")
        else:
            print(f"❌ Erro no usuário {account_number}: {response.status_code} {response.text}")

    print(f"✅ {created} usuário(s) criado(s)")

if __name__ == "__main__":
    # Vamos criar 20 usuários de uma vez
    populate(20)
