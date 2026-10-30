import { DASH, formatINR, toNumber } from "../lib/format";

// Gains and losses always carry a sign and an arrow, never color alone.
export default function Pnl({ value, className = "" }) {
  const number = toNumber(value);
  if (number === null) return <span className={`tabular-nums text-secondary ${className}`}>{DASH}</span>;

  const tone = number > 0 ? "text-positive" : number < 0 ? "text-negative" : "text-secondary";
  const arrow = number > 0 ? "▲" : number < 0 ? "▼" : "";
  const sign = number > 0 ? "+" : number < 0 ? "−" : "";

  return (
    <span className={`whitespace-nowrap tabular-nums ${tone} ${className}`}>
      {arrow && (
        <span className="mr-1 text-[0.7em] align-[0.1em]" aria-hidden="true">
          {arrow}
        </span>
      )}
      {sign}
      {formatINR(Math.abs(number))}
    </span>
  );
}
