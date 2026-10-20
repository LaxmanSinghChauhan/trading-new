#!/usr/bin/env python3
"""Indian Railways train search: erail.in timetable/route + ConfirmTkt live seat availability.

Read-only helper. It never logs in, books, solves captchas or pays.

Subcommands (run any of them with -h for options):
  trains     trains between two stations (erail.in)
  route      full stop list of one train (erail.in)
  ct-search  ConfirmTkt train list for one date: running-day check, specials, cached availability
  avl        one live availability lookup (ConfirmTkt fetchAvailability)
  plan       filter trains (time window, ends-at-destination), pick upstream boarding
             stations, work out the ticket date for every station
  fetch      live availability for every (train, from-station, class) job of a plan, in parallel
  report     markdown summary + detail tables and best.json from a plan and fetched availability

Typical run:
  trainsearch.py plan  --from BAQ --to NZM --start 2026-11-10 --end 2026-11-15 \
                       --window 18:00-02:00 --must-end-at-dest --extra-upstream BPL,RKMP,BHS,ET,JBP \
                       --out plan.json
  trainsearch.py fetch --plan plan.json --classes SL,3A,2A --out avl.json
  trainsearch.py report --plan plan.json --avl avl.json --outdir . --lang hinglish
"""
import argparse
import datetime as dt
import json
import os
import re
import sys
import time
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed

UA = ("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
      "(KHTML, like Gecko) Chrome/124.0 Safari/537.36")
ERAIL = "https://erail.in"
CT = "https://cttrainsapi.confirmtkt.com"
# Headers the confirmtkt.com desktop web app sends (channel "mwebd" maps to client "ct-web").
CT_HEADERS = {
    "ClientId": "ct-web", "ApiKey": "ct-web!2$", "DeviceId": "trainsearch-skill",
    "Content-Type": "application/json", "Accept": "application/json",
    "Origin": "https://www.confirmtkt.com", "Referer": "https://www.confirmtkt.com/",
}
WEEKDAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"]


# ---------------------------------------------------------------- helpers

def log(*a):
    print(*a, file=sys.stderr, flush=True)


def http(url, method="GET", headers=None, timeout=60, retries=3):
    h = {"User-Agent": UA}
    h.update(headers or {})
    last = None
    for attempt in range(retries):
        try:
            req = urllib.request.Request(url, data=b"" if method == "POST" else None,
                                         headers=h, method=method)
            with urllib.request.urlopen(req, timeout=timeout) as r:
                return r.read().decode("utf-8", "replace")
        except Exception as e:  # network errors, 5xx, Cloudflare hiccups
            last = e
            time.sleep(2 * (attempt + 1))
    raise RuntimeError(f"{method} {url.split('?')[0]} failed: {last}")


def hhmm(s):
    s = (s or "").strip()
    return s.replace(".", ":") if re.fullmatch(r"\d\d[.:]\d\d", s) else s


def minutes(s):
    h, m = hhmm(s).split(":")
    return int(h) * 60 + int(m)


def to_int(s, default=0):
    try:
        return int(s)
    except (TypeError, ValueError):
        return default


def parse_date(s):
    for fmt in ("%Y-%m-%d", "%d-%m-%Y", "%d/%m/%Y"):
        try:
            return dt.datetime.strptime(s, fmt).date()
        except ValueError:
            pass
    raise argparse.ArgumentTypeError(f"bad date {s!r}; use YYYY-MM-DD")


def valid_days(days):
    return bool(days) and len(days) == 7 and set(days) <= {"0", "1"}


def runs_on(days, date):
    """Running-day strings from erail and ConfirmTkt are Monday-first: '1011110' = Mon,Wed,Thu,Fri,Sat."""
    return days[date.weekday()] == "1" if valid_days(days) else True


def shift_days(days, by):
    """Days string at the origin -> days string at a station reached `by` calendar days later."""
    if not valid_days(days) or by % 7 == 0:
        return days
    out = ["0"] * 7
    for i, c in enumerate(days):
        out[(i + by) % 7] = c
    return "".join(out)


def fmt_days(days):
    if not valid_days(days) or days == "1111111":
        return "daily"
    return ",".join(WEEKDAYS[i] for i, c in enumerate(days) if c == "1")


def csv_set(s):
    return {x.strip().upper() for x in s.split(",") if x.strip()} if s else set()


def dump(obj, path):
    with open(path, "w") as f:
        json.dump(obj, f, indent=1, ensure_ascii=False)


# ---------------------------------------------------------------- erail.in

COACH_PREFIX_CLASS = {"M": "3E", "C": "CC", "E": "EC", "D": "2S"}


def coach_counts(comp):
    """erail composition 'S,S1,SL:B,B1,3A:M,M1,:H,H1,1A:HA,HA1,1A+2A' -> {'SL': 1, '3A': 1, '3E': 1, ...}."""
    out = {}
    for c in (comp or "").split(":"):
        p = c.split(",")
        if len(p) < 3 or not p[1]:
            continue
        for cls in (p[2] or COACH_PREFIX_CLASS.get(p[0], "")).split("+"):
            if cls:
                out[cls] = out.get(cls, 0) + 1
    return out


