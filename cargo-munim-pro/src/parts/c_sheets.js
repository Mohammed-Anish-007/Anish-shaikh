
/* ================= DRAWERS ================= */
const SH = {};
const drHd = (title, sub, extra = '') => `<div class="sheet-hd"><h2>${title}${sub ? ` <small>${sub}</small>` : ''}</h2>${extra}<button class="x" data-act="close" aria-label="close">${I('x', 18)}</button></div>`;
SH.alerts = () => { const A = alerts(); return drHd(T('ध्यान दो','Needs attention'), `${A.length}`) + `<section class="card">${alertRows(A)}</section>`; };

SH.item = () => {
  const i = item(sheet.id); if (!i) return closeMissing();
  const st = stockOf(i.id), low = st < (+i.min || 0), d = C.days[i.id], avg = C.sold60[i.id] / 60;
  const reorder = Math.max(0, Math.ceil(avg * 45 - st - (C.incoming[i.id] || 0)));
  const sales = S.txns.filter(x => x.type === 'sale' && (x.lines || []).some(l => l.item === i.id)), buys = S.txns.filter(x => x.type === 'buy' && (x.lines || []).some(l => l.item === i.id));
  const agg = arr => { const m = {}; for (const x of arr) for (const l of x.lines) if (l.item === i.id){ const r = m[x.party] = m[x.party] || {q: 0, a: 0}; r.q += l.qty; r.a += l.qty * l.rate; } return Object.entries(m).map(([pid, r]) => ({p: party(pid), ...r})).filter(r => r.p).sort((a, b) => b.q - a.q); };
  const aggList = rows => rows.length ? `<div class="list">${rows.map(r => `<button class="li" data-act="open-party" data-id="${r.p.id}" data-say="${esc(r.p.name + ', ' + qtyFmt(r.q) + ' ' + i.unit)}">${pic(r.p, 'sm')}<div class="body"><div class="t1">${esc(r.p.name)}</div><div class="t2">${flag(r.p.country)} ${esc(cname(r.p.country))}</div></div><div class="rt"><div class="num" style="font-weight:600">${qtyFmt(r.q)} ${esc(i.unit)}</div>${seeMoney() ? `<div class="t2 num">${money(r.a)}</div>` : ''}</div></button>`).join('')}</div>` : `<div class="list"><div class="empty">${T('अभी कुछ नहीं है','Nothing yet')}</div></div>`;
  return drHd(esc(i.name), i.hs ? 'HS ' + esc(i.hs) : '', P.role !== 'worker' ? `<button class="x" data-act="item-form" data-id="${i.id}" aria-label="edit">${I('edit', 16)}</button>` : '') + `
  <div class="dr-hero" data-say="${esc(i.name + '. ' + qtyFmt(st) + ' ' + i.unit + '. ' + daysTxt(d))}">${pic(i, 'big')}<div><h3>${esc(i.name)}</h3><div class="muted">${esc(i.alias || '')}${i.pack ? ` · ${qtyFmt(i.pack)} ${esc(i.unit)} / ${T('पैकेज','package')}` : ''}</div>
    <div class="row" style="gap:6px;margin-top:8px">${low ? status('bad', T('माल कम है','Low stock')) : d < 30 ? status('warn', T('खत्म होने वाला','Running low')) : status('good', T('ठीक है','In stock'))}${C.incoming[i.id] ? status('info', `${qtyFmt(C.incoming[i.id])} ${T('आ रहा','incoming')}`) : ''}</div></div></div>
  <div class="kv"><div><small>${T('गोदाम में','In stock')}</small><b class="${low ? 'bad' : ''} num">${qtyFmt(st)} ${esc(i.unit)}</b></div><div><small>${T('कितने दिन का','Days left')}</small><b class="num">${d === Infinity ? '—' : '~' + d}</b></div>
    ${seeMoney() ? `<div><small>${T('खरीद भाव','Buy price')}</small><b class="num">${money(i.buy)}</b></div><div><small>${T('बिक्री भाव','Sell price')}</small><b class="num">${money(i.sell)}</b><span class="muted num" style="font-size:12px">$${(i.sell / fxOf('USD')).toFixed(2)} · ${i.sell ? Math.round((i.sell - i.buy) / i.sell * 100) : 0}% ${T('मार्जिन','margin')}</span></div>` : ''}</div>
  ${reorder > 0 && P.role !== 'worker' ? `<div class="note info">${I('trend', 16)}<span><b>${T('सुझाव: और मँगाओ','Suggestion: reorder')} — ${qtyFmt(reorder)} ${esc(i.unit)}</b><br>${T('पिछले 60 दिन की बिक्री के हिसाब से 45 दिन का माल','45 days of stock, based on the last 60 days of sales')}</span></div>` : ''}
  <div class="act-row">
    <button data-act="adj" data-id="${i.id}" data-m="in" data-say="${T('माल आया','Stock in')}"><span class="sq green">${I('plus', 18)}</span>${T('माल आया','Stock in')}</button>
    <button data-act="adj" data-id="${i.id}" data-m="out" data-say="${T('माल गया','Stock out')}"><span class="sq red">${I('minus', 18)}</span>${T('माल गया','Stock out')}</button>
    ${seeMoney() ? `<button data-act="price" data-id="${i.id}" data-say="${T('भाव बदलो','Change price')}"><span class="sq blue">${I('tag', 18)}</span>${T('भाव बदलो','Change price')}</button>` : ''}
  </div>
  ${seeMoney() ? `<section class="panel"><h3>${T('भाव कैसे बदला','Price history')}</h3>${priceChart(i)}</section>` : ''}
  <h3 class="dr-sec">${T('किसको कितना माल दिया','Sold to')}</h3>${aggList(agg(sales))}
  <h3 class="dr-sec">${T('किससे कितना माल आया','Bought from')}</h3>${aggList(agg(buys))}
  ${isOwner() ? `<button class="btn ${armed === 'del-item' ? 'armed' : 'bad'}" data-act="del-item" data-id="${i.id}">${I('trash', 16)} ${armed === 'del-item' ? T('पक्का? फिर दबाओ','Confirm delete') : T('ये माल मिटाओ','Delete item')}</button>` : ''}`;
};

