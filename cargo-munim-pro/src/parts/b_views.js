
/* ================= ICONS (line icons, 24px grid) ================= */
const ICONS = {
  menu:'<path d="M4 6h16M4 12h16M4 18h16"/>',
  bell:'<path d="M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9"/><path d="M10.3 21a1.94 1.94 0 0 0 3.4 0"/>',
  search:'<circle cx="11" cy="11" r="8"/><path d="m21 21-4.3-4.3"/>',
  dash:'<rect width="7" height="9" x="3" y="3" rx="1"/><rect width="7" height="5" x="14" y="3" rx="1"/><rect width="7" height="9" x="14" y="12" rx="1"/><rect width="7" height="5" x="3" y="16" rx="1"/>',
  box:'<path d="m7.5 4.27 9 5.15"/><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z"/><path d="m3.3 7 8.7 5 8.7-5"/><path d="M12 22V12"/>',
  ship:'<path d="M2 21c.6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1 .6.5 1.2 1 2.5 1 2.5 0 2.5-2 5-2 1.3 0 1.9.5 2.5 1"/><path d="M19.38 20A11.6 11.6 0 0 0 21 14l-9-4-9 4c0 2.9.94 5.34 2.81 7.76"/><path d="M19 13V7a2 2 0 0 0-2-2H7a2 2 0 0 0-2 2v6"/><path d="M12 10v4M12 2v3"/>',
  users:'<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>',
  wallet:'<path d="M19 7V4a1 1 0 0 0-1-1H5a2 2 0 0 0 0 4h15a1 1 0 0 1 1 1v4h-3a2 2 0 0 0 0 4h3a1 1 0 0 0 1-1v-2a1 1 0 0 0-1-1"/><path d="M3 5v14a2 2 0 0 0 2 2h15a1 1 0 0 0 1-1v-4"/>',
  file:'<path d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7Z"/><path d="M14 2v4a2 2 0 0 0 2 2h4M16 13H8M16 17H8M10 9H8"/>',
  receipt:'<path d="M4 2v20l2-1 2 1 2-1 2 1 2-1 2 1 2-1 2 1V2l-2 1-2-1-2 1-2-1-2 1-2-1-2 1Z"/><path d="M16 8h-6a2 2 0 1 0 0 4h4a2 2 0 1 1 0 4H8M12 17.5v-11"/>',
  chart:'<path d="M3 3v18h18"/><path d="M18 17V9M13 17V5M8 17v-3"/>',
  history:'<path d="M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8"/><path d="M3 3v5h5M12 7v5l4 2"/>',
  sliders:'<path d="M21 4h-7M10 4H3M21 12h-9M8 12H3M21 20h-5M12 20H3M14 2v4M8 10v4M16 18v4"/>',
  lock:'<rect width="18" height="11" x="3" y="11" rx="2"/><path d="M7 11V7a5 5 0 0 1 10 0v4"/>',
  plus:'<path d="M5 12h14M12 5v14"/>',
  'chev-r':'<path d="m9 18 6-6-6-6"/>', 'chev-l':'<path d="m15 18-6-6 6-6"/>',
  sort:'<path d="m7 15 5 5 5-5M7 9l5-5 5 5"/>',
  up:'<path d="m7 9 5-5 5 5"/>', down:'<path d="m7 15 5 5 5-5"/>',
  refresh:'<path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8"/><path d="M21 3v5h-5M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16"/><path d="M8 16H3v5"/>',
  alert:'<path d="m21.73 18-8-14a2 2 0 0 0-3.48 0l-8 14A2 2 0 0 0 4 21h16a2 2 0 0 0 1.73-3"/><path d="M12 9v4M12 17h.01"/>',
  clock:'<circle cx="12" cy="12" r="10"/><path d="M12 6v6l4 2"/>',
  check:'<path d="M20 6 9 17l-5-5"/>',
  ok:'<circle cx="12" cy="12" r="10"/><path d="m9 12 2 2 4-4"/>',
  'in':'<path d="M17 7 7 17M17 17H7V7"/>', out:'<path d="M7 7h10v10M7 17 17 7"/>',
  x:'<path d="M18 6 6 18M6 6l12 12"/>',
  edit:'<path d="M17 3a2.85 2.83 0 1 1 4 4L7.5 20.5 2 22l1.5-5.5Z"/>',
  trash:'<path d="M3 6h18M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"/>',
  download:'<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>',
  printer:'<path d="M6 9V2h12v7M6 18H4a2 2 0 0 1-2-2v-5a2 2 0 0 1 2-2h16a2 2 0 0 1 2 2v5a2 2 0 0 1-2 2h-2"/><rect width="12" height="8" x="6" y="14"/>',
  msg:'<path d="M7.9 20A9 9 0 1 0 4 16.1L2 22Z"/>',
  mic:'<path d="M12 2a3 3 0 0 0-3 3v7a3 3 0 0 0 6 0V5a3 3 0 0 0-3-3Z"/><path d="M19 10v2a7 7 0 0 1-14 0v-2M12 19v3"/>',
  vol:'<path d="M11 5 6 9H2v6h4l5 4V5ZM15.54 8.46a5 5 0 0 1 0 7.07M19.07 4.93a10 10 0 0 1 0 14.14"/>',
  mute:'<path d="M11 5 6 9H2v6h4l5 4V5ZM22 9l-6 6M16 9l6 6"/>',
  spark:'<path d="m12 3-1.9 5.8a2 2 0 0 1-1.3 1.3L3 12l5.8 1.9a2 2 0 0 1 1.3 1.3L12 21l1.9-5.8a2 2 0 0 1 1.3-1.3L21 12l-5.8-1.9a2 2 0 0 1-1.3-1.3Z"/>',
  anchor:'<path d="M12 22V8M5 12H2a10 10 0 0 0 20 0h-3"/><circle cx="12" cy="5" r="3"/>',
  cal:'<rect width="18" height="18" x="3" y="4" rx="2"/><path d="M16 2v4M8 2v4M3 10h18"/>',
  trend:'<path d="M22 7 13.5 15.5 8.5 10.5 2 17"/><path d="M16 7h6v6"/>',
  cash:'<rect width="20" height="12" x="2" y="6" rx="2"/><circle cx="12" cy="12" r="2"/><path d="M6 12h.01M18 12h.01"/>',
  clip:'<rect width="8" height="4" x="8" y="2" rx="1"/><path d="M16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2"/><path d="m9 14 2 2 4-4"/>',
  copy:'<rect width="14" height="14" x="8" y="8" rx="2"/><path d="M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2"/>',
  globe:'<circle cx="12" cy="12" r="10"/><path d="M12 2a14.5 14.5 0 0 0 0 20 14.5 14.5 0 0 0 0-20M2 12h20"/>',
  tag:'<path d="M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z"/><circle cx="7.5" cy="7.5" r=".5"/>',
  minus:'<path d="M5 12h14"/>', square:'<rect width="14" height="14" x="5" y="5" rx="2"/>',
  upload:'<path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12"/>',
  logout:'<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9"/>',
  truck:'<path d="M14 18V6a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2v11a1 1 0 0 0 1 1h2M15 18H9M19 18h2a1 1 0 0 0 1-1v-3.65a1 1 0 0 0-.22-.624l-3.48-4.35A1 1 0 0 0 17.52 8H14"/><circle cx="17" cy="18" r="2"/><circle cx="7" cy="18" r="2"/>',
  shield:'<path d="M20 13c0 5-3.5 7.5-7.66 8.95a1 1 0 0 1-.67-.01C7.5 20.5 4 18 4 13V6a1 1 0 0 1 1-1c2 0 4.5-1.2 6.24-2.72a1.17 1.17 0 0 1 1.52 0C14.51 3.81 17 5 19 5a1 1 0 0 1 1 1z"/>',
  bank:'<path d="M3 22h18M6 18v-7M10 18v-7M14 18v-7M18 18v-7M12 2l8 5H4z"/>',
  dots:'<circle cx="12" cy="12" r="1"/><circle cx="19" cy="12" r="1"/><circle cx="5" cy="12" r="1"/>'
};
const I = (n, s = 18) => `<svg class="i" width="${s}" height="${s}" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">${ICONS[n] || ''}</svg>`;
const TYPE_SQ = {sale:['out','blue'], buy:['in','amber'], payIn:['wallet','green'], payOut:['cash','red'], adj:['refresh','gray'], exp:['receipt','violet']};
const sqIcon = (type, sm) => { const [ic, c] = TYPE_SQ[type] || ['file','gray']; return `<span class="sq ${c} ${sm ? 'sm' : ''}">${I(ic, sm ? 16 : 20)}</span>`; };
const EXP_IC = {freight:'ship', duty:'shield', cha:'file', insurance:'shield', transport:'truck', packing:'box', bank:'bank', other:'dots'};
const STAGE_SVG = ['box','anchor','ship','clip','ok'];
const stageCls = s => ['gray','info','violet','warn','good'][s];