def _erail_row(f):
    # Field positions verified Oct 2026; f[33] is erail's internal train id (needed for routes),
    # f[59] the coach composition.
    return {"train": f[0], "name": f[1].strip(), "origin": f[3], "origin_name": f[2].strip(),
            "dest": f[5], "dest_name": f[4].strip(), "from": f[7], "to": f[9],
            "dep": hhmm(f[10]), "arr": hhmm(f[11]), "duration": hhmm(f[12]), "days": f[13],
            "type": f[32], "erail_id": f[33], "coaches": coach_counts(f[59]) if len(f) > 59 else {}}


def _erail_rows(txt):
    rows = []
    for r in txt.split("^")[1:]:
        f = r.split("~")
        if len(f) > 33 and f[0].strip():
            rows.append(_erail_row(f))
    return rows


def erail_trains(frm, to):
    return _erail_rows(http(f"{ERAIL}/rail/getTrains.aspx?Station_From={frm}&Station_To={to}"
                            "&DataSource=0&Language=0&Cache=true"))


def erail_train(train_no):
    rows = _erail_rows(http(f"{ERAIL}/rail/getTrains.aspx?TrainNo={train_no}"
                            "&DataSource=0&Language=0&Cache=true"))
    return rows[0] if rows else None


def erail_route(erail_id):
    """Stops of a train. Data1 is erail's internal id, NOT the train number: passing a train
    number silently returns some other train's route."""
    txt = http(f"{ERAIL}/data.aspx?Action=TRAINROUTE&Password=2012&Data1={erail_id}&Data2=0&Cache=true")
    stops = []
    for r in txt.split("^")[1:]:
        s = r.split("~")
        if len(s) < 8:
            continue
        stops.append({"seq": to_int(s[0]), "code": s[1], "name": re.sub(r"\s+", " ", s[2]).strip(),
                      "arr": hhmm(s[3]), "dep": hhmm(s[4]), "halt": to_int(s[5]), "km": to_int(s[6]),
                      "day": to_int(s[7], 1)})
    return stops


def route_for(train_no, erail_id=None):
    info = None
    if not erail_id:
        info = erail_train(train_no)
        if not info:
            raise RuntimeError(f"erail.in has no train {train_no}")
        erail_id = info["erail_id"]
    stops = erail_route(erail_id)
    if not stops:
        raise RuntimeError(f"empty route for {train_no} (erail id {erail_id})")
    return stops, info


# ---------------------------------------------------------------- ConfirmTkt

def ct_search(frm, to, date):
    q = dict(sourceStationCode=frm, destinationStationCode=to, addAvailabilityCache="true",
             excludeMultiTicketAlternates="false", excludeBoostAlternates="false", sortBy="DEFAULT",
             dateOfJourney=date.strftime("%d-%m-%Y"), enableNearby="true", enableTG="false", tGPlan="",
             showTGPrediction="false", tgColor="DEFAULT", showPredictionGlobal="true")
    j = json.loads(http(f"{CT}/api/v1/trains/search?" + urllib.parse.urlencode(q),
                        headers={"Accept": "application/json"}))
    data = j.get("data") or {}
    trains = []
    for t in data.get("trainList") or []:
        cached = {c: {"status": a.get("availability"), "pred": a.get("prediction"),
                      "cacheTime": a.get("cacheTime")}
                  for c, a in (t.get("availabilityCache") or {}).items() if a and a.get("availability")}
        trains.append({"train": t.get("trainNumber"), "name": t.get("trainName"),
                       "dep": t.get("departureTime"), "arr": t.get("arrivalTime"),
                       "origin": t.get("trainOriginStationCode"), "dest": t.get("trainDestinationStationCode"),
                       "days": t.get("runningDays"), "classes": t.get("avlClasses"), "cached_avl": cached})
    return {"date": date.isoformat(), "source": data.get("sourceStationName") or frm,
            "destination": data.get("destinationStationName") or to, "error": data.get("errorMessage"),
            "trains": trains}


def ct_avl(train, frm, to, cls, date, quota="GN"):
    """Live availability. Returns up to ~4 *running* dates starting at `date`."""
    q = dict(trainNo=train, travelClass=cls, quota=quota, sourceStationCode=frm, destinationStationCode=to,
             dateOfJourney=date.strftime("%d-%m-%Y"), enableTG="false", tGPlan="", showTGPrediction="false",
             tgColor="DEFAULT", showPredictionGlobal="true", showNewMealOptions="true",
             showNewAlternates="false", showNewAltText="true")
    j = json.loads(http(f"{CT}/api/v1/availability/fetchAvailability?" + urllib.parse.urlencode(q),
                        method="POST", headers=CT_HEADERS))
    data = j.get("data") or {}
    days = {}
    for x in data.get("avlDayList") or []:
        d, m, y = (int(p) for p in x["availablityDate"].split("-"))  # sic: API spelling, unpadded "9-11-2026"
        days[dt.date(y, m, d).isoformat()] = {"status": x.get("availablityStatus"),
                                               "pred": x.get("prediction"), "ct": x.get("confirmTktStatus"),
                                               "ts": data.get("timeStamp"), "cached": data.get("fromCache")}
    err = data.get("errorMessage") or j.get("errorMessage")
    if isinstance(err, dict):
        err = f"{err.get('code')}: {str(err.get('message', '')).strip()}"
    return {"days": days, "fare": (data.get("fareInfo") or {}).get("totalFare"),
            "timeStamp": data.get("timeStamp"), "fromCache": data.get("fromCache"),
            "error": None if days else (err or "no avlDayList")}