SH.party = () => {
  const p = party(sheet.id); if (!p) return closeMissing();
  const b = C.bal[p.id] || 0, tx = S.txns.filter(x => x.party === p.id).sort((a, c) => c.d.localeCompare(a.d)), ov = C.pOver[p.id];
  const aggItems = type => { const m = {}; for (const x of tx.filter(x => x.type === type)) for (const l of x.lines || []){ const r = m[l.item] = m[l.item] || {q: 0, a: 0}; r.q += l.qty; r.a += l.qty * l.rate; } return Object.entries(m).map(([iid, r]) => ({i: item(iid), ...r})).filter(r => r.i); };
  const rows = rs => `<div class="list">${rs.map(r => `<button class="li" data-act="open-item" data-id="${r.i.id}" data-say="${esc(r.i.name + ' ' + qtyFmt(r.q) + ' ' + r.i.unit)}">${pic(r.i, 'sm')}<div class="body"><div class="t1">${esc(r.i.name)}</div></div><div class="rt"><div class="num" style="font-weight:600">${qtyFmt(r.q)} ${esc(r.i.unit)}</div>${seeMoney() ? `<div class="t2 num">${money(r.a)}</div>` : ''}</div></button>`).join('')}</div>`;
  const given = aggItems('sale'), got = aggItems('buy'), ps = S.ships.filter(s => s.party === p.id && s.stage < 4);
  const openInv = tx.filter(x => x.type === 'sale' && C.inv[x.id]?.open > .5);
  const msg = p.country === 'IN' && hi()
    ? `नमस्ते ${p.name}, आपका ${money(b)} बकाया है${openInv.length ? ` (${openInv.map(x => x.inv).join(', ')})` : ''}। कृपया जल्दी भुगतान करें। धन्यवाद — ${S.settings.company || ''}`
    : `Dear ${p.name}, a payment of ${money(b)}${p.cur && p.cur !== 'INR' ? ` (≈ ${fmoney(b / fxOf(p.cur), p.cur)})` : ''} is pending${openInv.length ? ` against ${openInv.map(x => x.inv).join(', ')}` : ''}. Kindly arrange the remittance. Thank you — ${S.settings.company || ''}`;
  const wa = b > 0 ? waLink(p.phone, msg) : '';
  return drHd(esc(p.name), '', P.role !== 'worker' ? `<button class="x" data-act="party-form" data-id="${p.id}" aria-label="edit">${I('edit', 16)}</button>` : '') + `
  <div class="dr-hero">${pic(p, 'big')}<div><h3>${esc(p.name)}</h3><div class="muted">${flag(p.country)} ${esc(cname(p.country))}${p.phone ? ` · <span class="num" style="user-select:all">${esc(p.phone)}</span>` : ''}</div>
    <div class="row" style="gap:6px;margin-top:8px">${p.kind === 'customer' ? status('info', T('ग्राहक','Buyer')) : p.kind === 'supplier' ? status('violet', T('सप्लायर','Supplier')) : status('teal', T('दोनों','Both'))}${p.cur && p.cur !== 'INR' ? status('gray', p.cur) : ''}${ov > 0 ? status('bad', `${money(ov)} ${T('लेट','overdue')}`) : ''}</div></div></div>
  ${seeMoney() ? `<div class="kv" data-say="${esc(b > 0 ? T('ये हमें देंगे','They will pay us') + ' ' + sayMoney(b) : b < 0 ? T('हमें इन्हें देना है','We must pay them') + ' ' + sayMoney(b) : T('हिसाब बराबर','All settled'))}">
    <div><small>${b > 0 ? T('हमें मिलेगा','Receivable') : b < 0 ? T('हमें देना है','Payable') : T('बकाया','Balance')}</small><b class="${b > 0 ? 'good' : b < 0 ? 'bad' : ''} num">${money(b)}</b>${p.cur && p.cur !== 'INR' && b ? `<span class="muted num" style="font-size:12px">≈ ${fmoney(b / fxOf(p.cur), p.cur)}</span>` : ''}</div>
    <div><small>${T('लेट रकम','Overdue')}</small><b class="${ov > 0 ? 'bad' : ''} num">${money(ov)}</b></div>
    <div><small>${T('उधार दिन','Credit days')}</small><b class="num">${+p.terms || 0}</b></div><div><small>${T('उधार सीमा','Credit limit')}</small><b class="num">${+p.limit ? money(p.limit) : '—'}</b></div></div>
  ${wa ? `<a class="btn wa" href="${esc(wa)}" target="_blank" rel="noopener">${I('msg', 16)} ${T('WhatsApp पर पैसे की याद दिलाओ','Send WhatsApp payment reminder')}</a>` : ''}
  <div class="act-row">${['sale','buy','payIn','payOut'].map(k => `<button data-act="wiz" data-type="${k}" data-party="${p.id}" data-say="${esc(TYPE_NM(k))}">${sqIcon(k)}${TYPE_NM(k)}</button>`).join('')}</div>` : ''}
  ${ps.length ? `<h3 class="dr-sec">${T('रास्ते में शिपमेंट','Shipments in transit')}</h3><div class="list">${ps.map(s => `<button class="li" data-act="open-ship" data-id="${s.id}"><span class="flag">${flag(s.country)}</span><div class="body"><div class="t1">${esc(s.ref)}</div><div class="t2">${fmtDate(s.eta)}</div></div>${status(stageCls(s.stage), STAGES()[s.stage])}</button>`).join('')}</div>` : ''}
  ${given.length ? `<h3 class="dr-sec">${T('इनको क्या-क्या माल दिया','Goods sold to them')}</h3>${rows(given)}` : ''}
  ${got.length ? `<h3 class="dr-sec">${T('इनसे क्या-क्या माल लिया','Goods bought from them')}</h3>${rows(got)}` : ''}
  ${seeMoney() ? txnTable('ledger-' + p.id, tx, T('हिसाब किताब','Ledger'), false) : ''}
  ${isOwner() ? `<button class="btn ${armed === 'del-party' ? 'armed' : 'bad'}" data-act="del-party" data-id="${p.id}">${I('trash', 16)} ${armed === 'del-party' ? T('पक्का? फिर दबाओ','Confirm delete') : T('पार्टी मिटाओ','Delete party')}</button>` : ''}`;
};

