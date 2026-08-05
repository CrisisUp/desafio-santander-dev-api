export interface Account {
  id?: number;
  number: string;
  agency: string;
  balance: number;
  limit: number;
}

export interface Card {
  id?: number;
  number: string;
  limit: number;
}

export interface Feature {
  id?: number;
  icon?: string;
  description: string;
}

export interface News {
  id?: number;
  icon?: string;
  description: string;
}

export interface User {
  id?: number;
  name: string;
  account: Account;
  card: Card;
  features: Feature[];
  news: News[];
}

export interface UserPage {
  content: User[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}