def now_ist():
    return dt.datetime.now(dt.timezone.utc).replace(tzinfo=None) + dt.timedelta(hours=5, minutes=30)


def age_minutes(ts):
    """Age of a ConfirmTkt timeStamp (IST, e.g. '2026-10-20T08:04:10.787') in minutes; None if unknown."""
    try:
        return (now_ist() - dt.datetime.fromisoformat(ts[:19])).total_seconds() / 60
    except (TypeError, ValueError):
        return None


def parse_status(st):
    """-> (rank, label). Lower rank is better: AVL < RAC < WL < other < REGRET < missing.
    Status strings are 'booking/current', e.g. 'RLWL27/WL23', 'RAC  89/RAC  84', 'GNWL1/RAC12'."""
    if not st:
        return (9, 0), "n/a"
    s = st.strip()
    u = s.upper()
    if u.startswith("REGRET") or "NOT AVAILABLE" in u:
        return (8, 0), "REGRET"
    cur, book = s.split("/")[-1].strip(), s.split("/")[0].strip()
    m = re.search(r"(?:AVAILABLE|AVBL|AVL)[-\s]*0*(\d+)", cur.upper())
    if m:
        return (0, -int(m.group(1))), f"AVL {int(m.group(1))}"
    m = re.search(r"RAC\s*0*(\d+)", cur.upper())
    if m:
        return (1, int(m.group(1))), f"RAC {int(m.group(1))}"
    m = re.search(r"WL\s*0*(\d+)", cur.upper())
    if m:
        q = re.match(r"([A-Z]*WL)", book.upper())
        return (2, int(m.group(1))), f"{q.group(1) if q else 'WL'} {int(m.group(1))}"
    return (7, 0), s


# ---------------------------------------------------------------- plan

def board_offset(dep, window):
    """0 if `dep` falls on the night's own date, 1 if after midnight (next calendar date),
    None if outside the window. Window like ('18:00', '02:00') may wrap past midnight."""
    s, e, t = minutes(window[0]), minutes(window[1]), minutes(dep)
    if s <= e:
        return 0 if s <= t <= e else None
    if t >= s:
        return 0
    return 1 if t <= e else None


def pick_upstream(stops, board_idx, explicit, extra, max_n):
    """Stations before the boarding stop worth querying. Auto mode: the 3 nearest stops,
    then junctions / long halts nearest-first (capped), plus the origin (own GN quota)."""
    before = stops[:board_idx]
    if not before:
        return []
    if explicit:
        chosen = [s for s in before if s["code"] in explicit]
    else:
        near = list(reversed(before))
        chosen = near[:3] + [s for s in near[3:]
                             if re.search(r"\bJN\b", s["name"].upper()) or s["halt"] >= 5]
        chosen = chosen[:max_n]
        if before[0] not in chosen:
            chosen.append(before[0])
    chosen += [s for s in before if s["code"] in extra and s not in chosen]
    return sorted(chosen, key=lambda s: s["seq"])