SH.ship = () => {
  const s = ship(sheet.id); if (!s) return closeMissing();
  const x = S.txns.find(t => t.id === s.txn), p = party(s.party), exps = S.expenses.filter(e => e.ship === s.id), st = STAGES(), dl0 = daysFrom(s.eta);
  const rev = x?.amount || 0, cost = x ? sum(x.lines || [], lineCost) : 0, e = sum(exps, e => e.amount);
  const msg = `Shipment update ${s.ref}: ${['Packing','At port','At sea','In customs','Delivered'][s.stage]}. ETA ${s.eta}${s.vessel ? ', vessel ' + s.vessel : ''}${s.container ? ', container ' + s.container : ''}${s.bl ? ', B/L ' + s.bl : ''}. — ${S.settings.company || ''}`;
  const wa = waLink(p?.phone, msg);
  return drHd(esc(s.ref), s.dir === 'out' ? T('एक्सपोर्ट','Export') : T('इम्पोर्ट','Import')) + `
  ${routeSVG(s)}
  <div class="stepper">${st.map((n, k) => `<div class="stp ${k < s.stage ? 'done' : k === s.stage ? 'now' : ''}"><span class="d">${I(k < s.stage ? 'check' : STAGE_SVG[k], 18)}</span>${n}</div>`).join('')}</div>
  <div class="kv"><div><small>${T('पार्टी','Party')}</small><b>${esc(p?.name || '')}</b></div><div><small>${T('देश','Country')}</small><b>${flag(s.country)} ${esc(cname(s.country))}</b></div>
    <div><small>${T('पहुँचेगा','ETA')}</small><b class="num ${s.stage < 4 && dl0 < 0 ? 'bad' : ''}">${fmtDate(s.eta)}</b><span class="muted" style="font-size:12px">${s.stage >= 4 ? st[4] : dl0 < 0 ? -dl0 + ' ' + T('दिन लेट','days late') : dl0 + ' ' + T('दिन बाकी','days left')}</span></div>
    <div><small>${T('माल','Goods')}</small><b style="font-size:13.5px">${esc((s.lines || []).map(l => { const i = item(l.item); return i ? `${i.name} × ${qtyFmt(l.qty)} ${i.unit}` : ''; }).join(', '))}</b></div></div>
  <div class="row">${s.stage < 4 ? `<button class="btn pri" data-act="ship-next" data-id="${s.id}" data-say="${esc(T('अगला कदम','Next step') + ': ' + st[s.stage + 1])}">${I(STAGE_SVG[s.stage + 1], 16)} ${T('अगला कदम','Move to')}: ${st[s.stage + 1]}</button>` : ''}
    ${x && seeMoney() ? `<button class="btn" data-act="open-inv" data-id="${x.id}">${I('file', 16)} ${T('इनवॉइस','Invoice')}</button><button class="btn" data-act="open-inv" data-id="${x.id}" data-k="pl">${I('clip', 16)} ${T('पैकिंग लिस्ट','Packing list')}</button>` : ''}
    ${wa ? `<a class="btn wa" href="${esc(wa)}" target="_blank" rel="noopener">${I('msg', 16)} ${T('पार्टी को अपडेट','Update party')}</a>` : ''}</div>
  <section class="panel"><h3>${T('कागज़ात — जो तैयार है उस पर टिक करो','Documents — tick what is ready')}</h3>
  <div class="docchk">${DOCS.map(d => `<button class="${(s.docs || {})[d[0]] ? 'ok' : ''}" data-act="doc-tog" data-id="${s.id}" data-k="${d[0]}" data-say="${esc(d[2] + ' ' + ((s.docs || {})[d[0]] ? T('तैयार','ready') : T('बाकी','pending')))}">${I((s.docs || {})[d[0]] ? 'ok' : 'file', 16)}${d[2]}</button>`).join('')}</div></section>
  ${seeMoney() && s.dir === 'out' && x ? `<section class="panel" data-say="${esc(T('इस शिपमेंट का मुनाफ़ा','Profit on this shipment') + ' ' + sayMoney(rev - cost - e))}"><h3>${T('इस शिपमेंट का हिसाब','Shipment profit')}</h3>
    <div class="hbars">${[[T('बिक्री','Sale'), rev, 'good'], [T('माल की लागत','Cost of goods'), cost, 'cargo'], [T('खर्चे','Expenses'), e, 'bad']].map(r => `<div class="hb"><span class="nm">${r[0]}</span><span class="v">${money(r[1])}</span><span class="track2"><i class="${r[2]}" style="width:${rev ? Math.min(100, r[1] / rev * 100) : 0}%"></i></span></div>`).join('')}</div>
    <div class="row" style="border-top:1px solid var(--border-2);padding-top:10px"><b>${T('मुनाफ़ा','Profit')}</b><span class="sp"></span><span class="amt num ${rev - cost - e >= 0 ? 'good' : 'bad'}">${smoney(rev - cost - e)}</span></div></section>` : ''}
  ${seeMoney() ? `<section class="panel"><div class="row"><h3>${T('इस शिपमेंट के खर्चे','Expenses on this shipment')}</h3><span class="sp"></span><button class="btn sm" data-act="wiz" data-type="exp" data-ship="${s.id}">${I('plus', 14)} ${T('खर्चा जोड़ो','Add expense')}</button></div>
    <div class="list">${exps.map(e => `<div class="li"><span class="sq violet sm">${I(EXP_IC[e.cat] || 'receipt', 16)}</span><div class="body"><div class="t1">${esc(expNm(e.cat))}</div><div class="t2">${fmtDate(e.d)}${e.note ? ' · ' + esc(e.note) : ''}</div></div><span class="money-neg num">${money(e.amount)}</span></div>`).join('') || `<div class="empty">${T('कोई खर्चा नहीं','No expenses yet')}</div>`}</div></section>` : ''}
  <section class="panel"><h3>${T('शिप की जानकारी','Shipping details')}</h3>
  <form class="form" id="shipF"><div class="two">
    <label>${T('जहाज़ का नाम','Vessel')}<input id="s_vessel" value="${esc(s.vessel || '')}"></label>
    <label>Container No.<input id="s_container" value="${esc(s.container || '')}"></label>
    <label>B/L No.<input id="s_bl" value="${esc(s.bl || '')}"></label>
    <label>${T('पहुँचने की तारीख','Arrival date')}<input id="s_eta" type="date" value="${esc(s.eta)}"></label>
    <label>${T('लोडिंग पोर्ट','Port of loading')}<input id="s_pol" value="${esc(s.pol || '')}"></label>
    <label>${T('उतरने का पोर्ट','Port of discharge')}<input id="s_pod" value="${esc(s.pod || '')}"></label>
  </div><button class="btn pri" type="submit">${I('check', 16)} ${T('सेव करो','Save details')}</button></form></section>`;
};

