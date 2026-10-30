import PageHeader from "../components/PageHeader";
import Pnl from "../components/Pnl";
import { EmptyState, ErrorState, Skeleton } from "../components/States";
import { useApi } from "../hooks/useApi";
import { fetchPositions } from "../api/client";
import { formatINR } from "../lib/format";

const columns = ["Symbol", "Quantity", "Entry", "Last", "Stop loss", "Unrealized"];

export default function Positions() {
  const { data: positions, error, loading, reload } = useApi(fetchPositions, { refreshMs: 5000 });

  if (error && !positions) {
    return (
      <section>
        <PageHeader title="Positions" />
        <ErrorState onRetry={reload} />
      </section>
    );
  }

  return (
    <section>
      <PageHeader title="Positions" subtitle="Open trades the engine is managing right now." />
      {!loading && !positions?.length ? (
        <EmptyState
          icon="positions"
          title="No open positions"
          message="When the engine enters a trade, it shows up here with live P&L."
        />
      ) : (
        <div className="card overflow-x-auto">
          <table className="w-full min-w-[640px] text-left text-body">
            <thead>
              <tr className="border-b border-separator text-footnote text-secondary">
                {columns.map((column, index) => (
                  <th
                    key={column}
                    scope="col"
                    className={`px-5 py-3 font-medium ${index === 0 ? "text-left" : "text-right"}`}
                  >
                    {column}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {loading
                ? [0, 1, 2].map((row) => (
                    <tr key={row} className="border-b border-separator last:border-0">
                      {columns.map((column, index) => (
                        <td key={column} className="px-5 py-4">
                          <Skeleton className={`h-4 w-16 ${index === 0 ? "" : "ml-auto"}`} />
                        </td>
                      ))}
                    </tr>
                  ))
                : positions.map((position) => (
                    <tr
                      key={position.symbol}
                      className="border-b border-separator transition-colors duration-fast last:border-0 hover:bg-fill/60"
                    >
                      <th scope="row" className="px-5 py-4 text-left font-semibold text-label">
                        {position.symbol}
                        {position.trailingActive && (
                          <span className="ml-2 text-footnote font-normal text-accent">Trailing</span>
                        )}
                      </th>
                      <td className="px-5 py-4 text-right tabular-nums">
                        {position.remainingQuantity}
                        <span className="text-secondary"> / {position.totalQuantity}</span>
                      </td>
                      <td className="px-5 py-4 text-right tabular-nums">{formatINR(position.entryPrice)}</td>
                      <td className="px-5 py-4 text-right tabular-nums">{formatINR(position.lastPrice)}</td>
                      <td className="px-5 py-4 text-right tabular-nums text-secondary">
                        {formatINR(position.trailStopPrice ?? position.stopLossPrice)}
                      </td>
                      <td className="px-5 py-4 text-right font-medium">
                        <Pnl value={position.unrealizedPnl} />
                      </td>
                    </tr>
                  ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}
