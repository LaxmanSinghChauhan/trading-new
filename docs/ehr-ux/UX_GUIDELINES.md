# EHR UX Guidelines

This is the playbook for every EHR screen we design and build from now on. It turns the [usability principles](./USABILITY_PRINCIPLES.md) into concrete decisions: layout, placement, color, type, components, the calendar control, and copy. Library choices are in [LIBRARY_RESEARCH.md](./LIBRARY_RESEARCH.md).

> **North star:** a front-desk person on the phone can find the right patient and book the right slot in under 30 seconds, without looking away from the conversation for more than a glance, and without ever wondering "which patient is this?" or "did that save?"

---

## 1. Layout and placement

### 1.1 App shell

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ TOP BAR 56px  [Logo · Location ▾]     [ Search patients… (/) ]   [?] [🔔] [MH] │
├──────┬───────────────────────────────────────────────────────┬───────────────┤
│ NAV  │ PAGE HEADER 64px                                      │               │
│ 72px │ Title · Today ‹ › Mon, Oct 19   [Day|Week|Month] [Filters] [+ New]    │
│      ├───────────────────────────────────────────────────────┤ CONTEXT PANEL │
│Sched │                                                       │ 400px         │
│Pts   │  MAIN WORK AREA (calendar / list / board)             │ (details or   │
│Check │  fluid, min 640px                                     │  booking form)│
│Msgs  │                                                       │ opens on      │
│Bill  │                                                       │ select, Esc   │
│      │                                                       │ closes        │
└──────┴───────────────────────────────────────────────────────┴───────────────┘
```

- **Top bar (56 px):** identity of the place (logo, location switcher) on the left. The **global patient search** in the center (always available, `/` to focus). Help, notifications and the user menu on the right.
- **Left nav (72 px rail, icon + short label):** Schedule, Patients, Check-in, Messages, Billing, Reports. Can expand to 232 px. Never more than 7 items (Hick's law).
- **Page header (64 px):** what you're looking at (title + date navigation) on the left. How you're looking at it (view switch, filters) in the middle. The **single primary action** on the far right.
- **Context panel (400 px, right):** opens when something is selected (an appointment, a free slot, "New appointment"). It sits **beside** the calendar so context is preserved (P7). Below 1200 px wide it overlays the calendar with a scrim, and on tablet it becomes a bottom sheet.

### 1.2 Placement rules

| What | Where | Why |
|---|---|---|
| Patient identity (banner) | Top of any patient context, fixed | NIST: static place for patient ID |
| Date navigation | Top left of the work area | First thing scanned (F-pattern); matches Google/Outlook |
| View switch (Day / Week / Month / List) | Top right of the work area, before the primary button | Familiar placement, near the content it changes |
| Filters (provider, location, visit type) | Left sidebar or a filter bar directly under the header | Filters sit before the content they filter |
| Primary action | Top right of the page, and at the bottom right of forms | Predictable; end of the reading path (Fitts) |
| Destructive actions (Cancel appt, No-show) | Separated from primary, in a secondary group or menu | Prevents slips |
| Details / forms | Right context panel | Keeps the calendar in view |
| System feedback (toasts) | Bottom left (not over the context panel's actions) | Never hides the primary button or focused element |
| Alerts about the current patient | Inline, right next to the thing they're about | Severity-tiered (P11) |

### 1.3 Grid and breakpoints

- **8 px spacing grid** (4 px for tight internal gaps). Steps: 4, 8, 12, 16, 24, 32, 48.
- Design at **1440 px**, verify at **1280 px** (most common clinic monitor) and **1024 px** (tablet landscape).
- ≥ 1440: nav + work area + 400 px panel side by side.
- 1200–1439: panel 360 px.
- 1024–1199: nav collapses to icons; panel overlays the calendar.
- < 1024: single column; panel is a bottom sheet; the calendar defaults to List view.

---

## 2. Color

### 2.1 Rules

1. **Neutral shell.** Backgrounds, surfaces, borders and text are cool grays.
2. **One accent** (blue) for interactive elements only: primary buttons, links, selected states, focus ring, the "now" line.
3. **Semantic colors only for status and alerts.** Five families, always paired with an **icon and a word**.
4. **Never** color-code two dimensions on one element (for example, status and visit type). Pick one per view. Status is the default; "color by visit type" is an allowed user setting (§6.4). Whichever is not colored is written as text or shown as an icon.
5. Every pairing meets WCAG: text ≥ 4.5:1, borders and icons ≥ 3:1. Values below were measured.
6. Status color appears in **only two forms**:
   - as the **color of a glyph + word** (lists, panels, popovers)
   - as the **solid fill of a calendar block**, which is clickable

   **Never** as a pill, badge or tinted box around a label. Pills make labels look like buttons (see §5.0).

### 2.2 Neutral and accent tokens (light theme)

| Token | Hex | Use | Contrast |
|---|---|---|---|
| `--bg-app` | `#F6F7F9` | Page background | — |
| `--bg-surface` | `#FFFFFF` | Cards, calendar, panels | — |
| `--bg-subtle` | `#F1F3F6` | Hover rows, read-only fields, header strips | — |
| `--border-subtle` | `#E3E7EC` | Dividers, grid lines | decorative |
| `--border` | `#C5CCD3` | Card outlines | decorative |
| `--border-control` | `#7D8796` | Input, checkbox and select borders | 3.6:1 on white ✅ |
| `--text` | `#1B2430` | Primary text | 15.7:1 ✅ |
| `--text-muted` | `#5B6573` | Secondary text, meta | 5.9:1 on white, 5.5:1 on app bg ✅ |
| `--accent` | `#1F5BD1` | Primary button, links, selection, focus, now line | 6.0:1 ✅ (white text on it 6.0:1 ✅) |
| `--accent-hover` | `#1849AB` | Hover / pressed | 8.1:1 ✅ |
| `--accent-subtle` | `#E6EDF9` | Selected row / day background | — |
| `--focus-ring` | `2px solid #1F5BD1, 2px offset` | All focusable elements | ✅ |

