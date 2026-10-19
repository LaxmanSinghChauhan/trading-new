# Library Research: Calendars, Date Pickers, Components, Color, Type, Icons

What we can build the EHR front end with, judged on what matters for clinical scheduling: **accessibility, resource (per-provider) views, licensing cost, theming to our tokens, and maintenance.** Prices and license terms change often. They're as found in October 2026, so confirm on the vendor page before buying.

---

## 1. Scheduler / calendar components

The scheduling screen needs a **resource time grid** (one column per provider), a **resource timeline** (providers as rows), a **list/agenda** view, drag-to-reschedule, and full theming.

| Library | License / cost | Resource views | Drag & resize | Framework | Strengths | Watch-outs |
|---|---|---|---|---|---|---|
| **EventCalendar** (`@event-calendar/core`, vkurko) | **MIT, free** | ✅ `resourceTimeGridDay/Week`, `resourceTimelineDay/Week/Month/Year` | ✅ (Interaction plugin) | Vanilla JS + Svelte 5; easy to wrap in React | Free resource views, zero dependencies, small (~37 kB), FullCalendar-like API, CSS-grid based so easy to restyle with our tokens | Small team; limited official a11y docs, so we add our own ARIA labels and keyboard grid; no React wrapper maintained by the author |
| **FullCalendar** | Core MIT. **Premium from $480 per developer** (resource time grid + timeline) | ✅ Premium only | ✅ | React, Vue, Angular, vanilla | Most mature and best documented, huge community, keyboard support | Resource views are paid; heavier DOM; styling needs CSS overrides |
| **MUI X Scheduler** | Community Event Calendar **MIT**; Premium adds timeline and recurrence | ✅ resources in Community; Timeline in Premium | ✅ | React + MUI | Modern React API, first-class theming if we use MUI, MUI's a11y track record | v9 was in alpha/beta through 2026 (APIs may change); requires `@mui/material` |
| **Schedule-X** | Core free; **v4 moved drag-and-drop, resize and the Time Grid Resource View to Premium** (per project) | ✅ Premium | ✅ Premium | React, Vue, Angular, vanilla | Modern look, good DX | The things we need most are paid; per-project pricing |
| **react-big-calendar** | MIT, free | ⚠️ `resources` prop in day/week views (basic) | ✅ addon | React | Simple, Google-calendar-like, free | Dated styling; no timeline; limited keyboard support |
| **Bryntum Calendar / Scheduler** | Commercial, about $680–950 per developer (one-time + renewals) | ✅ excellent | ✅ | All | Enterprise-grade, very large datasets, rich resource planning | Cost; heavy; its own styling system |
| **DHTMLX Scheduler** | Free GPL edition (limited) / commercial one-time license | ✅ Units + Timeline (commercial) | ✅ | All | Doctor-appointment demos, one-time license | Older look; GPL for the free edition |
| **Syncfusion Scheduler** | Commercial; **free Community License** if < $1M revenue, ≤ 5 developers, ≤ 10 employees (not for government-funded organizations) | ✅ | ✅ | React, Angular, Vue, Blazor | Full-featured, decent a11y | Large bundle, enterprise look, license limits |

### Recommendation

1. **Default: EventCalendar (MIT)** for the time grid and timeline, wrapped in a thin React component, with our own **List view** built from our table component. It's the only free option with both resource time grid and resource timeline, and its CSS-grid DOM makes our token-based styling easy. We add: ARIA labels per appointment, roving-focus keyboard navigation across slots, and the ghost "booking" block.
2. **If budget allows and we want maximum maturity: FullCalendar Premium** (same API family, so switching from EventCalendar later is cheap).
3. **If the product standardizes on MUI: MUI X Scheduler** once it's stable.
4. Avoid Schedule-X for this use case: the needed features are all behind Premium.

**Spike before committing (1–2 days):** build the Day-by-provider view with 3 providers, 15-min slots, a now line, a ghost booking block, drag-to-reschedule with Undo, and full keyboard navigation in the top two candidates. Measure keyboard and screen-reader behavior with NVDA and VoiceOver.

---

## 2. Date pickers

| Library | Accessibility | Notes |
|---|---|---|
| **React Aria `DatePicker` / `Calendar` / `RangeCalendar`** (Adobe) | ★★★★★ | Segmented typed input + calendar popover (exactly our §7 pattern), full keyboard, localized screen-reader messages, 13 calendar systems, time-zone aware via `@internationalized/date`. Unstyled, so we style it with our tokens. **Our pick.** |
| **react-day-picker** (used by shadcn/ui) | ★★★★ | WCAG 2.1 AA, lightweight, easy to style. Calendar grid only; we'd build the typed field ourselves. |
| **Mantine Dates** | ★★★★ | Good if we use Mantine. |
| **MUI X Date Pickers** | ★★★★ | Good if we use MUI. Field + picker pattern. |
| `react-datepicker` | ★★ | Popular but has known accessibility problems. Avoid. |
| Native `<input type="date">` | ★★★ | Accessible but inconsistent across browsers, hard to style, no quick picks or availability dots. OK as a fallback only. |

---

## 3. Component foundations

