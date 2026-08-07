import { User } from '../user/user';

export interface DashboardBucket {
  /** Human label, e.g. "R$ 0–999,99". */
  label: string;
  min: number;
  max: number;
  count: number;
  /** count / maxCount * 100, so the tallest bucket is 100. */
  pct: number;
}

export interface DashboardData {
  totalAccounts: number;
  totalBalance: number;
  avgBalance: number;
  maxBalance: number;
  minBalance: number;
  buckets: DashboardBucket[];
}

/** Fixed balance ranges, sequential-safe (ordered, low → high). */
const BUCKETS = [
  { label: 'R$ 0–999,99', min: 0, max: 999.99 },
  { label: 'R$ 1.000–1.999,99', min: 1000, max: 1999.99 },
  { label: 'R$ 2.000–2.999,99', min: 2000, max: 2999.99 },
  { label: 'R$ 3.000–3.999,99', min: 3000, max: 3999.99 },
  { label: 'R$ 4.000+', min: 4000, max: Infinity },
];

/**
 * Pure aggregation of users into dashboard KPIs + a balance-distribution
 * histogram. No Angular dependencies — this is the testable core.
 */
export function computeDashboard(users: User[]): DashboardData {
  const balances = users.map((u) => u.account?.balance ?? 0);
  const totalAccounts = balances.length;
  const totalBalance = round2(balances.reduce((a, b) => a + b, 0));
  const avgBalance = totalAccounts > 0 ? round2(totalBalance / totalAccounts) : 0;
  const maxBalance = totalAccounts > 0 ? Math.max(...balances) : 0;
  const minBalance = totalAccounts > 0 ? Math.min(...balances) : 0;

  // Bucket 0 is the catch-all for low/negative balances (min is a floor, not a
  // gate); higher buckets require bal >= b.min. Keeps negative balances from
  // falling through every bucket.
  const counts = BUCKETS.map((b) =>
    balances.filter((bal) => (b.min === 0 ? bal <= b.max : bal >= b.min && bal <= b.max)).length
  );
  const maxCount = Math.max(...counts, 1);
  const buckets: DashboardBucket[] = BUCKETS.map((b, i) => ({
    ...b,
    count: counts[i],
    pct: Math.round((counts[i] / maxCount) * 100),
  }));

  return { totalAccounts, totalBalance, avgBalance, maxBalance, minBalance, buckets };
}

function round2(n: number): number {
  return Math.round(n * 100) / 100;
}