### 2.3 Appointment status tokens

| Status (UI label) | Icon (Lucide) | Glyph + text color (lists, panels) | Solid block fill (calendar) | Text on block | Block text contrast |
|---|---|---|---|---|---|
| **Booked** (shown as "Not confirmed" in day-of views) | `circle` (hollow) | `#3B4450` | `#5B6573` | `#FFFFFF` | 5.9:1 |
| **Confirmed** | `circle-check` | `#1A4FB4` | `#2563EB` | `#FFFFFF` | 5.2:1 |
| **Arrived** + minutes waited | `clock` | `#8A4B00` | `#B45309` | `#FFFFFF` | 5.0:1 |
| **In room** | `door-open` | `#0B6448` | `#047857` | `#FFFFFF` | 5.5:1 |
| **Checked out** | `check` | `#5D6672` | `#E2E6EB` (recedes) | `#475160` | 6.4:1 |
| **No-show** | `circle-x` | `#A3261A` | `#B42318` | `#FFFFFF` | 6.6:1 |
| **Cancelled** | `ban` | `#5D6672`, hidden by default ("Show cancelled" filter) | no fill, dashed `#C5CCD3` outline, strikethrough | `#5D6672` | 5.5:1 |

All glyph text colors are ≥ 6:1 on white. Every block shows the **status icon** (and the word when there is room), so color is never the only cue.

**Visit-type palette** (only when the user switches "Color by" to visit type). Ordered for color-blind separation; checked with the data-viz palette validator. CVD separation passes; teal is at the edge of its chroma floor, which is acceptable because every block carries a text label.