| Option | What it is | Fit for EHR |
|---|---|---|
| **React Aria Components** (Adobe) | Unstyled, accessibility-first components (combobox, grid, table, dialog, date pickers, toast) | **Best a11y.** Behaviors like a combobox for patient search, a grid for the calendar, and date fields are hard to get right ourselves. Pairs with Tailwind. |
| **shadcn/ui** (Radix primitives + Tailwind) | Copy-in components we own | Very popular, easy to theme with our tokens, solid a11y via Radix. Missing a true date field and data grid, so we fill those with React Aria pieces. |
| **Mantine** | Batteries-included React library | Fast to build, good a11y (AA), includes dates and notifications. Less control over the look. |
| **MUI** | Material Design library + MUI X (data grid, scheduler, pickers) | Most complete set (grid, scheduler, pickers). Material look needs heavy theming to feel clinical. |

**Recommended stack:** React + **Tailwind CSS v4** (tokens as CSS variables) + **React Aria Components** for complex widgets (patient search combobox, date fields, grid, table, toasts) + **shadcn/ui-style** owned components for simple ones (button, card, badge, panel). The existing repo UI already uses React + Vite + Tailwind, so this adds no new build tooling.

**Design systems worth studying** (public, accessibility-tested, health or government): U.S. Web Design System (USWDS), VA.gov Design System, CMS Design System, NHS.UK design system, IBM Carbon (data-dense enterprise patterns), Atlassian Design System (status lozenges, density).

---

## 4. Color tooling

- **Radix Colors**: 12-step scales with a defined job per step (1–2 backgrounds, 3–5 component backgrounds, 6–8 borders, 9–10 solids, 11–12 text). Our tokens follow this logic. It's a good source if we need more hues.
- **Tailwind v4 palette**: OKLCH-based, so lightness is consistent across hues.
- **Okabe–Ito palette**: the reference color-blind-safe palette (blue `#0072B2`, orange `#E69F00`, vermillion `#D55E00`, …). We follow its advice: blue/orange for distinctions that matter, never red/green alone.
- **Checkers:** WebAIM Contrast Checker; APCA for large-text tuning; the Chrome DevTools "Emulate vision deficiencies" panel for protanopia, deuteranopia and tritanopia.

---

## 5. Fonts

| Font | License | Why consider |
|---|---|---|
| **Atkinson Hyperlegible Next** + **Mono** | OFL (Google Fonts) | Built by the Braille Institute for character distinction (I/l/1, 0/O). 7 weights + variable + mono. **Our pick for UI.** |
| Public Sans | OFL | USWDS's font: neutral, very legible, government health feel |
| Source Sans 3 | OFL | Highly readable in dense UI |
| IBM Plex Sans + Mono | OFL | Technical, good mono for IDs |

---

## 6. Icons

| Set | License | Notes |
|---|---|---|
| **Lucide** | ISC | 1,500+ consistent stroke icons, React package, includes `door-open`, `circle-check`, `clock`, `circle-x`, `ban`, `stethoscope`. **Our pick.** |
| Phosphor | MIT | 6 weights; good for illustration-heavy screens |
| Tabler | MIT | 5,000+ icons; stroke style similar to Lucide |

---

## Sources

- EventCalendar (vkurko), repository and docs (MIT, views list): https://github.com/vkurko/calendar
- FullCalendar Premium pricing and licensing: https://fullcalendar.io/premium, https://fullcalendar.io/license
- Schedule-X v4 announcement (drag/resize/resource view moved to Premium): https://schedule-x.dev/blog/schedule-x-v4
- Schedule-X resource scheduler docs: https://schedule-x.dev/docs/calendar/resource-scheduler
- MUI X Scheduler: https://mui.com/x/react-scheduler/ and https://mui.com/blog/introducing-mui-x-scheduler-v9-alpha/
- Scheduler comparisons (vendor-written, read with care): https://rv-grid.com/blog/best-js-scheduler-in-2026, https://bryntum.com/blog/the-best-javascript-calendar-components/, https://dhtmlx.com/blog/top-javascript-scheduler-libraries-building-modern-booking-systems/, https://toolradar.dev/compare/schedule-x-vs-fullcalendar/
- Bryntum vs DHTMLX pricing: https://www.trustradius.com/compare-products/bryntum-vs-dhtmlx
- Syncfusion Community License: https://syncfusion.com/communitylicense
- React Aria date and time pickers: https://react-aria.adobe.com/blog/date-and-time-pickers-for-all, https://react-aria.adobe.com/RangeCalendar/useRangeCalendar
- React date picker comparisons: https://component-depot.com/posts/best-react-datepicker, https://docs.bswen.com/blog/2026-03-22-react-datepicker-library-comparison/
- React component library comparisons: https://www.builder.io/blog/react-component-libraries-2026, https://designrevision.com/blog/best-react-component-libraries
- Radix Colors scale: https://radix-ui.com/colors/docs/palette-composition/understanding-the-scale
- Okabe–Ito palette reference: https://conceptviz.app/blog/scientific-color-palette-for-research-papers-and-posters
- Atkinson Hyperlegible Next: https://fontsource.org/fonts/atkinson-hyperlegible-next/about, https://ophthalmologymanagement.com/news/2025/applied-design-and-braille-institute-partner-to-launch-typeface-for-making-reading-easier/
