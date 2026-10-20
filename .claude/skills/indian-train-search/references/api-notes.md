# API notes (verified Oct 2026)

All calls are plain HTTPS GET/POST and need no login. The script sends a desktop Chrome
User-Agent.

## Contents
1. erail.in: trains between stations
2. erail.in: single train info
3. erail.in: train route
4. ConfirmTkt: search (train list per date)
5. ConfirmTkt: fetchAvailability (live)
6. Rediscovering the ConfirmTkt API if it changes
7. What did not work

---

## 1. erail.in: trains between stations

`GET https://erail.in/rail/getTrains.aspx?Station_From=BAQ&Station_To=NZM&DataSource=0&Language=0&Cache=true`

The response is text. It starts with a header (`~BAQ~Ganj Basoda~NZM~...~<timestamp>~~~`) and has
one record per train, each starting with `^`. Fields are separated by `~`:

| idx | meaning | example |
|---|---|---|
| 0 | train number | 12721 |
| 1 | name | DAKSHIN SF EXP |
| 2,3 | origin name, code | Hyderabad Decan, HYB |
| 4,5 | final destination name, code | Hazrat Nizamuddin, NZM |
| 6,7 | matched from-station name, code | Ganj Basoda, BAQ |
| 8,9 | matched to-station name, code | Hazrat Nizamuddin, NZM |
| 10 | dep at from (HH.MM) | 18.20 |
| 11 | arr at to | 03.45 |
| 12 | duration | 09.25 |
| 13 | running days at from-station, Monday-first | 1111111 / 0100001 |
| 26 | dep at origin | 23.00 |
| 32 | type | SUPERFAST |
| 33 | **erail internal id** (needed for the route) | 2379 |
| 37,38 | timetable validity from/to | 2026-04-16 |
| 39 | distance from→to (km) | 601 |
| 59 | coach composition | ...:S,S1,SL:B,B1,3A:... |

The search is city-wide: a BPL search also returns trains that end at RKMP. Fields 7 and 9 give the
station actually matched.

## 2. erail.in: single train info

`GET https://erail.in/rail/getTrains.aspx?TrainNo=12721&DataSource=0&Language=0&Cache=true`

Same record layout (origin → destination). Field 33 is the internal id, field 13 the running days at
the origin.

## 3. erail.in: train route

`GET https://erail.in/data.aspx?Action=TRAINROUTE&Password=2012&Data1=<erail_id>&Data2=0&Cache=true`

Records start with `^`. Fields: `0 seq, 1 code, 2 name, 3 arr ("First" at origin), 4 dep ("Last" at
destination), 5 halt minutes, 6 km, 7 day number (1 = origin day), 8 platform, 10 zone, 11 division,
12 Hindi name (HTML entities), 14/15 lat/long`.

**Data1 must be the internal id.** Passing the train number (e.g. 12721) returns some other train's
route without any error (it gave a Bengaluru train).

## 4. ConfirmTkt: search

`GET https://cttrainsapi.confirmtkt.com/api/v1/trains/search?sourceStationCode=BAQ&destinationStationCode=NZM&addAvailabilityCache=true&excludeMultiTicketAlternates=false&excludeBoostAlternates=false&sortBy=DEFAULT&dateOfJourney=10-11-2026&enableNearby=true&enableTG=false&tGPlan=&showTGPrediction=false&tgColor=DEFAULT&showPredictionGlobal=true`

No special headers. `data.trainList[]` has `trainNumber, trainName, departureTime, arrivalTime,
trainOriginStationCode, trainDestinationStationCode, runningDays (Monday-first), avlClasses,
availabilityCache{cls: {availability, prediction, cacheTime, fare...}}`.

- Only trains that run from the from-station on that date are listed, which makes this a good
  running-day check.
- `availabilityCache` is ConfirmTkt's cache, often days or weeks old (see `cacheTime`). Use it only
  as a hint and use fetchAvailability for real numbers.
- `trainDestinationStationCode` can differ from erail (20423 showed DSJ, erail FZR). Treat erail plus
  the route as the source of truth for "where does it end".

## 5. ConfirmTkt: fetchAvailability (live)

