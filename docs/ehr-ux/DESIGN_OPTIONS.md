# Scheduling: Seven Design Directions

Canvas: https://claude.ai/artifact/4Sb6XJvvcTLzj1k3NJiqTu (row "Seven directions · pick one"; the V1/V2 drafts are kept above it for comparison).

All seven follow [USABILITY_PRINCIPLES.md](./USABILITY_PRINCIPLES.md) and [UX_GUIDELINES.md](./UX_GUIDELINES.md):

- same status vocabulary, with icon + word on every status
- two patient identifiers everywhere
- one primary button per view
- neutral shell with one accent color

They differ in **layout model**, which is the decision to make. Every option has an accent-color tweak on the canvas, so color can be changed independently of the layout.

All of them use the same scenario: Monday, Oct 19 at 10:05 AM. Front desk is rebooking Test, Aarav (12 y), who missed his 9:15 AM asthma follow-up with Dr. Mann.

| Option | Layout model | Best for | Main trade-off |
|---|---|---|---|
| **A · Calendar Classic** | Mini-month and provider filters on the left, day-by-provider calendar in the middle, booking panel on the right | Front desks used to Google/Outlook; booking while seeing the whole day | Three regions side by side need ≥ 1280 px; the sidebar hides on smaller screens |
| **B · Agenda + Booking Composer** | Time-ordered list of the day (with open times inline) + a phone-call booking form showing next-available time chips | Phone-heavy scheduling; screen readers; tablets | No visual time grid, so gaps between appointments are less obvious |
| **C · Slot Finder** | "Find a time" search bar (patient, visit, provider, dates) → best matches + provider × day availability matrix | Finding the right slot across many days and providers quickly | Not a view of the day; needs a separate schedule screen for check-in work |
| **D · Resource Timeline** | Providers *and rooms* as rows, time across; details drawer below | Bigger clinics, rooms and equipment, spotting gaps across many resources | 15-min visits get narrow; needs horizontal scroll for a full day |
| **E · Night Mode, Keyboard-first** | Dark theme, waiting-room queue on the left, week view, command palette (Ctrl K) for "book aarav asthma tue" | Power users, dim front desks, evening shifts | Learning curve; the command palette must be discoverable |
| **F · Patient-centric** | Patient banner + care due + visit history, then a "Book a visit" week picker with time buttons | Booking from the chart; follow-ups driven by care gaps | Two clicks further from the clinic-wide day view |
| **G · Today Flow Board** | Status lanes: Coming up → Arrived → In room → Done, with wait timers and open times for walk-ins | Day-of operations: check-in, rooming, waits | Not a booking tool on its own; pairs with A, B or C |

**Combining options:** these are meant to be mixed. A common product shape is **A (or B) for Schedule + G for Today's flow + F inside the patient chart**, with E's dark theme and command palette available as settings.

---

## Round 2: new metaphors + reviewer references (H–N)

These are on the canvas page **"Round 2 · New directions"**; Round 1 moved to its own page.

The reviewer's feedback on Round 1:

- "Still basic and similar to V1/V2"
- "Everything looks like a button, even labels"

So Round 2 follows the new clickability rule (P15 in USABILITY_PRINCIPLES.md, §5.0 in UX_GUIDELINES.md): only actions look like buttons, status is a glyph + word, and choices are radio rows, selects or segmented controls. L–N take their visual language from the reviewer's references: the Bryntum timeline and the DevExpress week view.

| Option | Metaphor | Best for | Main trade-off |
|---|---|---|---|
| **H · Day Sheet** | Printed appointment book: serif date masthead, ruled time lines, entries written in, "pencilled in" tentative booking, booking slip on the side | Calm, very readable day view; staff who think in paper books | Less visual punch; no block shapes to scan |
| **I · Sentence Booker** | Booking is one editable sentence: "Book *Test, Aarav* for an *asthma follow-up* with *Dr. Mann* *today at 11:00 AM*", with a day ruler for picking the time | Fast phone booking, new staff, zero training | One booking at a time; needs a separate schedule view. Mad-libs forms showed a 25–40% lift in one older A/B test, so test it with our users |
| **J · Arrivals Board** | Airport/railway board: dark, large type, Needs attention → Here now → Next hour → Later | Glanceable from across the room; wall display for MAs; check-in desk | Not for booking ahead; dark theme everywhere may not suit all clinics |
| **K · Clinic Floor Plan** | Live map of the clinic: exam rooms with occupant and timer, waiting room with draggable patient tokens, arrivals list | Rooming and patient flow; sees room cleaning state | Each clinic needs its plan configured; not a scheduling tool on its own |
| **L · Capacity Timeline** *(Bryntum-inspired)* | Providers and rooms as rows with avatars, 14 days across, open-slot counts per cell, solid bars for absences and reserved blocks, totals row, selection + popover with times | Booking follow-ups weeks ahead; seeing capacity and absences at a glance | Day-level only; you open a day to see individual appointments |
| **M · Week Planner** *(DevExpress-inspired)* | Quiet week grid, big day numbers, solid colored blocks with white text, provider toggles, booking popover on the selected slot; tweak to color by status or by visit type | Booking for one provider across a week | One or two providers at a time; many providers get crowded |
| **N · Day Scheduler** *(both references)* | Day view by provider with avatar headers, solid status blocks, hatched lunch and admin time, now line, totals row, and an appointment popover with the next action ("Room patient · Exam 1 is ready") | Front desk "home" screen: see the day and act on it | Dense on small screens; needs the List view as its mobile fallback |

**Suggested combination:** **N** as the Schedule home, **M** for booking within a provider's week, **L** for long-range capacity, plus **J** or **K** as the day-of flow screen. They share the same tokens, so they can live in one product.

### From the latest references (O–P)

| Option | Built from | Best for | Main trade-off |
|---|---|---|---|
| **O · Front Desk Dashboard** | Booking-dashboard reference: warm orange accent, soft rounded cards, tab nav, donut of today's appointments (still to come / here now / checked out), "waiting longest" callout, sortable appointments table with avatars, rooms panel with soft blocks | A morning-to-evening home screen for the front desk lead | The orange accent sits close to the amber "Arrived" color. If chosen, move Arrived to a different hue or tone the accent down. A donut is an at-a-glance summary only; numbers are always printed beside it |
| **P · Work Week** | Syncfusion reference: Today button, ‹ › date range, provider dropdown, view dropdown (Day / Week / Work Week / Month / Timeline), "All day" row, light grid, soft pastel blocks with stripe + visit-type icon, repeating lunch, red now line | Booking within one provider's week with many short visits | Colors show visit type, so status is only a corner icon; less glanceable for day-of status work (pair with N or J) |
