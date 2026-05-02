import { NavLink, Route, Routes } from "react-router-dom";
import Navbar from "./components/Navbar";
import Dashboard from "./pages/Dashboard";
import Positions from "./pages/Positions";
import Configuration from "./pages/Configuration";
import TradeHistory from "./pages/TradeHistory";
import Signals from "./pages/Signals";
import News from "./pages/News";

const navItems = [
  { to: "/", label: "Dashboard" },
  { to: "/positions", label: "Positions" },
  { to: "/configuration", label: "Configuration" },
  { to: "/trade-history", label: "Trade History" },
  { to: "/signals", label: "Signals" },
  { to: "/news", label: "News" }
];

function ShellLink({ to, label }) {
  return (
    <NavLink
      to={to}
      end={to === "/"}
      className={({ isActive }) =>
        `rounded-full px-4 py-2 text-sm font-semibold transition ${
          isActive
            ? "bg-tide text-white shadow-cloud"
            : "bg-white/70 text-ink hover:-translate-y-0.5 hover:bg-white"
        }`
      }
    >
      {label}
    </NavLink>
  );
}

export default function App() {
  return (
    <div className="min-h-screen px-4 py-6 md:px-8">
      <div className="mx-auto flex max-w-7xl flex-col gap-6">
        <Navbar />
        <div className="rounded-[28px] border border-black/5 bg-white/70 p-4 shadow-cloud backdrop-blur md:p-6">
          <div className="mb-5 flex flex-wrap gap-3">
            {navItems.map((item) => (
              <ShellLink key={item.to} to={item.to} label={item.label} />
            ))}
          </div>
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/positions" element={<Positions />} />
            <Route path="/configuration" element={<Configuration />} />
            <Route path="/trade-history" element={<TradeHistory />} />
            <Route path="/signals" element={<Signals />} />
            <Route path="/news" element={<News />} />
          </Routes>
        </div>
      </div>
    </div>
  );
}