SH.inv = () => {
  const x = S.txns.find(t => t.id === sheet.id); if (!x) return closeMissing();
  const kinds = x.type === 'sale' ? [['inv', T('इनवॉइस','Invoice')], ['pro', T('प्रोफ़ॉर्मा','Proforma')], ['pl', T('पैकिंग लिस्ट','Packing list')]] : [['inv', T('बिल','Bill')], ['pl', T('पैकिंग लिस्ट','Packing list')]];
  const p = party(x.party), s = C.inv[x.id] || {};
  const waT = `${x.type === 'sale' ? 'Invoice' : 'Bill'} ${x.inv || ''} dated ${x.d}: ${linesTxt(x).replace(/[^\p{L}\p{N}\s,.×]/gu, '').trim()}. Total ${x.cur && x.cur !== 'INR' ? fmoney(x.famount, x.cur) : money(x.amount)}${x.due ? ', due ' + x.due : ''}. — ${S.settings.company || ''}`, w = waLink(p?.phone, waT);
  return drHd(esc(x.inv || T('बिल','Bill')), esc(p?.name || '')) + `
  <div class="kv" data-say="${esc((p?.name || '') + '. ' + sayMoney(x.amount) + '. ' + (s.status === 'paid' ? T('पूरा चुका','fully paid') : T('बाकी','open') + ' ' + sayMoney(s.open || 0)))}">
    <div><small>${T('रकम','Amount')}</small><b class="num">${money(x.amount)}</b>${x.cur && x.cur !== 'INR' ? `<span class="muted num" style="font-size:12px">${fmoney(x.famount, x.cur)} @ ${x.fx}</span>` : ''}</div>
    <div><small>${T('मिला','Paid')}</small><b class="good num">${money(s.paid || 0)}</b></div><div><small>${T('बाकी','Open')}</small><b class="${s.open > .5 ? 'bad' : ''} num">${money(s.open || 0)}</b></div>
    <div><small>${T('स्थिति','Status')}</small><b>${invStatus(x)}</b><span class="muted" style="font-size:12px">${T('आख़िरी तारीख','Due')} ${fmtDate(x.due)}</span></div></div>
  <div class="row"><div class="tabs">${kinds.map(([k, l]) => `<button class="${(sheet.kind || 'inv') === k ? 'on' : ''}" data-act="inv-kind" data-k="${k}">${l}</button>`).join('')}</div><span class="sp"></span>
    <button class="btn pri" data-act="inv-dl">${I('download', 16)} ${T('डाउनलोड','Download')}</button>${canPrint() ? `<button class="btn" data-act="inv-print">${I('printer', 16)} ${T('प्रिंट','Print')}</button>` : ''}${w ? `<a class="btn wa" href="${esc(w)}" target="_blank" rel="noopener">${I('msg', 16)} WhatsApp</a>` : ''}</div>
  ${!S.settings.company ? `<div class="note">${I('alert', 16)}<span>${T('बिल पर अपनी कंपनी का नाम, पता, IEC, बैंक डालने के लिए सेटिंग में जाओ।','Add your company name, address, IEC and bank details in Settings to print them on documents.')}</span></div>` : ''}
  ${docHTML(x, sheet.kind || 'inv')}`;
};

