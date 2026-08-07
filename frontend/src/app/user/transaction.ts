export type TransactionType = 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'PAYMENT';

export interface Transaction {
  id: number;
  type: TransactionType;
  amount: number;
  accountId: number;
  destinationAccountId?: number;
  createdAt: string;
}

export interface TransactionPage {
  content: Transaction[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface TransactionRequest {
  type: TransactionType;
  amount: number;
  destinationAccountId?: number;
}

/** One row of the aggregate endpoint: total + count per transaction type. */
export interface TransactionTypeStat {
  type: TransactionType;
  total: number;
  count: number;
}
