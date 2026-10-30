import { useEffect, useState } from "react";
import { NavLink } from "react-router-dom";
import Icon from "./Icon";

const clockFormat = new Intl.DateTimeFormat("en-IN", {
  timeZone: "Asia/Kolkata",
  hour: "2-digit",
  minute: "2-digit",
  second: "2-digit",
  hourCycle: "h23"
});

const dayFormat = new Intl.DateTimeFormat("en-IN", {
  timeZone: "Asia/Kolkata",
  weekday: "short",
  day: "numeric",
  month: "short"
});

const partsFormat = new Intl.DateTimeFormat("en-US", {
  timeZone: "Asia/Kolkata",
  weekday: "short",
  hour: "numeric",
  minute: "numeric",
  hourCycle: "h23"
});

// NSE equity hours, 09:15–15:30 IST on weekdays. Exchange holidays aren't modelled.
function marketSession(date) {
  const parts = Object.fromEntries(partsFormat.formatToParts(date).map((part) => [part.type, part.value]));
  const minutes = Number(parts.hour) * 60 + Number(parts.minute);
  const weekday = !["Sat", "Sun"].includes(parts.weekday);
  if (weekday && minutes >= 555 && minutes < 930) return { label: "Market hours", tone: "bg-positive" };
  if (weekday && minutes >= 540 && minutes < 555) return { label: "Pre-open", tone: "bg-caution" };
  return { label: "After hours", tone: "bg-tertiary" };
}

function useNow() {
  const [now, setNow] = useState(() => new Date());
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date()), 1000);
    return () => clearInterval(timer);
  }, []);
  return now;
}

function Brand() {
  return (
    <div className="flex items-center gap-3">
      <div className="flex h-9 w-9 items-center justify-center rounded-[10px] bg-accent text-white">
        <Icon name="positions" className="h-5 w-5" strokeWidth={2.25} />
      </div>
      <div className="leading-tight">
        <div className="text-headline font-semibold text-label">Momentum</div>
        <div className="text-footnote text-secondary">Control Room</div>
      </div>
    </div>
  );
}

function SessionStatus({ now, compact = false }) {
  const session = marketSession(now);
  return (
    <div className={compact ? "flex items-center gap-2" : "rounded-control bg-fill px-3 py-2.5"}>
      <div className="flex items-center gap-2 text-footnote font-medium text-label">
        <span className={`h-2 w-2 rounded-full ${session.tone}`} aria-hidden="true" />
        {session.label}
      </div>
      {!compact && (
        <div className="mt-1 text-footnote tabular-nums text-secondary">
          {dayFormat.format(now)} · {clockFormat.format(now)} IST
        </div>
      )}
    </div>
  );
}

export default function Navbar({ items }) {
  const now = useNow();

  return (
    <>
      <aside className="material sticky top-0 hidden h-screen w-64 shrink-0 flex-col border-r border-separator px-3 py-5 md:flex">
        <div className="px-3">
          <Brand />
        </div>
        <nav aria-label="Main" className="mt-8 flex flex-1 flex-col gap-0.5">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) =>
                `pressable flex items-center gap-3 rounded-control px-3 py-2 text-body font-medium ${
                  isActive ? "bg-accent/12 text-accent" : "text-label hover:bg-fill"
                }`
              }
            >
              <Icon name={item.icon} />
              {item.label}
            </NavLink>
          ))}
        </nav>
        <SessionStatus now={now} />
      </aside>

      <header className="material sticky top-0 z-20 border-b border-separator md:hidden">
        <div className="flex items-center justify-between px-4 pb-2 pt-3">
          <Brand />
          <SessionStatus now={now} compact />
        </div>
        <nav aria-label="Main" className="no-scrollbar flex gap-1 overflow-x-auto px-3 pb-2">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === "/"}
              className={({ isActive }) =>
                `pressable flex min-h-[44px] shrink-0 items-center gap-2 rounded-full px-4 text-body font-medium ${
                  isActive ? "bg-accent/12 text-accent" : "text-secondary"
                }`
              }
            >
              <Icon name={item.icon} className="h-4 w-4" />
              {item.label}
            </NavLink>
          ))}
        </nav>
      </header>
    </>
  );
}