def cmd_plan(a):
    frm, to = a.frm.upper(), a.to.upper()
    window = tuple(x.strip() for x in a.window.split("-")) if a.window else ("00:00", "23:59")
    nights = [a.start + dt.timedelta(days=i) for i in range((a.end - a.start).days + 1)]
    explicit = None if not a.upstream or a.upstream.lower() == "auto" else csv_set(a.upstream)
    extra, include, exclude = csv_set(a.extra_upstream), csv_set(a.include), csv_set(a.exclude)

    rows = erail_trains(frm, to)
    known = {r["train"] for r in rows}
    for tno in csv_set(a.add_train) - known:  # e.g. a special train ConfirmTkt lists but erail's pair search misses
        info = erail_train(tno)
        if info:
            info["_added"] = True
            rows.append(info)
        else:
            log(f"warning: erail.in has no train {tno}; check it by hand with `avl`")

    trains, skipped, jobs = [], [], []
    for r in rows:
        tno = r["train"]
        base = {"train": tno, "name": r["name"], "origin": r["origin"], "dest": r["dest"]}
        if include and tno not in include and not r.get("_added"):
            continue
        if tno in exclude:
            skipped.append({**base, "reason": "excluded by user"})
            continue
        if a.must_end_at_dest and r["dest"] not in {to, r["to"]}:
            skipped.append({**base, "reason": f"does not end at {to} (runs on to {r['dest']})"})
            continue
        if not r.get("_added"):
            off = board_offset(r["dep"], window)
            if off is None:
                skipped.append({**base, "dep": r["dep"],
                                "reason": f"departs {frm} {r['dep']}, outside {window[0]}-{window[1]}"})
                continue
        try:
            stops, _ = route_for(tno, r["erail_id"])
        except Exception as e:
            skipped.append({**base, "reason": f"route lookup failed: {e}"})
            continue
        codes = [s["code"] for s in stops]
        # erail's pair search works city-wide (BPL also returns trains to RKMP), so use the
        # codes it actually matched for this train and flag them when they differ.
        b_code = frm if frm in codes else (r["from"] if r["from"] in codes else None)
        d_code = to if to in codes else (r["to"] if r["to"] in codes else None)
        if not b_code or not d_code or codes.index(b_code) >= codes.index(d_code):
            skipped.append({**base, "reason": f"route check failed ({frm}->{to} not in stop list)"})
            continue
        bi, di = codes.index(b_code), codes.index(d_code)
        b, d = stops[bi], stops[di]
        off = board_offset(b["dep"], window)
        if off is None:
            skipped.append({**base, "dep": b["dep"],
                            "reason": f"departs {frm} {b['dep']}, outside {window[0]}-{window[1]}"})
            continue
        days = r["days"] if not r.get("_added") else shift_days(r["days"], b["day"] - 1)
        running = [n for n in nights if runs_on(days, n + dt.timedelta(days=off))]
        if not running:
            skipped.append({**base, "reason": f"does not run on these nights (runs {fmt_days(days)} at {frm})"})
            continue
        ups = [] if a.no_upstream else pick_upstream(stops, bi, explicit, extra, a.max_upstream)
        stations = []
        for s in ups + [b]:
            stations.append({"code": s["code"], "name": s["name"], "seq": s["seq"], "dep": s["dep"],
                             "offset": s["day"] - b["day"] + off, "is_board": s is b,
                             "is_origin": s["seq"] == stops[0]["seq"]})
        trains.append({**base, "days": days, "days_text": fmt_days(days), "coaches": r.get("coaches") or {},
                       "board": {"code": b_code, "dep": b["dep"], "offset": off},
                       "to": d_code, "dest_arr": d["arr"], "arr_offset": d["day"] - b["day"] + off,
                       "nights": [n.isoformat() for n in running], "stations": stations,
                       "route": [f"{s['code']} {s['arr']}/{s['dep']} d{s['day']}" for s in stops]})
        for st in stations:
            jobs.append({"train": tno, "from": st["code"], "to": d_code,
                         "dates": [(n + dt.timedelta(days=st["offset"])).isoformat() for n in running]})

    # Coach classes per train (ConfirmTkt search lists them), so fetch skips classes a train lacks.
    need = {t["train"] for t in trains}
    for n in sorted({n for t in trains for n in t["nights"]}):
        if not need:
            break
        bdate = dt.date.fromisoformat(n) + dt.timedelta(days=1 if any(
            t["board"]["offset"] for t in trains if t["train"] in need and n in t["nights"]) else 0)
        for bd in sorted({dt.date.fromisoformat(n), bdate}):
            try:
                found = {x["train"]: x["classes"] for x in ct_search(frm, to, bd)["trains"] if x.get("classes")}
            except Exception as e:
                log(f"warning: ConfirmTkt search failed for {bd}: {e}")
                continue
            for t in trains:
                if t["train"] in need and t["train"] in found:
                    t["classes"] = found[t["train"]]
                    need.discard(t["train"])

    plan = {"from": frm, "to": to, "window": list(window), "nights": [n.isoformat() for n in nights],
            "must_end_at_dest": a.must_end_at_dest, "created": dt.datetime.now().isoformat(timespec="seconds"),
            "trains": trains, "skipped": skipped, "jobs": jobs}
    dump(plan, a.out)

    print(f"{frm} -> {to}, nights {nights[0]} .. {nights[-1]}, window {window[0]}-{window[1]}")
    for t in trains:
        sts = ", ".join(f"{s['code']}({s['offset']:+d})" for s in t["stations"])
        print(f"  KEEP {t['train']} {t['name']}: {t['origin']}->{t['dest']}, {t['board']['code']} {t['board']['dep']}"
              f"{' (+1)' if t['board']['offset'] else ''}, {t['to']} {t['dest_arr']} (+{t['arr_offset']}), "
              f"runs {t['days_text']}, nights {len(t['nights'])}, classes {','.join(t.get('classes') or ['?'])}; "
              f"stations {sts}")
    for s in skipped:
        print(f"  SKIP {s['train']} {s['name']}: {s['reason']}")
    print(f"{len(jobs)} station jobs -> about {len(jobs) * 2} calls per class. Plan saved to {a.out}")


# ---------------------------------------------------------------- fetch

