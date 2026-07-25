import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';

import LoginPage        from './pages/LoginPage';
import SignupPage       from './pages/SignupPage';
import OnboardingPage   from './pages/OnboardingPage';
import DashboardPage    from './pages/DashboardPage';
import InterventionPage from './pages/InterventionPage';
import MoodLogPage      from './pages/MoodLogPage';
import ScriptPage       from './pages/ScriptPage';
import FamilyPage       from './pages/FamilyPage';
import ResourcesPage    from './pages/ResourcesPage';
import SafetyPlanPage   from './pages/SafetyPlanPage';

function Layout({ children }) {
  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      <main className="pb-20 md:pb-6">{children}</main>
    </div>
  );
}

function AppRoutes() {
  const { user } = useAuth();
  return (
    <Routes>
      <Route path="/login"  element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route path="/onboarding" element={<ProtectedRoute><OnboardingPage /></ProtectedRoute>} />

      <Route path="/dashboard"    element={<ProtectedRoute><Layout><DashboardPage /></Layout></ProtectedRoute>} />
      <Route path="/intervention" element={<ProtectedRoute><Layout><InterventionPage /></Layout></ProtectedRoute>} />
      <Route path="/moodlog"      element={<ProtectedRoute><Layout><MoodLogPage /></Layout></ProtectedRoute>} />
      <Route path="/scripts"      element={<ProtectedRoute><Layout><ScriptPage /></Layout></ProtectedRoute>} />
      <Route path="/family"       element={<ProtectedRoute><Layout><FamilyPage /></Layout></ProtectedRoute>} />
      <Route path="/resources"    element={<ProtectedRoute><Layout><ResourcesPage /></Layout></ProtectedRoute>} />
      <Route path="/safety-plan"  element={<ProtectedRoute><Layout><SafetyPlanPage /></Layout></ProtectedRoute>} />

      <Route path="/"  element={<Navigate to={user ? '/dashboard' : '/login'} replace />} />
      <Route path="*"  element={<Navigate to="/dashboard" replace />} />
    </Routes>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </AuthProvider>
  );
}
