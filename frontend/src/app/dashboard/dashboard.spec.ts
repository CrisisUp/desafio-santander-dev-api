import { computeDashboard, computeTransactionStats } from './dashboard-data';
import { User } from '../user/user';

/** Minimal user fixture: only the fields computeDashboard reads. */
function user(balance: number, name = 'u'): User {
  return {
    name,
    account: { number: '1', agency: '1', balance, limit: 0 },
    card: { number: '1', limit: 0 },
    features: [],
    news: [],
  };
}

describe('computeDashboard', () => {
  it('computes KPI math from a list of users', () => {
    const d = computeDashboard([user(100), user(200), user(300), user(400)]);
    expect(d.totalAccounts).toBe(4);
    expect(d.totalBalance).toBe(1000);
    expect(d.avgBalance).toBe(250);
    expect(d.maxBalance).toBe(400);
    expect(d.minBalance).toBe(100);
  });

  it('places each balance in its own ordered bucket', () => {
    const d = computeDashboard([user(500), user(1500), user(2500), user(3500), user(4500)]);
    expect(d.buckets.map((b) => b.count)).toEqual([1, 1, 1, 1, 1]);
    // Labels stay low → high.
    expect(d.buckets[0].label).toContain('0');
    expect(d.buckets[4].label).toContain('4.000');
  });

  it('handles an empty list without throwing', () => {
    const d = computeDashboard([]);
    expect(d.totalAccounts).toBe(0);
    expect(d.totalBalance).toBe(0);
    expect(d.avgBalance).toBe(0);
    expect(d.buckets.every((b) => b.count === 0)).toBe(true);
  });

  it('clamps negative and zero balances into the first bucket', () => {
    const d = computeDashboard([user(-50), user(0)]);
    expect(d.buckets[0].count).toBe(2);
    expect(d.minBalance).toBe(-50);
  });

  it('pct normalizes so the tallest bucket is 100', () => {
    const d = computeDashboard([user(500), user(1500), user(2500), user(3500)]);
    // Buckets 0-3 have count 1 (tied max) → pct 100; bucket 4 (4000+) is empty → pct 0.
    expect(d.buckets.filter((b) => b.count > 0).every((b) => b.pct === 100)).toBe(true);
    expect(d.buckets[4].pct).toBe(0);
  });

  it('handles a missing account defensively as balance 0', () => {
    const partial = { name: 'x' } as unknown as User;
    const d = computeDashboard([partial]);
    expect(d.totalBalance).toBe(0);
    expect(d.buckets[0].count).toBe(1);
  });
});

describe('computeTransactionStats', () => {
  it('zero-fills missing types so all 4 slots are present in fixed order', () => {
    const s = computeTransactionStats([
      { type: 'DEPOSIT', total: 100, count: 1 },
      { type: 'WITHDRAWAL', total: 25, count: 1 },
    ]);
    expect(s.buckets.map((b) => b.type)).toEqual(['DEPOSIT', 'WITHDRAWAL', 'PAYMENT', 'TRANSFER']);
    expect(s.buckets.find((b) => b.type === 'TRANSFER')).toMatchObject({ total: 0, count: 0, pct: 0 });
  });

  it('normalizes pct so the largest total is 100', () => {
    const s = computeTransactionStats([
      { type: 'DEPOSIT', total: 200, count: 2 },
      { type: 'PAYMENT', total: 50, count: 1 },
    ]);
    expect(s.buckets.find((b) => b.type === 'DEPOSIT')?.pct).toBe(100);
    expect(s.buckets.find((b) => b.type === 'PAYMENT')?.pct).toBe(25);
  });

  it('handles an empty payload and pure-zero totals without throwing', () => {
    expect(computeTransactionStats([]).buckets.every((b) => b.total === 0 && b.pct === 0)).toBe(true);
    expect(
      computeTransactionStats([
        { type: 'DEPOSIT', total: 0, count: 0 },
        { type: 'PAYMENT', total: 0, count: 0 },
      ]).buckets.every((b) => b.pct === 0)
    ).toBe(true);
  });
});
