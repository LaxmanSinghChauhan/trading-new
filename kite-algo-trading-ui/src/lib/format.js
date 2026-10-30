const MONTHS = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

const inr = new Intl.NumberFormat("en-IN", {
  style: "currency",
  currency: "INR",
  minimumFractionDigits: 2,
  maximumFractionDigits: 2
});

const plain = new Intl.NumberFormat("en-IN", { maximumFractionDigits: 2 });

export const DASH = "—";

export function toNumber(value) {
  if (value === null || value === undefined || value === "") return null;
  const number = Number(value);
  return Number.isFinite(number) ? number : null;
}

export function formatINR(value) {
  const number = toNumber(value);
  return number === null ? DASH : inr.format(number);
}

export function formatNumber(value) {
  const number = toNumber(value);
  return number === null ? DASH : plain.format(number);
}

export function formatPct(value, { signed = true } = {}) {
  const number = toNumber(value);
  if (number === null) return DASH;
  const sign = signed && number > 0 ? "+" : "";
  return `${sign}${plain.format(number)}%`;
}

// Server timestamps are zone-less IST ISO strings ("2026-10-30T14:32:05.123"), so read the
// parts straight from the string instead of letting the browser shift them to its own zone.
export function formatTimestamp(value) {
  if (!value) return DASH;
  const match = String(value).match(/^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/);
  if (!match) return String(value);
  const [, year, month, day, hour, minute, second = "00"] = match;
  const time = `${hour}:${minute}:${second}`;
  if (`${year}-${month}-${day}` === todayInIST()) return time;
  return `${Number(day)} ${MONTHS[Number(month) - 1]}, ${hour}:${minute}`;
}

export function todayInIST() {
  return new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Kolkata" }).format(new Date());
}

export function humanize(value) {
  if (!value) return DASH;
  const text = String(value).replace(/_/g, " ").toLowerCase();
  return text.charAt(0).toUpperCase() + text.slice(1);
}
