export type RegisterPayload = {
  username: string;
  email: string;
  password: string;
};

export type LoginPayload = {
  username: string;
  password: string;
};

export type LoginResponse = {
  token: string;
  tokenType: string;
};

export type MeResponse = {
  email: string;
  score: number;
  turns: number;
};

export type LeaderboardEntry = {
  username: string;
  score: number;
};

export type GuessResponse = {
  serverResult: number;
  isCorrect: boolean;
};

export type BuyTurnsResponse = {
  url: string;
  message: string;
  remainingTurns: number;
};

export type ApiErrorResponse = {
  message?: string;
};

export type Status = {
  tone: "success" | "error";
  text: string;
} | null;


export type CreatePaymentResponse = {
    paymentUrl: string;
};