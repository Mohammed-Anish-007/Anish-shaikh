# कार्गो मुनीम PRO (Cargo Munim Pro) — Import / Export ERP

The advanced version of Cargo Munim. Still made for people who cannot read: pictures, colours, a big number pad and a voice that reads everything out (Hindi / English). Under the hood it is a full small-business ERP.

## Run it
| Where | How | Data |
|---|---|---|
| **claude.ai artifact** (team) | open the shared link | one live database shared by everyone you share the page with; every entry carries the person's name |
| **Desktop / laptop** | double-click `index.html` | stays in that browser; use ⚙️ → Backup to move it |

## What's new compared with the basic version
- **Team data + activity log**: everyone sees the same numbers live; "who did what" screen.
- **Roles with PIN**: 👑 Owner, 🧮 Accountant (cannot delete / change settings), 👷 Worker (sees only stock and shipments, no money).
- **Multi-item bills**, automatic invoice numbers (`INV-2026-0001`), purchase bills (`PB-…`).
- **Multi-currency**: USD, AED, EUR, GBP, SAR with your exchange rates; every amount also kept in ₹.
- **Payment terms & due dates**: Advance / 15 / 30 / 60 / 90 days, Incoterm (EXW/FOB/CFR/CIF), part payments matched to the oldest bills, ✅ Paid / 🌓 Part / ⏰ Overdue, aging (1–30 / 31–60 / 60+ days), credit limit alerts.
- **Documents**: Commercial Invoice, Proforma Invoice, Packing List (HS code, packages, net weight, bank details, IEC/GSTIN) — download as a file, print on desktop, share on WhatsApp.
- **Shipments**: route picture with the ship moving between ports, vessel / container / B/L, 8-document checklist (Invoice, Packing list, Shipping bill, B/L, COO, Phyto, Insurance, LC), WhatsApp update to the party.
- **Expenses**: freight, customs duty, CHA, insurance, transport, packing, bank charges — linked to shipments → **profit per shipment**.
- **Reports**: 6-month sales / purchase / expense chart with profit, profit by item, exports by country, top buyers, RoDTEP + Drawback estimate, CSV/Excel downloads.
- **Stock intelligence**: "~N days of stock left", goods on the way, re-order suggestion, reason for stock changes (count / damaged / sample).
- **🎤 Speak an entry**: "अल नूर को 500 किलो बासमती 96 रुपये में दिया" → filled bill to confirm. Works offline with a built-in parser; uses Claude for better understanding inside claude.ai.
- **🤖 Ask Munim ji** (inside claude.ai): ask questions about your business in Hindi; the answer is spoken back.
- **💬 WhatsApp payment reminders** with the open invoice numbers.
- Search (items, people, invoice numbers, containers, B/L), desktop sidebar layout, dark mode.

## Notes
- PINs switch roles on a shared device; they are not strong security.
- The shared database reads up to 1,000 records per list (entries, items…). For more history, export CSV and start a new year.

## Development
Edit `src/app.html`, then run `./build.sh` to regenerate `index.html`.