def fetch_one(job, cls, to, quota, pause, max_age=None):
    need = sorted(set(job["dates"]))
    got, errors, stamps, fare, tried = {}, [], [], None, set()

    def ask(d0):
        nonlocal fare
        tried.add(d0)
        try:
            r = ct_avl(job["train"], job["from"], to, cls, dt.date.fromisoformat(d0), quota)
        except Exception as e:
            errors.append(f"{d0}: {e}")
            return None
        finally:
            time.sleep(pause)
        if r["days"]:
            fare = fare or r["fare"]
            stamps.append(r["timeStamp"])
        return r

    for _ in range(len(need) + 1):
        missing = [d for d in need if d not in got and d not in tried]
        if not missing:
            break
        r = ask(missing[0])
        if r and r["days"]:
            got.update(r["days"])
        elif r:
            errors.append(f"{missing[0]}: {r['error']}")

    # ConfirmTkt caches per (train, class, from, to, start date): re-asking the same start date
    # returns the same snapshot, but an earlier start date is a different key whose answer
    # still contains the target date. Use that to refresh stale dates.
    if max_age:
        for d in need:
            age = age_minutes((got.get(d) or {}).get("ts"))
            if age is None or age <= max_age:
                continue
            attempts = 0
            for k in range(1, 8):  # non-running start dates just fail with 533, so walk back a week
                start = (dt.date.fromisoformat(d) - dt.timedelta(days=k)).isoformat()
                if start in tried:
                    continue
                if attempts == 3:
                    break
                attempts += 1
                r = ask(start)
                x = r and r["days"].get(d)
                if x and (age_minutes(x.get("ts")) or 1e9) < age:
                    for dd, v in r["days"].items():  # take every date this answer improves
                        if dd in got and (age_minutes(v.get("ts")) or 1e9) < (age_minutes(got[dd].get("ts")) or 1e9):
                            got[dd] = v
                    if age_minutes(x.get("ts")) <= max_age:
                        break
    return f"{job['train']}|{job['from']}|{cls}", {"days": got, "fare": fare, "timeStamps": stamps,
                                                    "errors": errors, "needed": need}


def cmd_fetch(a):
    plan = json.load(open(a.plan))
    classes = [c.strip().upper() for c in a.classes.split(",") if c.strip()]
    results = json.load(open(a.out)) if a.resume and os.path.exists(a.out) else {}
    has = {t["train"]: set(t.get("classes") or []) for t in plan["trains"]}
    lacking = sorted({f"{j['train']} {c}" for j in plan["jobs"] for c in classes
                      if has.get(j["train"]) and c not in has[j["train"]]})
    if lacking:
        log("skipping classes the train does not have: " + ", ".join(lacking))
    tasks = [(j, c) for j in plan["jobs"] for c in classes
             if f"{j['train']}|{j['from']}|{c}" not in results
             and (not has.get(j["train"]) or c in has[j["train"]])]
    log(f"{len(tasks)} lookups, {a.workers} workers (ConfirmTkt live API)")
    with ThreadPoolExecutor(max_workers=a.workers) as ex:
        futs = [ex.submit(fetch_one, j, c, j.get("to", plan["to"]), a.quota, a.pause, a.max_age)
                for j, c in tasks]
        for i, f in enumerate(as_completed(futs), 1):
            key, val = f.result()
            results[key] = val
            short = " ".join(f"{d[5:]}={v['status']}" for d, v in sorted(val["days"].items())
                             if d in val["needed"])
            log(f"[{i}/{len(tasks)}] {key} {short}{'  ERR ' + '; '.join(val['errors']) if val['errors'] else ''}")
            if i % 10 == 0:
                dump(results, a.out)
    dump(results, a.out)
    log(f"saved {a.out}")


# ---------------------------------------------------------------- report

LABELS = {
    "hinglish": {
        "title": "{frm} → {to} | {n0} – {n1}", "night": "raat", "train": "Train",
        "book": "Kahan se book karna hai", "board": "{frm} time", "arr": "{to} arrival",
        "detail": "Har from-station ka poora data (fare = us station se {to} tak)",
        "from": "From (ticket date)", "dep": "Dep", "pool": "Pool", "ticket": "ticket",
        "skipped": "Skip ki gayi trains", "errors": "Jo fetch nahi ho paaya",
        "legend": ("(+1) = agla din. `@STN` = is class ke liye best from-station; bracket mein {frm} ka apna "
                   "status. % = ConfirmTkt ka confirm chance (sirf andaza). Same Pool letter = un stations "
                   "ka status bilkul same hai (ek hi quota), wahan se book karne ka fayda nahi. † = ConfirmTkt ka purana cache (2 ghante se zyada), IRCTC pe dobara dekho."),
        "source": "Data: ConfirmTkt live availability (IRCTC se), {t0} – {t1} IST. Route/timetable: erail.in.",
        "none": "Is raat koi train nahi.",
    },
    "en": {
        "title": "{frm} → {to} | {n0} – {n1}", "night": "night", "train": "Train",
        "book": "Book from", "board": "{frm} dep", "arr": "{to} arr",
        "detail": "Every from-station (fare = that station to {to})",
        "from": "From (ticket date)", "dep": "Dep", "pool": "Pool", "ticket": "ticket",
        "skipped": "Skipped trains", "errors": "Not fetched",
        "legend": ("(+1) = next day. `@STN` = best from-station for that class; brackets show {frm}'s own "
                   "status. % = ConfirmTkt confirmation estimate. Same Pool letter = identical status "
                   "(same quota), so booking from there gains nothing. † = older ConfirmTkt cache snapshot (>2 h); re-check on IRCTC."),
        "source": "Data: ConfirmTkt live availability (sourced from IRCTC), {t0} – {t1} IST. Route/timetable: erail.in.",
        "none": "No train this night.",
    },
}