| Visit type | Fill | White text contrast |
|---|---|---|
| Follow-up | `#2563EB` | 5.2:1 |
| Sick visit | `#C2410C` | 5.2:1 |
| Well-child | `#007A87` | 5.1:1 |
| Sports physical | `#BE185D` | 6.0:1 |
| New patient | `#7C3AED` | 5.7:1 |

Why these five hues: neutral, blue, amber, green and red differ in **lightness** as well as hue, and amber/blue is the color-blind-safe pair for the two statuses that need action (Confirmed vs Arrived). Red (No-show) always comes with its icon and the word.

### 2.4 Alert tokens

| Tier | Treatment | Text | Icon |
|---|---|---|---|
| Error / blocking | The only alert that gets a box: `#FDECEA` background, `#D9534A` border, with the two resolving actions inside it | `#A3261A` | `octagon-alert` |
| Warning | Icon + text line next to the thing it's about. No box. | `#8A4B00` | `triangle-alert` |
| Info | Plain text | `#5B6573` | `info` |
| Success | Toast only (dark `#1B2430` surface, white text) | | `check` |

### 2.5 Dark theme (for dim front desks and night shifts)

| Token | Hex |
|---|---|
| `--bg-app` | `#0E141C` |
| `--bg-surface` | `#121821` |
| `--bg-subtle` | `#1A2230` |
| `--border` | `#2A3546` |
| `--text` | `#E8EDF3` (15.1:1) |
| `--text-muted` | `#9AA6B5` (7.2:1) |
| `--accent` | `#7AA7FF` (7.5:1; dark text on it) |

Status tints in dark mode: same hues at roughly 18% opacity over the surface, with text from the 300–200 range of the same hue. Always re-check contrast.

---

## 3. Typography

| Role | Family | Why |
|---|---|---|
| UI and body | **Atkinson Hyperlegible Next** (Google Fonts, OFL) | Designed by the Braille Institute so commonly confused characters (I/l/1, 0/O, rn/m) are distinct. That matters for names, IDs and doses. 7 weights. |
| Times, IDs, chart numbers | **Atkinson Hyperlegible Mono** | Fixed-width so times and IDs align in columns |
| Fallback | `system-ui, -apple-system, "Segoe UI", sans-serif` | Close metrics while loading |

Alternatives if a brand requires: Public Sans (USWDS), Source Sans 3, IBM Plex Sans. Avoid Inter/Roboto/Arial defaults.

**Scale** (px / line-height / weight):

| Token | Size | LH | Weight | Use |
|---|---|---|---|---|
| `display` | 28 | 36 | 600 | Rare: empty states, dashboards |
| `title-1` | 22 | 28 | 600 | Page title |
| `title-2` | 18 | 24 | 600 | Panel and section titles |
| `title-3` | 16 | 22 | 600 | Card titles, patient name in banner |
| `body` | 14 | 20 | 400 | Default UI text, form inputs (16 on touch) |
| `body-strong` | 14 | 20 | 600 | Patient names in lists |
| `meta` | 13 | 18 | 400 | Secondary info, table cells in dense mode |
| `caption` | 12 | 16 | 500 | Minimum size: labels, captions. Never smaller. |

Rules: sentence case everywhere (no ALL CAPS except 2–3 letter abbreviations). Maximum 2 weights on one card. Numbers in tables right-aligned or monospaced.

---

## 4. Shape, elevation, icons, motion

- **Radius:** 6–8 px controls and calendar blocks, 10–16 px cards and panels, 999 px only for toggle switches and avatars. One radius per element type.
- **Borders over shadows.** Cards use a 1 px `--border`. Shadows only for floating layers (popover, menu, overlay panel): `0 8px 24px rgba(16,24,40,.12)`.
- **No** gradients, glassmorphism, left-accent stripes, or emoji.
- **Icons:** Lucide, 1.75 px stroke, 16 px in dense UI and 20 px in nav. An icon alone is allowed only for universally known actions (close, search, more); everything else gets a text label. Icon-only buttons need `aria-label` and a tooltip.
- **Motion:** 150 ms for hover and focus, 200 ms for panels sliding in, `ease-out`. Never animate the calendar grid itself. Respect `prefers-reduced-motion` (switch to instant).

