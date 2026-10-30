---
name: apple-ux
description: Apple-inspired UI/UX rules for the Kite Momentum web console (React + Vite + Tailwind in kite-algo-trading-ui). Use whenever you build, restyle, or review any screen, component, animation, copy, or data display in the UI, so new work stays calm, smooth, legible, and consistent with the existing design tokens.
---

# Apple-style UX for the Momentum console

This skill sums up how Apple's interface designers work, as described in the Human Interface
Guidelines and seen across apple.com and macOS/iOS apps, and turns it into rules for this repo's
web UI. The goal: an operator can glance at the screen mid-session and know what is happening,
with nothing on screen fighting for attention.

The tokens below are already implemented in `kite-algo-trading-ui/tailwind.config.js` and
`kite-algo-trading-ui/src/index.css`. **Use the tokens. Never hard-code hex values in components.**

## 1. The three principles

1. **Clarity.** Every screen answers one question ("How is today going?", "What am I holding?").
   Text is legible at every size, icons are unambiguous, and numbers line up.
2. **Deference.** The UI frames the data and stays out of the way. No decorative gradients behind
   content, no ornamental labels, no developer notes (API paths, class names) in user-facing copy.
3. **Depth.** Hierarchy comes from layering: a translucent chrome layer (sidebar/top bar) over
   opaque content cards over the canvas. Motion confirms where things came from and went.

## 2. Color (semantic tokens, light and dark)

| Token (Tailwind) | Light | Dark | Use |
|---|---|---|---|
| `canvas` | #F5F5F7 | #000000 | Page background |
| `surface` | #FFFFFF | #1C1C1E | Cards, grouped lists, tables |
| `fill` | #F2F2F7 | #2C2C2E | Control backgrounds, skeletons, row hover |
| `label` | #1D1D1F | #F5F5F7 | Primary text |
| `secondary` | #6E6E73 | #A1A1A6 | Supporting text (still ≥ 4.5:1) |
| `tertiary` | #86868B | #6E6E73 | Non-essential only: placeholders, disabled |
| `separator` | #E5E5EA | #38383A | Hairlines between rows |
| `accent` | #0066CC | #2997FF | Links, selection, primary buttons, focus |
| `positive` | #1A7431 | #30D158 | Gains, healthy states |
| `negative` | #C00013 | #FF6961 | Losses, breakers tripped, errors |
| `caution` | #B52F00 | #FF9F0A | Warnings, paused states |