/* ================= ENTRY WIZARD ================= */
SH.wiz = () => {
  const s = stepName(), steps = W.type ? STEPS[W.type] : STEPS.sale;
  const title = W.type ? TYPE_NM(W.type) : T('नई एंट्री','New entry');
  let body = '', canNext = false, hideNext = false;
  const q = (txt, extra = '') => `<h3 class="wz-q">${extra}${txt}</h3>`;
  if (s === 'type'){
    body = q(T('क्या हुआ?','What happened?')) + `<div class="bigchoice">${['sale','buy','payIn','payOut','exp'].map(k => `<button class="choice" data-act="w-type" data-v="${k}" data-say="${esc(TYPE_NM(k))}">${sqIcon(k)}<span style="min-width:0"><b>${TYPE_NM(k)}</b><small>${TYPE_SUB(k)}</small></span></button>`).join('')}</div>`; hideNext = true;
  } else if (s === 'party'){
    const list = S.parties.filter(p => W.type === 'sale' || W.type === 'payIn' ? p.kind !== 'supplier' : p.kind !== 'customer');
    body = q(T('किसके साथ?','With whom?')) + `<div class="pick">${list.map(p => { const b = C.bal[p.id] || 0; return `<button class="pk ${W.party === p.id ? 'on' : ''}" data-act="w-party" data-v="${p.id}" data-say="${esc(p.name)}">${pic(p)}${esc(p.name)}<small>${flag(p.country)} ${b ? `<span class="${b > 0 ? 'money-pos' : 'money-neg'}">${money(b)}</span>` : ''}</small></button>`; }).join('')}
      <button class="pk" data-act="party-form" data-ret="1"><span class="sq blue">${I('plus', 20)}</span>${T('नई पार्टी','New party')}</button></div>`; hideNext = true;
  } else if (s === 'item'){
    body = q(T('कौन सा माल?','Which item?')) + `<div class="pick">${S.items.map(i => `<button class="pk ${W.item === i.id ? 'on' : ''}" data-act="w-item" data-v="${i.id}" data-say="${esc(i.name + ', ' + qtyFmt(stockOf(i.id)) + ' ' + i.unit)}">${pic(i)}${esc(i.name)}<small class="num">${qtyFmt(stockOf(i.id))} ${esc(i.unit)}</small></button>`).join('')}
      <button class="pk" data-act="item-form" data-ret="1"><span class="sq blue">${I('plus', 20)}</span>${T('नया माल','New item')}</button></div>`; hideNext = true;
  } else if (s === 'qty'){
    const i = item(W.item), already = sum(W.lines.filter(l => l.item === i.id), l => l.qty), avail = stockOf(i.id) - already, over = W.type === 'sale' && +W.buf > avail;
    body = q(T('कितना माल?','How much?'), pic(i, 'sm')) + numpad(i.unit, `${T('गोदाम में','In stock')}: <b class="num">${qtyFmt(avail)} ${esc(i.unit)}</b>${+i.pack && +W.buf ? ` · ${Math.ceil(+W.buf / i.pack)} ${T('पैकेज','packages')}` : ''}`, true)
      + (over ? `<div class="note" style="border-color:var(--danger-200);background:var(--danger-50)">${I('alert', 16)}<b>${T('इतना माल गोदाम में नहीं है','Not enough stock in the godown')}</b></div>` : '');
    canNext = +W.buf > 0 && !over;
  } else if (s === 'rate'){
    const i = item(W.item), base = (+W.buf || 0) * fxOf(W.cur);
    body = q(T('एक का भाव कितना?','Price per unit?'), pic(i, 'sm')) + curChips() + numpad(CUR[W.cur].trim() + ' / ' + i.unit, `${T('कुल','Total')}: <b class="num">${fmoney(W.qty * (+W.buf || 0), W.cur)}</b>${W.cur !== 'INR' ? ` = ${money(W.qty * base)}` : ''}${W.type === 'sale' && base && base < i.buy ? `<br><span class="money-neg">${T('खरीद के भाव से कम!','Below buying price!')} (${money(i.buy)})</span>` : ''}`, true);
    canNext = +W.buf > 0;
  } else if (s === 'cart'){
    body = q(T('इस बिल में','On this bill')) + `<div class="list">${W.lines.map((l, k) => { const i = item(l.item); return `<div class="li">${pic(i, 'sm')}<div class="body"><div class="t1">${esc(i?.name)}</div><div class="t2 num">${qtyFmt(l.qty)} ${esc(i?.unit)} × ${fmoney(l.frate, W.cur)}</div></div><b class="num">${fmoney(l.qty * l.frate, W.cur)}</b><button class="x" data-act="w-rmline" data-k="${k}" aria-label="remove">${I('trash', 15)}</button></div>`; }).join('')}</div>
      <div class="row"><b>${T('कुल','Total')}</b><span class="sp"></span><span class="amt num">${fmoney(sum(W.lines, l => l.qty * l.frate), W.cur)}</span></div>
      <button class="choice" data-act="w-more" data-say="${T('और माल जोड़ो','Add another item')}"><span class="sq blue">${I('plus', 20)}</span><span><b>${T('और माल जोड़ो','Add another item')}</b><small>${T('एक ही बिल में कई माल','Several items on one bill')}</small></span></button>`;
    canNext = W.lines.length > 0;
  } else if (s === 'terms'){
    body = q(T('पैसा कब मिलेगा / देना है?','When is payment due?')) + `<div class="pick sm">${[[0, T('एडवांस','Advance')], [15, '15 ' + T('दिन','days')], [30, '30 ' + T('दिन','days')], [60, '60 ' + T('दिन','days')], [90, '90 ' + T('दिन','days')]].map(([d, l]) => `<button class="pk ${W.terms === d ? 'on' : ''}" data-act="w-terms" data-v="${d}" data-say="${esc(l)}"><span class="sq ${W.terms === d ? 'blue' : 'gray'}">${I(d ? 'cal' : 'cash', 18)}</span>${l}</button>`).join('')}</div>
      ${W.type === 'sale' ? q(T('सौदे की शर्त (Incoterm)','Incoterm')) + `<div class="pick sm">${[['EXW', T('हमारे गोदाम पर','At our godown')], ['FOB', T('जहाज़ पर चढ़ाने तक','Loaded on board')], ['CFR', T('भाड़ा हमारा','We pay freight')], ['CIF', T('भाड़ा + बीमा हमारा','Freight + insurance')]].map(([k, l]) => `<button class="pk ${W.incoterm === k ? 'on' : ''}" data-act="w-inco" data-v="${k}" data-say="${esc(k + ', ' + l)}"><b style="font-size:16px">${k}</b><small>${l}</small></button>`).join('')}</div>` : ''}`;
    canNext = true;
  } else if (s === 'amount'){
    if (W.type === 'exp') body = q(`${esc(expNm(W.expcat))} — ${T('कितने पैसे?','How much?')}`, `<span class="sq violet sm">${I(EXP_IC[W.expcat] || 'receipt', 16)}</span>`) + numpad('₹', '', false);
    else { const b = C.bal[W.party] || 0, p = party(W.party);
      body = q(T('कितने पैसे?','How much?'), pic(p, 'sm')) + curChips() + numpad(CUR[W.cur].trim(), (b ? `${b > 0 ? T('ये हमें देंगे','They owe us') : T('हमें देना है','We owe them')}: <b class="num">${money(b)}</b>` : T('हिसाब बराबर','All settled')) + (W.cur !== 'INR' && +W.buf ? ` · = ${money(+W.buf * fxOf(W.cur))}` : ''), true)
        + q(T('कैसे?','Payment mode')) + `<div class="seg">${Object.entries(MODES).map(([k, m]) => `<button class="${W.mode === k ? 'on' : ''}" data-act="w-mode" data-v="${k}" data-say="${esc(hi() ? m[1] : m[2])}">${hi() ? m[1] : m[2]}</button>`).join('')}</div>`; }
    canNext = +W.buf > 0;
  } else if (s === 'expcat'){
    body = q(T('किस चीज़ का खर्चा?','What was it for?')) + `<div class="pick sm">${Object.keys(EXP).map(k => `<button class="pk ${W.expcat === k ? 'on' : ''}" data-act="w-expcat" data-v="${k}" data-say="${esc(expNm(k))}"><span class="sq violet">${I(EXP_IC[k], 20)}</span>${esc(expNm(k))}</button>`).join('')}</div>`; hideNext = true;
  } else if (s === 'link'){
    const ships = S.ships.filter(x => x.stage < 4 || daysFrom(x.eta) > -45);
    body = q(T('किस शिपमेंट का खर्चा? (ज़रूरी नहीं)','Which shipment? (optional)')) + `<div class="pick">${[`<button class="pk ${!W.shipLink ? 'on' : ''}" data-act="w-link" data-v="" data-say="${T('ऑफ़िस','Office')}"><span class="sq gray">${I('bank', 20)}</span>${T('ऑफ़िस / कोई नहीं','Office / none')}</button>`].concat(ships.map(x => `<button class="pk ${W.shipLink === x.id ? 'on' : ''}" data-act="w-link" data-v="${x.id}" data-say="${esc(cname(x.country) + ' ' + x.ref)}"><span class="ic">${flag(x.country)}</span>${esc(x.ref)}<small>${esc(party(x.party)?.name || '')}</small></button>`)).join('')}</div>`;
    canNext = true;
  } else if (s === 'ship'){
    const sale = W.type === 'sale';
    body = q(sale ? T('माल कैसे गया?','How did the goods go?') : T('माल आ गया या रास्ते में है?','Arrived or on the way?')) + `<div class="bigchoice">
      <button class="choice ${W.ship === true ? 'on' : ''}" data-act="w-ship" data-v="1" data-say="${sale ? T('शिप से भेजा','Sent by ship') : T('रास्ते में है','On the way')}"><span class="sq blue">${I('ship', 20)}</span><span><b>${sale ? T('शिप / ट्रक से भेजा','Shipped (ship / truck)') : T('रास्ते में है','Still on the way')}</b><small>${T('शिपमेंट बनेगा','Creates a shipment')}</small></span></button>
      <button class="choice ${W.ship === false ? 'on' : ''}" data-act="w-ship" data-v="0" data-say="${sale ? T('यहीं दे दिया','Handed over here') : T('माल आ गया','Goods arrived')}"><span class="sq green">${I(sale ? 'users' : 'box', 20)}</span><span><b>${sale ? T('यहीं दे दिया','Handed over locally') : T('माल आ गया','Goods received')}</b><small>${sale ? T('कोई शिपमेंट नहीं','No shipment') : T('गोदाम में जुड़ेगा','Added to stock now')}</small></span></button></div>
      ${W.ship ? q(sale ? T('किस देश जा रहा है?','Destination country') : T('किस देश से आ रहा है?','Origin country')) + `<div class="pick sm">${COUNTRIES.map(c => `<button class="pk ${W.country === c[0] ? 'on' : ''}" data-act="w-country" data-v="${c[0]}" data-say="${esc(hi() ? c[1] : c[2])}"><span class="ic">${flag(c[0])}</span><small style="color:inherit;font-weight:600">${esc(hi() ? c[1] : c[2])}</small></button>`).join('')}</div>
      ${q(T('कितने दिन में पहुँचेगा?','Transit time'))}<div class="seg">${[5, 10, 20, 30, 45, 60].map(d => `<button class="${W.eta === d ? 'on' : ''}" data-act="w-eta" data-v="${d}" data-say="${d} ${T('दिन','days')}">${d} ${T('दिन','days')}</button>`).join('')}</div>` : ''}`;
    canNext = W.ship === false || (W.ship === true && !!W.country);
  } else if (s === 'confirm'){ body = q(T('एक बार देख लो','Review and confirm')) + confirmBody(); canNext = true; }
  const bar = W.k > 0 ? `<div class="bottom-bar"><button class="btn" data-act="w-back" data-say="${T('पीछे','Back')}">${I('chev-l', 16)} ${T('पीछे','Back')}</button><span class="sp"></span>
    ${hideNext ? '' : s === 'confirm' ? `<button class="btn good lg" style="flex:1" data-act="w-save" data-say="${T('पक्का करो','Confirm')}">${I('check', 18)} ${T('पक्का करो','Confirm & save')}</button>` : `<button class="btn pri lg" style="flex:1" data-act="w-next" ${canNext ? '' : 'disabled'} data-say="${T('आगे','Next')}">${T('आगे','Next')} ${I('chev-r', 16)}</button>`}</div>` : '';
  return drHd(title, W.type ? `${T('कदम','Step')} ${W.k + 1} / ${steps.length}` : '') + `<div class="steps">${steps.map((_, k) => `<i class="${k <= W.k ? 'on' : ''}"></i>`).join('')}</div>${body}${bar}`;
};