/* ================= STATE ================= */
let view = 'home', shipDir = 'out', partyFilter = 'all', invFilter = 'open', invKind = 'sale', sheet = null, W = null, armed = null, lockState = null, lastSaved = null;
const V = {};
const TS = {};
const ts = id => TS[id] = TS[id] || {q: '', sk: null, sd: 1, page: 1, per: 10};

/* ================= BUILDING BLOCKS ================= */
const status = (cls, text) => `<span class="status ${cls}"><i></i>${text}</span>`;
const openBtn = (act, id, extra = '') => `<button class="open-btn" data-act="${act}" data-id="${id}" ${extra}>${T('खोलो','Open')} ${I('chev-r', 15)}</button>`;
const cellP = (o, main, sub) => `<div class="cellp">${pic(o, 'sm')}<div><div class="cell-main">${main}</div>${sub ? `<div class="cell-sub">${sub}</div>` : ''}</div></div>`;
const stat = (ic, color, num, label, sub, attrs = '', say = '') => `<${attrs ? 'button' : 'div'} class="stat" ${attrs} ${say ? `data-say="${esc(say)}"` : ''}><span class="stat-ic ${color}">${I(ic, 24)}</span><span><span class="stat-num num">${num}</span><span class="stat-lbl">${label}</span>${sub ? `<span class="stat-sub ${sub.bad ? 'bad' : ''}">${sub.t || sub}</span>` : ''}</span></${attrs ? 'button' : 'div'}>`;
const pageHd = (crumbs, title, sub, actions = '') => `<div class="crumb">${crumbs.map((c, k) => (k ? `<span>/</span>` : '') + esc(c)).join('')}</div>
  <div class="page-hd"><div><h1>${title}</h1><p>${sub}</p></div>${actions ? `<div class="row">${actions}</div>` : ''}</div>`;
const dtCell = iso => `<span class="dt">${I('clock', 14)}${fmtDate(iso)}</span>`;
const noAccess = () => `<div class="empty-big">${I('lock', 32)}<b>${T('ये हिस्सा मालिक / मुनीम के लिए है','This section is for the owner / accountant')}</b></div>`;

/* generic data table: search, sort, pagination, phone card layout */
function dataTable({id, title, cols, rows, search, ph, tools = '', rowAttrs, empty}){
  const s = ts(id); const q = s.q.trim().toLowerCase();
  let list = q && search ? rows.filter(r => search(r).toLowerCase().includes(q)) : rows.slice();
  const col = cols.find(c => c.k === s.sk);
  if (col && col.sort) list.sort((a, b) => { const x = col.sort(a), y = col.sort(b); return (x > y ? 1 : x < y ? -1 : 0) * s.sd; });
  const pages = Math.max(1, Math.ceil(list.length / s.per)); s.page = clamp(s.page, 1, pages);
  const slice = list.slice((s.page - 1) * s.per, s.page * s.per);
  const pg = []; for (let p = 1; p <= pages; p++){ if (p === 1 || p === pages || Math.abs(p - s.page) <= 1) pg.push(p); else if (pg[pg.length - 1] !== '…') pg.push('…'); }
  return `<section class="card">
    <div class="card-hd"><h2>${title}</h2><span class="count">${list.length} ${T('रिकॉर्ड','results')}</span><span class="sp"></span>${tools}
      ${search ? `<label class="searchbox">${I('search', 16)}<input data-tsearch="${id}" value="${esc(s.q)}" placeholder="${esc(ph || T('खोजो…','Search…'))}" aria-label="search"></label>` : ''}</div>
    <div class="tbl-wrap"><table class="tbl"><thead><tr>${cols.map(c => `<th class="${c.cls || ''}">${c.sort ? `<button class="sortbtn ${s.sk === c.k ? 'on' : ''}" data-act="tsort" data-t="${id}" data-k="${c.k}">${c.label} ${I(s.sk === c.k ? (s.sd > 0 ? 'up' : 'down') : 'sort', 13)}</button>` : c.label}</th>`).join('')}</tr></thead>
    <tbody>${slice.map(r => `<tr ${rowAttrs ? rowAttrs(r) : ''}>${cols.map((c, k) => `<td class="${c.cls || ''} ${k === 0 ? 'lead' : ''} ${c.act ? 'act-cell' : ''}" data-label="${esc(c.label)}">${c.cell(r)}</td>`).join('')}</tr>`).join('') || `<tr><td colspan="${cols.length}" class="lead"><div class="empty">${empty || T('अभी कुछ नहीं है','Nothing here yet')}</div></td></tr>`}</tbody></table></div>
    ${list.length > 10 || s.page > 1 ? `<div class="card-ft"><span>${T('एक पेज पर','Rows per page')}</span><select data-tper="${id}" aria-label="rows per page">${[10, 25, 50].map(n => `<option ${s.per === n ? 'selected' : ''}>${n}</option>`).join('')}</select>
      <div class="pager"><button data-act="tpage" data-t="${id}" data-p="${s.page - 1}" ${s.page <= 1 ? 'disabled' : ''} aria-label="previous">${I('chev-l', 16)}</button>${pg.map(p => p === '…' ? `<button disabled>…</button>` : `<button class="${p === s.page ? 'on' : ''}" data-act="tpage" data-t="${id}" data-p="${p}">${p}</button>`).join('')}<button data-act="tpage" data-t="${id}" data-p="${s.page + 1}" ${s.page >= pages ? 'disabled' : ''} aria-label="next">${I('chev-r', 16)}</button></div></div>` : ''}
  </section>`;
}

/* ================= ALERTS ================= */
function alerts(){
  const a = [];
  for (const s of S.ships) if (s.stage < 4 && daysFrom(s.eta) < 0){ const p = party(s.party); a.push({sev: 'red', ic: 'clock', t: T('शिपमेंट लेट है','Shipment is late'), d: `${flag(s.country)} ${esc(s.ref)} · ${esc(p?.name)} · ${-daysFrom(s.eta)} ${T('दिन लेट','days late')}`, dsay: `${p?.name}, ${-daysFrom(s.eta)} ${T('दिन लेट','days late')}`, act: 'open-ship', id: s.id}); }
  if (seeMoney()) for (const x of S.txns) if (x.type === 'sale' && C.inv[x.id]?.over > 0){ const p = party(x.party); a.push({sev: 'red', ic: 'wallet', t: T('पैसा लेट है','Payment overdue'), d: `${esc(x.inv || '')} · ${esc(p?.name)} · ${money(C.inv[x.id].open)} · ${C.inv[x.id].over} ${T('दिन','days')}`, dsay: `${p?.name} ${T('से','from')} ${sayMoney(C.inv[x.id].open)}, ${C.inv[x.id].over} ${T('दिन से लेट','days late')}`, act: 'open-inv', id: x.id}); }
  if (seeMoney()) for (const x of S.txns) if (x.type === 'buy' && C.inv[x.id]?.open > .5 && x.due && daysFrom(x.due) <= 7){ const p = party(x.party), d = daysFrom(x.due); a.push({sev: d < 0 ? 'red' : 'amber', ic: 'cash', t: T('पैसा देना है','Payment due'), d: `${esc(p?.name)} · ${money(C.inv[x.id].open)} · ${d < 0 ? -d + ' ' + T('दिन लेट','days late') : d + ' ' + T('दिन में','days left')}`, dsay: `${p?.name} ${T('को','to')} ${sayMoney(C.inv[x.id].open)}`, act: 'open-party', id: x.party}); }
  for (const i of S.items){ const st = stockOf(i.id), d = C.days[i.id]; if (st < (+i.min || 0) || d < 15) a.push({sev: st < (+i.min || 0) ? 'red' : 'amber', ic: 'box', t: `${T('माल कम है','Low stock')}: ${esc(i.name)}`, d: `${qtyFmt(st)} ${esc(i.unit)} · ${daysTxt(d)}${C.incoming[i.id] ? ` · ${qtyFmt(C.incoming[i.id])} ${T('आ रहा','incoming')}` : ''}`, dsay: `${i.name}, ${qtyFmt(st)} ${i.unit} ${T('बचा','left')}`, act: 'open-item', id: i.id}); }
  for (const p of S.parties) if (seeMoney() && +p.limit > 0 && C.bal[p.id] > +p.limit) a.push({sev: 'red', ic: 'alert', t: T('उधार की सीमा पार','Credit limit crossed'), d: `${esc(p.name)} · ${money(C.bal[p.id])} / ${money(p.limit)}`, dsay: p.name, act: 'open-party', id: p.id});
  for (const s of S.ships) if (s.stage >= 2 && s.stage < 4 && !(s.docs || {}).bl) a.push({sev: 'amber', ic: 'file', t: T('कागज़ बाकी: Bill of Lading','Missing document: Bill of Lading'), d: `${flag(s.country)} ${esc(s.ref)}`, dsay: s.ref, act: 'open-ship', id: s.id});
  for (const s of S.ships) if (s.dir === 'in' && s.stage < 4 && daysFrom(s.eta) >= 0 && daysFrom(s.eta) <= 7){ const p = party(s.party); a.push({sev: 'blue', ic: 'in', t: T('माल जल्दी आने वाला है','Goods arriving soon'), d: `${flag(s.country)} ${esc(p?.name)} · ${daysFrom(s.eta)} ${T('दिन बाकी','days left')}`, dsay: `${p?.name}, ${daysFrom(s.eta)} ${T('दिन बाकी','days left')}`, act: 'open-ship', id: s.id}); }
  return a;
}
const alertRows = list => `<div class="attn">${list.map(a => `<button data-act="${a.act}" data-id="${a.id}" data-say="${esc(a.t + '. ' + a.dsay)}"><span class="sq ${a.sev}">${I(a.ic, 18)}</span><span style="min-width:0"><b>${a.t}</b><small>${a.d}</small></span>${I('chev-r', 16).replace('class="i"', 'class="i chev"')}</button>`).join('') || `<div class="empty">${I('ok', 28)}<div style="margin-top:6px">${T('सब ठीक है','All clear')}</div></div>`}</div>`;

