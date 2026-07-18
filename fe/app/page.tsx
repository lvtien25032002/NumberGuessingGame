"use client";

import api from "@/lib/api";
import { getErrorMessage } from "@/lib/commomMessage";
import {
  BuyTurnsResponse,
  CreatePaymentResponse,
  GuessResponse,
  LeaderboardEntry,
  LoginPayload,
  LoginResponse,
  MeResponse,
  RegisterPayload,
  Status,
} from "@/types";
import { FormEvent, memo, useCallback, useEffect, useState } from "react";

const defaultRegisterForm: RegisterPayload = {
  username: "",
  email: "",
  password: "",
};

const defaultLoginForm: LoginPayload = {
  username: "",
  password: "",
};

const isSameMe = (left: MeResponse | null, right: MeResponse) => {
  if (!left) {
    return false;
  }

  return (
    left.email === right.email &&
    left.score === right.score &&
    left.turns === right.turns
  );
};

const isSameLeaderboard = (left: LeaderboardEntry[], right: LeaderboardEntry[]) => {
  if (left.length !== right.length) {
    return false;
  }

  return left.every(
    (entry, index) =>
      entry.username === right[index]?.username && entry.score === right[index]?.score,
  );
};

type RegisterSectionProps = {
  setSuccess: (text: string) => void;
  setError: (text: string) => void;
};

const RegisterSection = memo(function RegisterSection({
  setSuccess,
  setError,
}: RegisterSectionProps) {
  const [registerForm, setRegisterForm] = useState<RegisterPayload>(defaultRegisterForm);
  const [registerLoading, setRegisterLoading] = useState(false);

  const handleRegister = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setRegisterLoading(true);
    try {
      await api.post("/auth/register", registerForm);
      setSuccess("Đăng ký thành công. Bạn có thể đăng nhập ngay.");
      setRegisterForm(defaultRegisterForm);
    } catch (error) {
      setError(getErrorMessage(error, "Đăng ký thất bại."));
    } finally {
      setRegisterLoading(false);
    }
  };

  return (
    <section className="rounded border p-4">
      <h2 className="mb-3 text-lg font-semibold">1) Đăng ký</h2>
      <form className="grid gap-2 md:grid-cols-4" onSubmit={handleRegister}>
        <input
          className="rounded border px-3 py-2"
          placeholder="Username"
          value={registerForm.username}
          onChange={(event) =>
            setRegisterForm((prev) => ({ ...prev, username: event.target.value }))
          }
          required
        />
        <input
          className="rounded border px-3 py-2"
          placeholder="Email"
          type="email"
          value={registerForm.email}
          onChange={(event) =>
            setRegisterForm((prev) => ({ ...prev, email: event.target.value }))
          }
          required
        />
        <input
          className="rounded border px-3 py-2"
          placeholder="Password"
          type="password"
          value={registerForm.password}
          onChange={(event) =>
            setRegisterForm((prev) => ({ ...prev, password: event.target.value }))
          }
          minLength={6}
          required
        />
        <button
          className="rounded border px-3 py-2 hover:bg-green-600 hover:shadow-lg transition-all duration-200"
          disabled={registerLoading}
          type="submit"
        >
          Đăng ký
        </button>
      </form>
    </section>
  );
});

type AuthSectionProps = {
  token: string | null;
  onLoginSuccess: (token: string) => Promise<void>;
  onLogout: () => void;
  setError: (text: string) => void;
};

const AuthSection = memo(function AuthSection({
  token,
  onLoginSuccess,
  onLogout,
  setError,
}: AuthSectionProps) {
  const [loginForm, setLoginForm] = useState<LoginPayload>(defaultLoginForm);
  const [authLoading, setAuthLoading] = useState(false);

  const [mounted, setMounted] = useState(false);

  useEffect(() => {
    setMounted(true);
  }, []);

  const hasToken = mounted ? !!token : false;

  const handleLogin = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    setAuthLoading(true);
    try {
      const response = await api.post<LoginResponse>("/auth/login", loginForm);
      localStorage.setItem("token", response.data.token);
      await onLoginSuccess(response.data.token);
      setLoginForm(defaultLoginForm);
    } catch (error) {
      setError(getErrorMessage(error, "Đăng nhập thất bại."));
    } finally {
      setAuthLoading(false);
    }
  };

  return (
    <section className="rounded border p-4">
      <h2 className="mb-3 text-lg font-semibold">2) Đăng nhập / Đăng xuất</h2>
      <form className="grid gap-2 md:grid-cols-3" onSubmit={handleLogin}>
        <input
          className="rounded border px-3 py-2"
          placeholder="Username"
          value={loginForm.username}
          onChange={(event) =>
            setLoginForm((prev) => ({ ...prev, username: event.target.value }))
          }
          required
        />
        <input
          className="rounded border px-3 py-2"
          placeholder="Password"
          type="password"
          value={loginForm.password}
          onChange={(event) =>
            setLoginForm((prev) => ({ ...prev, password: event.target.value }))
          }
          required
        />
        <div className="flex gap-2">
          <button
            className="rounded border px-3 py-2 hover:bg-green-600 hover:shadow-lg transition-all duration-200"
            disabled={authLoading}
            type="submit"
          >
            Đăng nhập
          </button>
          <button
            className="rounded border px-3 py-2 hover:bg-red-600 hover:shadow-lg transition-all duration-200"
            disabled={!token || authLoading}
            type="button"
            onClick={() => {
              onLogout();
              setLoginForm(defaultLoginForm);
            }}
          >
            Đăng xuất
          </button>
        </div>
      </form>
      <p className="mt-2 text-sm">
        Trạng thái token:{" "}
        <span className={token ? "font-semibold text-green-700" : "font-semibold text-gray-500"}>
          {hasToken ? "Đã đăng nhập" : "Chưa đăng nhập"}
        </span>
      </p>
    </section>
  );
});