def cmd_report(a):
    plan = json.load(open(a.plan))
    res = json.load(open(a.avl))
    L = LABELS[a.lang]
    classes = [c.strip().upper() for c in a.classes.split(",")] if a.classes else \
        sorted({k.split("|")[2] for k in res}, key=lambda c: ["SL", "3E", "3A", "2A", "1A", "CC", "EC", "2S"].index(c)
               if c in ["SL", "3E", "3A", "2A", "1A", "CC", "EC", "2S"] else 99)
    frm, to = plan["from"], plan["to"]
    fmt = lambda iso: dt.date.fromisoformat(iso).strftime("%d %b")

    def cell(tno, stn, cls, date):
        r = res.get(f"{tno}|{stn}|{cls}")
        x = r and r["days"].get(date)
        if not x:
            return None
        rank, label = parse_status(x.get("status"))
        pred = (x.get("pred") or "").replace(" Chance", "") if rank[0] == 2 else ""
        age = age_minutes(x.get("ts")) if x.get("ts") else None
        if age is not None and a.stale and age > a.stale:
            label += "†"
        return {"rank": rank, "label": label, "pred": pred, "fare": r.get("fare"), "raw": x.get("status"),
                "ts": x.get("ts")}

    def show(c, pred=True, bold=True):
        if not c:
            return "n/a"
        s = f"**{c['label']}**" if bold and c["rank"][0] in (0, 1) else c["label"]
        return s + (f" ({c['pred']})" if pred and c["pred"] else "")

    def sdate(night, st):
        return (dt.date.fromisoformat(night) + dt.timedelta(days=st["offset"])).isoformat()

    # quota pools: stations whose status is identical in every class on every night
    pools = {}
    for t in plan["trains"]:
        sig_to_letter = {}
        for st in sorted(t["stations"], key=lambda s: -s["seq"]):
            sig = tuple((cell(t["train"], st["code"], c, sdate(n, st)) or {}).get("raw")
                        for c in classes for n in t["nights"])
            if not any(sig):
                pools[(t["train"], st["code"])] = "-"
                continue
            sig_to_letter.setdefault(sig, "ABCDEFGHIJKLMNOP"[len(sig_to_letter)])
            pools[(t["train"], st["code"])] = sig_to_letter[sig]

    stamps = sorted(s for v in res.values() for s in v.get("timeStamps", []) if s)
    head = [f"# {L['title'].format(frm=frm, to=to, n0=fmt(plan['nights'][0]), n1=fmt(plan['nights'][-1]))}", "",
            L["source"].format(t0=stamps[0][:16].replace('T', ' ') if stamps else "?",
                               t1=stamps[-1][:16].replace('T', ' ') if stamps else "?"),
            f"Window {plan['window'][0]}–{plan['window'][1]} at {frm}." +
            (f" Only trains ending at {to}." if plan.get("must_end_at_dest") else ""),
            "", L["legend"].format(frm=frm), ""]
    summary, detail, best_json = list(head), list(head) + [f"## {L['detail'].format(to=to)}"], []

    for night in plan["nights"]:
        nd = dt.date.fromisoformat(night)
        h = f"\n### {nd.strftime('%d %b %Y')} ({WEEKDAYS[nd.weekday()]}) {L['night']}\n"
        summary.append(h)
        detail.append(h)
        tr = [t for t in plan["trains"] if night in t["nights"]]
        if not tr:
            summary.append(L["none"])
            detail.append(L["none"])
            continue
        summary += [f"| {L['train']} | {L['book']} | {L['board'].format(frm=frm)} | {L['arr'].format(to=to)} | "
                    + " | ".join(classes) + " |", "|---" * (4 + len(classes)) + "|"]
        detail += [f"| {L['train']} | {L['from']} | {L['dep']} | {L['pool']} | " + " | ".join(classes) + " |",
                   "|---" * (4 + len(classes)) + "|"]
        for t in sorted(tr, key=lambda t: (t["board"]["offset"], minutes(t["board"]["dep"]))):
            stations = sorted(t["stations"], key=lambda s: s["seq"])
            board_st = next(s for s in stations if s["is_board"])
            entry = {"train": t["train"], "name": t["name"], "night": night, "board_dep": t["board"]["dep"],
                     "board_date": sdate(night, board_st), "dest_arr": t["dest_arr"],
                     "arr_offset": t["arr_offset"], "classes": {}}
            cols, places = [], {}
            for cls in classes:
                cands = [(cell(t["train"], s["code"], cls, sdate(night, s)), s) for s in stations]
                cands = [(c, s) for c, s in cands if c]
                bc = cell(t["train"], board_st["code"], cls, sdate(night, board_st))
                if not cands:
                    cols.append("n/a")
                    continue
                c, s = min(cands, key=lambda cs: (cs[0]["rank"], -cs[1]["seq"]))  # tie -> nearest to boarding
                txt = show(c)
                if not s["is_board"]:
                    extra = c["fare"] - bc["fare"] if bc and c["fare"] and bc["fare"] else None
                    txt += f" @{s['code']}" + (f" +₹{extra:.0f}" if extra else "") + \
                        (f" ({frm}: {show(bc, pred=False, bold=False)})" if bc else "")
                cols.append(txt)
                d = sdate(night, s)
                place = s["code"] + (f" ({L['ticket']} {fmt(d)})" if d != entry["board_date"] else "")
                places.setdefault(place, []).append(cls)
                entry["classes"][cls] = {"from": s["code"], "ticket_date": d, "status": c["label"],
                                         "raw": c["raw"], "pred": c["pred"], "fare": c["fare"],
                                         "board_status": bc["label"] if bc else None,
                                         "board_fare": bc["fare"] if bc else None}
            book = places and (next(iter(places)) if len(places) == 1 else
                               " · ".join(f"{'/'.join(v)}: {k}" for k, v in places.items())) or "n/a"
            bdep = t["board"]["dep"] + (" (+1)" if t["board"]["offset"] else "")
            if t["board"]["code"] != frm:
                bdep += f" {t['board']['code']}"
            arr = t["dest_arr"] + (f" (+{t['arr_offset']})" if t["arr_offset"] else "")
            if t.get("to", to) != to:
                arr += f" {t['to']}"
            summary.append(f"| {t['train']} {t['name']} | {book} | {bdep} | {arr} | " + " | ".join(cols) + " |")
            best_json.append(entry)
            for s in stations:
                d = sdate(night, s)
                row = []
                for cls in classes:
                    c = cell(t["train"], s["code"], cls, d)
                    row.append(show(c) + (f" ₹{c['fare']:.0f}" if c and c["fare"] else "") if c else "n/a")
                detail.append(f"| {t['train']} {t['name']} | {s['name']} ({s['code']}) {fmt(d)} | {s['dep']} | "
                              f"{pools.get((t['train'], s['code']), '-')} | " + " | ".join(row) + " |")

    tail = []
    if plan["skipped"]:
        tail += [f"\n## {L['skipped']}", ""] + [f"- {s['train']} {s['name']}: {s['reason']}" for s in plan["skipped"]]
    # Classes ConfirmTkt sells but erail's coach list doesn't show: the WL may never confirm.
    doubt = sorted({(t["train"], c) for t in plan["trains"] for c in classes
                    if t.get("coaches") and c not in t["coaches"]
                    and any(res.get(f"{t['train']}|{s['code']}|{c}", {}).get("days") for s in t["stations"])})
    if doubt:
        note = (" — erail ki coach list mein is class ka coach nahi dikhta; IRCTC pe class dikhe tabhi bharosa karo."
                if a.lang == "hinglish" else
                " — not in erail's coach composition; trust only if IRCTC lists the class.")
        tail += ["", "Coach check: " + ", ".join(f"{tn} {c}" for tn, c in doubt) + note]
    errs = [(k, v) for k, v in res.items() if v.get("errors") or set(v.get("needed", [])) - set(v["days"])]
    if errs:
        tail += [f"\n## {L['errors']}", ""]
        for k, v in sorted(errs):
            miss = sorted(set(v.get("needed", [])) - set(v["days"]))
            tail.append(f"- {k}: missing {', '.join(miss) or '-'}; {'; '.join(v.get('errors', []))[:200]}")
    summary += tail
    detail += tail

    os.makedirs(a.outdir, exist_ok=True)
    open(os.path.join(a.outdir, "summary.md"), "w").write("\n".join(summary) + "\n")
    open(os.path.join(a.outdir, "detail.md"), "w").write("\n".join(detail) + "\n")
    dump(best_json, os.path.join(a.outdir, "best.json"))
    print("\n".join(summary))
    log(f"wrote summary.md, detail.md, best.json to {a.outdir}")