/* ================= VIEWS ================= */
function linesTxt(x){ return (x.lines || []).map(l => { const i = item(l.item); return i ? `${i.icon} ${i.name} × ${qtyFmt(l.qty)} ${i.unit}` : ''; }).filter(Boolean).join(', '); }
function invStatus(x){
  const s = C.inv[x.id]; if (!s) return '';
  if (s.status === 'paid') return status('good', T('चुका दिया','Paid'));
  if (s.over > 0) return status('bad', `${T('लेट','Overdue')} ${s.over}${T(' दिन','d')}`);
  return s.status === 'part' ? status('warn', T('आधा मिला','Part paid')) : status('info', T('बाकी','Unpaid'));
}
function txnTable(id, list, title, showParty = true){
  const cols = [
    {k: 'd', label: T('तारीख','Date'), sort: x => x.at || x.d, cell: x => `<div class="cellp">${sqIcon(x.type, 1)}<div><div class="cell-main">${TYPE_NM(x.type)}</div><div class="cell-sub">${fmtDate(x.d)}</div></div></div>`},
    showParty ? {k: 'p', label: T('पार्टी','Party'), sort: x => party(x.party)?.name || '', cell: x => { const p = party(x.party); return p ? cellP(p, esc(p.name), `${flag(p.country)} ${esc(cname(p.country))}`) : '—'; }} : null,
    {k: 'doc', label: T('बिल नं.','Doc no.'), sort: x => x.inv || '', cell: x => `<span class="strong">${esc(x.inv || '—')}</span>`},
    {k: 'g', label: T('माल','Goods'), cell: x => x.type === 'adj' ? `${esc(item(x.item)?.name || '')} ${x.qty > 0 ? '+' : ''}${qtyFmt(x.qty)}` : `<span class="cell-sub" style="font-size:13px">${esc(linesTxt(x)) || (x.mode && MODES[x.mode] ? (hi() ? MODES[x.mode][1] : MODES[x.mode][2]) : '—')}</span>`},
    seeMoney() ? {k: 'a', label: T('रकम','Amount'), cls: 'r', sort: x => x.amount || 0, cell: x => x.amount ? `<div class="${x.type === 'sale' || x.type === 'payIn' ? 'money-pos' : 'money-neg'} num">${money(x.amount)}</div>${x.cur && x.cur !== 'INR' ? `<div class="cell-sub num">${fmoney(x.famount || x.amount / (x.fx || 1), x.cur)}</div>` : ''}` : '—'} : null,
    seeMoney() ? {k: 's', label: T('स्थिति','Status'), cell: x => invStatus(x) || status('gray', T('दर्ज','Recorded'))} : null,
    {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: x => (x.type === 'sale' || x.type === 'buy') && seeMoney() ? openBtn('open-inv', x.id) : x.party ? openBtn('open-party', x.party) : x.item ? openBtn('open-item', x.item) : ''}
  ].filter(Boolean);
  return dataTable({id, title, cols, rows: list, search: x => `${x.inv || ''} ${party(x.party)?.name || ''} ${linesTxt(x)} ${TYPE_NM(x.type)}`, ph: T('बिल नं., पार्टी, माल…','Doc no, party, goods…'),
    rowAttrs: x => `data-say="${esc(`${TYPE_NM(x.type)}. ${party(x.party)?.name || ''}. ${seeMoney() && x.amount ? sayMoney(x.amount) : ''}`)}" class="${C.inv[x.id]?.over > 0 ? 'flag-bad' : ''}"`});
}

