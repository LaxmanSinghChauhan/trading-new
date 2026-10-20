---
name: irctc-upstream-finder
description: >-
  Find a confirmed/RAC berth on Indian Railways by checking LIVE availability on IRCTC in the user's
  own browser (they log in themselves) — not cached aggregator data — and, crucially, by testing
  earlier stations on the route (origin, junctions like Itarsi/Bhopal/Jabalpur) whose quota still
  has seats, then booking "earlier station → destination" with the boarding point set to the
  traveller's station. Plans which stations are worth checking (quota pools via a quick ConfirmTkt
  pre-screen + erail routes), drives the IRCTC search pages read-only, records what IRCTC shows,
  judges the confirmation chances and gives an exact booking recipe. Use it whenever the user wants
  IRCTC/live/non-cached availability, says they will log in to IRCTC (or MakeMyTrip), asks "BAQ ke
  pehle kis station se ticket lun", "boarding point badal ke", "kahan se seat milegi", "chances kya
  hain", or distrusts ConfirmTkt/RailYatri numbers — even if they don't say "skill". Never logs in,
  types passwords/OTP/captcha, books or pays.
---

# IRCTC upstream seat finder (live, user logs in)

Anyone can search "BAQ → NZM". The value here is finding **where** a seat exists. Indian Railways
gives each station a quota: the origin and some big junctions hold the General (GN) quota, while
small stations get a Remote Location (RL) or Pooled (PQ) quota that sells out early. A ticket
"Itarsi → NZM" with **Boarding Station = Ganj Basoda** can be RAC or available while "Ganj Basoda →
NZM" shows waitlist. Aggregators (ConfirmTkt, RailYatri, Goibibo) serve cached snapshots that were
seen hours to a month old, so the final answer must come from IRCTC itself, read live while the user
is logged in.

## Ground rules (non-negotiable)

- **The user logs in themselves.** Never type, read aloud, or store a username, password, OTP or
  captcha. When a login form or captcha appears, stop, ask the user to complete it, and wait for
  them to say done.
- **Read-only.** Use only search and availability views. Never click Book Now, never open the
  passenger or payment pages, never pick berths, never touch Tatkal booking. The user books.
- **Human pace.** One action at a time, let each page settle, stay around 20 searches or fewer per
  session, and stop if IRCTC shows a captcha, an "unable to process" error, or logs out. Don't retry
  in a loop. IRCTC also pauses around 23:45–00:20 IST every night, so don't try then.
- **Be honest about chances.** Every chance is an estimate; label it as one.

## What you need

A browser the user can see and log into:
- **Claude in Chrome** (`mcp__claude-in-chrome__*` tools): load the `anthropic-skills:chrome-browser`
  skill before the first browser step.
- **The Claude desktop app's built-in browser** (`mcp__Claude_Browser__*` or
  `mcp__remote-devices__Claude_Browser__*`): load the `anthropic-skills:built-in-browser` skill first.

If neither is available (for example in a cloud session that isn't linked to the user's computer),
say in one line that the IRCTC step has to run on their computer: IRCTC blocks cloud IPs with 403
"Access Denied", even for headless Chromium. They can use the Claude desktop app, or run
`claude remote-control` in a terminal. Then do Phase 1 anyway and hand over the checklist.

## Phase 1: decide which stations to check (no browser)

`scripts/trainsearch.py` is plain Python 3. Write outputs to a scratch directory, not a repo.

```bash
T=<skill-dir>/scripts/trainsearch.py
python3 -I $T plan --from BAQ --to NZM --start 2026-11-10 --end 2026-11-15 \
  --window 18:00-02:00 --must-end-at-dest --extra-upstream BPL,RKMP,BHS,ET,JBP --out plan.json
python3 -I $T fetch --plan plan.json --classes 3A,2A --out prescreen.json      # quick, cached: only to find pools
python3 -I $T irctc-checklist --plan plan.json --avl prescreen.json --classes 3A,2A --out checklist.json
```

- **`plan`** keeps the trains that fit the time window, destination and running days. For each
  train it lists the earlier stations and the **ticket date at each one**: after-midnight boardings
  shift the date, so 12192 from Itarsi carries the night's date while from BAQ it carries the next
  date. Flags are described in `trainsearch.py plan -h`.
- **`fetch`** is a ConfirmTkt pre-screen. Use it only to see which stations share a quota pool
  (identical numbers) and which pool looks better. Its numbers are not the answer.
- **`irctc-checklist`** groups the work into IRCTC searches, one per from-station, because one IRCTC
  search lists every train from that station that day. It always includes the boarding station as
  the baseline, adds only stations whose pool beat the baseline on some night (and only those
  nights), and drops picks costing more than `--max-extra-fare` (₹500) extra, listing them as
  skipped.