Rules:
- One accent. Color carries meaning (state, direction), never decoration.
- Gains and losses must **never rely on color alone**: always add a sign and an arrow (▲ / ▼).
- Dark mode is automatic (`prefers-color-scheme`); tokens swap, components don't change.
- Tinted badges use the tone at ~12% opacity behind the full-strength tone text (`bg-positive/12 text-positive`).
- These light-mode tones are deliberately a step darker than Apple's stock system colors: the stock
  green (#248A3D) and blue (#0071E3) measure about 3.8–4.0:1 on their own 12% tint, which fails
  for 13px badge text. Re-check contrast whenever you add a tone or change a tint.

## 3. Typography

System font stack first (`-apple-system`, `BlinkMacSystemFont`, `SF Pro`), so Apple devices get San
Francisco and others get their native UI font. Sizes (Tailwind `fontSize` tokens):

| Token | Size / line | Weight | Use |
|---|---|---|---|
| `text-large-title` | 34 / 41 | 700 | One per page, the page title |
| `text-title` | 22 / 28 | 600 | Hero numbers, section titles |
| `text-headline` | 17 / 22 | 600 | Row titles, card values |
| `text-body` | 15 / 22 | 400 | Default text |
| `text-footnote` | 13 / 18 | 400–500 | Labels, metadata, timestamps |

- Tighten tracking as size grows (built into the tokens); never letter-space small text.
- **Sentence case** for labels and descriptions, **Title Case** for nav items and buttons.
- No ALL-CAPS tracked "eyebrow" labels.
- Every price, quantity, P&L, percentage, and clock uses `tabular-nums` so digits don't jitter
  while values update. Numeric table columns are right-aligned.

## 4. Layout and spacing

- 4/8 pt grid. Card padding 20–24px, row height ≥ 44px, section gap 24–32px.
- Desktop: macOS-style **sidebar** (source list) + content. Mobile: translucent top bar with a
  horizontally scrolling nav row. Never horizontal page scroll; wide tables scroll inside their card.
- Content max width ~1150px; reading text ≤ 70ch.
- A tall hero card gets its own full row; don't let short tiles beside it stretch into empty boxes.
- Secondary buttons on the canvas use `surface` + `shadow-card`; a `fill` background vanishes on `canvas`.
- Group related rows into **inset grouped lists** (one rounded card, hairline separators inset
  from the left edge) instead of a separate card per item.

## 5. Materials, shape, elevation

- Chrome only (sidebar, top bar) gets the material: `.material` = 72% surface + `saturate(180%) blur(20px)`.
  Content stays opaque for legibility.
- Radii: cards `rounded-card` (18px), controls `rounded-control` (10px), badges/pills `rounded-full`.
- Shadows are barely there in light mode (`shadow-card`) and off in dark mode; elevation in dark
  mode comes from a lighter surface, not a shadow.

## 6. Motion (smooth, never showy)

| Token | Value | Use |
|---|---|---|
| `duration-fast` | 150ms | Press, hover, color changes |
| `duration-base` | 250ms | State changes (expand, select) |
| `duration-slow` | 350ms | Page and content entrance |
| `ease-apple` | cubic-bezier(0.22, 1, 0.36, 1) | Everything that arrives (fast start, soft landing) |
| `ease-apple-in` | cubic-bezier(0.4, 0, 1, 1) | Things leaving; make exits ~70% of the enter time |

- Animate only `transform` and `opacity`. Never width/height/top/left.
- Page change: content fades in and rises 8px (`.animate-rise`). Lists stagger 40ms per item,
  capped at ~8 items (`style={{ "--i": index }}`).
- Pressable things scale to 0.97 while held (`.pressable`) so every tap gets instant feedback.
- Motion must mean something (arrival, cause and effect). At most 1–2 animated things per view.
- Never block input while animating; live data updates swap in place without re-running entrances.
- `prefers-reduced-motion` turns all of this off (already handled globally in `index.css`).

## 7. States: loading, empty, error

- **Loading:** skeleton shapes in the final layout (`<Skeleton />`), never a spinner on a blank page.
  When refreshing data that's already shown, keep the old values visible; no flicker.
- **Empty:** icon + short title + one sentence on what will appear and when (`<EmptyState />`).
- **Error:** say what happened and what to do, with a Try Again button (`<ErrorState />`). No stack
  traces, status codes, or URLs. Never reassure about state the UI can't verify ("your positions are
  unaffected") while the backend is unreachable.
- Live screens poll quietly and **pause while the tab is hidden** (`useApi(..., { refreshMs })`).

## 8. Accessibility (non-negotiable)

- Text contrast ≥ 4.5:1 (the tokens are chosen for this; don't put `tertiary` on body text).
- Visible focus ring on keyboard focus (`:focus-visible`, accent at 50%) and never `outline: none`.
- Hit targets ≥ 44×44px on touch layouts.
- Icon-only buttons need `aria-label`; decorative SVGs get `aria-hidden="true"`.
- Use real semantics: `<nav>`, `<main>`, `<table>` with `<th scope>`, `aria-current` on the active nav item.
- Live values that matter (P&L, status) are text, not only color or icon.

## 9. Trading-specific rules

- Money in INR via `Intl.NumberFormat("en-IN")`, 2 decimals for prices, lakh grouping.
- P&L: `<Pnl value={x} />`, which renders sign, arrow, and positive/negative color.
- Times shown in IST, 24-hour, `tabular-nums`. Server timestamps are zone-less IST ISO strings, so
  format them by parsing the string, not via `new Date()` in the browser's zone.
- Safety-critical states (trading paused, loss breaker tripped, market breaker active) must be
  visible on the Overview without scrolling, using `negative`/`caution` badges with text.
- Never flash large areas on tick updates.

## 10. Copy voice

Plain, calm, operator-facing. Say what the thing is and what happens next.

- Bad: "These cards are thin wrappers around `/api/health`."  Good: "Live state of the engine, refreshed every few seconds."
- Bad: "No data."  Good: "No open positions. When the engine enters a trade, it shows up here."

## Pre-ship checklist

- [ ] Only tokens used (no raw hex, no ad-hoc shadows/radii)
- [ ] Looks right in light **and** dark mode
- [ ] Phone width: no horizontal page scroll, 44px targets, nav reachable
- [ ] Loading, empty, and error states all designed
- [ ] Numbers tabular, right-aligned in tables, P&L has sign + arrow
- [ ] Keyboard: tab through everything, focus ring visible
- [ ] Reduced motion: nothing moves
- [ ] `npm run build` passes

## Going deeper

The general, stack-agnostic version of this skill (tokens.css, retrofit layer, review checklist,
contrast script) lives in LaxmanSinghChauhan/Ai-skills under `plugins/apple-ux`.

For a searchable database of styles, palettes, and 100+ UX rules, install the community skill
**ui-ux-pro-max** (MIT): `/plugin marketplace add nextlevelbuilder/ui-ux-pro-max-skill` then
`/plugin install ui-ux-pro-max@ui-ux-pro-max-skill`. To audit code against Apple HIG rules,
`npx hig-doctor .` (raintree-technology/hig-doctor). Apple's own reference is the Human Interface
Guidelines at developer.apple.com/design/human-interface-guidelines.