V.home = () => {
  if (loading) return `<div class="empty-big"><span class="spin">${I('refresh', 28)}</span><b>${T('डेटा आ रहा है…','Loading data…')}</b></div>`;
  if (!S.items.length && !S.parties.length) return emptyStart();
  const out = S.ships.filter(s => s.dir === 'out' && s.stage < 4), inn = S.ships.filter(s => s.dir === 'in' && s.stage < 4);
  let lena = 0, dena = 0; for (const p of S.parties){ const b = C.bal[p.id]; if (b > 0) lena += b; else dena -= b; }
  const overdue = sum(Object.values(C.pOver)), m0 = today().slice(0, 7);
  const salesM = sum(S.txns.filter(x => x.type === 'sale' && x.d.startsWith(m0)), x => x.amount);
  const A = alerts();
  const said = `${T('लेना है','To receive')} ${sayMoney(lena)}. ${T('देना है','To pay')} ${sayMoney(dena)}. ${out.length} ${T('शिपमेंट जा रहे हैं','shipments going')}, ${inn.length} ${T('आ रहे हैं','coming')}. ${A.length} ${T('बातों पर ध्यान दो','items need attention')}.`;
  const transit = out.concat(inn).sort((a, b) => a.eta.localeCompare(b.eta));
  return `${pageHd([T('डैशबोर्ड','Dashboard')], T('डैशबोर्ड','Dashboard'), T('माल, पैसे और शिपमेंट का आज का हाल','Today’s overview of stock, money and shipments'),
      `${sampleCap ? `<button class="btn" data-act="ai">${I('spark', 16)} ${T('मुनीम जी से पूछो','Ask Munim')}</button>` : ''}<button class="btn" data-say="${esc(said)}">${I('vol', 16)} ${T('सुनो','Listen')}</button>${P.role !== 'worker' ? `<button class="btn pri" data-act="wiz">${I('plus', 16)} ${T('नई एंट्री','New entry')}</button>` : ''}`)}
  ${S.settings.demo ? `<div class="note">${I('alert', 16)}<span>${T('यह नमूना (demo) डेटा है। अपना काम शुरू करने के लिए सेटिंग में "सब मिटाओ" दबाएँ।','This is sample data. To start with your own records, use "Erase all" in Settings.')}</span></div>` : ''}
  <div class="stats">
    ${seeMoney() ? stat('wallet', 'green', money(lena), T('लेना है','To receive'), overdue > 0 ? {t: `${money(overdue)} ${T('लेट','overdue')}`, bad: 1} : T('ग्राहकों से','from buyers'), `data-act="nav" data-v="money"`, T('लेना है','To receive') + ' ' + sayMoney(lena))
      + stat('cash', 'red', money(dena), T('देना है','To pay'), T('सप्लायरों को','to suppliers'), `data-act="nav" data-v="money"`, T('देना है','To pay') + ' ' + sayMoney(dena))
      + stat('trend', 'blue', money(salesM), T('इस महीने बिक्री','Sales this month'), `${S.txns.filter(x => x.type === 'sale' && x.d.startsWith(m0)).length} ${T('बिल','invoices')}`, `data-act="nav" data-v="reports"`, T('इस महीने बिक्री','Sales this month') + ' ' + sayMoney(salesM))
      : stat('box', 'blue', S.items.length, T('माल','Items'), '', `data-act="nav" data-v="stock"`) + stat('alert', 'red', A.filter(a => a.ic === 'box').length, T('माल कम है','Low stock'), '', `data-act="nav" data-v="stock"`)}
    ${stat('ship', 'teal', `${out.length + inn.length}`, T('शिपमेंट रास्ते में','Shipments in transit'), `${out.length} ${T('जा रहे','export')} · ${inn.length} ${T('आ रहे','import')}`, `data-act="nav" data-v="ships"`, `${out.length} ${T('जा रहे','going')}, ${inn.length} ${T('आ रहे','coming')}`)}
  </div>
  ${P.role !== 'worker' ? `<div class="quick">${[['sale', T('बिक्री / माल दिया','Sale / goods out'), T('बिल बनाओ','Create invoice')], ['buy', T('खरीद / माल लिया','Purchase / goods in'), T('सप्लायर का बिल','Supplier bill')], ['payIn', T('पैसा मिला','Payment received'), T('ग्राहक से','From a buyer')], ['payOut', T('पैसा दिया','Payment made'), T('सप्लायर को','To a supplier')], ['exp', T('खर्चा','Expense'), T('भाड़ा, कस्टम…','Freight, duty…')]].map(([k, l, s]) => `<button class="qa" data-act="wiz" data-type="${k}" data-say="${esc(l)}">${sqIcon(k)}<span style="min-width:0"><b>${l}</b><small>${s}</small></span></button>`).join('')}</div>` : ''}
  <div class="grid2">
    <section class="card"><div class="card-hd"><h2>${T('ध्यान दो','Needs attention')}</h2>${A.length ? `<span class="count" style="color:var(--danger);background:var(--danger-50);border-color:var(--danger-200)">${A.length}</span>` : ''}<span class="sp"></span>${A.length > 6 ? `<button class="btn sm" data-act="alerts">${T('सब देखो','View all')}</button>` : ''}</div>${alertRows(A.slice(0, 6))}</section>
    <section class="card"><div class="card-hd"><h2>${T('रास्ते में शिपमेंट','Shipments in transit')}</h2><span class="sp"></span><button class="btn sm" data-act="nav" data-v="ships">${T('सब देखो','View all')}</button></div>
      <div class="attn">${transit.slice(0, 6).map(s => { const p = party(s.party), dl = daysFrom(s.eta); return `<button data-act="open-ship" data-id="${s.id}" data-say="${esc(cname(s.country) + '. ' + (p?.name || '') + '. ' + STAGES()[s.stage])}"><span class="sq gray" style="font-size:20px">${flag(s.country)}</span><span style="min-width:0;flex:1"><b>${esc(s.ref)} <span class="muted" style="font-weight:500">· ${s.dir === 'out' ? T('एक्सपोर्ट','Export') : T('इम्पोर्ट','Import')}</span></b><small>${esc(p?.name || '')} · ${esc(cname(s.country))}</small></span><span style="text-align:right">${status(stageCls(s.stage), STAGES()[s.stage])}<small class="${dl < 0 ? 'money-neg' : ''}" style="display:block;margin-top:4px;font-size:12px">${dl < 0 ? -dl + ' ' + T('दिन लेट','d late') : dl + ' ' + T('दिन बाकी','d left')}</small></span></button>`; }).join('') || `<div class="empty">${T('कोई शिपमेंट रास्ते में नहीं','Nothing in transit')}</div>`}</div></section>
  </div>
  ${txnTable('recent', S.txns.slice().sort((a, b) => (b.at || b.d).localeCompare(a.at || a.d)), T('हाल की एंट्री','Recent entries'))}`;
};
function emptyStart(){
  return `${pageHd([T('डैशबोर्ड','Dashboard')], T('कार्गो मुनीम में स्वागत है','Welcome to Cargo Munim'), T('पहले अपना माल और अपनी पार्टियाँ जोड़ो','Start by adding your goods and your trading partners'))}
  <div class="empty-big"><span class="stat-ic blue">${I('ship', 26)}</span><b style="font-size:16px">${T('अभी कोई डेटा नहीं है','No records yet')}</b>
  <p class="muted" style="margin:0;max-width:52ch">${T('माल और ग्राहक / सप्लायर जोड़ो। या नमूना डेटा डालकर देखो कि सिस्टम कैसे काम करता है।','Add goods and buyers / suppliers, or load sample data to see how the system works.')}</p>
  <div class="row" style="justify-content:center"><button class="btn pri" data-act="item-form">${I('box', 16)} ${T('माल जोड़ो','Add goods')}</button><button class="btn pri" data-act="party-form">${I('users', 16)} ${T('पार्टी जोड़ो','Add party')}</button>${isOwner() ? `<button class="btn" data-act="demo-load">${T('नमूना डेटा डालो','Load sample data')}</button>` : ''}</div>
  ${mode === 'cloud' ? `<div class="note info">${I('users', 16)}<span>${T('ये डेटा पूरी टीम के साथ शेयर होता है।','This data is shared with your whole team.')}</span></div>` : ''}</div>`;
}

V.stock = () => {
  const low = S.items.filter(i => stockOf(i.id) < (+i.min || 0)).length, inc = S.items.filter(i => C.incoming[i.id]).length;
  const val = sum(S.items, i => Math.max(0, stockOf(i.id)) * (+i.buy || 0));
  const cols = [
    {k: 'n', label: T('माल','Item'), sort: i => i.name.toLowerCase(), cell: i => cellP(i, esc(i.name), [i.hs ? 'HS ' + esc(i.hs) : '', i.alias ? esc(i.alias) : ''].filter(Boolean).join(' · '))},
    {k: 's', label: T('गोदाम में','In stock'), sort: i => stockOf(i.id), cell: i => { const st = stockOf(i.id), lowI = st < (+i.min || 0), d = C.days[i.id]; return `<div class="strong num">${qtyFmt(st)} <span class="muted" style="font-weight:500">${esc(i.unit)}</span></div><div class="bar ${lowI ? 'low' : d < 30 ? 'mid' : ''}" style="margin-top:6px"><i style="width:${Math.max(3, Math.min(100, st / Math.max(1, (+i.min || 1) * 3) * 100))}%"></i></div>`; }},
    {k: 'd', label: T('कितने दिन का','Days left'), sort: i => C.days[i.id] === Infinity ? 1e9 : C.days[i.id], cell: i => { const d = C.days[i.id]; return d === Infinity ? `<span class="muted">—</span>` : `<span class="num ${d < 15 ? 'money-neg' : ''}">~${d} ${T('दिन','days')}</span>`; }},
    {k: 'in', label: T('आ रहा','Incoming'), cls: 'r', sort: i => C.incoming[i.id] || 0, cell: i => C.incoming[i.id] ? `<span class="num">${qtyFmt(C.incoming[i.id])}</span>` : `<span class="muted">—</span>`},
    seeMoney() ? {k: 'b', label: T('खरीद भाव','Buy'), cls: 'r', sort: i => +i.buy, cell: i => `<span class="num">${money(i.buy)}</span>`} : null,
    seeMoney() ? {k: 'sl', label: T('बिक्री भाव','Sell'), cls: 'r', sort: i => +i.sell, cell: i => `<span class="num strong">${money(i.sell)}</span>`} : null,
    {k: 'st', label: T('स्थिति','Status'), cell: i => { const st = stockOf(i.id), d = C.days[i.id]; return st < (+i.min || 0) ? status('bad', T('कम है','Low')) : d < 30 ? status('warn', T('खत्म होने वाला','Running low')) : status('good', T('ठीक है','In stock')); }},
    {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: i => openBtn('open-item', i.id)}
  ].filter(Boolean);
  return `${pageHd([T('माल','Stock')], T('माल (स्टॉक)','Stock'), T('गोदाम में कितना माल है, कितने दिन चलेगा और कितना आ रहा है','What is in the godown, how long it will last and what is on the way'),
      P.role !== 'worker' ? `<button class="btn pri" data-act="item-form">${I('plus', 16)} ${T('नया माल','New item')}</button>` : '')}
  <div class="stats">${stat('box', 'blue', S.items.length, T('कुल माल','Items'), '')}${seeMoney() ? stat('wallet', 'teal', money(val), T('गोदाम की कीमत','Stock value'), T('खरीद भाव पर','at buying price')) : ''}${stat('alert', 'red', low, T('माल कम है','Low stock'), '')}${stat('in', 'amber', inc, T('रास्ते में माल','Items incoming'), '')}</div>
  ${dataTable({id: 'stock', title: T('सारा माल','All items'), cols, rows: S.items, search: i => `${i.name} ${i.alias || ''} ${i.hs || ''}`, ph: T('माल का नाम, HS code…','Item name, HS code…'),
    rowAttrs: i => `data-act="open-item" data-id="${i.id}" data-say="${esc(`${i.name}. ${qtyFmt(stockOf(i.id))} ${i.unit}. ${daysTxt(C.days[i.id])}`)}" class="${stockOf(i.id) < (+i.min || 0) ? 'flag-bad' : C.days[i.id] < 30 ? 'flag-warn' : ''}"`})}`;
};

