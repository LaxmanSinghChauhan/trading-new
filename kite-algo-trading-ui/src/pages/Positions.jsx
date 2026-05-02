import { useEffect, useState } from "react";
import { fetchPositions } from "../api/client";

export default function Positions() {
  const [positions, setPositions] = useState([]);

  useEffect(() => {
    fetchPositions().then(setPositions).catch(() => setPositions([]));
  }, []);

  return (
    <section className="grid gap-5">
      <div>
        <h2 className="text-3xl font-black tracking-[-0.05em] text-ink">Positions</h2>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-slate-600">
          Open positions come from `/api/positions` and reflect the paper/live lifecycle managed by the new engine.
        </p>
      </div>
      <div className="overflow-hidden rounded-3xl border border-black/5 bg-white/80">
        <table className="min-w-full text-left text-sm">
          <thead className="bg-black/5 text-xs uppercase tracking-[0.16em] text-slate-500">
            <tr>
              <th className="px-5 py-4">Symbol</th>
              <th className="px-5 py-4">Token</th>
              <th className="px-5 py-4">Entry</th>
              <th className="px-5 py-4">Last</th>
              <th className="px-5 py-4">Qty</th>
              <th className="px-5 py-4">Unrealized</th>
            </tr>
          </thead>
          <tbody>
            {positions.length ? (
              positions.map((position) => (
                <tr key={position.symbol} className="border-t border-black/5">
                  <td className="px-5 py-4 font-semibold text-ink">{position.symbol}</td>
                  <td className="px-5 py-4 text-slate-600">{position.instrumentToken}</td>
                  <td className="px-5 py-4">{position.entryPrice}</td>
                  <td className="px-5 py-4">{position.lastPrice}</td>
                  <td className="px-5 py-4">{position.remainingQuantity}/{position.totalQuantity}</td>
                  <td className="px-5 py-4">{position.unrealizedPnl}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td className="px-5 py-6 text-slate-500" colSpan="6">No open positions.</td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
