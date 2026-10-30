import { Route, Routes, useLocation } from "react-router-dom";
import Navbar from "./components/Navbar";
import Dashboard from "./pages/Dashboard";
import Positions from "./pages/Positions";
import Configuration from "./pages/Configuration";
import TradeHistory from "./pages/TradeHistory";
import Signals from "./pages/Signals";
import News from "./pages/News";

const navItems = [
  { to: "/", label: "Overview", icon: "overview" },
  { to: "/positions", label: "Positions", icon: "positions" },
  { to: "/signals", label: "Signals", icon: "signals" },
  { to: "/trade-history", label: "Trade History", icon: "history" },
  { to: "/news", label: "News", icon: "news" },
  { to: "/configuration", label: "Configuration", icon: "configuration" }
];

export default function App() {
  const location = useLocation();

  return (
    <div className="min-h-screen md:flex">
      <Navbar items={navItems} />
      <main className="min-w-0 flex-1 px-4 pb-16 pt-6 md:px-10 md:pt-10">
        {/* Keyed on the route so each page arrives with the same gentle rise. */}
        <div key={location.pathname} className="animate-rise mx-auto max-w-6xl">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/positions" element={<Positions />} />
            <Route path="/configuration" element={<Configuration />} />
            <Route path="/trade-history" element={<TradeHistory />} />
            <Route path="/signals" element={<Signals />} />
            <Route path="/news" element={<News />} />
          </Routes>
        </div>
      </main>
    </div>
  );
}
