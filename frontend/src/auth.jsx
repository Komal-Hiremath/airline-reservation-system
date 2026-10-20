import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { api, SESSION_KEY, setUnauthorizedHandler } from "./api.js";

const AuthContext = createContext(null);

// Reads the exp claim of the JWT so an expired token is dropped before it is ever sent.
function isExpired(token) {
  try {
    const payload = JSON.parse(
      atob(token.split(".")[1].replace(/-/g, "+").replace(/_/g, "/")),
    );
    return payload.exp * 1000 < Date.now();
  } catch {
    return true;
  }
}

function loadSession() {
  try {
    const session = JSON.parse(localStorage.getItem(SESSION_KEY));
    return session?.token && !isExpired(session.token) ? session : null;
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [session, setSession] = useState(loadSession);

  const logout = useCallback(() => {
    localStorage.removeItem(SESSION_KEY);
    setSession(null);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
  }, [logout]);

  const login = useCallback(async (email, password) => {
    const result = await api.login({ email, password });

    // The backend may wrap responses in ResponseStructure<T>.
    const payload = result?.data ?? result;
    const userData = payload?.user ?? payload?.account ?? payload;

    const rawRole =
      userData?.role ??
      userData?.userRole ??
      payload?.role ??
      payload?.user?.role;

    const roleText = typeof rawRole === "string"
      ? rawRole
      : rawRole?.name ?? rawRole?.authority ?? "";

    const role = roleText.toUpperCase().replace(/^ROLE_/, "");
    const token =
      payload?.token ??
      payload?.accessToken ??
      payload?.jwt ??
      result?.token;

    if (!token) {
      throw new Error("Login response did not contain a token. Check the /auth/login response in the browser Network tab.");
    }
    if (!["ADMIN", "CUSTOMER"].includes(role)) {
      console.error("Could not identify role in login response:", result);
      throw new Error("Login succeeded, but the server response did not include a recognized ADMIN or CUSTOMER role.");
    }

    const next = {
      token,
      name: userData?.name ?? userData?.fullName ?? userData?.email ?? payload?.name ?? "",
      email: userData?.email ?? payload?.email ?? email,
      role,
    };
    localStorage.setItem(SESSION_KEY, JSON.stringify(next));
    setSession(next);
    return next;
  }, []);

  const value = useMemo(
    () => ({
      user: session,
      isAdmin: session?.role === "ADMIN",
      isCustomer: session?.role === "CUSTOMER",
      login,
      logout,
    }),
    [session, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export const useAuth = () => useContext(AuthContext);
