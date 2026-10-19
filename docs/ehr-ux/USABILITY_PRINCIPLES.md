# EHR Usability Principles

These are the usability rules every EHR screen we design has to follow, starting with scheduling. Use them in two ways:

1. **While designing:** each principle has a short rule, the reason behind it, and concrete do's and don'ts.
2. **While reviewing:** the checklist in section 5 is the gate a design passes before it goes to build.

The visual system (colors, type, spacing, components, calendar spec) lives in [`UX_GUIDELINES.md`](./UX_GUIDELINES.md). Library choices live in [`LIBRARY_RESEARCH.md`](./LIBRARY_RESEARCH.md).

---

## 1. Who we design for

| Person | Main jobs | What their day is like | What this means for design |
|---|---|---|---|
| Front desk / scheduler | Find the patient, book or reschedule, check patients in, take payments | On the phone while typing. Interrupted every few minutes. Repeats the same 5 tasks hundreds of times a day. | Fast keyboard paths, one search box, no dead ends, state survives interruptions. |
| Medical assistant / nurse | Room patients, take vitals, track waiting times | Moves between rooms. Glances at screens from a distance. | Big, high-contrast status. Information readable at a glance. |
| Clinician | Look at the day, open the chart, document | Very little time. A lot of information to take in. | Dense but well-organized screens, and patient identity always visible. |
| Billing / admin | Insurance, eligibility, reports | Detail-heavy tables | Clear tables, filters, exports |

Shared conditions: shared workstations, mixed eyesight, older monitors, glare, gloves, PHI on screen in public areas, and a high cost for mistakes. A booking for the wrong patient or the wrong time is a safety problem, not just a usability one.

---

## 2. Where these principles come from

We didn't invent these. They merge the following sources:

- **HIMSS EHR Usability Task Force, "Defining and Testing EMR Usability" (2009):** nine principles. These are simplicity, naturalness, consistency, minimizing cognitive load, efficient interactions, forgiveness and feedback, effective use of language, effective information presentation, and preservation of context.
- **NIST NISTIR 7804 (EHR Usability Protocol) and NISTIR 7804-1 (2015):** expert review plus validation testing on realistic tasks, aimed at removing "never events". NIST's safety guidance also says to keep patient-identifying information in a fixed, static part of the screen, to add cues that prevent entry in the wrong chart, and to make outdated items in lists easy to spot.
- **ONC SAFER Guide, Patient Identification (updated):** use two identifiers everywhere. Show a patient photo where available. Display the identifying information clearly and the same way on every surface.
- **Nielsen's 10 usability heuristics:** visibility of system status, match with the real world, user control, consistency, error prevention, recognition over recall, flexibility, minimalist design, error recovery, help.
- **WCAG 2.2 AA:** text contrast 4.5:1, contrast for non-text UI 3:1, color never the only signal (1.4.1), minimum target size 24×24 px (2.5.8), focus not hidden (2.4.11).
- **Alert-fatigue research (AHRQ PSNet and others):** tier alerts by severity, and interrupt only for severe ones.
- **EHR information-density studies:** dense screens are fine when the information is relevant and well grouped. Clutter and fragmentation across tabs are what hurt.
- **Laws of UX:** Jakob's law (people expect your app to work like the apps they already use), Hick's law (more choices means slower decisions), Fitts's law (big, nearby targets are faster), and Miller and chunking (group information into small sets).

---

## 3. The 14 principles

### P1. The patient's identity is never in doubt
**Rule:** Anywhere a patient appears, show at least **two identifiers**: full name (Last, First) and date of birth with age. Add the chart number where space allows. In a patient context, a **patient banner** stays fixed at the top and never scrolls away.

- Do: show "Test, Aarav · 12 y · DOB Mar 14, 2014 · TEST-0001" in search results, on appointment cards (name + age at minimum) and in the booking summary.
- Do: flag likely duplicates (same DOB and phone) **in the list itself**, before the user picks one.
- Do: show a photo or initials avatar in the banner. Studies link banner photos to fewer wrong-patient orders.
- Don't: show the name alone in a confirmation step.
- Don't: let two patients' details appear on screen at once in a way that could be confused, for example a details panel for patient B left open while booking patient A.

### P2. One screen, one job, one primary action
**Rule:** Each view has one main job and **one** filled primary button. Everything else is secondary, ghost, or tucked into a menu.

- Do: "Book appointment" is the only solid accent button while booking.
- Don't: put a progress wizard, a calendar, a form, an AI box, an activity log and a toast all on screen together. V2 did this.