V.parties = () => {
  let ps = S.parties.map(p => ({p, b: C.bal[p.id] || 0, o: C.pOver[p.id] || 0}));
  const cnt = {all: ps.length, cust: ps.filter(x => x.p.kind !== 'supplier').length, sup: ps.filter(x => x.p.kind !== 'customer').length, late: ps.filter(x => x.o > 0).length};
  if (partyFilter === 'lena') ps = ps.filter(x => x.b > 0); if (partyFilter === 'dena') ps = ps.filter(x => x.b < 0);
  if (partyFilter === 'cust') ps = ps.filter(x => x.p.kind !== 'supplier'); if (partyFilter === 'sup') ps = ps.filter(x => x.p.kind !== 'customer');
  if (partyFilter === 'late') ps = ps.filter(x => x.o > 0);
  const tab = (k, l) => `<button class="${partyFilter === k ? 'on' : ''}" data-act="pfilter" data-f="${k}">${l}</button>`;
  const cols = [
    {k: 'n', label: T('पार्टी','Party'), sort: x => x.p.name.toLowerCase(), cell: x => cellP(x.p, esc(x.p.name), x.p.phone ? esc(x.p.phone) : (x.p.alias ? esc(x.p.alias) : ''))},
    {k: 'c', label: T('देश','Country'), sort: x => cname(x.p.country), cell: x => `<span class="row" style="gap:6px;flex-wrap:nowrap"><span class="flag">${flag(x.p.country)}</span>${esc(cname(x.p.country))}</span>`},
    {k: 't', label: T('प्रकार','Type'), cell: x => x.p.kind === 'customer' ? status('info', T('ग्राहक','Buyer')) : x.p.kind === 'supplier' ? status('violet', T('सप्लायर','Supplier')) : status('teal', T('दोनों','Both'))},
    {k: 'cd', label: T('उधार दिन','Credit'), cls: 'r', sort: x => +x.p.terms || 0, cell: x => `<span class="num">${+x.p.terms || 0} ${T('दिन','d')}</span>`},
    seeMoney() ? {k: 'b', label: T('बकाया','Balance'), cls: 'r', sort: x => x.b, cell: x => x.b ? `<div class="${x.b > 0 ? 'money-pos' : 'money-neg'} num">${money(x.b)}</div><div class="cell-sub">${x.b > 0 ? T('हमें मिलेगा','receivable') : T('हमें देना','payable')}</div>` : `<span class="muted">—</span>`} : null,
    seeMoney() ? {k: 'o', label: T('स्थिति','Status'), sort: x => x.o, cell: x => x.o > 0 ? status('bad', `${money(x.o)} ${T('लेट','overdue')}`) : x.b ? status('good', T('समय पर','On time')) : status('gray', T('हिसाब बराबर','Settled'))} : null,
    {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: x => openBtn('open-party', x.p.id)}
  ].filter(Boolean);
  return `${pageHd([T('पार्टी','Parties')], T('पार्टी (ग्राहक और सप्लायर)','Parties'), T('हर ग्राहक और सप्लायर का हिसाब, देश और बकाया','Every buyer and supplier with their country and balance'),
      P.role !== 'worker' ? `<button class="btn pri" data-act="party-form">${I('plus', 16)} ${T('नई पार्टी','New party')}</button>` : '')}
  <div class="stats">${stat('users', 'blue', cnt.all, T('कुल पार्टी','Total parties'), '', `data-act="pfilter" data-f="all"`)}${stat('trend', 'teal', cnt.cust, T('ग्राहक','Buyers'), '', `data-act="pfilter" data-f="cust"`)}${stat('truck', 'violet', cnt.sup, T('सप्लायर','Suppliers'), '', `data-act="pfilter" data-f="sup"`)}${seeMoney() ? stat('clock', 'red', cnt.late, T('पैसा लेट','With overdue'), '', `data-act="pfilter" data-f="late"`) : ''}</div>
  ${dataTable({id: 'parties', title: T('पार्टी','Parties'), cols, rows: ps, search: x => `${x.p.name} ${x.p.alias || ''} ${x.p.phone || ''} ${cname(x.p.country)}`, ph: T('नाम, फ़ोन, देश…','Name, phone, country…'),
    tools: `<div class="tabs">${tab('all', T('सब','All'))}${seeMoney() ? tab('lena', T('लेना है','Receivable')) + tab('dena', T('देना है','Payable')) + tab('late', T('लेट','Overdue')) : ''}${tab('cust', T('ग्राहक','Buyers'))}${tab('sup', T('सप्लायर','Suppliers'))}</div>`,
    rowAttrs: x => `data-act="open-party" data-id="${x.p.id}" data-say="${esc(x.p.name + (seeMoney() && x.b ? '. ' + (x.b > 0 ? T('ये हमें देंगे','They owe us') : T('हमें देना है','We owe them')) + ' ' + sayMoney(x.b) : ''))}" class="${x.o > 0 ? 'flag-bad' : ''}"`})}`;
};

V.ships = () => {
  const all = S.ships, act = all.filter(s => s.stage < 4);
  const list = all.filter(s => shipDir === 'all' || s.dir === shipDir);
  const late = act.filter(s => daysFrom(s.eta) < 0).length, docsP = act.filter(s => DOCS.filter(d => (s.docs || {})[d[0]]).length < DOCS.length).length;
  const tab = (k, l) => `<button class="${shipDir === k ? 'on' : ''}" data-act="ship-tab" data-d="${k}">${l}</button>`;
  const cols = [
    {k: 'r', label: T('शिपमेंट','Shipment'), sort: s => s.ref, cell: s => `<div class="cellp"><span class="sq ${s.dir === 'out' ? 'blue' : 'amber'} sm">${I(s.dir === 'out' ? 'out' : 'in', 16)}</span><div><div class="cell-main">${esc(s.ref)}</div><div class="cell-sub">${esc(s.vessel || (s.dir === 'out' ? T('एक्सपोर्ट','Export') : T('इम्पोर्ट','Import')))}</div></div></div>`},
    {k: 'p', label: T('पार्टी','Party'), sort: s => party(s.party)?.name || '', cell: s => { const p = party(s.party); return p ? cellP(p, esc(p.name), esc((s.lines || []).map(l => item(l.item)?.name).filter(Boolean).join(', '))) : '—'; }},
    {k: 'c', label: T('रास्ता','Route'), sort: s => cname(s.country), cell: s => `<div class="strong" style="white-space:nowrap">${flag(s.dir === 'out' ? 'IN' : s.country)} → ${flag(s.dir === 'out' ? s.country : 'IN')} ${esc(cname(s.country))}</div><div class="cell-sub">${esc(s.pol || '')} → ${esc(s.pod || '')}</div>`},
    {k: 's', label: T('कहाँ है','Stage'), sort: s => s.stage, cell: s => `${status(stageCls(s.stage), STAGES()[s.stage])}<div class="mini-prog">${[0, 1, 2, 3, 4].map(k => `<i class="${k < s.stage ? 'done' : k === s.stage ? 'on' : ''}"></i>`).join('')}</div>`},
    {k: 'e', label: T('पहुँचेगा','ETA'), sort: s => s.eta, cell: s => { const d = daysFrom(s.eta); return `${dtCell(s.eta)}<div class="cell-sub ${s.stage < 4 && d < 0 ? 'money-neg' : ''}">${s.stage >= 4 ? T('पहुँच गया','Delivered') : d < 0 ? -d + ' ' + T('दिन लेट','days late') : d + ' ' + T('दिन बाकी','days left')}</div>`; }},
    {k: 'dc', label: T('कागज़','Docs'), cls: 'c', sort: s => DOCS.filter(d => (s.docs || {})[d[0]]).length, cell: s => { const n = DOCS.filter(d => (s.docs || {})[d[0]]).length; return `<span class="pill ${n === DOCS.length ? 'good' : n >= 4 ? 'info' : 'warn'} num">${n}/${DOCS.length}</span>`; }},
    {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: s => openBtn('open-ship', s.id)}
  ];
  return `${pageHd([T('शिपमेंट','Shipments')], T('शिपमेंट','Shipments'), T('कौन सा माल किस देश जा रहा है और कहाँ से आ रहा है','Which goods are going where, and what is coming in'))}
  <div class="stats">${stat('out', 'blue', act.filter(s => s.dir === 'out').length, T('एक्सपोर्ट रास्ते में','Exports in transit'), '', `data-act="ship-tab" data-d="out"`)}${stat('in', 'amber', act.filter(s => s.dir === 'in').length, T('इम्पोर्ट रास्ते में','Imports in transit'), '', `data-act="ship-tab" data-d="in"`)}${stat('clock', 'red', late, T('लेट शिपमेंट','Late shipments'), '')}${stat('file', 'teal', docsP, T('कागज़ बाकी','Documents pending'), '')}</div>
  ${dataTable({id: 'ships', title: T('शिपमेंट','Shipments'), cols, rows: list.sort((a, b) => (a.stage === 4) - (b.stage === 4) || a.eta.localeCompare(b.eta)), search: s => `${s.ref} ${party(s.party)?.name || ''} ${cname(s.country)} ${s.container || ''} ${s.bl || ''} ${s.vessel || ''}`, ph: T('नंबर, पार्टी, कंटेनर…','Ref, party, container…'),
    tools: `<div class="tabs">${tab('out', T('एक्सपोर्ट','Export'))}${tab('in', T('इम्पोर्ट','Import'))}${tab('all', T('सब','All'))}</div>`,
    rowAttrs: s => `data-act="open-ship" data-id="${s.id}" data-say="${esc(`${cname(s.country)}. ${party(s.party)?.name || ''}. ${STAGES()[s.stage]}`)}" class="${s.stage < 4 && daysFrom(s.eta) < 0 ? 'flag-bad' : ''}"`})}`;
};