---

## 5. Components

### 5.0 Clickable vs. read-only (the most important rule in this section)

If something looks like a button, people will click it. If a real button looks like a label, people will miss it. NN/g's eye-tracking study found weak or ambiguous clickability signals made people about 22% slower, with more eye fixations ("click uncertainty"). Every element falls into exactly one row of this table:

| Element is… | It looks like… | Never looks like… |
|---|---|---|
| **An action** (Book, Check in, Room) | A **button**: rectangle, 6–8 px radius, solid accent (primary) or 1.5 px outline (secondary), verb label | — |
| **Navigation / "change"** | A **link**: accent text, underlined | A button, unless it's a primary task |
| **An editable value** | A **field**: box with a visible border and label above; or, in sentence-style forms, dashed underline + chevron | Plain text |
| **A choice** (visit type, provider) | **Radio rows**, a **select**, or a **segmented control** (joined segments) | Floating pills or chips |
| **Status / metadata** (Confirmed, 12 y, Eligible) | **Plain text**, optionally with a small colored glyph | A pill, badge, border or tinted box |
| **A calendar appointment** | A **solid block** (it opens details) | — |
| **A draggable item** | Raised card with a grip handle (⋮⋮) + shadow | Flat text |
| **Disabled** | The same control at reduced contrast, with a reason nearby | Hidden |

Checks:
- [ ] No rounded pill shape anywhere except on toggle switches.
- [ ] Squint test: everything that looks raised, filled or outlined can be clicked; nothing else is.
- [ ] Every link is underlined or sits in an obvious nav bar.

### 5.1 Buttons

| Variant | Use | Style |
|---|---|---|
| Primary | **One per view** | Solid `--accent`, white text, 600 weight |
| Secondary | Other actions | White, `--border-control` 1 px, `--text` |
| Ghost | Low-emphasis, toolbars | No border, `--text-muted`, hover `--bg-subtle` |
| Danger | Destructive, in its own group | White with red text `#A3261A`, red border; solid red only inside a confirmation |

Sizes: **32 px** (dense toolbar), **40 px** (default), **44 px** (primary on forms and all touch). Label is a verb + noun ("Book appointment", "Check in"), never "OK" or "Submit".

### 5.2 Inputs

- The label sits **above** the field, always visible (no placeholder-as-label).
- Format hint in the label or helper ("MM/DD/YYYY").
- Errors appear below the field in red with an icon, plus a red border. The message says how to fix it.
- Required fields: mark the **optional** ones ("Email (optional)"). Most fields are required, so this is less noise.
- Height 40 px (44 px touch). Border `--border-control`. Focus ring `--accent`.

### 5.3 Patient search (combobox)

- One box: "Search name, DOB, phone or chart #". `/` focuses it from anywhere.
- Results show: **Last, First** (bold) · age + sex · DOB · phone · chart # (mono) · next appointment. Matching characters highlighted.
- Look-alike records (same DOB + similar name, or same phone) show a `⚠ Possible duplicate` warning (icon + text, no chip) **in the result row**.
- Keyboard: ↑ ↓ to move, Enter to select, Esc to clear. The last item is always "+ Register new patient '<typed text>'".

### 5.4 Patient banner

`[Avatar/photo] Test, Aarav  ·  12 y M  ·  DOB Mar 14, 2014  ·  TEST-0001  |  Riverside Health PPO ✓ eligible  |  Allergies: none known  |  ⚠ 1 no-show this year`

- Fixed at the top of the patient context (panel or page). Name 16 px / 600.
- Two identifiers minimum (name + DOB) on **every** surface that acts on a patient.

### 5.5 Status text (not a badge)

