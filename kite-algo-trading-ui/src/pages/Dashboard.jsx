import { useEffect, useState } from "react";
import StatusCard from "../components/StatusCard";
import { fetchHealth, fetchRiskStatus, fetchSignalStats } from "../api/client";

export default function Dashboard() {
  const [health, setHealth] = useState(null);
  const [risk, setRisk] = useState(null);
  const [signals, setSignals] = useState(null);

  useEffect(() => {
    Promise.all([fetchHealth(), fetchRiskStatus(), fetchSignalStats()])
      .then(([healthData, riskData, signalData]) => {
        setHealth(healthData);
        setRisk(riskData);
        setSignals(signalData);
      })
      .catch(() => {
        setHealth(null);
        setRisk(null);
        setSignals(null);
      });
  }, []);

  return (
    <section className="grid gap-5">
      <div>
        <h2 className="text-3xl font-black tracking-[-0.05em] text-ink">Dashboard</h2>
        <p className="mt-2 max-w-2xl text-sm leading-7 text-slate-600">
          The backend runtime is already live. These cards are intentionally thin wrappers around `/api/health`,
          `/api/risk/status`, and `/api/signals/stats`.
        </p>
      </div>
      <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
        <StatusCard
          label="Health"
          value={health?.status ?? "--"}
          detail={health ? `Tick queue ${health.tickQueueDepth}, execution queue ${health.executionQueueDepth}` : "Start backend to populate"}
        />
        <StatusCard
          label="Trading"
          value={risk?.tradingEnabled ? "Enabled" : "Paused"}
          detail={risk ? `Open positions ${risk.openPositions}` : "Waiting for backend"}
        />
        <StatusCard
          label="Signals"
          value={signals?.totalSignals ?? 0}
          detail={signals?.lastSignalAt ? `Last signal ${signals.lastSignalAt}` : "No signals recorded yet"}
        />
        <StatusCard
          label="Capital"
          value={risk?.availableCapital ?? "--"}
          detail={risk ? `Reserved ${risk.reservedCapital}` : "Runtime risk status pending"}
        />
        <StatusCard
          label="PnL"
          value={risk?.realizedPnl ?? "--"}
          detail={risk?.dailyLossBreakerActive ? "Daily breaker active" : "Daily breaker clear"}
        />
        <StatusCard
          label="Universe"
          value={health?.activeUniverseSize ?? "--"}
          detail={health?.brokerMode ? `Broker mode ${health.brokerMode}` : "Backend unavailable"}
        />
      </div>
    </section>
  );
}