/* ================= LOCK ================= */
function renderLock(){
  const root = document.getElementById('lockRoot');
  if (!lockState){ root.innerHTML = ''; return; }
  const r = lockState.role;
  root.innerHTML = `<div class="lock" role="dialog" aria-modal="true"><div class="lock-card">
    <span class="sb-logo" style="width:52px;height:52px;border-radius:14px">${I('ship', 26)}</span>
    <div><h2>${T('कार्गो मुनीम','Cargo Munim')}</h2><div class="muted">${T('आप कौन हो?','Who is using the app?')}</div></div>
    <div class="roles">${Object.keys(ROLES).filter(k => k === 'owner' || S.settings.pins[k] || k === 'worker').map(k => `<button class="${r === k ? 'on' : ''}" data-act="lk-role" data-v="${k}" data-say="${esc(roleNm(k))}"><span class="sq ${k === 'owner' ? 'blue' : k === 'munim' ? 'teal' : 'amber'}">${I(k === 'owner' ? 'shield' : k === 'munim' ? 'wallet' : 'box', 20)}</span>${roleNm(k)}</button>`).join('')}</div>
    ${r && S.settings.pins[r] ? `<div class="muted">${T('PIN डालो','Enter PIN')}</div><div class="dots">${[...Array(S.settings.pins[r].length)].map((_, k) => `<i class="${k < lockState.buf.length ? 'f' : ''}"></i>`).join('')}</div>
    <div class="keys">${['1','2','3','4','5','6','7','8','9','','0','⌫'].map(k => k ? `<button data-act="lk-key" data-k="${k}" ${k === '⌫' ? 'class="k-del"' : ''}>${k}</button>` : '<span></span>').join('')}</div>` : r ? `<button class="btn pri wide lg" data-act="lk-enter">${T('अंदर जाओ','Continue')} ${I('chev-r', 16)}</button>` : ''}
    ${lockState.err ? `<b class="money-neg">${T('गलत PIN','Wrong PIN')}</b>` : ''}
    ${!needLock() ? `<button class="btn ghost" data-act="lk-cancel">${T('रहने दो','Cancel')}</button>` : ''}
  </div></div>`;
}

