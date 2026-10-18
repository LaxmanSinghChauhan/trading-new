# Worked example: Ganj Basoda (BAQ) → Hazrat Nizamuddin (NZM), nights 10–15 Nov 2026

The search was run on 18–19 Oct 2026. Use this as the model for depth and tone, not as current
data.

## Request (paraphrased from the user, in Hinglish)

- BAQ → NZM, 10 to 15 Nov 2026. Departure from BAQ 18:00–01:00 (02:00 is OK too).
- The train must END at NZM (no trains continuing to Delhi/Katra/Amritsar).
- The user boards at BAQ, but also wanted tickets from earlier junctions checked (Bhopal, Rani
  Kamlapati, Vidisha, Itarsi, Jabalpur...).
- The date counts at the origin side: a train reaching BAQ after midnight belongs to the previous
  night.
- Tools in order: railway MCP → ixigo/ConfirmTkt/IRCTC → erail. Don't ask questions. No booking,
  login, captcha or payment.
- Output: one Hinglish table per date (Train | Kahan se book karna hai | BAQ time | NZM arrival |
  SL/3A/2A), then "Best option" in 3 lines, then what couldn't be verified.

## Commands

```bash
T=<skill-dir>/scripts/trainsearch.py
python3 -I $T plan --from BAQ --to NZM --start 2026-11-10 --end 2026-11-15 \
  --window 18:00-02:00 --must-end-at-dest --extra-upstream BPL,RKMP,BHS,ET,JBP --out plan.json
python3 -I $T ct-search --from BAQ --to NZM --date 2026-11-10 --days 7      # running days / specials
python3 -I $T fetch --plan plan.json --classes SL,3A,2A --out avl.json      # 102 lookups, ~4 min
python3 -I $T report --plan plan.json --avl avl.json --outdir . --lang hinglish
```

## What the plan found

- Kept: 12721 Dakshin (HYB→NZM, BAQ 18:20, NZM 03:45 +1, daily); 12405 Gondwana (BSL→NZM, BAQ 20:38,
  NZM 05:00 +1, **Tue & Sun only**); 12409 Gondwana SF (RIG→NZM, same times, **Mon, Wed–Sat**);
  12155 RKMP→NZM (BAQ 23:59, NZM 08:00 +1, daily); 12192 JBP→NZM (BAQ **01:05 next day**, NZM 12:00,
  daily).
- Skipped because they don't end at NZM: 11077, 12137, 12615, 12919, 18237, 11057, 20423.
- No November 2026 special train announced on this pair at the time.

## Key findings

- On 12721, 12405/09 and 12192, the stations BPL, RKMP, BHS and BAQ were one RL pool with identical
  numbers, so booking there gave no gain.
- 12192: Itarsi (ET) shares Jabalpur's GN quota. On 12, 13, 14 and 15 Nov an ET→NZM ticket showed SL
  RAC 26–36 and 3A RAC 18–29, while BAQ showed RLWL. The ET fare was only ₹75 (SL) / ₹190 (3A) more.
- 12155: RKMP/BPL sell GN, while BAQ/BHS get PQ. SL from BPL was RAC 84 (12 Nov) and RAC 38 (13 Nov).
- 12409 on 11 Nov was REGRET from BAQ/BPL/BHS in every class. Only Itarsi/Nagpur still sold (WL).

## Final answer (shape)

- Per-night tables like those `summary.md` produces, with judgment applied: no Hyderabad picks for
  a ₹1300 premium, and RAC called out in bold.
- Best option:
  1. 15 Nov (Sun) night, 12192 JBP–NZM: BAQ 16 Nov 01:05 → NZM 16 Nov 12:00.
  2. Ticket Itarsi (ET) → NZM, journey date 15 Nov, Boarding Station Ganj Basoda: 3A RAC 18
     (₹1205), SL RAC 26 (₹470).
  3. For an 08:00 arrival: 13 Nov night, 12155, ticket Bhopal → NZM, SL RAC 38. For 2A, BAQ direct
     PQWL 1.
- Not verified: IRCTC/ixigo blocked (403), railway MCP down (525); ConfirmTkt % is only an
  estimate; some rows were cached for hours; boarding-point change rule not checked; specials may
  still be announced; 3E/1A not checked.
