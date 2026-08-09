"""Shared auth helper for the ETL scripts.

The API requires a Bearer JWT. entrypoint.sh logs in once and exports ETL_TOKEN;
auth_headers() reuses it (no repeated /auth/login calls, which would exhaust the
20 POST/min/IP rate limit). Fallback: login here when ETL_TOKEN is absent
(e.g. running scripts directly).
"""
import os
import requests

from dotenv import load_dotenv

load_dotenv()

BASE_URL = os.getenv('API_URL', 'http://localhost:8080')
AUTH_USERNAME = os.getenv('ETL_USERNAME', 'devweekerson')
AUTH_PASSWORD = os.getenv('ETL_PASSWORD', 'admin123')


def auth_headers():
    """Returns {'Authorization': 'Bearer <token>'}, reusing ETL_TOKEN if set."""
    token = os.getenv('ETL_TOKEN')
    if not token:
        resp = requests.post(f"{BASE_URL}/auth/login", json={
            'username': AUTH_USERNAME,
            'password': AUTH_PASSWORD,
        })
        resp.raise_for_status()
        token = resp.json()['token']
    return {'Authorization': f'Bearer {token}'}
