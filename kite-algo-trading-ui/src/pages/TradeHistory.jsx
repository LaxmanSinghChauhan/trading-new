import { useEffect, useState } from "react";
import { fetchTradeHistory } from "../api/client";

export default function TradeHistory() {
  const [rows, setRows] = useState([]);

  useEffect(() => {
    fetchTradeHistory().then(setRows).catch(() => setRows([]));
  }, []);

  return (
    <section className="grid gap-5">
      <div>
        <h2 className="text-3xl font-black tracking-[-0.05em] text-ink">Trade History</h2>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-slate-600">
          The new `trade_log` and `trade_leg_log` schema supports partial exits and audit trails. This view is
          scaffolded around `/api/positions/history`.
        </p>
      </div>
      <div className="grid gap-4">
        {rows.length ? (
          rows.map((row) => (
            <div key={row.trade.id} className="rounded-3xl border border-black/5 bg-white/80 p-5">
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <div className="text-xl font-bold text-ink">{row.trade.symbol}</div>
                  <div className="mt-1 text-sm text-slate-600">{row.trade.status} • {row.trade.finalExitReason || "Open"}</div>
                </div>
                <div className="rounded-full bg-ember/10 px-3 py-1 text-xs font-bold uppercase tracking-[0.16em] text-ember">
                  Net {row.trade.netPnl}
                </div>
              </div>
              <div className="mt-4 text-sm text-slate-600">Entry {row.trade.entryPriceVwap} • Exit {row.trade.exitPriceVwap || "--"}</div>
              <div className="mt-2 text-sm text-slate-600">Legs recorded: {row.legs.length}</div>
            </div>
          ))
        ) : (
          <div className="rounded-3xl border border-black/5 bg-white/80 p-6 text-slate-500">
            No trade history yet.
          </div>
        )}
      </div>
    </section>
  );
}