Icon + word in the status text color from §2.3, with **no** background, border or pill shape (§5.0). Example: `◷ Arrived · 7 min` in `#8A4B00`. After 15 minutes the waiting time turns bold and the icon becomes filled. It stays the same color.

### 5.6 Context panel

Header: title + patient banner (if any) + close (×, Esc). Body scrolls. **Footer is sticky** with the primary action at the right and secondary at the left. Width 400 px. Opening it never moves the calendar's scroll position.

### 5.7 Toasts

Bottom left, dark surface, max 1 line + one action. Every reversible action gets **Undo** (8 s). Toasts never hold the only copy of important info; it's also in the activity history.

### 5.8 Dialogs

Only for destructive, irreversible actions or blocking decisions. The title names the object: "Cancel Test, Aarav's appointment on Mon, Oct 19 at 11:00 AM?". Buttons are "Keep appointment" (secondary) and "Cancel appointment" (danger). Never "Yes/No".

### 5.9 Empty, loading, error states

- Empty: one sentence + one action ("No appointments for Dr. Mann today. [Book appointment]").
- Loading: skeletons in the shape of the content; spinners only with a sentence ("Checking eligibility with Riverside Health…").
- Error: what happened + what to do + retry ("Couldn't reach the payer. Eligibility will be checked again at check-in. [Retry now]").

---

## 6. Calendar control (scheduling)

This is the most important control in the product.

### 6.1 Views

| View | Columns / rows | Default for | Notes |
|---|---|---|---|
| **Day · by provider** (resource time grid) | One column per provider, time down the side | Front desk | The default. Up to ~6 providers before horizontal scroll; then use filters. |
| **Week · one provider** | 5 or 7 day columns | Booking follow-ups for a specific provider | Provider picker in the header |
| **Timeline** (resource timeline) | Providers as rows, time across | Large clinics (> 6 providers), rooms and equipment | Best for spotting gaps across many resources |
| **List / agenda** | Rows grouped by time | Phones, tablets, check-in, accessibility | Always available; the screen-reader-first view |
| **Month** | Day cells with counts | Overview only | Never used for booking individual slots |

### 6.2 Header (left to right)

`[Today] [‹] [›]  Mon, Oct 19, 2026 ▾      [Day | Week | Timeline | List]   [Providers ▾] [Visit type ▾]   [+ New appointment]`

- **Today** is a button that is disabled when you're on today.
- The date label is a button that opens the mini month (§7).
- Keyboard: `T` today, `←`/`→` previous/next period, `D`/`W`/`L` views. Single-key shortcuts only work when focus isn't in a text field, and they can be turned off in settings (WCAG 2.1.4).

### 6.3 Time grid

- Visible hours: clinic open hours (for example 8 AM–6 PM). Scrolls to **now − 1 h** on load for today.
- **Slot size 15 min, row height 24 px** (dense) or **32 px** (comfortable, default). Hour lines `--border` and half-hour lines `--border-subtle`. No line at 15 and 45.
- Hour labels: "8 AM", "9 AM"… in `caption`, top-aligned with the hour line, in a 56 px gutter.
- **Now line:** 2 px `--accent` across today's columns with a "10:05 AM" time label in the gutter.
- **Today** column header: date in an accent-filled circle. Weekend and closed days get a `--bg-subtle` background.
- **Blocked time** (lunch, admin, out of office): diagonal hatch in `--bg-subtle` with a label ("Lunch"). It can't be booked unless overridden with a reason.
- Provider column header: name (600) + specialty (meta) + day summary ("8 booked · 3 open").

### 6.4 Appointment block anatomy

These are **solid blocks with white text**, in the style of Bryntum and DevExpress schedulers. The block is clickable, so it may look like an object; the grid around it stays quiet.

```
█████████████████████████████████████
█ Chen, Wei, 57        ◷ Arrived 7m █  ← name (700) + age = 2 identifiers · status icon + word
█ Annual physical                   █  ← visit type
█ 10:00–10:30 AM                    █  ← time range
█████████████████████████████████████
```

