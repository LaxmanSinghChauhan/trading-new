import { useEffect, useState } from "react";

export default function Navbar() {
  const [clock, setClock] = useState(() =>
    new Intl.DateTimeFormat("en-IN", {
      timeZone: "Asia/Kolkata",
      dateStyle: "medium",
      timeStyle: "medium"
    }).format(new Date())
  );

  useEffect(() => {
    const timer = setInterval(() => {
      setClock(
        new Intl.DateTimeFormat("en-IN", {
          timeZone: "Asia/Kolkata",
          dateStyle: "medium",
          timeStyle: "medium"
        }).format(new Date())
      );
    }, 1000);

    return () => clearInterval(timer);
  }, []);

  return (
    <header className="rounded-[30px] border border-black/5 bg-white/75 p-6 shadow-cloud backdrop-blur">
      <div className="flex flex-col gap-5 md:flex-row md:items-end md:justify-between">
        <div>
          <div className="inline-flex items-center gap-2 rounded-full bg-white/80 px-3 py-2 text-xs uppercase tracking-[0.22em] text-slate-500">
            <span className="h-2.5 w-2.5 rounded-full bg-green-600 shadow-[0_0_0_8px_rgba(22,163,74,0.12)]" />
            Momentum Control Room
          </div>
          <h1 className="mt-4 max-w-3xl text-4xl font-black tracking-[-0.06em] text-ink md:text-6xl">
            Operator portal for paper-mode validation and live-readiness.
          </h1>
          <p className="mt-4 max-w-3xl text-sm leading-7 text-slate-600 md:text-base">
            React/Vite/Tailwind workspace scaffolded for the v1 admin portal. The backend already exposes
            the runtime APIs for health, signals, risk, positions, config, and news.
          </p>
        </div>
        <div className="rounded-3xl border border-black/5 bg-white/85 px-5 py-4 text-sm font-semibold text-slate-700">
          {clock}
          <div className="mt-1 text-xs uppercase tracking-[0.18em] text-slate-500">Asia/Kolkata</div>
        </div>
      </div>
    </header>
  );
}
