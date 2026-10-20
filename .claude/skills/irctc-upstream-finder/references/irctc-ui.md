# Operating IRCTC's search pages (read-only)

These notes describe the IRCTC web UI as of 2025–26. It changes often, so find elements by visible
text and confirm with a screenshot rather than relying on selectors.

## Contents
1. Before you start
2. Login hand-off
3. Filling the search form
4. Reading the results
5. Things never to click
6. Errors and what to do
7. Recording what you read

## 1. Before you start

- Use a new tab: `https://www.irctc.co.in/nget/train-search`.
- IRCTC is unavailable around 23:45–00:20 IST every night.
- Popups (Aadhaar or KYC reminders, AskDisha chatbot, cookie or alert banners): close them with
  their ✕ or "OK". Don't accept anything that changes the account.
- Keep to about 20 searches per session. IRCTC can show a captcha on search or log the user out
  when activity looks automated.

## 2. Login hand-off

- Click **LOGIN** only to open the dialog if the user asks you to. Otherwise tell them to log in.
- The user types the username, password and captcha, and handles the OTP. Don't read the dialog
  back, screenshot it, or store anything from it.
- Logged in looks like the user's name or a "Logout" link in the header. Wait for the user to say
  done, then check.

## 3. Filling the search form

| Field | How |
|---|---|
| From | Click, clear, type the station **code** (e.g. `ET`), wait for suggestions, and pick the line ending in `- ET` (e.g. "ITARSI JN - ET"). Codes avoid picking the wrong station with a similar name. |
| To | Same, e.g. `NZM` → "HAZRAT NIZAMUDDIN - NZM". |
| Date | The **ticket date at the From station** from the checklist. The input takes `DD/MM/YYYY`: select all, type, and press Escape to close the calendar. Check that the field shows the right date. |
| Class | "All Classes". |
| Quota | "GENERAL". Ladies, Senior Citizen and Tatkal are different pools. Check GENERAL unless the user asks for another. |
| Checkboxes | Leave "Flexible With Date", "Train with Available Berth", "Person With Disability Concession" and "Railway Pass Concession" **unchecked**. "Available Berth" would hide waitlisted trains, which you need to see. |

Click **Search**. The results header reads like `N Results for ITARSI JN → HAZRAT NIZAMUDDIN |
Sun, 15 Nov For Quota | General`. Check it before reading.

## 4. Reading the results

- Each train card shows the name and number (e.g. "JBP NZM SF EXP (12192)") and its departure and
  arrival times **for the From station**. Match the train by number.
- Class buttons or tabs ("AC 3 Tier (3A)", "AC 2 Tier (2A)", "AC 3 Economy (3E)", "First AC (1A)",
  "Sleeper (SL)") may say "Refresh". Clicking one shows a horizontal strip of dates with a status
  under each, for example `AVAILABLE-0012`, `RAC 16`, `RLWL5/WL3`, `GNWL34/WL20`, `PQWL4/WL2`,
  `REGRET/WL`, `NOT AVAILABLE`. It usually covers the searched date and the next few running dates.
- The fare appears for the selected date (sometimes only after choosing a date chip). Choosing a
  date chip is fine; **Book Now** is not.
- Read the card text with the page-text or find tool, then confirm with a screenshot when
  statuses or dates are ambiguous. Don't infer a status for a date you didn't see.
- For an after-midnight boarding train (12192 at BAQ 01:05), searching "From ET, 15 Nov" shows the
  15 Nov departure from Itarsi. Searching "From BAQ" needs **16 Nov**.
- If the train isn't listed: that station may not be a stop on that date, the date is wrong for an
  after-midnight train, or the train doesn't run that day. Check the date first.

## 5. Things never to click

Book Now; Proceed; Continue; anything on the passenger, review or payment pages; "Add to
favourites" and other account changes; Tatkal booking; e-wallet; and saving passenger lists.
Changing the date with the date chips and the "Modify Search" panel is fine.

## 6. Errors and what to do

| What you see | Do |
|---|---|
| "Sorry!!! Please Try again!!" / "Unable to process current transaction" | Wait ~30 s and retry once. If it repeats, stop and tell the user. |
| Captcha on search | Stop and ask the user to solve it. Never solve it yourself. |
| Logged out / session expired | Ask the user to log in again. Don't re-enter anything. |
| "No direct trains" / empty list | Check From/To codes and the date. The station may not be a stop. |
| Access Denied (Akamai) | You're on a blocked network (cloud). The step needs the user's own browser. |

## 7. Recording what you read

Right after reading a class, record it (ticket dates at the From station):

```bash
python3 -I <skill-dir>/scripts/trainsearch.py record --out irctc.json --train 12192 --from ET --cls 3A \
  --status 2026-11-12="RAC 36" 2026-11-13="RAC 40" 2026-11-15="RAC 16" --fare 1205
```

Recording as you go keeps the report honest and lets a dropped session resume without re-searching.