V.money = () => {
  if (!seeMoney()) return noAccess();
  const ps = S.parties.map(p => ({p, b: C.bal[p.id] || 0, o: C.pOver[p.id] || 0}));
  const le = ps.filter(x => x.b > 0), de = ps.filter(x => x.b < 0), ag = aging('sale'), ap = aging('buy');
  const pcols = sign => [
    {k: 'n', label: T('पार्टी','Party'), sort: x => x.p.name, cell: x => cellP(x.p, esc(x.p.name), `${flag(x.p.country)} ${esc(cname(x.p.country))}`)},
    {k: 'b', label: T('बकाया','Balance'), cls: 'r', sort: x => Math.abs(x.b), cell: x => `<span class="${sign > 0 ? 'money-pos' : 'money-neg'} num">${money(x.b)}</span>${x.p.cur && x.p.cur !== 'INR' ? `<div class="cell-sub num">≈ ${fmoney(x.b / fxOf(x.p.cur), x.p.cur)}</div>` : ''}`},
    {k: 'o', label: T('स्थिति','Status'), sort: x => x.o, cell: x => x.o > 0 ? status('bad', `${money(x.o)} ${T('लेट','overdue')}`) : status('good', T('समय पर','On time'))},
    {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: x => openBtn('open-party', x.p.id)}];
  const ra = x => `data-act="open-party" data-id="${x.p.id}" data-say="${esc(x.p.name + ' ' + sayMoney(x.b))}" class="${x.o > 0 ? 'flag-bad' : ''}"`;
  return `${pageHd([T('पैसा','Finance'), T('लेना-देना','Receivables & payables')], T('लेना-देना','Receivables & payables'), T('किससे कितना पैसा लेना है और किसे कितना देना है','Who owes us, and whom we owe'),
      `<button class="btn" data-act="wiz" data-type="payIn">${I('wallet', 16)} ${T('पैसा मिला','Payment received')}</button><button class="btn pri" data-act="wiz" data-type="payOut">${I('cash', 16)} ${T('पैसा दिया','Payment made')}</button>`)}
  <div class="stats">${stat('wallet', 'green', money(sum(le, x => x.b)), T('लेना है','To receive'), `${le.length} ${T('पार्टी','parties')}`, '', T('लेना है','To receive') + ' ' + sayMoney(sum(le, x => x.b)))}${stat('cash', 'red', money(sum(de, x => -x.b)), T('देना है','To pay'), `${de.length} ${T('पार्टी','parties')}`, '', T('देना है','To pay') + ' ' + sayMoney(sum(de, x => -x.b)))}${stat('clock', 'amber', money(ag.late), T('लेना — लेट','Receivable overdue'), '')}${stat('alert', 'violet', money(ap.late), T('देना — लेट','Payable overdue'), '')}</div>
  <div class="grid2 even">
    <section class="card"><div class="card-hd"><h2>${T('लेना है — कितना पुराना','Receivables by age')}</h2></div><div class="card-bd">${agingBars(ag)}</div></section>
    <section class="card"><div class="card-hd"><h2>${T('देना है — कितना पुराना','Payables by age')}</h2></div><div class="card-bd">${agingBars(ap)}</div></section>
  </div>
  <div class="grid2 even">
    ${dataTable({id: 'recv', title: T('लेना है','Receivables'), cols: pcols(1), rows: le, rowAttrs: ra, empty: T('हिसाब बराबर','All settled')})}
    ${dataTable({id: 'pay', title: T('देना है','Payables'), cols: pcols(-1), rows: de, rowAttrs: ra, empty: T('हिसाब बराबर','All settled')})}
  </div>
  ${txnTable('pays', S.txns.filter(x => x.type === 'payIn' || x.type === 'payOut').sort((a, b) => b.d.localeCompare(a.d)), T('पैसे के लेन-देन','Payments'))}`;
};

V.invoices = () => {
  if (!seeMoney()) return noAccess();
  const all = S.txns.filter(x => x.type === invKind);
  let list = all;
  if (invFilter === 'open') list = list.filter(x => C.inv[x.id]?.status !== 'paid');
  if (invFilter === 'late') list = list.filter(x => C.inv[x.id]?.over > 0);
  if (invFilter === 'paid') list = list.filter(x => C.inv[x.id]?.status === 'paid');
  const openAmt = sum(all, x => C.inv[x.id]?.open || 0), lateAmt = sum(all.filter(x => C.inv[x.id]?.over > 0), x => C.inv[x.id].open);
  const tab = (k, l) => `<button class="${invFilter === k ? 'on' : ''}" data-act="ifilter" data-f="${k}">${l}</button>`;
  const cols = [
    {k: 'n', label: T('बिल नं.','Invoice no.'), sort: x => x.inv || '', cell: x => `<div class="cellp"><span class="sq ${invKind === 'sale' ? 'blue' : 'amber'} sm">${I('file', 16)}</span><div><div class="cell-main">${esc(x.inv || '—')}</div><div class="cell-sub">${esc(x.incoterm || '')}${x.ship ? ' · ' + esc(ship(x.ship)?.ref || '') : ''}</div></div></div>`},
    {k: 'p', label: T('पार्टी','Party'), sort: x => party(x.party)?.name || '', cell: x => { const p = party(x.party); return p ? cellP(p, esc(p.name), `${flag(p.country)} ${esc(cname(p.country))}`) : '—'; }},
    {k: 'd', label: T('तारीख','Date'), sort: x => x.d, cell: x => dtCell(x.d)},
    {k: 'due', label: T('आख़िरी तारीख','Due'), sort: x => x.due || '', cell: x => dtCell(x.due)},
    {k: 'a', label: T('रकम','Amount'), cls: 'r', sort: x => x.amount, cell: x => `<div class="strong num">${money(x.amount)}</div>${x.cur && x.cur !== 'INR' ? `<div class="cell-sub num">${fmoney(x.famount, x.cur)}</div>` : ''}${C.inv[x.id]?.status === 'part' ? `<div class="cell-sub num">${T('बाकी','open')} ${money(C.inv[x.id].open)}</div>` : ''}`},
    {k: 's', label: T('स्थिति','Status'), sort: x => C.inv[x.id]?.over || 0, cell: x => invStatus(x)},
    {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: x => openBtn('open-inv', x.id)}];
  return `${pageHd([T('पैसा','Finance'), T('बिल','Invoices')], T('बिल / इनवॉइस','Invoices & bills'), T('हर बिल, उसकी आख़िरी तारीख और कितना पैसा आया','Every invoice, its due date and how much has been paid'),
      `<div class="tabs"><button class="${invKind === 'sale' ? 'on' : ''}" data-act="ikind" data-k="sale">${T('बिक्री के बिल','Sales invoices')}</button><button class="${invKind === 'buy' ? 'on' : ''}" data-act="ikind" data-k="buy">${T('खरीद के बिल','Purchase bills')}</button></div>`)}
  <div class="stats">${stat('file', 'blue', all.length, T('कुल बिल','Total bills'), money(sum(all, x => x.amount)))}${stat('clock', 'amber', money(openAmt), T('बाकी रकम','Outstanding'), `${all.filter(x => C.inv[x.id]?.status !== 'paid').length} ${T('बिल','bills')}`)}${stat('alert', 'red', money(lateAmt), T('लेट रकम','Overdue'), `${all.filter(x => C.inv[x.id]?.over > 0).length} ${T('बिल','bills')}`)}${stat('ok', 'green', all.filter(x => C.inv[x.id]?.status === 'paid').length, T('पूरे चुके','Fully paid'), '')}</div>
  ${dataTable({id: 'inv-' + invKind, title: invKind === 'sale' ? T('बिक्री के बिल','Sales invoices') : T('खरीद के बिल','Purchase bills'), cols, rows: list.sort((a, b) => b.d.localeCompare(a.d)), search: x => `${x.inv || ''} ${party(x.party)?.name || ''} ${linesTxt(x)}`, ph: T('बिल नं., पार्टी…','Invoice no, party…'),
    tools: `<div class="tabs">${tab('open', T('बाकी','Unpaid'))}${tab('late', T('लेट','Overdue'))}${tab('paid', T('चुका','Paid'))}${tab('all', T('सब','All'))}</div>`,
    rowAttrs: x => `data-act="open-inv" data-id="${x.id}" data-say="${esc((x.inv || '') + '. ' + (party(x.party)?.name || '') + '. ' + sayMoney(x.amount))}" class="${C.inv[x.id]?.over > 0 ? 'flag-bad' : ''}"`})}`;
};