type GameplaySectionProps = {
  token: string | null;
  guessResult: GuessResponse | null;
  onGuessSuccess: (guessResponse: GuessResponse) => Promise<void>;
  setError: (text: string) => void;
};

const GameplaySection = memo(function GameplaySection({
  token,
  guessResult,
  onGuessSuccess,
  setError,
}: GameplaySectionProps) {
  const [guess, setGuess] = useState(1);
  const [guessLoading, setGuessLoading] = useState(false);
  const [buyTurnsLoading, setBuyTurnsLoading] = useState(false);
  const gameplayLoading = guessLoading || buyTurnsLoading;

  const handleGuess = async () => {
    setGuessLoading(true);
    try {
      const response = await api.post<GuessResponse>("/games/guess", { guess });
      await onGuessSuccess(response.data);
    } catch (error) {
      setError(getErrorMessage(error, "Không thể gửi lượt đoán."));
    } finally {
      setGuessLoading(false);
    }
  };

  const handleBuyTurns = async () => {
    setBuyTurnsLoading(true);
    try {
      const response = await api.post<CreatePaymentResponse>("/payment/create");
      if (response.status === 200 && response.data.paymentUrl) {
        window.location.href = response.data.paymentUrl;
      } else {
        setError(getErrorMessage(null, "Không thể khởi tạo link thanh toán!"));
      }
    } catch (error) {
      setError(getErrorMessage(error, "Không thể mua thêm lượt."));
    } finally {
      setBuyTurnsLoading(false);
    }
  };

  return (
    <section className="rounded border p-4">
      <h2 className="mb-3 text-lg font-semibold">3) Gameplay & Store</h2>
      <div className="flex flex-wrap items-center gap-2">
        <label className="text-sm" htmlFor="guess-input">
          Guess (1-5):
        </label>
        <input
          id="guess-input"
          className="w-24 rounded border px-3 py-2"
          type="number"
          min={1}
          max={5}
          value={guess}
          onChange={(event) => setGuess(Number(event.target.value))}
        />
        <button
          className="rounded border px-3 py-2 hover:bg-green-600 hover:shadow-lg transition-all duration-200"
          disabled={!token || gameplayLoading}
          onClick={handleGuess}
          type="button"
        >
          Guess Number
        </button>
        <button
          className="rounded border px-3 py-2 hover:bg-yellow-600 hover:shadow-lg transition-all duration-200"
          disabled={!token || gameplayLoading}
          onClick={handleBuyTurns}
          type="button"
        >
          Buy Turns
        </button>
      </div>

      {guessResult && (
        <div className="mt-3 rounded border bg-gray-50 p-3 text-sm">
          <p>Server result: {guessResult.serverResult}</p>
          <p>Kết quả: {guessResult.isCorrect ? "Đúng" : "Sai"}</p>
        </div>
      )}
    </section>
  );
});

type ProfileSectionProps = {
  me: MeResponse | null;
};

const ProfileSection = memo(function ProfileSection({ me }: ProfileSectionProps) {
  return (
    <section className="rounded border p-4 w-1/2">
      <h2 className="mb-2 text-lg font-semibold">4) Hồ sơ cá nhân</h2>
      {me ? (
        <ul className="list-disc space-y-1 pl-5">
          <li>Email: {me.email}</li>
          <li>Score: {me.score}</li>
          <li>Turns: {me.turns}</li>
        </ul>
      ) : (
        <p className="text-gray-600">Chưa có dữ liệu hồ sơ (hãy đăng nhập trước).</p>
      )}
    </section>
  );
});

type LeaderboardSectionProps = {
  leaderboard: LeaderboardEntry[];
};