- If ConfirmTkt is unreachable, run `irctc-checklist` without `--avl`. It then falls back to the
  origin, junctions, and `--stations` the user named.

Show the user the checklist in a few lines (how many searches, which stations, and why) before
opening the browser.

## Phase 2: open IRCTC and hand over the login

1. Open `https://www.irctc.co.in/nget/train-search` in a new tab and close any popups (Aadhaar
   reminders, AskDisha).
2. Tell the user: "IRCTC khul gaya hai, please login kar lo (username, password, captcha, OTP sab
   aap), phir bata dena." Wait.
3. Confirm the login (the user's name or "Logout" in the header). Never read or keep anything from
   the login form.

## Phase 3: run the checklist on IRCTC

Follow `references/irctc-ui.md` for field-by-field steps and pitfalls. For each search:

1. Fill From (type the code, then pick the suggestion with that code), To, Date (the **ticket date
   at that station**), Quota GENERAL, class All Classes. Leave "Flexible with date" and "Train with
   available berth" unchecked, then Search.
2. Find each listed train by number. For each class to check, click the class and read the
   availability strip: dates, statuses, and the fare if shown. Take a screenshot when the text is
   unclear.
3. Record it straight away, one call per train, station and class, with every date you could read:
   ```bash
   python3 -I $T record --out irctc.json --train 12192 --from ET --cls 3A \
     --status 2026-11-12="RAC 36" 2026-11-13="RAC 40" 2026-11-15="RAC 16" --fare 1205
   ```
   Copy the status exactly as IRCTC shows it (`RLWL5/WL3`, `AVAILABLE-0012`, `RAC 16`, `REGRET`). If
   IRCTC shows a probability, pass it with `--pred`.
4. If a needed date isn't in the strip, change the date and search again. Don't guess.

Do the baseline (the boarding station) first. If an upstream station shows nothing better than
the baseline, move on. Stop early for a train and class once a good option (AVL or a low RAC) is
found, unless the user wants everything.

## Phase 4: report, chances, booking recipe

```bash
python3 -I $T report --plan plan.json --avl irctc.json --outdir . --lang hinglish \
  --source "IRCTC live (logged in)" --stale 0
```

Then judge each option with `references/chances.md`. The report's `High*/Medium*/Low*` is a rule of
thumb; refine it using the quota type, WL size against the number of coaches, days left, and RAC.

Answer in the user's language with:

1. One line: these numbers are IRCTC live at <time>, plus how many searches were done.
2. Per night: `Train | Kahan se book karna hai | <FROM> time | <TO> arrival | <classes…>`, each cell
   giving the status, chance, and `@STN +₹extra` when upstream.
3. **Booking recipe** for the best one or two options:
   "IRCTC → From **Itarsi (ET)** To **NZM**, date **15 Nov**, class 3A, quota General → passenger
   page → **Boarding Station: GANJ BASODA (BAQ)**". Then: "You board at BAQ on 16 Nov 01:05. You
   can't board at Itarsi, and the Itarsi–BAQ part isn't refunded."
4. **Where the cache was wrong.** Compare with `prescreen.json` and name any row where the aggregator
   differed from IRCTC. This tells the user how far to trust the apps next time.
5. **Not verified.** Dates or stations you couldn't read, searches skipped (fare cap), rules not
   checked (later boarding-point changes), and festival specials.

## Optional second source

MakeMyTrip and Goibibo (same group, IRCTC partners) show availability with a refresh button.
Goibibo's `goibibo.com/trains/<train>-train-seat-availability/` page has a two-month per-class
calendar with "x hrs ago" labels. Use them only if the user asks for a cross-check, in their
browser, read-only. MakeMyTrip blocks cloud IPs; Goibibo opened from the cloud in Oct 2026, but its
numbers were snapshots.

## Files

- `scripts/trainsearch.py`: `plan`, `fetch`, `irctc-checklist`, `record`, `report`, plus `trains`,
  `route`, `ct-search` and `avl` for one-off checks.
- `references/irctc-ui.md`: operating the IRCTC search UI step by step, what not to click, and
  error handling.
- `references/chances.md`: how to judge confirmation chances, and the extra tricks (other class,
  other night, Tatkal, VIKALP, longer ticket).
- `references/railway-rules.md`: quotas, status strings, boarding point, ticket dates, booking
  windows.
- `references/api-notes.md`: erail and ConfirmTkt endpoints used by Phase 1, and what is blocked.
