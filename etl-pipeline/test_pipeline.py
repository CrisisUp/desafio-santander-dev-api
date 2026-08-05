"""Unit tests for the ETL pipeline (no network calls)."""
import os
from unittest import mock

import main


def test_transform_user_appends_personalized_message():
    user = {
        "id": 1,
        "name": "Devweekerson",
        "account": {"balance": 624.12},
        "news": [],
    }
    transformed = main.transform_user(user)
    assert transformed["news"] == [
        {"description": "Olá Devweekerson, seu saldo de R$ 624.12 é excelente!"}
    ]


def test_load_to_file_writes_json(tmp_path, monkeypatch):
    monkeypatch.chdir(tmp_path)
    user = {"id": 42, "name": "X"}
    assert main.load_to_file(user) is True
    written = os.path.join("data", "output", "user_42_updated.json")
    assert os.path.exists(written)
    import json
    assert json.load(open(written)) == user


@mock.patch("main.requests.get")
def test_extract_users_returns_json(mock_get):
    mock_get.return_value.status_code = 200
    mock_get.return_value.json.return_value = {
        "content": [{"id": 1}, {"id": 2}],
        "totalPages": 1,
    }
    mock_get.return_value.raise_for_status = mock.Mock()
    users = main.extract_users()
    assert users == [{"id": 1}, {"id": 2}]
    mock_get.assert_called_once_with("http://localhost:8080/users",
                                     params={"page": 0, "size": main.PAGE_SIZE})


@mock.patch("main.requests.get")
def test_extract_users_paginates(mock_get):
    # Page 0 has 1 item and totalPages=2 -> the loop must fetch page 1 too.
    mock_get.return_value.status_code = 200
    mock_get.return_value.raise_for_status = mock.Mock()
    mock_get.return_value.json.side_effect = [
        {"content": [{"id": 1}], "totalPages": 2},
        {"content": [{"id": 2}], "totalPages": 2},
    ]
    users = main.extract_users()
    assert users == [{"id": 1}, {"id": 2}]
    assert mock_get.call_count == 2


@mock.patch("main.requests.get")
def test_run_pipeline_handles_all_users(mock_get):
    mock_get.return_value.status_code = 200
    mock_get.return_value.json.return_value = {
        "content": [
            {"id": 1, "name": "A", "account": {"balance": 1.0}, "news": []},
            {"id": 2, "name": "B", "account": {"balance": 2.0}, "news": []},
        ],
        "totalPages": 1,
    }
    mock_get.return_value.raise_for_status = mock.Mock()
    with mock.patch.object(main, "load_to_file") as mock_load:
        main.run_pipeline()
        assert mock_load.call_count == 2