# ---------------------------------------------------------------- small commands

def cmd_trains(a):
    rows = erail_trains(a.frm.upper(), a.to.upper())
    if a.json:
        print(json.dumps(rows, indent=1))
        return
    for r in sorted(rows, key=lambda r: minutes(r["dep"]) if re.fullmatch(r"\d\d:\d\d", r["dep"]) else 0):
        print(f"{r['train']:>6} {r['name'][:22]:22} {r['origin']:>5}->{r['dest']:<5} "
              f"dep {r['dep']} arr {r['arr']} ({r['duration']}) {fmt_days(r['days'])}  erail_id={r['erail_id']}")


def cmd_route(a):
    stops, info = route_for(a.train)
    if a.json:
        print(json.dumps({"info": info, "stops": stops}, indent=1))
        return
    if info:
        print(f"{info['train']} {info['name']}: {info['origin']} -> {info['dest']}, runs {fmt_days(info['days'])} "
              f"(at origin)")
    for s in stops:
        print(f"{s['seq']:>3} {s['code']:<6} {s['name'][:28]:28} arr {s['arr']:>5} dep {s['dep']:>5} "
              f"halt {s['halt']:>2}m day {s['day']} {s['km']} km")


def cmd_ct_search(a):
    d = a.date
    for i in range(a.days):
        r = ct_search(a.frm.upper(), a.to.upper(), d + dt.timedelta(days=i))
        if a.json:
            print(json.dumps(r, indent=1))
            continue
        print(f"== {r['date']} ({WEEKDAYS[(d + dt.timedelta(days=i)).weekday()]}) {r['source']} -> {r['destination']}"
              + (f"  error: {r['error']}" if r["error"] else ""))
        for t in r["trains"]:
            cached = " ".join(f"{c}:{v['status']}" for c, v in t["cached_avl"].items())
            print(f"  {t['train']:>6} {str(t['name'])[:22]:22} {t['origin']}->{t['dest']} dep {t['dep']} "
                  f"arr {t['arr']} {fmt_days(t['days'])} | cached {cached}")
        time.sleep(0.5)