- Fill from the status "solid block fill" column (§2.3), or from the visit-type palette when the user picks "Color by: Visit type". Text is white (≥ 5:1).
- No borders, no left stripe. 6–7 px radius, 1 px soft shadow, 3–5 px gap from the column edges.
- Checked-out blocks use the light fill and dark text, so the past recedes.
- 15-min appointments show a single line: `10:15 Dubois, Henri · 7 y  ◷ Arrived`. The full content is in the tooltip and the panel.
- Selected block: 2–3 px outline + 2 px offset. Never a different fill.
- Checked out cards are de-emphasized (muted) so the eye skips the past.
- Overlapping appointments split the column width side by side (max 3), then show a "+2 more" link.

### 6.5 Free slots and booking

- Hovering an empty 15-min cell shows a ghost "+ 11:00 AM". Clicking (or Enter on a focused cell) opens the booking panel **pre-filled with that provider and time**.
- While booking, the chosen slot shows as a **dashed accent outline with the patient's name** in the grid, so the user sees exactly what they're about to create.
- Suggested slots (from the booking panel) are highlighted in the grid with a subtle accent tint when the panel is open.
- **Drag to move / resize** is allowed for staff with permission. On drop, a toast says what changed with **Undo**. Moving to a different day or provider asks for confirmation inline.

### 6.6 Patterns borrowed from Bryntum and DevExpress (reference screens)

- **Quiet grid, loud events:** light gridlines, white background, and the solid event blocks carry the color.
- **Big day numbers** in column headers ("Mon" small, "19" at 32–40 px); today in the accent color.
- **Resource rows/columns with an avatar** (photo or initials) + name + a count ("8 visits · 3 open").
- **Hatched fill** for time that can't be booked (lunch, out of office, closed days), labelled in words.
- **Totals row** at the bottom (open/booked per provider or per day).
- **Selection box + small popover:** selecting free time or an appointment shows an outlined box and a popover with the key facts and one primary action, so the user never leaves the grid.
- **Toolbar:** ‹ › and the date range on the left, then a filter field, joined provider toggles and a view dropdown; primary action at the far right.

### 6.7 Accessibility of the grid

- The grid is a `role="grid"` with each slot a gridcell. Arrow keys move by slot and provider, Enter opens, Esc closes.
- Each appointment button has a full accessible name: "Chen, Wei, 57 years, Annual physical, 10:00 to 10:30 AM, Dr. Beatty, Arrived, waiting 7 minutes".
- The **List view** is always one click away as the linear, screen-reader-friendly equivalent.

---

## 7. Date and time pickers

- **Typed input first, calendar second.** A segmented date field (MM / DD / YYYY) that accepts typing, plus a calendar button that opens a month popover. This is the React Aria DatePicker pattern.
- Quick picks above the month grid: **Today · Tomorrow · +1 week · +2 weeks · +1 month · +3 months** (follow-ups are usually "come back in 2 weeks").
- In the month popover: today has an outlined circle, the selection is filled accent, days with no availability are muted with a strikethrough, and each day can show availability as a small dot ("3 open").
- Birth dates: **never** a calendar popover. Use three typed segments (month, day, year), because scrolling back 70 years is unusable.
- Time selection is done on the calendar or from suggested time buttons. Avoid free-typed times for booking.

---

## 8. Booking flow (replaces the 5-step wizard)

One context panel, **one progressive form**, four sections. A completed section collapses to a one-line summary with "Change".

1. **Patient:** search box → select → collapses to the patient banner. A duplicate warning appears inline if relevant. "Register new patient" expands a short form (name, DOB, sex, phone; everything else later at check-in).
2. **Visit:** visit type as radio rows or a select, with durations ("Follow-up · 15 min"). Never chips (§5.0). Reason (optional). Provider (defaults to the usual one).
3. **Time:** 3 suggested times as radio rows, each with a one-line reason ("Earliest · Dr. Mann"), + "or choose on the calendar". The chosen time appears as a dashed outline in the grid.
4. **Insurance:** pre-filled from the record; eligibility runs automatically. The status shows inline (✓ Eligible · copay $25 / ⚠ Not verified, can continue).

