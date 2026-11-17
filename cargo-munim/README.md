# कार्गो मुनीम (Cargo Munim) — Import / Export ERP

A simple ERP for an import–export business, designed so that **even a person who cannot read can use it**:
big pictures, colours (green = money coming to us, red = money we owe), and a **voice** that speaks every button in Hindi or English.

## Open it
- **Desktop / laptop / phone:** double-click `index.html` (works offline, no install, no server).
- Data is saved automatically in that browser. Use **⚙️ Settings → Backup** to download a backup file and **Restore** it on another computer.

## What it does
| Screen | Shows |
|---|---|
| 🏠 Home | लेना है (to receive), देना है (to pay), godown value, shipments on the way, alerts (low stock, late shipments, goods arriving soon), countries where goods are going / coming from, recent entries |
| 📦 Stock (माल) | Every item with photo, stock bar (red when low), buy & sell price, price trend chart, who got how much of it, who supplied it, stock in / out, change price |
| 🚢 Shipments | Export & import with country flag and 5 picture stages: 📦 Packing → ⚓ Port → 🚢 At sea → 🛃 Customs → ✅ Delivered, days left / days late. Import goods are added to stock automatically when delivered |
| 👥 People (लोग) | Buyers & suppliers with photo, country, phone, balance, full ledger, which goods we gave them / got from them |
| ➕ New entry | 4 picture buttons: 📤 Gave goods (sale), 📥 Got goods (purchase), 💰 Money received, 💸 Money paid — step by step with pictures and a big number pad |

## Accessibility for non-readers
- 🔊 Voice on: tapping anything reads it aloud (Hindi `hi-IN` voice when installed on the device).
- Pictures for every item / person (choose an icon or take a photo).
- Every wizard step asks one question aloud; final screen shows a picture "equation": Us ➡️ goods ➡️ person = total.
- Switch Hindi / English with the **EN / हि** button.

## Development
Edit `src/app.html`, then run `./build.sh` to regenerate `index.html`.