const LeaderboardSection = memo(function LeaderboardSection({
  leaderboard,
}: LeaderboardSectionProps) {
  return (
    <section className="rounded border p-4 w-1/2">
      <h2 className="mb-2 text-lg font-semibold">5) Leaderboard</h2>
      {leaderboard.length > 0 ? (
        <ol className="list-decimal space-y-1 pl-5">
          {leaderboard.map((entry) => (
            <li key={entry.username}>
              {entry.username} - {entry.score}
            </li>
          ))}
        </ol>
      ) : (
        <p className="text-gray-600">Chưa có dữ liệu bảng xếp hạng.</p>
      )}
    </section>
  );
});

type StatusNoticeProps = {
  status: Status;
};

const StatusNotice = memo(function StatusNotice({ status }: StatusNoticeProps) {
  if (!status) {
    return null;
  }

  return (
    <div
      className={`rounded border p-3 text-sm ${status.tone === "success"
        ? "border-green-300 bg-green-50 text-green-700"
        : "border-red-300 bg-red-50 text-red-700"
        }`}
    >
      {status.text}
    </div>
  );
});

export default function Home() {
  const [token, setToken] = useState<string | null>(() => {
    if (typeof window === "undefined") {
      return null;
    }

    return localStorage.getItem("token");
  });
  const [me, setMe] = useState<MeResponse | null>(null);
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [guessResult, setGuessResult] = useState<GuessResponse | null>(null);
  const [status, setStatus] = useState<Status>(null);

  const setSuccess = useCallback((text: string) => {
    setStatus({ tone: "success", text });
  }, []);

  const setError = useCallback((text: string) => {
    setStatus({ tone: "error", text });
  }, []);

  const loadLeaderboard = useCallback(async () => {
    try {
      const response = await api.get<LeaderboardEntry[]>("/leaderboards");
      setLeaderboard((prev) => (isSameLeaderboard(prev, response.data) ? prev : response.data));
    } catch (error) {
      setError(getErrorMessage(error, "Không lấy được bảng xếp hạng."));
    }
  }, [setError]);

  const loadMe = useCallback(async () => {
    try {
      const response = await api.get<MeResponse>("/users/me");
      setMe((prev) => (isSameMe(prev, response.data) ? prev : response.data));
    } catch (error) {
      setError(getErrorMessage(error, "Không lấy được hồ sơ người dùng."));
    }
  }, [setError]);

  const handleLoginSuccess = useCallback(
    async (nextToken: string) => {
      setToken(nextToken);
      setGuessResult(null);
      setSuccess("Đăng nhập thành công.");
      await Promise.all([loadMe(), loadLeaderboard()]);
    },
    [loadLeaderboard, loadMe, setSuccess],
  );

  const handleGuessSuccess = useCallback(
    async (nextGuessResult: GuessResponse) => {
      setGuessResult(nextGuessResult);
      setSuccess(nextGuessResult.isCorrect ? "Bạn đoán đúng!" : "Bạn đoán sai.");
      if (nextGuessResult.isCorrect) {
        await Promise.all([loadMe(), loadLeaderboard()]);
      } else {
        await loadMe();
      }
    },
    [loadLeaderboard, loadMe, setSuccess],
  );



  const handleLogout = useCallback(() => {
    localStorage.removeItem("token");
    setToken(null);
    setMe(null);
    setGuessResult(null);
    setSuccess("Đã đăng xuất.");
  }, [setSuccess]);

  useEffect(() => {
    const timerId = window.setTimeout(() => {
      void loadLeaderboard();
      if (token) {
        void loadMe();
      }

      // Check payment status from URL
      if (typeof window !== "undefined") {
        const urlParams = new URLSearchParams(window.location.search);
        const paymentStatus = urlParams.get("payment");
        if (paymentStatus === "success") {
          setSuccess("Thanh toán thành công. Đã cập nhật số lượt của bạn.");
          window.history.replaceState({}, document.title, window.location.pathname);
        } else if (paymentStatus === "failed") {
          setError("Thanh toán thất bại hoặc đã bị hủy.");
          window.history.replaceState({}, document.title, window.location.pathname);
        }
      }
    }, 0);

    return () => {
      window.clearTimeout(timerId);
    };
  }, [loadLeaderboard, loadMe, token, setSuccess, setError]);

  return (
    <main className="mx-auto flex w-full max-w-5xl flex-col gap-5 p-6">
      <h1 className="text-2xl font-bold">Number Guessing Game - API Test UI</h1>
      <RegisterSection setSuccess={setSuccess} setError={setError} />
      <AuthSection
        token={token}
        onLoginSuccess={handleLoginSuccess}
        onLogout={handleLogout}
        setError={setError}
      />
      {token ? (
        <GameplaySection
          token={token}
          guessResult={guessResult}
          onGuessSuccess={handleGuessSuccess}
          setError={setError}
        />
      ) : null}

      <section className="flex flex-row">
        <ProfileSection me={me} />
        <LeaderboardSection leaderboard={leaderboard} />
      </section>

      <StatusNotice status={status} />
    </main>
  );
}