V.expenses = () => {
  if (!seeMoney()) return noAccess();
  const m0 = today().slice(0, 7), thisM = S.expenses.filter(e => e.d.startsWith(m0)), byCat = groupBy(thisM, e => e.cat);
  const tot = sum(thisM, e => e.amount), mx = Math.max(1, ...Object.values(byCat).map(a => sum(a, e => e.amount)));
  const cols = [
    {k: 'd', label: T('तारीख','Date'), sort: e => e.d, cell: e => `<div class="cellp"><span class="sq violet sm">${I(EXP_IC[e.cat] || 'receipt', 16)}</span><div><div class="cell-main">${esc(expNm(e.cat))}</div><div class="cell-sub">${fmtDate(e.d)}</div></div></div>`},
    {k: 's', label: T('शिपमेंट','Shipment'), sort: e => ship(e.ship)?.ref || '', cell: e => { const s = e.ship ? ship(e.ship) : null; return s ? `<button class="btn sm" data-act="open-ship" data-id="${s.id}">${flag(s.country)} ${esc(s.ref)}</button>` : `<span class="muted">${T('ऑफ़िस','Office')}</span>`; }},
    {k: 'n', label: T('नोट','Note'), cell: e => esc(e.note || '—')},
    {k: 'a', label: T('रकम','Amount'), cls: 'r', sort: e => e.amount, cell: e => `<span class="money-neg num">${money(e.amount)}</span>`},
    isOwner() ? {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: e => `<button class="btn sm ${armed === 'dx-' + e.id ? 'armed' : ''}" data-act="del-exp" data-id="${e.id}">${armed === 'dx-' + e.id ? T('पक्का?','Confirm?') : I('trash', 15)}</button>`} : null].filter(Boolean);
  return `${pageHd([T('पैसा','Finance'), T('खर्चे','Expenses')], T('खर्चे','Expenses'), T('भाड़ा, कस्टम, एजेंट, बीमा — शिपमेंट से जोड़ो तो हर शिपमेंट का मुनाफ़ा दिखेगा','Freight, duty, agent, insurance — link them to shipments to see profit per shipment'),
      `<button class="btn pri" data-act="wiz" data-type="exp">${I('plus', 16)} ${T('नया खर्चा','New expense')}</button>`)}
  <div class="stats">${stat('receipt', 'violet', money(tot), T('इस महीने','This month'), `${thisM.length} ${T('एंट्री','entries')}`)}${stat('ship', 'teal', money(sum(thisM.filter(e => e.ship), e => e.amount)), T('शिपमेंट पर','On shipments'), '')}${stat('bank', 'amber', money(sum(thisM.filter(e => !e.ship), e => e.amount)), T('ऑफ़िस खर्चे','Office'), '')}${stat('chart', 'blue', money(sum(S.expenses, e => e.amount)), T('कुल खर्चे','All time'), '')}</div>
  <div class="grid2">
    ${dataTable({id: 'exp', title: T('सारे खर्चे','All expenses'), cols, rows: S.expenses.slice().sort((a, b) => b.d.localeCompare(a.d)), search: e => `${expNm(e.cat)} ${e.note || ''} ${ship(e.ship)?.ref || ''}`, rowAttrs: e => `data-say="${esc(expNm(e.cat) + ' ' + sayMoney(e.amount))}"`})}
    <section class="card" style="align-self:start"><div class="card-hd"><h2>${T('इस महीने किस पर खर्च','This month by category')}</h2></div><div class="card-bd"><div class="hbars">${Object.entries(byCat).sort((a, b) => sum(b[1], e => e.amount) - sum(a[1], e => e.amount)).map(([k, a]) => { const v = sum(a, e => e.amount); return `<div class="hb" data-say="${esc(expNm(k) + ' ' + sayMoney(v))}"><span class="nm">${esc(expNm(k))}</span><span class="v">${money(v)}</span><span class="track2"><i style="width:${v / mx * 100}%;background:var(--violet)"></i></span></div>`; }).join('') || `<div class="muted">${T('इस महीने कोई खर्चा नहीं','No expenses this month')}</div>`}</div></div></section>
  </div>`;
};