def cmd_avl(a):
    r = ct_avl(a.train, a.frm.upper(), a.to.upper(), a.cls.upper(), a.date, a.quota)
    if a.json:
        print(json.dumps(r, indent=1))
        return
    print(f"{a.train} {a.frm.upper()}->{a.to.upper()} {a.cls.upper()} {a.quota} fare {r['fare']} "
          f"(as of {r['timeStamp']} IST){'  error: ' + str(r['error']) if r['error'] else ''}")
    for d, v in sorted(r["days"].items()):
        print(f"  {d} {WEEKDAYS[dt.date.fromisoformat(d).weekday()]}  {v['status']:<22} {v['pred'] or ''}")


def main():
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = p.add_subparsers(dest="cmd", required=True)

    s = sub.add_parser("trains", help="trains between two stations (erail.in)")
    s.add_argument("--from", dest="frm", required=True)
    s.add_argument("--to", required=True)
    s.add_argument("--json", action="store_true")
    s.set_defaults(fn=cmd_trains)

    s = sub.add_parser("route", help="full route of a train (erail.in)")
    s.add_argument("--train", required=True)
    s.add_argument("--json", action="store_true")
    s.set_defaults(fn=cmd_route)

    s = sub.add_parser("ct-search", help="ConfirmTkt train list per date (running days, specials, cached avl)")
    s.add_argument("--from", dest="frm", required=True)
    s.add_argument("--to", required=True)
    s.add_argument("--date", type=parse_date, required=True, help="boarding-station date")
    s.add_argument("--days", type=int, default=1, help="how many consecutive dates")
    s.add_argument("--json", action="store_true")
    s.set_defaults(fn=cmd_ct_search)

    s = sub.add_parser("avl", help="one live availability lookup (ConfirmTkt)")
    s.add_argument("--train", required=True)
    s.add_argument("--from", dest="frm", required=True)
    s.add_argument("--to", required=True)
    s.add_argument("--cls", default="3A")
    s.add_argument("--date", type=parse_date, required=True, help="date at the --from station")
    s.add_argument("--quota", default="GN")
    s.add_argument("--json", action="store_true")
    s.set_defaults(fn=cmd_avl)

    s = sub.add_parser("plan", help="build the search plan")
    s.add_argument("--from", dest="frm", required=True, help="station where the traveller boards")
    s.add_argument("--to", required=True)
    s.add_argument("--start", type=parse_date, required=True, help="first night (YYYY-MM-DD)")
    s.add_argument("--end", type=parse_date, required=True, help="last night (YYYY-MM-DD)")
    s.add_argument("--window", default=None,
                   help="departure window at --from, e.g. 18:00-02:00 (wraps midnight; after-midnight "
                        "departures count for the previous night)")
    s.add_argument("--must-end-at-dest", action="store_true", help="drop trains that run beyond --to")
    s.add_argument("--upstream", default="auto", help="'auto' or comma list of earlier stations to query")
    s.add_argument("--extra-upstream", default="", help="comma list always added when on the route")
    s.add_argument("--max-upstream", type=int, default=6, help="cap for auto-picked stations (origin added on top)")
    s.add_argument("--no-upstream", action="store_true", help="only query the boarding station")
    s.add_argument("--include", default="", help="only these train numbers")
    s.add_argument("--exclude", default="", help="skip these train numbers")
    s.add_argument("--add-train", default="", help="train numbers to add even if the pair search misses them")
    s.add_argument("--out", default="plan.json")
    s.set_defaults(fn=cmd_plan)

    s = sub.add_parser("fetch", help="live availability for every plan job")
    s.add_argument("--plan", required=True)
    s.add_argument("--classes", default="SL,3A,2A")
    s.add_argument("--quota", default="GN")
    s.add_argument("--workers", type=int, default=6)
    s.add_argument("--pause", type=float, default=1.0, help="seconds between calls per worker")
    s.add_argument("--resume", action="store_true", help="keep results already in --out")
    s.add_argument("--max-age", type=float, default=90,
                   help="minutes; dates served from an older ConfirmTkt cache snapshot are re-asked via an "
                        "earlier start date (0 = off)")
    s.add_argument("--out", default="avl.json")
    s.set_defaults(fn=cmd_fetch)

    s = sub.add_parser("report", help="markdown tables from plan + availability")
    s.add_argument("--plan", required=True)
    s.add_argument("--avl", required=True)
    s.add_argument("--classes", default="", help="default: classes present in --avl")
    s.add_argument("--lang", choices=sorted(LABELS), default="hinglish")
    s.add_argument("--outdir", default=".")
    s.add_argument("--stale", type=float, default=120,
                   help="mark cells whose ConfirmTkt snapshot is older than this many minutes with † (0 = off)")
    s.set_defaults(fn=cmd_report)

    a = p.parse_args()
    a.fn(a)


if __name__ == "__main__":
    main()
