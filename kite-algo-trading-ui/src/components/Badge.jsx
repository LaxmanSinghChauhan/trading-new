const tones = {
  neutral: "bg-fill text-secondary",
  accent: "bg-accent/12 text-accent",
  positive: "bg-positive/12 text-positive",
  negative: "bg-negative/12 text-negative",
  caution: "bg-caution/12 text-caution"
};

export default function Badge({ tone = "neutral", dot = false, children }) {
  return (
    <span
      className={`inline-flex items-center gap-1.5 whitespace-nowrap rounded-full px-2.5 py-1 text-footnote font-medium ${tones[tone]}`}
    >
      {dot && <span className="h-1.5 w-1.5 rounded-full bg-current" aria-hidden="true" />}
      {children}
    </span>
  );
}
