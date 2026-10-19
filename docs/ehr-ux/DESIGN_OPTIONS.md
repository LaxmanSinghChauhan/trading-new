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
