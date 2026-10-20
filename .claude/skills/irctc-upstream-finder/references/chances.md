# Judging confirmation chances

Nobody outside IRCTC knows the real cancellation flow, so treat every chance as an estimate. Give
High / Medium / Low with a one-line reason, and never say "pakka" unless the status is AVL.

## Order of preference

1. **AVAILABLE-n**: a confirmed berth if booked now. A small n (< 10) can vanish within hours, so
   say "book soon".
2. **RAC n**: travel is guaranteed on a shared side-lower berth, which often turns into a full
   berth by charting. The lower the n, and the more days left, the better.
3. **WL n**: depends heavily on the quota type (below). A fully waitlisted e-ticket is
   auto-cancelled and refunded at charting, and you can't board on it.
4. **REGRET**: can't book in that quota. Look at another station, class or night.

## What moves a waitlist

| Factor | Effect |
|---|---|
| Quota type | GNWL (origin or big junction) clears best. RLWL (intermediate stations) clears less, because only its own small pool's cancellations feed it. PQWL (pooled) usually clears least. TQWL (Tatkal) clears rarely. |
| WL size vs coaches | The same WL number is very different in a train with 6 × 3A coaches and one with 1 × 3E coach. The plan's `coaches` field (erail composition) tells you how many. Single-coach classes (often 1A and 3E) have tiny quotas. |
| Days left | More days means more cancellations. Within 2–3 days of travel, downgrade a level. |
| Trend | If you have an earlier reading (pre-screen or a previous run), a WL number that fell is good and one that climbed is bad. Mention the direction. |
| Season | Festival weeks (Diwali, Chhath, Holi, summer, wedding season) clear less. |

## Rule of thumb used by the report (`High*` / `Medium*` / `Low*`)

| Quota | High if WL ≤ | Medium if WL ≤ | else |
|---|---|---|---|
| GNWL | 10 | 30 | Low |
| RLWL | 3 | 10 | Low |
| PQWL | 2 | 6 | Low |
| other WL (TQWL, CKWL, plain WL) | – | 3 | Low |

Drop one level when fewer than 3 days are left. Adjust by hand for coach count, season and trend,
and say that you did.

If IRCTC shows its own probability label for a waitlist, report it next to yours. ConfirmTkt's %
from the pre-screen is a third opinion only.

## Tricks beyond "book from an earlier station"

Mention whichever fit, briefly:
- **Train origin.** The fullest GN quota, but the fare rises with distance. Worth it only for a small
  premium; the checklist caps it at ₹500 by default.
- **Another class, same train.** 3E vs 3A, or 2A with a low RLWL when 3A is RAC-heavy.
- **Neighbouring nights.** Often one night is much better (seen: 15 Nov 3A RAC 16 vs 14 Nov RLWL 13).
- **Tatkal / Premium Tatkal.** Opens the day before the ticket date (AC 10:00, non-AC 11:00 IST).
  It's a fallback, not a plan, and needs an Aadhaar-verified account (verify).
- **VIKALP.** When booking a waitlisted ticket, opting into IRCTC's alternate-train scheme can move
  the passenger to another train if one has space.
- **A longer ticket on trains that continue past the destination** (only if the user allows such
  trains): booking to a farther station can draw on a bigger quota. Deboarding early is allowed;
  travelling beyond the booked station is not.

## Booking recipe wording (fill in)

"IRCTC → From **<upstream name (code)>** To **<dest>**, date **<ticket date at upstream>**, class
**<cls>**, quota **General** → passenger page → **Boarding Station: <boarding station>**. You board
at <boarding station> on <actual date/time>. You can't board at <upstream>, and the unused part
isn't refunded."