`POST https://cttrainsapi.confirmtkt.com/api/v1/availability/fetchAvailability?trainNo=12721&travelClass=3A&quota=GN&sourceStationCode=BAQ&destinationStationCode=NZM&dateOfJourney=10-11-2026&enableTG=false&tGPlan=&showTGPrediction=false&tgColor=DEFAULT&showPredictionGlobal=true&showNewMealOptions=true&showNewAlternates=false&showNewAltText=true`

Required headers (what the desktop web app sends):

```
ClientId: ct-web
ApiKey: ct-web!2$
DeviceId: <any string>
Content-Type: application/json
Origin: https://www.confirmtkt.com
Referer: https://www.confirmtkt.com/
```

The body is empty. The response has `data.avlDayList[]` with `availablityDate` (sic, unpadded
`9-11-2026`), `availablityStatus` (`RLWL18/WL16`, `RAC  42/RAC  38`, `AVAILABLE-0087`, `REGRET`,
`GNWL1/RAC12`), `prediction` (`65% Chance`), `confirmTktStatus`. Other useful fields:
`data.fareInfo.totalFare`, and `data.timeStamp` (IST; may be a cached snapshot hours old).

- One call returns up to ~4 **running** dates starting at `dateOfJourney`. Non-running days are
  skipped, so a Tue/Sun train asked from Tue returns Tue, Sun, Tue, Sun...
- When `dateOfJourney` itself isn't a running day, the response has no list and
  `errorMessage {code: 533, message: "Unable to process your request"}`.
- A class the train doesn't have gives an error or an empty list. Show it as n/a.
- `dateOfJourney` is the date at `sourceStationCode` (the ticket's from-station), the same as IRCTC.
- Caching: answers are cached per (train, class, from, to, dateOfJourney). Re-sending the same
  query returns the identical `timeStamp` (seen 8+ hours old). Other client ids (`ct-mweb`) and
  extra params (`refresh`, `forceRefresh`) don't bypass it. An earlier `dateOfJourney` (a different
  key) usually brings a fresh snapshot that still includes the later dates. `data.fromCache` is 1
  for nearly all answers, so use `timeStamp` for age.

## 6. Rediscovering the ConfirmTkt API if it changes

1. `curl -A "Mozilla/5.0" https://www.confirmtkt.com/rbooking/trains/from/BAQ/to/NZM/10-11-2026`
   and find the `src="/rbooking/assets/index-XXXX.js"` bundle.
2. Download it and grep for `COMMON_API_HOST:"https://..."` (the host), `getSixDaysAvailabilityApi`
   (a chunk name; fetch `/rbooking/assets/getSixDaysAvailabilityApi-XXXX.js` to see the path and
   query params), and `buildCommonBackendHeader` (header names: `ClientId`, `ApiKey` =
   `${channel}!2$`, `DeviceId`).
3. The channel mapping is in an object like `Fo={...,mwebd:"ct-web",mweb:"ct-mweb",...}`. Desktop
   is `ct-web`.

## 7. What did not work (Oct 2026)

- `railway-mcp.amithv.xyz`: HTTP 525 (Cloudflare origin SSL failure), including `/mcp`, on both
  18 and 20 Oct.
- `www.ixigo.com`, `www.irctc.co.in`: HTTP 403 to curl. IRCTC also returned 403 "Access Denied"
  (Akamai) to headless Chromium via Playwright, which looks like a cloud-IP block.
- RailYatri: `GET https://trainticketapi.railyatri.in/api/trains-between-station-with-sa.json?from=BAQ&to=NZM&dateOfJourney=10-11-2026&action=train_between_station&controller=train_ticket_tbs&device_type_id=6&from_code=BAQ&journey_date=10-11-2026&journey_quota=GN&to_code=NZM&authentication_token=&v_code=null&user_id=-1&temp_user_id=-1`
  works without login. `train_between_stations[].sa_data[]` holds `availibility` (sic) and
  `seat_availibility[].last_updated_at`. This is RailYatri's own cache: one entry showed
  `AVAILABLE-0051` "As of 1 month ago" while live IRCTC-relayed data showed WL. Its "Tap to refresh"
  didn't fire a capturable request in headless mode. Its `class_type[].coach_count` was also stale
  (3E = 0 on 12192).
- erail route with a train number in Data1: wrong train, see section 3.
