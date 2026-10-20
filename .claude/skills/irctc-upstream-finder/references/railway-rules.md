# Indian Railways rules that matter for the answer

Rules change. Anything marked (verify) should be stated to the user as "check on IRCTC" rather than
as fact.

## Status strings

The format is `booking-status/current-status`. Read the part after `/` and keep the quota prefix
from the first part.

| String | Meaning |
|---|---|
| `AVAILABLE-0034`, `CURR_AVBL-0010` | 34 berths free; booking gives a confirmed berth |
| `RAC 38` | Reservation Against Cancellation. You travel for sure, on a shared side-lower berth until a confirmed passenger cancels |
| `GNWL 12` | General quota waitlist. Usually the best-clearing WL; held at the origin and the stations sharing its quota |
| `RLWL 9` | Remote Location quota (intermediate stations). Clears from that small pool first, so it can be slower |
| `PQWL 6` | Pooled Quota (several intermediate stations share one pool). Usually clears worst |
| `TQWL` / `CKWL` | Tatkal waitlist |
| `REGRET` / `NOT AVAILABLE` | Waitlist full, no booking possible in that quota |

A fully waitlisted e-ticket is auto-cancelled and refunded if it is still WL after charting. You
can't board on it.

## Quota pools and why upstream booking helps

- Each station gets its berths from one quota: GN at the origin and at major stations that share
  it, RL or PQ at many intermediate stations.
- Stations sharing a pool show identical numbers (the report's "Pool" letter). Booking from another
  station in the same pool gains nothing.
- A different, healthier pool upstream (the origin's GN, or a junction like Itarsi sharing Jabalpur's
  GN on 12192) can show RAC/AVL while the traveller's station shows RLWL/REGRET.
- With an upstream ticket, the berth is held from the ticket's from-station. Selecting the
  traveller's station as **Boarding Station during booking** tells the TTE they will join there.

## Ticket date vs boarding time

- The IRCTC journey date is the departure date from the ticket's from-station, not the train's
  start date and not the traveller's boarding time. Example: 12192 leaves Itarsi 15 Nov 21:35 and
  reaches Ganj Basoda 16 Nov 01:05. An Itarsi ticket carries **15 Nov**, a Ganj Basoda ticket
  **16 Nov**.
- "Night of D" (user language) = boarding between evening D and early D+1, so after-midnight trains
  carry D+1 at the boarding station.

## Boarding point

- Choose it on the booking page (Boarding Station dropdown), or change it later from booking
  history. The time limit for later changes has been revised more than once (verify), so recommend
  choosing it at booking.
- After the boarding point is changed, the traveller can't board at the original station. Nothing
  is refunded for the unused part.

## Booking windows (verify close to travel)

- Advance Reservation Period: 60 days excluding the journey date (reduced from 120 on 1 Nov 2024).
- Tatkal opens one day before the journey date (date at the from-station): AC classes 10:00, non-AC
  11:00 IST. IRCTC now requires an Aadhaar-verified account for Tatkal (verify).
- Charts are prepared several hours before departure (recently moved earlier, verify). After the
  first chart, current-availability seats may appear.

## ConfirmTkt prediction %

ConfirmTkt's own model. It changes with the from-station even when the status is identical. Use
it as a tie-breaker and never as a promise.