### P3. Familiar over novel (Jakob's law)
**Rule:** Use the calendar, search, and form patterns people already know from Google Calendar, Outlook, and common booking sites. Save novelty for things that are new to the clinic.

- Do: put Today, ‹ › and the date label at top left, and the Day/Week/Month switch at top right.
- Do: mark "now" with a line across the grid and highlight today's date.
- Don't: invent new metaphors for time, or hide the time axis.

### P4. Color means something, or it isn't there
**Rule:** The interface is **neutral** (white/gray) with **one** accent color for interactive elements. Other colors are **semantic only**: appointment status and alerts. Status color is **always** paired with a text label and an icon (WCAG 1.4.1).

- Do: limit semantic hues to five: neutral (booked/done), blue (confirmed), amber (arrived/waiting), green (in room), red (no-show / error).
- Do: make colors that must be told apart differ in **lightness** too, not just hue. Avoid red/green-only distinctions, because about 1 in 12 men has a color-vision deficiency.
- Don't: use 7 rainbow status colors that need a legend (V1/V2), a dark teal header + teal buttons + purple AI box + orange warnings, or gradients.
- Don't: color-code visit types and statuses at the same time. Two color dimensions on one card can't be read.

### P5. Recognition over recall
**Rule:** People shouldn't have to remember anything from one screen to the next. Labels, not legends. Times, not positions.

- Do: write the status in words on the card ("Arrived · 7 min").
- Do: show start and end times on a selected slot.
- Do: show the patient summary next to every step that acts on that patient.
- Don't: "Dashed block = the slot you are booking · red line = now". If you need a legend, the design isn't clear yet.

### P6. Dense, but organized
**Rule:** Clinical users can handle density. They can't handle clutter. Group related things, line them up on one grid, and use whitespace to separate groups, not to pad.

- Do: use an 8 px spacing grid, consistent card padding, one alignment edge per column.
- Do: use progressive disclosure. Show the 5 fields that matter and put "More details" behind a link.
- Do: put related data on one screen instead of spreading it across tabs. Studies favor one organized page over multiple tabs.
- Don't: use borders around everything. Use one surface level plus dividers.

### P7. Preserve context
**Rule:** Never take the user away from what they're looking at in order to do a related task. Details and forms open **beside** the calendar (side panel), not instead of it. Work in progress survives interruptions.

- Do: keep the calendar visible while booking so the chosen slot can be seen in context.
- Do: keep a half-finished booking when the user opens another appointment, and offer "Resume booking Test, Aarav".
- Don't: use full-page navigation for a quick action, or modal dialogs for multi-step work.

### P8. Forgiveness and feedback
**Rule:** Every action shows its result immediately. Anything that can be undone gets **Undo** instead of an "Are you sure?" dialog. Truly destructive, irreversible actions get a specific confirmation that names the patient and the time.

- Do: after booking, show a toast: "Booked Test, Aarav · Today 11:00 AM · Dr. Mann [Undo]".
- Do: validate inline as the user leaves a field, with an error message that says how to fix it ("Use MM/DD/YYYY", not "Invalid").
- Do: show loading states (skeleton, spinner with text: "Checking eligibility with Riverside Health…").
- Don't: show silent failures or errors only after submit.

### P9. Efficient interactions
**Rule:** The top 5 tasks (find patient, book, reschedule, check in, cancel) take the **fewest possible steps** and have keyboard paths. Defaults do the work.

- Do: one search box that accepts name, DOB, phone, or chart number. `/` focuses it.
- Do: smart defaults (visit length from visit type, usual provider pre-selected, next available slots suggested).
- Do: big targets near where the eye already is (Fitts). The primary action sits at the end of the form, aligned with the reading direction.
- Don't: use a 5-step wizard for something a single, well-ordered form can do. Wizards suit rare, complex tasks. Booking is frequent and expert, so it's neither.

### P10. Speak the clinic's language
**Rule:** Use the words the front desk uses, the same way everywhere. No database names, no abbreviations unless universal.

- Do: use a fixed vocabulary: Booked, Confirmed, Arrived, In room, Checked out, No-show, Cancelled.
- Do: names as "Last, First". Dates as "Mon, Oct 19" in lists and "Oct 19, 2026" when the year matters. Times in one format across the product ("10:15 AM" for US clinics; see guidelines §9.2).
- Don't: "checked_in", "appt", "Pt", "DOB*" without a label, or mixed 12/24-hour clocks on one screen (V2 used both).

### P11. Tier alerts by severity
**Rule:** Interrupt only for safety-critical issues. Everything else is inline or passive.