/* ================= RENDER ================= */
let rq = false;
function schedule(){ if (rq) return; rq = true; requestAnimationFrame(() => { rq = false; render(); }); }
function sidebarHTML(){
  const n = alerts().length;
  const link = (k, ic, l, extra = '') => `<button class="sb-link ${view === k ? 'on' : ''}" data-act="nav" data-v="${k}" data-say="${esc(l)}">${I(ic, 18)}<span>${l}</span>${extra}</button>`;
  return `<div class="sb-brand"><span class="sb-logo">${I('ship', 20)}</span><div><b>${T('कार्गो मुनीम','Cargo Munim')}</b><small>Pro · ${T('आयात-निर्यात','Import / Export')}</small></div></div>
  <nav class="sb-nav">
    <div class="sb-sec">${T('काम','Workspace')}</div>
    ${link('home', 'dash', T('डैशबोर्ड','Dashboard'), n ? `<span class="cnt">${n}</span>` : '')}${link('stock', 'box', T('माल (स्टॉक)','Stock'))}${link('ships', 'ship', T('शिपमेंट','Shipments'))}${link('parties', 'users', T('पार्टी','Parties'))}
    ${seeMoney() ? `<div class="sb-sec">${T('पैसा','Finance')}</div>${link('money', 'wallet', T('लेना-देना','Receivables & payables'))}${link('invoices', 'file', T('बिल','Invoices'))}${link('expenses', 'receipt', T('खर्चे','Expenses'))}${link('reports', 'chart', T('रिपोर्ट','Reports'))}` : ''}
    <div class="sb-sec">${T('अकाउंट','Account')}</div>
    ${link('activity', 'history', T('किसने क्या किया','Activity log'))}
    <button class="sb-link" data-act="settings">${I('sliders', 18)}<span>${T('सेटिंग','Settings')}</span></button>
    ${lockOn() ? `<button class="sb-link" data-act="lock">${I('logout', 18)}<span>${T('यूज़र बदलो','Switch user')}</span></button>` : ''}
  </nav>
  <div class="sync"><div class="sync-st">${I(mode === 'cloud' ? 'ok' : 'check', 16)}<span><b>${mode === 'cloud' ? T('टीम के साथ लाइव','Live team sync') : T('इस कंप्यूटर पर सेव','Saved on this device')}</b>${lastSaved ? T('आख़िरी बदलाव','Last change') + ': ' + lastSaved.toLocaleString(hi() ? 'hi-IN' : 'en-IN', {day: '2-digit', month: '2-digit', year: 'numeric', hour: 'numeric', minute: '2-digit'}) : T('सब अप-टू-डेट','Everything up to date')}</span></div>
    ${P.role !== 'worker' ? `<button class="btn pri wide" data-act="wiz">${I('plus', 16)} ${T('नई एंट्री','New entry')}</button>` : ''}
    ${mode === 'local' ? `<button class="btn wide" data-act="settings">${I('download', 16)} ${T('बैकअप लो','Back up')}</button>` : ''}</div>`;
}
function render(){
  if (!lockOn() && P.role !== 'worker') P.role = 'owner';
  compute();
  document.documentElement.lang = P.lang;
  if (!V[view] || (!seeMoney() && ['money','invoices','expenses','reports'].includes(view))) view = 'home';
  const af = document.activeElement, tsid = af?.dataset?.tsearch, pos = tsid ? af.selectionStart : 0;
  document.getElementById('sidebar').innerHTML = sidebarHTML();
  document.getElementById('hambBtn').innerHTML = I('menu', 20);
  document.getElementById('searchBtn').innerHTML = I('search', 19);
  document.getElementById('tbTitle').textContent = T('आयात-निर्यात प्रबंधन','Import & Export Management');
  const mic = document.getElementById('micBtn'); mic.innerHTML = I('mic', 18); mic.hidden = !canListen() || P.role === 'worker';
  const vb = document.getElementById('voiceBtn'); vb.innerHTML = I(P.voice ? 'vol' : 'mute', 19); vb.classList.toggle('on', P.voice); vb.setAttribute('aria-label', P.voice ? T('आवाज़ चालू','Voice on') : T('आवाज़ बंद','Voice off'));
  document.getElementById('langBtn').textContent = hi() ? 'EN' : 'हि';
  const n = alerts().length; document.getElementById('bellBtn').innerHTML = I('bell', 19) + (n ? `<span class="dot">${n > 99 ? '99+' : n}</span>` : '');
  const nm = mode === 'cloud' && me?.name ? me.name : (S.settings.company || T('कार्गो मुनीम','Cargo Munim'));
  document.getElementById('userBtn').innerHTML = `<span class="avatar">${esc(initials(nm))}</span><span class="who"><b>${esc(nm)}</b><small>${roleNm(P.role || 'owner')}</small></span>`;
  const fab = document.getElementById('fab'); fab.innerHTML = I('plus', 26); fab.hidden = P.role === 'worker';
  document.getElementById('main').innerHTML = V[view]();
  if (tsid){ const f = document.querySelector(`[data-tsearch="${tsid}"]`); if (f){ f.focus(); try{ f.setSelectionRange(pos, pos); }catch(e){} } }
  renderSheet(); renderLock();
}
function renderSheet(){
  const root = document.getElementById('sheetRoot');
  if (!sheet || !SH[sheet.k]){ root.innerHTML = ''; return; }
  const old = root.querySelector('.sheet'), sc = old ? old.scrollTop : 0, keepKey = old?.dataset.key === sheet.k + (sheet.id || '');
  const focus = document.activeElement && root.contains(document.activeElement) ? (document.activeElement.id || (document.activeElement.dataset.tsearch ? '#t:' + document.activeElement.dataset.tsearch : null)) : null;
  const vals = {}; root.querySelectorAll('input:not([type=file]),select,textarea:not([readonly])').forEach(x => { if (x.id) vals[x.id] = x.value; });
  const html = SH[sheet.k](); if (!html){ root.innerHTML = ''; return; }
  root.innerHTML = `<div class="sheet-bg" data-act="bg"><div class="sheet ${['inv','ship','party'].includes(sheet.k) ? 'xl' : ''}" role="dialog" aria-modal="true" data-key="${esc(sheet.k + (sheet.id || ''))}">${html}</div></div>`;
  const ns = root.querySelector('.sheet');
  if (keepKey){ ns.scrollTop = sc; for (const k in vals){ const x = document.getElementById(k); if (x && x.value !== vals[k]) x.value = vals[k]; } }
  if (focus){ const f = focus.startsWith('#t:') ? root.querySelector(`[data-tsearch="${focus.slice(3)}"]`) : document.getElementById(focus); if (f){ f.focus(); try{ f.setSelectionRange(f.value.length, f.value.length); }catch(e){} } }
}
document.addEventListener('input', e => { const id = e.target.dataset?.tsearch; if (id){ ts(id).q = e.target.value; ts(id).page = 1; if (e.target.closest('#sheetRoot')) renderSheet(); else render(); } });
document.addEventListener('change', e => { const id = e.target.dataset?.tper; if (id){ ts(id).per = +e.target.value; ts(id).page = 1; render(); } });