**Sticky footer:** a one-line summary ("Test, Aarav · Mon, Oct 19 · 11:00–11:15 AM · Dr. Mann") + `[Book appointment]`.
After booking: toast with Undo + the panel shows the confirmation with "Send confirmation text" and "Book another".

---

## 9. Content and microcopy

### 9.1 Vocabulary (use exactly these words)

| Concept | Say | Don't say |
|---|---|---|
| Appointment states | Booked, Confirmed, Arrived, In room, Checked out, No-show, Cancelled | Scheduled/Pending, Checked-in, checked_in, Roomed, Complete, NS |
| Person | Patient | Pt, Client, Member (except insurance) |
| Practitioner | Provider (in UI), Dr. Lastname / Firstname Lastname, NP | Doc, Resource |
| Visit kind | Visit type | Appt type, Encounter type |

### 9.2 Formats (US clinic)

| Thing | Format | Example |
|---|---|---|
| Patient name | Last, First | Test, Aarav |
| Age | number + y (m for < 2 y) | 12 y · 18 m |
| Date in UI | Weekday, Mon D | Mon, Oct 19 |
| Date with year | Mon D, YYYY | Oct 19, 2026 |
| Date input | MM/DD/YYYY (segmented) | 03/14/2014 |
| Time | h:mm AM/PM | 10:15 AM |
| Range | start–end AM/PM | 10:00–10:30 AM |
| Relative | Relative + absolute | Today, Oct 19 · Tomorrow, Oct 20 |
| Duration | number + min / h | 15 min · 1 h 30 min |
| Phone | (555) 014-2000 style per locale | |
| IDs | Monospace | TEST-0001 |

### 9.3 Tone

Short, specific, calm. Say what happened and what to do next. No exclamation marks, no blame ("Couldn't verify eligibility", not "You entered an invalid member ID").

---

## 10. Keyboard shortcuts (front desk)

| Key | Action |
|---|---|
| `/` | Search patients |
| `N` | New appointment |
| `T` | Go to today |
| `←` / `→` | Previous / next day or week |
| `D` `W` `L` | Day / Week / List view |
| Arrow keys in grid | Move between slots |
| `Enter` | Open selected appointment / book focused slot |
| `Esc` | Close panel / clear search |
| `?` | Show all shortcuts |

---

## 11. Accessibility checklist (WCAG 2.2 AA)

- Contrast: text ≥ 4.5:1, large text ≥ 3:1, UI components and focus ≥ 3:1.
- Color is never the only signal (status = color + icon + word).
- Targets ≥ 24 px (we use 32 px minimum, 44 px for primary and touch).
- A visible focus ring on everything; never hidden by sticky headers or panels (2.4.11).
- Full keyboard operation; logical tab order (header → filters → grid → panel).
- Landmarks: `header`, `nav`, `main`, `aside` (panel), `role="status"` for toasts.
- Reflow at 320 CSS px for lists and forms; the calendar falls back to List view.
- Reduced motion respected; no time limits except toasts (Undo also available in history).

---

## 12. Design QA before handoff

- [ ] Passes the [usability checklist](./USABILITY_PRINCIPLES.md#5-review-checklist-copy-this-into-every-design-review)
- [ ] Uses only the tokens in this file (no new hex values)
- [ ] One primary button per view
- [ ] All states drawn: default, hover, focus, selected, disabled, loading, empty, error
- [ ] Checked at 1440, 1280, 1024 px and dark theme
- [ ] Real copy, no lorem ipsum; formats match §9.2
- [ ] Keyboard path written down for each task