| Tier | Example | Treatment |
|---|---|---|
| Blocking (rare) | Booking would create a duplicate record for the same person | Inline panel that must be resolved before continuing, with a clear choice of two actions |
| Warning | Eligibility not verified; patient has an outstanding balance | Inline amber notice next to the related field. Doesn't block. |
| Info | "Asthma follow-up due this month" | Quiet text or a badge. Never a colored box. |

- Don't: dashed purple "AI · PREVIEW" boxes competing with the real form.

### P12. Accessible by default (WCAG 2.2 AA)
**Rule:** Accessibility isn't a later pass. Every design is checked at the mockup stage.

- Text contrast ≥ 4.5:1 (≥ 3:1 at 24 px+). Control borders, focus rings and icons ≥ 3:1.
- Pointer targets ≥ 24×24 px (WCAG 2.5.8). We use **32 px minimum on dense desktop** and **44 px** for primary actions and on touch devices.
- Everything works with the keyboard. Focus is always visible and never hidden behind sticky headers or panels.
- Works at 200% zoom and with Windows High Contrast. Respects `prefers-reduced-motion`.
- Real `<button>`, `<a>`, `<input>` + `<label>` elements. Icon-only buttons get `aria-label`.

### P13. Time is unambiguous
**Rule:** Dates and times are where scheduling errors happen. Always show weekday + date + time + provider for any booking. Mark today and now.

- Do: "Mon, Oct 19 · 11:00–11:15 AM · Dr. Chantell Mann".
- Do: write relative dates ("Today", "Tomorrow") **together with** the absolute date.
- Do: show the clinic's time zone when the user's device is in a different one.
- Don't: show times like "10:30" without AM/PM in a 12-hour product, or show a slot without its end time.

### P14. Honest, optional AI
**Rule:** Suggestions are labelled, explain themselves in one line, and never act on their own.

- Do: "Suggested · Today 11:00 AM · Dr. Mann (pediatrics, earliest)" as a selectable option among the normal choices.
- Don't: give AI suggestions a different visual system, make them look more important than the user's own choice, or show "PREVIEW" badges in production.

---

## 4. Audit of the current drafts (V1 and V2)

This explains why the current canvas feels messy and confusing. Each issue is mapped to the principle it breaks.

