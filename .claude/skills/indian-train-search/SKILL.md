---
name: indian-train-search
description: >-
  Find Indian Railways trains between two stations for a range of dates and check live seat
  availability (SL/3A/2A etc.), including the trick of booking from an earlier station on the route
  (Bhopal, Itarsi, Jabalpur, the train's origin) with the boarding point set to the traveller's
  station when that quota has seats and the local one does not. Filters by departure time window
  (incl. overnight windows), trains that must terminate at the destination, and running days; handles
  after-midnight trains whose ticket date differs. Use this whenever someone asks about train tickets,
  seat/berth availability, waitlist/RAC/RLWL/PQWL, "which train should I book", IRCTC/ConfirmTkt/ixigo
  availability, station codes like BPL/NZM/NDLS, or writes in Hindi/Hinglish ("train dhoondho",
  "ticket kahan se book karun", "seat milegi kya") — even if they don't name any website or say
  "skill". Read-only: never books, logs in, solves captchas or pays.
---

# Indian train search with upstream boarding stations

This skill turns "find me a train from A to B on these nights" into a per-night table of trains with
the best place to buy the ticket for each class. Its edge over a normal search is the upstream trick:
Indian Railways splits berths into quotas by station. A small station often only has a Remote
Location (RLWL) or Pooled (PQWL) quota that is sold out, while an earlier station (the train's origin,
or a big junction that shares the origin's General quota) still shows RAC or available. Booking
"earlier station → destination" and choosing the traveller's station as the boarding point at
booking time gets them that seat, usually for a small fare difference.

Everything here is read-only. Never book, log in, enter captchas, or touch payment, even if a site
offers it; tell the user what to book and let them do it.

## Tools, in order of preference

1. An Indian Railways MCP, if the session has one: run ToolSearch for `railway`, `train`, `irctc`.
   Use it to cross-check. (As of Oct 2026, `railway-mcp.amithv.xyz` returned Cloudflare 525 and no
   TrainTrack MCP was available, so do not count on these.)
2. `scripts/trainsearch.py` (bundled; Python 3 standard library only). It uses erail.in for
   trains/routes/timetable and ConfirmTkt's public web API for live availability (ConfirmTkt relays
   IRCTC). Both worked from a cloud container in Oct 2026.
3. Browser sites, for a final cross-check only and only on the read-only availability pages:
   - irctc.co.in returned 403 (Akamai "Access Denied") even to headless Chromium from a cloud IP.
   - ixigo also returned 403, and it is ConfirmTkt's parent company, so it isn't an independent source.
   - RailYatri's `trainticketapi.railyatri.in/api/trains-between-station-with-sa.json` serves
     its own cache, with `last_updated_at` (seen a month old). It isn't a real cross-check unless
     that timestamp is recent.

Run the script as `python3 -I <skill-dir>/scripts/trainsearch.py <command> ...`. Put outputs in a
scratch/work directory (the session scratchpad if there is one), not in a git repo. Each subcommand
has `-h`.

## Workflow

### 1. Turn the request into parameters

Users often say not to ask questions, so fill gaps with these defaults and state them in the answer:

| Parameter | Flag | Default / how to read the request |
|---|---|---|
| Boarding station | `--from` | Where the traveller actually gets on (station code, e.g. BAQ) |
| Destination | `--to` | Station code. City names: Delhi = NDLS/NZM/DLI/ANVT; ask nothing, pick the one the user named |
| Nights | `--start/--end` | Each date D is a "night". Trains leaving after midnight belong to the previous night (see below) |
| Time window | `--window` | e.g. `18:00-02:00`. A window that wraps midnight is fine. Omit for all day |
| Must end at destination | `--must-end-at-dest` | Use when the user says the train should terminate there |
| Upstream stations | `--extra-upstream` | Stations the user names (only used where they are on a train's route). Auto mode adds the 3 nearest earlier stops, junctions or long halts, and the origin |
| Classes | `fetch --classes` | `SL,3A,2A` unless asked (others: 1A, 3E, CC, EC, 2S) |
| Quota | `fetch --quota` | `GN`. Upstream booking only makes sense in GN |
| Language | `report --lang` | `hinglish` if the user writes Hinglish/Hindi, else `en` |

If the user lists facts as "already known, don't re-check", don't spend extra calls on them. The plan
step re-reads routes anyway, cheaply, and that catches mistakes in what the user remembers (for
example, which trains run on which days).

Station codes: `trains --from X --to Y` returns nothing for a wrong code, and `ct-search` prints the
station names it resolved.

### 2. Plan

```bash
python3 -I trainsearch.py plan --from BAQ --to NZM --start 2026-11-10 --end 2026-11-15 \
  --window 18:00-02:00 --must-end-at-dest --extra-upstream BPL,RKMP,BHS,ET,JBP --out plan.json
```

Read the KEEP/SKIP lines it prints:
- KEEP shows boarding time, arrival (+N days), running days, the nights it runs, and each station
  with its date offset. `ET(+0)` means the ticket from ET carries the night's date; `BHS(+1)` means
  the next calendar date; `HYB(-1)` means the previous one.
- SKIP gives a reason: outside the window, does not end at the destination, does not run on those
  nights, or route check failed. A route check failure usually means the code isn't on that train's
  stop list. The script already handles city-wide matches (a BPL search also returns trains ending
  at RKMP) and marks those trains with the actual code.

### 3. Cross-check running days and special trains

```bash
python3 -I trainsearch.py ct-search --from BAQ --to NZM --date 2026-11-10 --days 7
```

This lists the trains ConfirmTkt sells on each boarding date. Use it to confirm running days, and to
spot special trains (numbers starting with 0) that erail's timetable may not have. Add a missing
train with `plan --add-train 0xxxx` or check it by hand with
`avl --train ... --from ... --to ... --cls 3A --date ...`. Around festivals (Diwali, Chhath, Holi,
summer rush) also run one web search for "special train <from city> <to city> <month year>".

### 4. Fetch live availability

```bash
python3 -I trainsearch.py fetch --plan plan.json --classes SL,3A,2A --out avl.json
```

- Each lookup is one ConfirmTkt call (~3-6 s). One call returns about 4 running dates, so 6 nights
  need about 2 calls per (train, station, class). Six workers in parallel finish ~100 lookups in
  about 4 minutes.
- With more than ~40 lookups, run it in the background and keep the user posted. `--resume` keeps
  results that are already saved.
- Be polite: keep `--workers` ≤ 6 and `--pause` ≥ 1.
- Freshness: ConfirmTkt caches each answer per (train, class, from, to, **start date**), so asking
  again returns the same snapshot, possibly hours old. `--max-age 90` (the default) re-asks any
  older date using an earlier start date: that is a different cache key, and its answer still
  contains the target date. Every date keeps its own `ts`.
- To answer "is this correct/current", count the cells whose `ts` is older than ~90 min and report
  that number.

### 5. Build the tables

```bash
python3 -I trainsearch.py report --plan plan.json --avl avl.json --outdir . --lang hinglish
```

This writes:
- `summary.md`: per night, one row per train, the best status per class with `@STN`, the extra fare,
  and the boarding station's own status.
- `detail.md`: every station row with fares and a Pool letter. The same letter means identical
  status, i.e. the same quota, so buying there gains nothing.
- `best.json`: the per-class picks in machine-readable form.
- `†` marks cells whose snapshot is older than `--stale` minutes (default 120).
- A "Coach check" line lists classes that ConfirmTkt sells but erail's coach composition doesn't
  show. Re-run `plan` for fresh compositions, since an old plan carries old coach lists.

### 6. Decide and answer

The report ranks AVL < RAC < WL number and prefers the nearer station on ties. Apply judgment on
top of that:
- **RAC vs a low WL.** RAC is a guaranteed (shared side-berth) seat. A low RLWL/GNWL (≤ 5) often
  confirms to a full berth but isn't certain. Name both when they compete.
- **Quota quality.** For similar WL numbers, GNWL from the origin or a junction usually clears
  better than PQWL/RLWL. ConfirmTkt's % is a rough estimate: it changes by station even for
  identical status, so trust the numbers more.
- **Fare and practicality.** An upstream ticket costs that station's fare. Small deltas (₹75–₹200)
  are worth it; a far origin (e.g. Hyderabad for a Bhopal-area boarding) usually isn't. Say so.
- **Timing.** Mention awkward arrivals (03:45) against convenient ones (08:00).
- **Ticket date.** For any upstream pick, state the ticket's journey date: the date at that station,
  which can differ from when the traveller boards. Also say to choose the boarding station at booking
  time.

## Output format

Unless the user asks for something else, answer in their language with:

1. One or two lines on what was checked (trains kept and why others were dropped, data timestamp)
   and the key pattern (which stations share a pool, where upstream booking helps).
2. Per night: `Train | Kahan se book karna hai | <FROM> time | <TO> arrival | SL | 3A | 2A`. Start
   from `summary.md`, and trim or annotate by hand where judgment changes the pick.
3. **Best option** in 3 lines: which night, which train, where to book from (with ticket date and
   boarding point), plus one backup.
4. **Not verified**: always cover the relevant items below.
5. The path to `detail.md` for the full station-by-station data.

Hinglish example row:

```
| 12192 JBP–NZM | SL/3A: Itarsi (ticket date 15 Nov) · 2A: BAQ | 01:05 (+1) | 12:00 (+1) | **RAC 26** (ET) | **RAC 18** (ET) | RLWL 2 |
```

### "Not verified" checklist (pick what applies)

- Which sources failed (MCP down, IRCTC/ixigo blocked) and that the user should re-check on IRCTC
  before paying.
- Data time window, and that rows can come from ConfirmTkt's cache (timestamps can be hours old).
- Prediction % is ConfirmTkt's estimate, not IRCTC's.
- The current IRCTC rule for changing the boarding point later wasn't checked, so pick it at booking.
- Special or festival trains not announced yet, and to re-check closer to the date.
- Classes not checked (3E, 1A), and far stations that were skipped or that only gave expensive
  options.
- The timetable comes from erail's copy of the IR timetable. Check last-minute changes on NTES.

## Gotchas (learned the hard way)

- erail route lookups need erail's internal id (field 33 of getTrains), not the train number. A
  train number silently returns some other train's route. The script resolves it.
- Running-day strings (erail and ConfirmTkt) are Monday-first, and they apply at the boarding
  station's calendar date. Verified with 12405 (Tue,Sun) and 12409 (Mon,Wed–Sat) at BAQ.
- The ConfirmTkt journey date is the date at the `--from` station, as on IRCTC.
  `availablityDate` (API spelling) comes back unpadded (`9-11-2026`). The script normalises it.
- Asking ConfirmTkt for a date the train doesn't run returns error 533 "Unable to process your
  request". The plan only asks for running dates.
- ConfirmTkt `timeStamp` is IST and can be several hours old (server cache). `fromCache: 1` is set
  on most answers even when fresh, so judge freshness by `timeStamp`. Same start date means the same
  snapshot; an earlier start date gets a new one (see `--max-age`).
- Coach composition changes over time: 12192 had no 3E coach in April 2026 data and one 3E coach in
  Oct 2026. A train with a single 3E/2A/1A coach has a tiny quota, so say so.
- Booking from an intermediate station in the same division usually shows exactly the same RLWL as
  the boarding station (one pool). Gains come from the origin, from junctions sharing the origin's
  GN quota (e.g. Itarsi for the Jabalpur–NZM train), or from stations with GN where the boarding
  station has PQ (e.g. RKMP/BPL vs BAQ on 12155).

## Files

- `scripts/trainsearch.py`: all commands (`trains`, `route`, `ct-search`, `avl`, `plan`, `fetch`,
  `report`, plus `irctc-checklist` and `record` for live IRCTC checks).
- To confirm on **IRCTC itself** (live, no cache) in the user's logged-in browser, use the
  `irctc-upstream-finder` skill if it is installed. It reuses this script: plan, then a ConfirmTkt
  pre-screen, then `irctc-checklist` to pick the few stations worth checking, then IRCTC readings
  saved with `record`, then `report --source "IRCTC live"`.
- `references/api-notes.md`: endpoints, headers, field layouts, and how to rediscover the ConfirmTkt
  API if it changes. Read it when a call fails or returns an odd shape.
- `references/railway-rules.md`: quotas, status strings, RAC, boarding point, ticket dates, Tatkal
  and ARP. Read it when explaining options to the user.
- `references/example-baq-nzm.md`: the worked Ganj Basoda → Nizamuddin run (commands, findings,
  final answer). Read it for a model answer.
