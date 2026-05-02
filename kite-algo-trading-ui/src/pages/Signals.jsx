import { useEffect, useState } from "react";
import { fetchSignals } from "../api/client";

export default function Signals() {
  const [signals, setSignals] = useState([]);

  useEffect(() => {
    fetchSignals().then(setSignals).catch(() => setSignals([]));
  }, []);

  return (
    <section className="grid gap-5">
      <div>
        <h2 className="text-3xl font-black tracking-[-0.05em] text-ink">Signals</h2>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-slate-600">
          Signal detection is already separated from risk gating. This page can evolve into a detailed feed with
          cooldown markers, momentum metrics, and rejection reasons.
        </p>
      </div>
      <div className="grid gap-4 md:grid-cols-2">
        {signals.length ? (
          signals.map((signal) => (
            <div key={`${signal.symbol}-${signal.detectedAt}`} className="rounded-3xl border border-black/5 bg-white/80 p-5">
              <div className="flex items-center justify-between gap-3">
                <div className="text-xl font-bold text-ink">{signal.symbol}</div>
                <div className="rounded-full bg-tide/10 px-3 py-1 text-xs font-bold uppercase tracking-[0.16em] text-tide">
                  {signal.strength}
                </div>
              </div>
              <div className="mt-4 text-sm text-slate-600">Price change: {signal.priceChangePct}%</div>
              <div className="mt-2 text-sm text-slate-600">Volume multiplier: {signal.volumeMultiplier}x</div>
              <div className="mt-2 text-sm text-slate-600">Detected at: {signal.detectedAt}</div>
            </div>
          ))
        ) : (
          <div className="rounded-3xl border border-black/5 bg-white/80 p-6 text-slate-500">
            No signals available yet.
          </div>
        )}
      </div>
    </section>
  );
}
