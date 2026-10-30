import PageHeader from "../components/PageHeader";

const settings = [
  { key: "trading_enabled", label: "Trading enabled", help: "Master switch for placing new orders." },
  { key: "signal_price_change_pct", label: "Signal price move", help: "Minimum % move that counts as a spike." },
  { key: "signal_volume_multiplier", label: "Signal volume", help: "Volume needed versus the usual average." },
  { key: "max_open_positions", label: "Max open positions", help: "Trades allowed at the same time." },
  { key: "market_fall_threshold_pct", label: "Market fall breaker", help: "Index drop that pauses new entries." }
];

export default function Configuration() {
  return (
    <section>
      <PageHeader
        title="Configuration"
        subtitle="Runtime settings the engine reads live. Editing becomes available once admin sign-in is connected."
      />
      <ul className="card divide-y divide-separator overflow-hidden">
        {settings.map((setting, index) => (
          <li
            key={setting.key}
            className="animate-rise flex min-h-[64px] flex-wrap items-center justify-between gap-x-6 gap-y-1 px-5 py-3"
            style={{ "--i": index }}
          >
            <div className="min-w-0">
              <div className="text-body font-medium text-label">{setting.label}</div>
              <div className="text-footnote text-secondary">{setting.help}</div>
            </div>
            <div className="text-right">
              <div className="text-body text-secondary">Not connected</div>
              <code className="font-mono text-[0.75rem] text-secondary">{setting.key}</code>
            </div>
          </li>
        ))}
      </ul>
    </section>
  );
}