| # | What we see in V1/V2 | Principle broken | Fix in the new directions |
|---|---|---|---|
| 1 | 7 status colors (blue, green, amber, violet, gray, red, strike-through) plus a legend row | P4, P5 | 5 semantic hues, each with an icon and a word on the card. No legend. |
| 2 | Wizard card (5 step tiles + progress bar) above the calendar, plus form, AI box and activity log on one screen | P2, P6 | One booking panel with a progressive single form. The calendar is the only other region. |
| 3 | Dark teal header, teal buttons, purple AI boxes, orange warnings, green success boxes: no single accent | P4 | Neutral shell. One accent for interaction. Color only where it means something. |
| 4 | Left-accent borders on every event block | P4, P6 | Full soft tinted card with a status icon. The color carries status, not decoration. |
| 5 | Mixed time formats (09:52, 10:30 AM, "Mon 10:30") | P10, P13 | One format. Every booking shows weekday, date, start–end, provider. |
| 6 | Free slots are invisible until hover ("+ 08:15" in transparent text) | P5, P12 | A visible "Open" affordance on hover **and** a keyboard path. Suggested slots are listed explicitly. |
| 7 | Day chips duplicate the date navigation; two segmented controls ("Day · providers" / "Week · one provider") | P3, P9 | Standard Today ‹ › date label, a mini-month, and one view switch. |
| 8 | Toasts with no Undo; status changes happen with no recovery path | P8 | Toast with Undo for every reversible action. |
| 9 | "AI · PREVIEW · CALLER ID" dashed purple box | P11, P14 | Quiet inline suggestion that looks like a normal option. |
| 10 | Patient identity shown as name only in several places (calendar blocks, toasts) | P1 | Name + age everywhere. DOB + chart in panels and confirmations. |
| 11 | Small 11–12 px gray labels (#5a6474 at 11 px) | P12 | Minimum 12 px for meta text, 14 px for body, contrast ≥ 4.5:1. |
| 12 | "Fill sample data", "simulated" and debug text in the UI | P10 | Prototype-only helpers live outside the product frame. |

---

## 5. Review checklist (copy this into every design review)

**Identity & safety**
- [ ] Every patient reference shows ≥ 2 identifiers (name + DOB/age, chart # in details)
- [ ] The patient banner is fixed in patient context; there's no way to act on patient A while seeing patient B
- [ ] Duplicate and look-alike records are flagged before selection
- [ ] Bookings always show weekday, date, start–end time, provider, location

**Clarity**
- [ ] One primary (solid accent) button per view
- [ ] Neutral shell, one accent, semantic colors only for status and alerts
- [ ] Every status shows color + icon + word; no legend needed
- [ ] Information is grouped, aligned to one grid, nothing purely decorative
- [ ] Words match the clinic's vocabulary; one time format throughout

**Efficiency**
- [ ] Top 5 tasks are reachable in ≤ 3 interactions from the schedule
- [ ] Single search box finds a patient by name, DOB, phone or chart #
- [ ] Sensible defaults (duration from visit type, usual provider, next available)
- [ ] Keyboard path for the full booking flow; shortcuts documented

**Context & recovery**
- [ ] Details/forms open beside the calendar, not on a new page
- [ ] Work in progress survives switching to another appointment
- [ ] Every reversible action has Undo; irreversible ones name what will be lost
- [ ] Inline validation with fix-it messages; loading, empty and error states designed

**Accessibility**
- [ ] Text ≥ 4.5:1, UI borders and icons ≥ 3:1 (checked, not guessed)
- [ ] Targets ≥ 32 px desktop / 44 px touch; nothing below 24 px
- [ ] Visible focus ring that's never hidden behind sticky elements
- [ ] Works at 200% zoom and 1280 px wide; reflows on tablet
- [ ] Respects reduced motion; no information conveyed only by animation

---

## 6. How we test

1. **Expert review** against this checklist (NIST EUP step 1). Two reviewers, independently.
2. **Task-based usability test** with 5 front-desk users per round (finds about 80% of major issues). Critical tasks:
   - Book a follow-up for a returning patient who calls in (with a look-alike record present)
   - Register a new patient and book the earliest new-patient visit
   - Reschedule a no-show to the earliest slot with the same provider
   - Check in an arrived patient and see who has waited longest
   - Cancel an appointment, then undo it
3. **Measure:** task success, time on task, error count (wrong patient / slot = critical), SUS score (target ≥ 80), and a single ease question per task.
4. **Fix and retest** until no critical errors remain (NIST EUP validation goal).

---

## 7. Sources

- HIMSS EHR Usability Task Force, *Defining and Testing EMR Usability* (2009): https://digital.ahrq.gov/health-it-tools-and-resources/health-it-bibliography/usability/defining-and-testing-emr-usability
- NIST, NISTIR 7804 *Technical Evaluation, Testing and Validation of the Usability of EHRs*: https://nvlpubs.nist.gov/nistpubs/ir/2012/NIST.IR.7804.pdf
- NIST, NISTIR 7804-1 (2015), patient-safety design guidance: https://www.nist.gov/document/nistir7804-1werb100615pdf
- Coverage of NIST wrong-patient design guidance: https://www.healthcareitnews.com/node/463161, https://www.beckershospitalreview.com/healthcare-information-technology/how-to-eliminate-never-events-5-takeaways-about-ehr-design-use-error-and-patient-harm.html
- ONC SAFER Guide, Patient Identification: https://www.healthit.gov/sites/default/files/safer_patient_identification.pdf
- Updated patient identification guidance review (2026): https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12772641/
- Patient photos in the EHR banner and wrong-patient orders: https://pmc.ncbi.nlm.nih.gov/articles/PMC7658731
- Joint Commission Quick Safety 45, HIT and patient identification: https://jointcommission.org/-/media/tjc/newsletters/quick-safety-45-hit-and-patient-id-9-25-18-final.pdf
- AHRQ PSNet, Alert fatigue primer: https://psnet.ahrq.gov/primer/alert-fatigue
- Alert design recommendations (BMC Med Inform Decis Mak, 2018): https://bmcmedinformdecismak.biomedcentral.com/articles/10.1186/s12911-018-0615-9/tables/2
- EHR data and system usability and physicians' cognitive load (2026): https://www.ncbi.nlm.nih.gov/pmc/articles/PMC12864774/
- Dense display in primary care EHRs (AHRQ project): https://digital.ahrq.gov/ahrq-funded-projects/use-dense-display-data-and-information-design-principles-primary-care-health
- W3C, What's new in WCAG 2.2: https://www.w3.org/WAI/standards-guidelines/wcag/new-in-22/
- Microsoft guidance on when to use wizards: https://learn.microsoft.com/en-us/windows/win32/uxguide/win-wizards
- NHS Common User Interface standards (withdrawn, historical reference for banner and date display): https://standards.nhs.uk/published-standards/common-user-interface-standards