V.reports = () => {
  if (!seeMoney()) return noAccess();
  const months = []; { const d = new Date(); d.setDate(1); for (let k = 5; k >= 0; k--){ const x = new Date(d.getFullYear(), d.getMonth() - k, 1); months.push(`${x.getFullYear()}-${pad2(x.getMonth() + 1)}`); } }
  const mData = months.map(m => { const sales = S.txns.filter(x => x.type === 'sale' && x.d.startsWith(m)), buys = S.txns.filter(x => x.type === 'buy' && x.d.startsWith(m)); const exp = sum(S.expenses.filter(e => e.d.startsWith(m)), e => e.amount); const rev = sum(sales, x => x.amount), cogs = sum(sales, x => sum(x.lines || [], lineCost)); return {m, rev, buy: sum(buys, x => x.amount), exp, profit: rev - cogs - exp}; });
  const cur = mData[5], allSales = S.txns.filter(x => x.type === 'sale');
  const itemP = {}; for (const x of allSales) for (const l of x.lines || []){ const r = itemP[l.item] = itemP[l.item] || {rev: 0, cost: 0}; r.rev += l.qty * l.rate; r.cost += lineCost(l); }
  const itemRows = Object.entries(itemP).map(([id, r]) => ({i: item(id), ...r, p: r.rev - r.cost})).filter(r => r.i).sort((a, b) => b.p - a.p);
  const cty = {}; for (const x of allSales){ const p = party(x.party), s = x.ship ? ship(x.ship) : null, cc = s?.country || p?.country || '??'; if (cc === 'IN') continue; cty[cc] = (cty[cc] || 0) + x.amount; }
  const ctyRows = Object.entries(cty).sort((a, b) => b[1] - a[1]), ctyTot = sum(ctyRows, r => r[1]);
  const cust = {}; for (const x of allSales) cust[x.party] = (cust[x.party] || 0) + x.amount;
  const custRows = Object.entries(cust).map(([id, v]) => ({p: party(id), v})).filter(r => r.p).sort((a, b) => b.v - a.v).slice(0, 6);
  const fyStart = (() => { const d = new Date(); const y = d.getMonth() >= 3 ? d.getFullYear() : d.getFullYear() - 1; return `${y}-04-01`; })();
  const exportFOB = sum(allSales.filter(x => x.ship && x.d >= fyStart && (ship(x.ship)?.country || 'IN') !== 'IN'), x => x.amount);
  const incent = exportFOB * ((+S.settings.rodtep || 0) + (+S.settings.drawback || 0)) / 100;
  const shipP = S.ships.filter(s => s.dir === 'out').map(s => { const x = S.txns.find(t => t.id === s.txn); if (!x) return null; const rev = x.amount, cost = sum(x.lines || [], lineCost), e = expOfShip(s.id); return {s, rev, cost, e, p: rev - cost - e}; }).filter(Boolean);
  const mxv = (rows, f) => Math.max(1, ...rows.map(f));
  const hcard = (title, body) => `<section class="card"><div class="card-hd"><h2>${title}</h2></div><div class="card-bd">${body}</div></section>`;
  return `${pageHd([T('पैसा','Finance'), T('रिपोर्ट','Reports')], T('रिपोर्ट','Reports'), T('बिक्री, खरीद, खर्चे और मुनाफ़ा एक नज़र में','Sales, purchases, expenses and profit at a glance'),
      `<button class="btn" data-act="csv" data-k="txns">${I('download', 16)} ${T('सब एंट्री (Excel)','All entries (Excel)')}</button>`)}
  <div class="stats">${stat('trend', 'blue', money(cur.rev), T('इस महीने बिक्री','Sales this month'), '')}${stat('in', 'amber', money(cur.buy), T('इस महीने खरीद','Purchases this month'), '')}${stat('receipt', 'violet', money(cur.exp), T('इस महीने खर्चे','Expenses this month'), '')}${stat(cur.profit >= 0 ? 'ok' : 'alert', cur.profit >= 0 ? 'green' : 'red', smoney(cur.profit), cur.profit >= 0 ? T('मुनाफ़ा','Profit this month') : T('नुकसान','Loss this month'), T('बिक्री − लागत − खर्चे','sales − cost − expenses'))}</div>
  ${hcard(T('पिछले 6 महीने','Last 6 months'), monthChart(mData))}
  <div class="grid2 even">
    ${hcard(T('किस माल में कितना मुनाफ़ा','Profit by item'), `<div class="hbars">${itemRows.map(r => `<div class="hb" data-say="${esc(r.i.name + ' ' + sayMoney(r.p))}"><span class="nm">${r.i.icon} ${esc(r.i.name)} <small>${r.rev ? Math.round(r.p / r.rev * 100) : 0}%</small></span><span class="v" style="color:var(--${r.p >= 0 ? 'success' : 'danger'})">${smoney(r.p)}</span><span class="track2"><i class="${r.p >= 0 ? 'good' : 'bad'}" style="width:${Math.abs(r.p) / mxv(itemRows, x => Math.abs(x.p)) * 100}%"></i></span></div>`).join('') || `<div class="muted">${T('अभी कुछ नहीं','Nothing yet')}</div>`}</div>`)}
    ${hcard(T('किस देश में कितना माल गया','Exports by country'), `<div class="hbars">${ctyRows.map(([cc, v]) => `<div class="hb" data-say="${esc(cname(cc) + ' ' + sayMoney(v))}"><span class="nm">${flag(cc)} ${esc(cname(cc))} <small>${Math.round(v / ctyTot * 100)}%</small></span><span class="v">${money(v)}</span><span class="track2"><i style="width:${v / ctyRows[0][1] * 100}%"></i></span></div>`).join('') || `<div class="muted">${T('अभी कुछ नहीं','Nothing yet')}</div>`}</div>`)}
    ${hcard(T('सबसे बड़े ग्राहक','Top buyers'), `<div class="hbars">${custRows.map(r => `<div class="hb" data-say="${esc(r.p.name + ' ' + sayMoney(r.v))}"><span class="nm">${flag(r.p.country)} ${esc(r.p.name)}</span><span class="v">${money(r.v)}</span><span class="track2"><i style="width:${r.v / custRows[0].v * 100}%;background:var(--teal)"></i></span></div>`).join('') || `<div class="muted">${T('अभी कुछ नहीं','Nothing yet')}</div>`}</div>`)}
    <section class="card"><div class="card-hd"><h2>${T('सरकार से मिलना है (अनुमान)','Export incentives (estimate)')}</h2></div><div class="card-bd" data-say="${esc(T('सरकार से मिलने का अनुमान','Estimated incentives') + ' ' + sayMoney(incent))}"><div class="row"><span class="stat-ic green">${I('bank', 22)}</span><div style="flex:1;min-width:0"><div class="stat-num num">${money(incent)}</div><div class="muted" style="font-size:13px">RoDTEP + Drawback · ${T('इस साल का एक्सपोर्ट','Exports this FY')} ${money(exportFOB)} × ${(+S.settings.rodtep || 0) + (+S.settings.drawback || 0)}%</div></div></div><p class="muted" style="font-size:12.5px;margin:12px 0 0">${T('सही दर सेटिंग में डालें।','Set your actual rates in Settings.')}</p></div></section>
  </div>
  ${dataTable({id: 'shipP', title: T('हर शिपमेंट का मुनाफ़ा','Profit per shipment'), rows: shipP, cols: [
    {k: 'r', label: T('शिपमेंट','Shipment'), sort: r => r.s.ref, cell: r => `<div class="cellp"><span class="sq gray sm" style="font-size:18px">${flag(r.s.country)}</span><div><div class="cell-main">${esc(r.s.ref)}</div><div class="cell-sub">${esc(party(r.s.party)?.name || '')}</div></div></div>`},
    {k: 'v', label: T('बिक्री','Sale'), cls: 'r', sort: r => r.rev, cell: r => `<span class="num">${money(r.rev)}</span>`},
    {k: 'c', label: T('माल की लागत','Cost of goods'), cls: 'r', sort: r => r.cost, cell: r => `<span class="num">${money(r.cost)}</span>`},
    {k: 'e', label: T('खर्चे','Expenses'), cls: 'r', sort: r => r.e, cell: r => `<span class="num">${money(r.e)}</span>`},
    {k: 'p', label: T('मुनाफ़ा','Profit'), cls: 'r', sort: r => r.p, cell: r => `<span class="${r.p >= 0 ? 'money-pos' : 'money-neg'} num">${smoney(r.p)}</span>`},
    {k: 'act', label: T('काम','Action'), cls: 'r', act: 1, cell: r => openBtn('open-ship', r.s.id)}], rowAttrs: r => `data-act="open-ship" data-id="${r.s.id}" class="${r.p < 0 ? 'flag-bad' : ''}"`})}
  <section class="card"><div class="card-hd"><h2>${T('Excel / CSV फ़ाइल निकालो','Export to Excel / CSV')}</h2></div><div class="card-bd row">
    ${[['stock', 'box', T('माल','Stock')], ['parties', 'users', T('पार्टी + बकाया','Parties + balances')], ['txns', 'file', T('सब एंट्री','All entries')], ['ships', 'ship', T('शिपमेंट','Shipments')], ['expenses', 'receipt', T('खर्चे','Expenses')]].map(([k, ic, l]) => `<button class="btn" data-act="csv" data-k="${k}">${I(ic, 16)} ${l}</button>`).join('')}</div></section>`;
};

V.activity = () => {
  const cols = [
    {k: 't', label: T('समय','Time'), sort: l => l.at, cell: l => `<span class="dt">${I('clock', 14)}${fmtDT(l.at)}</span>`},
    {k: 'u', label: T('किसने','User'), sort: l => whoName(l), cell: l => `<div class="cellp"><span class="avatar" style="width:30px;height:30px;font-size:11px">${esc(initials(whoName(l)))}</span><div><div class="cell-main">${esc(whoName(l))}</div><div class="cell-sub">${roleNm(l.role || 'owner')}</div></div></div>`},
    {k: 'x', label: T('क्या किया','Action'), cell: l => esc(l.text)}];
  return `${pageHd([T('अकाउंट','Account'), T('गतिविधि','Activity')], T('किसने क्या किया','Activity log'), mode === 'cloud' ? T('टीम के हर काम का रिकॉर्ड — नाम और समय के साथ','Every action by your team, with name and time') : T('इस कंप्यूटर पर किए गए काम','Actions recorded on this device'))}
  ${dataTable({id: 'log', title: T('गतिविधि','Activity'), cols, rows: S.log.slice().sort((a, b) => b.at.localeCompare(a.at)), search: l => `${l.text} ${whoName(l)}`, rowAttrs: l => `data-say="${esc(whoName(l) + '. ' + l.text)}"`})}`;
};
const initials = n => String(n || '?').split(/\s+/).filter(Boolean).slice(0, 2).map(w => w[0]).join('').toUpperCase() || '?';
