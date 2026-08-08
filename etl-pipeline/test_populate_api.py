"""Unit tests for populate_api.py (no network calls)."""
import unittest
from unittest import mock

import populate_api


def _page(content, total_pages=1):
    return {"content": content, "totalPages": total_pages}


def _user(account_number, card_number):
    return {
        "account": {"number": account_number},
        "card": {"number": card_number},
    }


class ExistingNumbersTest(unittest.TestCase):
    @mock.patch("populate_api.requests.get")
    def test_collects_accounts_and_cards(self, mock_get):
        mock_get.return_value.status_code = 200
        mock_get.return_value.json.return_value = _page(
            [_user("0002", "**** **** **** 2201"), _user("0003", "**** **** **** 2202")]
        )
        accounts, cards = populate_api.existing_account_and_card_numbers()
        self.assertEqual(accounts, {"0002", "0003"})
        self.assertEqual(cards, {"**** **** **** 2201", "**** **** **** 2202"})

    @mock.patch("populate_api.requests.get")
    def test_paginates_and_keeps_existing_account_numbers(self, mock_get):
        mock_get.return_value.status_code = 200
        mock_get.return_value.raise_for_status = mock.Mock()
        mock_get.return_value.json.side_effect = [
            _page([_user("0002", "c1")], total_pages=2),
            _page([_user("0003", "c2")], total_pages=2),
        ]
        numbers = populate_api.existing_account_numbers()
        self.assertEqual(numbers, {"0002", "0003"})
        self.assertEqual(mock_get.call_count, 2)


class CreateUserTest(unittest.TestCase):
    def test_uses_provided_account_number(self):
        payload = populate_api.create_user("9900000", "Usuario_Teste_9900000", set())
        self.assertEqual(payload["account"]["number"], "9900000")
        self.assertEqual(payload["name"], "Usuario_Teste_9900000")

    def test_does_not_reuse_card_numbers(self):
        used = {"**** **** **** 1234"}
        # First draw collides with `used`, the second one is free — the generator
        # must loop past the colliding number instead of returning it.
        with mock.patch("populate_api.random.randint", side_effect=[1234, 5678]):
            payload = populate_api.create_user("9900001", "x", used)
        self.assertEqual(payload["card"]["number"], "**** **** **** 5678")


class PopulateTest(unittest.TestCase):
    @mock.patch("populate_api.requests.get")
    @mock.patch("populate_api.requests.post")
    def test_skips_existing_accounts_and_reports_created(self, mock_post, mock_get):
        mock_get.return_value.status_code = 200
        # Seed already has account 9900000; card set is empty.
        mock_get.return_value.json.return_value = _page([_user("9900000", "")])

        mock_post.return_value.status_code = 201
        mock_post.return_value.text = ""

        with mock.patch("builtins.print"):
            populate_api.populate(2)

        # Only 9900001 is posted (9900000 already exists).
        self.assertEqual(mock_post.call_count, 1)
        posted = mock_post.call_args.kwargs["json"]
        self.assertEqual(posted["account"]["number"], "9900001")


if __name__ == "__main__":
    unittest.main()
