import requests
import random
import os
from dotenv import load_dotenv

load_dotenv()

# Em vez de API_URL = 'http://localhost:8080/users'
# Use a variável de ambiente definida no docker-compose
BASE_URL = os.getenv('API_URL', 'http://localhost:8080')
API_URL = f"{BASE_URL}/users"

def create_users(quantity):
    for i in range(2, quantity + 2):  # Começa no 2 pois o 1 já existe
        user_data = {
            "name": f"Usuario_Teste_{i}",
            "account": {
                "number": f"000{i}",
                "agency": "0001",
                "balance": round(random.uniform(500, 5000), 2),
                "limit": 1000.00
            },
            "card": {
                "number": f"**** **** **** {random.randint(1000, 9999)}",
                "limit": 5000.00
            },
            "features": [],
            "news": []
        }
        
        response = requests.post(API_URL, json=user_data)
        if response.status_code == 201:
            print(f"✅ Usuário {i} criado com sucesso!")
        else:
            print(f"❌ Erro no usuário {i}: {response.status_code}")

if __name__ == "__main__":
    # Vamos criar 20 usuários de uma vez
    create_users(20)