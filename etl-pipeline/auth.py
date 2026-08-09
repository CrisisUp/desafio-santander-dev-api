"""Shared auth helper for the ETL scripts.

The API now requires a Bearer JWT. Login once with the seeded admin
(devweekerson/admin123 by default, overridable via env vars) and return an
Authorization header reused across requests.
"""
import os
import requests

from dotenv import load_dotenv

load_dotenv()

BASE_URL = os.getenv('API_URL', 'http://localhost:8080')
AUTH_USERNAME = os.getenv('ETL_USERNAME', 'devweekerson')
AUTH_PASSWORD = os.getenv('ETL_PASSWORD', 'admin123')


def auth_headers():
    """Returns {'Authorization': 'Bearer <token>'} by logging in."""
    resp = requests.post(f"{BASE_URL}/auth/login", json={
        'username': AUTH_USERNAME,
        'password': AUTH_PASSWORD,
    })
    resp.raise_for_status()
    token = resp.json()['token']
    return {'Authorization': f'Bearer {token}'}
