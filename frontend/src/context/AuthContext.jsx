import { createContext, useContext, useState, useEffect } from 'react';

const Ctx = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('hg_token');
    const stored = localStorage.getItem('hg_user');
    if (token && stored) setUser(JSON.parse(stored));
    setLoading(false);
  }, []);

  const loginUser = data => {
    localStorage.setItem('hg_token', data.token);
    localStorage.setItem('hg_user', JSON.stringify(data));
    setUser(data);
  };

  const logoutUser = () => {
    localStorage.removeItem('hg_token');
    localStorage.removeItem('hg_user');
    setUser(null);
  };

  const completeOnboarding = () => {
    if (user) {
      const u = { ...user, onboardingComplete: true };
      localStorage.setItem('hg_user', JSON.stringify(u));
      setUser(u);
    }
  };

  return (
    <Ctx.Provider value={{ user, loading, loginUser, logoutUser, completeOnboarding }}>
      {children}
    </Ctx.Provider>
  );
}

export const useAuth = () => useContext(Ctx);
