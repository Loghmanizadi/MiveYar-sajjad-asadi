(()=> {
  const faDigits=s=>String(s??'').replace(/[۰-۹]/g,d=>String('۰۱۲۳۴۵۶۷۸۹'.indexOf(d))).replace(/[٠-٩]/g,d=>String('٠١٢٣٤٥٦٧٨٩'.indexOf(d)));
  const cleanDate=s=>faDigits(s).replace(/-/g,'/').trim();
  const catalog=[
    'سیب‌زمینی','پیاز زرد','پیاز قرمز','پیاز سفید','سیر','موسیر','گوجه‌فرنگی','گوجه گیلاسی','خیار سبز','خیار گلخانه‌ای','خیار بوته‌ای','بادمجان','بادمجان دلمه‌ای','کدو سبز','کدو حلوایی','کدو تنبل','فلفل دلمه‌ای سبز','فلفل دلمه‌ای قرمز','فلفل دلمه‌ای زرد','فلفل تند','فلفل قلمی','فلفل شیرین','هویج','شلغم','ترب سفید','تربچه','لبو','چغندر','کلم سفید','کلم قرمز','کلم بروکلی','گل‌کلم','کلم قمری','کلم برگ','کلم بروکسل','کاهو رسمی','کاهو پیچ','کاهو فرانسوی','کرفس','کنگر','رازیانه','ذرت','ذرت شیرین','لوبیا سبز','نخود سبز','باقلا','نخود خشک','عدس','ماش','لوبیا چیتی','لوبیا قرمز','لوبیا سفید','قارچ','زنجبیل','سیب‌زمینی شیرین','اسفناج','برگ چغندر','برگ مو','بامیه','مارچوبه','تره‌فرنگی','پیازچه','تره','جعفری','گشنیز','شوید','ریحان','نعناع','مرزه','ترخون','رزماری','آویشن تازه','شاهی','شنبلیله','سیر سبز','سبزی خوردن','سبزی قورمه','سبزی آش','سبزی کوکو','سبزی پلویی','سبزی سوپ','سبزی خشک','لیموترش','لیموشیرین','پرتقال','پرتقال خونی','نارنگی','یافا','کلمنتین','گریپ‌فروت','دارابی','بالنگ','نارنج','کامکوات','موسمی','پوملو','سیب زرد','سیب قرمز','سیب سبز','گلابی','به','ازگیل','انار','انگور سفید','انگور قرمز','انگور سیاه','کشمش تازه','موز','موز سبز','کیوی','توت‌فرنگی','توت سفید','توت سیاه','شاه‌توت','تمشک','بلوبری','زغال‌اخته','آلبالو','گیلاس','هلو','شلیل','شفتالو','زردآلو','قیسی تازه','آلو زرد','آلو قرمز','آلو سیاه','گوجه سبز','خرمالو','انجیر','خرما','رطب','کاکی','طالبی','ملون','خربزه','هندوانه','گرمک','آناناس','انبه','پاپایا','پشن‌فروت','نارگیل','آووکادو','لیچی','دراگون‌فروت','فیسالیس','تمر هندی','عناب','سنجد','توت خشک','زرشک تازه','زرشک خشک','گردو تازه','بادام تازه','فندق تازه','پسته تازه','بلوط خوراکی','قارچ صدفی','قارچ دکمه‌ای','کرفس کوهی','کنگر فرنگی','جوانه ماش','جوانه گندم','جوانه شبدر','کاهو چینی','پاک‌چوی','کلم چینی','شلغم برگ','سایر محصولات'
  ];
  if(!Array.isArray(db.advances))db.advances=[];
  if(!Array.isArray(db.dailyClosings))db.dailyClosings=[];
  if(Array.isArray(db.products)){
    const normalizeName=s=>String(s||'').normalize('NFC').trim().replace(/\s+/g,' ');
    const existing=new Set(db.products.map(p=>normalizeName(p.name)));
    let changed=false;
    catalog.forEach(name=>{
      const clean=normalizeName(name);
      if(!existing.has(clean)){
        const base='pcat_'+clean.replace(/[^\p{L}\p{N}]/gu,'').slice(0,18);
        let id=base, suffix=2;
        while(db.products.some(p=>String(p.id)===id)) id=base+'_'+(suffix++);
        db.products.push({id,name:clean}); existing.add(clean); changed=true;
      }
    });
    if(changed){try{localStorage.setItem(KEY,JSON.stringify(db))}catch(e){}}
  }
  const peopleView=document.getElementById('view-people');
  if(peopleView&&!document.getElementById('advance-modal')){
    const modal=document.createElement('div');modal.id='advance-modal';modal.className='overlay';
    modal.innerHTML='<div class="modal"><div class="modalhead"><h3>ثبت مساعده پرسنل</h3><button type="button" class="x" id="advance-close">✕</button></div><form id="advance-form"><div class="formgrid"><div class="field"><label>تاریخ شمسی</label><input name="date" required></div><div class="field"><label>نام پرسنل</label><select name="personId" required></select></div><div class="field"><label>مبلغ مساعده (تومان)</label><input name="amount" type="number" min="0.01" step="0.01" required></div><div class="field"><label>روش پرداخت</label><select name="method"><option>نقدی</option><option>کارت‌به‌کارت</option><option>کارت‌خوان</option><option>سایر</option></select></div><div class="field full"><label>توضیحات / علت</label><input name="notes" placeholder="اختیاری"></div></div><div class="form-actions"><button class="btn" type="submit">ذخیره مساعده</button><button class="btn secondary" type="button" id="advance-cancel">انصراف</button></div></form></div>';
    document.body.appendChild(modal);
    window.openAdvanceForm=()=>{const f=modal.querySelector('form');f.reset();f.elements.date.value=today();const people=db.people.filter(p=>String(p.name||'').trim());f.elements.personId.innerHTML=people.map(p=>'<option value="'+esc(p.id)+'">'+esc(p.name)+' · '+esc(p.role||'پرسنل')+'</option>').join('');if(!people.length){toast('ابتدا از بخش افراد، پرسنل را ثبت کن');return}modal.classList.add('show')};
    const close=()=>modal.classList.remove('show');modal.querySelector('#advance-close').onclick=modal.querySelector('#advance-cancel').onclick=close;modal.addEventListener('click',e=>{if(e.target===modal)close()});
    modal.querySelector('form').addEventListener('submit',e=>{e.preventDefault();const f=e.currentTarget;const amount=Number(f.elements.amount.value||0);if(!Number.isFinite(amount)||amount<=0){toast('مبلغ مساعده باید بیشتر از صفر باشد');return}if(!db.people.some(p=>p.id===f.elements.personId.value)){toast('پرسنل انتخاب‌شده معتبر نیست؛ فهرست را بررسی کن');return}const item={id:uid(),date:cleanDate(f.elements.date.value),personId:f.elements.personId.value,amount,method:f.elements.method.value,notes:f.elements.notes.value.trim(),createdAt:timestamp()};db.advances.unshift(item);persist();renderAdvances();close();toast('مساعده پرسنل ثبت شد')});
  }
  function renderAdvances(){
    const host=document.getElementById('advances-list');if(!host)return;
    host.innerHTML=db.advances.length?db.advances.map(x=>'<article class="item"><div class="itemtop"><div><h4>'+esc(nameOf(db.people,x.personId))+'</h4><div class="muted">'+esc(x.date)+' · '+esc(x.method||'نقدی')+'</div></div><div class="amount">'+money(x.amount)+'</div></div><div class="chips">'+(x.notes?'<span class="chip">'+esc(x.notes)+'</span>':'')+'</div><div class="actions"><button class="btn secondary small" data-advance-report="'+esc(x.id)+'">گزارش</button><button class="btn danger small" data-advance-delete="'+esc(x.id)+'">حذف</button></div></article>').join(''):'<div class="empty">هنوز مساعده‌ای ثبت نشده است.</div>';
    host.querySelectorAll('[data-advance-report]').forEach(b=>b.onclick=()=>{const x=db.advances.find(a=>a.id===b.dataset.advanceReport);if(x)alert('مساعده پرسنل\nنام: '+nameOf(db.people,x.personId)+'\nتاریخ: '+x.date+'\nمبلغ: '+money(x.amount)+'\nروش: '+(x.method||'')+'\nتوضیحات: '+(x.notes||''))});
    host.querySelectorAll('[data-advance-delete]').forEach(b=>b.onclick=()=>{if(confirm('این مساعده حذف شود؟')){db.advances=db.advances.filter(a=>a.id!==b.dataset.advanceDelete);persist();renderAdvances()}});
  }
  window.filterProducts=()=>{const q=(document.getElementById('product-search')?.value||'').trim().toLocaleLowerCase('fa');document.querySelectorAll('#products-list .item').forEach(el=>{el.hidden=!!q&&!el.textContent.toLocaleLowerCase('fa').includes(q)})};
  const oldRenderPeople=renderPeople;renderPeople=function(){oldRenderPeople();renderAdvances();window.filterProducts?.()};
  const oldRenderAll=renderAll;renderAll=function(){oldRenderAll();renderAdvances()};
  const period=document.getElementById('r-period'),fromEl=document.getElementById('r-from'),toEl=document.getElementById('r-to'),typeEl=document.getElementById('r-type'),queryEl=document.getElementById('r-query');
  function fmtJ(d){const p=new Intl.DateTimeFormat('en-US-u-ca-persian',{timeZone:'Asia/Tehran',year:'numeric',month:'numeric',day:'numeric'}).formatToParts(d);const y=p.find(x=>x.type==='year').value,m=p.find(x=>x.type==='month').value,day=p.find(x=>x.type==='day').value;return String(y).padStart(4,'0')+'/'+String(m).padStart(2,'0')+'/'+String(day).padStart(2,'0')}
  function jparts(d){const p=new Intl.DateTimeFormat('en-US-u-ca-persian',{timeZone:'Asia/Tehran',year:'numeric',month:'numeric',day:'numeric'}).formatToParts(d);return {y:+p.find(x=>x.type==='year').value,m:+p.find(x=>x.type==='month').value,d:+p.find(x=>x.type==='day').value}}
  function shiftMonths(date,delta){const p=jparts(date);let idx=p.y*12+(p.m-1)+delta,y=Math.floor(idx/12),m=((idx%12)+12)%12+1;let day=Math.min(p.d,m<=6?31:m<=11?30:29);for(let k=day;k>=1;k--){const candidate=String(y).padStart(4,'0')+'/'+String(m).padStart(2,'0')+'/'+String(k).padStart(2,'0');if(k>=1)return candidate}return ''}
  function presetRange(){
    if(!period||period.value==='custom')return;
    const now=new Date(),end=fmtJ(now);let start='';
    if(period.value==='all'){fromEl.value='';toEl.value='';return}
    if(period.value==='week'){const d=new Date(now);d.setDate(d.getDate()-6);start=fmtJ(d)}
    else {const n=period.value==='month'?1:period.value==='3months'?3:period.value==='6months'?6:12;start=shiftMonths(now,-n)}
    fromEl.value=start;toEl.value=end;
  }
  if(period)period.addEventListener('change',presetRange);
  if(fromEl&&toEl){fromEl.addEventListener('input',()=>{if(period)period.value='custom'});toEl.addEventListener('input',()=>{if(period)period.value='custom'})}
  function dateNum(s){const a=cleanDate(s).split('/').map(Number);return a.length===3?a[0]*10000+a[1]*100+a[2]:0}
  function inRange(x,stall){const d=dateNum(x.date);const f=dateNum(fromEl?.value),t=dateNum(toEl?.value);return (!f||d>=f)&&(!t||d<=t)&&(!stall||!x.stallId||x.stallId===stall)}
  function allRows(){
    const rows=[],stall=document.getElementById('r-stall')?.value||'',q=(queryEl?.value||'').trim().toLowerCase(),type=typeEl?.value||'all';
    const add=(kind,x,recordDate,stallId,searchText,values)=>{if(!inRange({date:recordDate||'' ,stallId},stall))return;if(q&&!String(searchText||'').toLowerCase().includes(q))return;rows.push({kind,date:recordDate||'',stall:nameOf(db.stalls,stallId),...values})};
    if(type==='all'||type==='loads')db.loads.forEach(x=>add('خرید بار',x,x.date,x.stallId,nameOf(db.products,x.productId)+' '+x.owner+' '+x.address,{محصول:nameOf(db.products,x.productId),تعداد:x.count,بسته:x.package,وزن_کیلو:x.weight,فی_تومان:x.price,مبلغ_پایه:x.baseAmount,دشت_تومان:x.dasht,کارگری_تومان:x.labor,پرداختی_تومان:x.paid,مانده_تومان:x.balance,صاحب_بار:x.owner,آدرس:x.address,توضیحات:x.notes}));
    if(type==='all'||type==='sales')db.sales.forEach(x=>add('فروش فروشگاه',x,x.date,x.stallId,nameOf(db.products,x.productId)+' '+x.cashier,{محصول:nameOf(db.products,x.productId),وزن_کیلو:x.weight,فی_تومان:x.price,مبلغ_تومان:x.total,روش_پرداخت:x.paymentMethod,صندوق‌دار:x.cashier}));
    if(type==='all'||type==='daily')db.dailyClosings.forEach(x=>add('جمع فروش روزانه',x,x.date,x.stallId,(x.terminals||[]).map(t=>t.name).join(' '),{کارتخوان‌ها:(x.terminals||[]).map(t=>t.name+': '+t.amount).join(' | '),نقدی_تومان:x.cash,جمع_کل_تومان:x.total,توضیحات:x.notes,زمان_ثبت:x.createdAt}));
    if(type==='all'||type==='expenses')db.expenses.forEach(x=>add('هزینه / پرداخت',x,x.date,x.stallId,(x.category||'')+' '+(x.recipient||'')+' '+(x.notes||''),{دسته:x.category,مبلغ_تومان:x.amount,پرداخت_به:x.recipient,روش_پرداخت:x.method,توضیحات:x.notes}));
    if(type==='all'||type==='people')db.advances.forEach(x=>add('مساعده پرسنل',x,x.date,'',nameOf(db.people,x.personId)+' '+x.notes,{نام_پرسنل:nameOf(db.people,x.personId),مبلغ_تومان:x.amount,روش_پرداخت:x.method,توضیحات:x.notes}));
    if(type==='all'||type==='people')db.people.forEach(x=>{const d=x.createdAt||'';if(!q||String(x.name+' '+x.role+' '+x.phone).toLowerCase().includes(q))rows.push({نوع:'پرسنل',تاریخ:d,غرفه:'',نام:x.name,نقش:x.role,تلفن:x.phone,حقوق_هفتگی_تومان:x.wage})});
    if(type==='all'||type==='drivers')db.drivers.forEach(x=>{if(!q||String(x.name+' '+x.phone+' '+x.vehicle).toLowerCase().includes(q))rows.push({نوع:'راننده',تاریخ:x.createdAt||'',غرفه:'',نام:x.name,تلفن:x.phone,خودرو:x.vehicle})});
    if(type==='all'||type==='products')db.products.forEach(x=>{if(!q||String(x.name).toLowerCase().includes(q))rows.push({نوع:'محصول',تاریخ:'',غرفه:'',نام_محصول:x.name,شناسه:x.id})});
    return rows;
  }
  function htmlTable(rows){if(!rows.length)return '<div class="empty">برای این بازه و فیلتر، رکوردی پیدا نشد.</div>';const keys=[...new Set(rows.flatMap(x=>Object.keys(x)))];return '<div class="panel" style="margin-top:12px"><div class="tablewrap"><table><thead><tr>'+keys.map(k=>'<th>'+esc(k)+'</th>').join('')+'</tr></thead><tbody>'+rows.map(r=>'<tr>'+keys.map(k=>'<td>'+esc(r[k]??'')+'</td>').join('')+'</tr>').join('')+'</tbody></table></div><p class="muted">تعداد رکوردها: '+faNum(rows.length)+'</p></div>'}
  window.renderReport=function(){
    const rows=allRows(),host=document.getElementById('report-result');if(!host)return;
    const totals=rows.reduce((a,r)=>{for(const [k,v] of Object.entries(r))if(/تومان$/.test(k)&&Number.isFinite(Number(v)))a[k]=(a[k]||0)+Number(v);return a},{});
    host.innerHTML='<div class="metrics"><div class="metric"><div class="label">تعداد رکوردها</div><strong>'+faNum(rows.length)+'</strong></div><div class="metric"><div class="label">بازه گزارش</div><strong style="font-size:15px">'+esc((fromEl?.value||'ابتدا')+' تا '+(toEl?.value||'امروز / همه سوابق'))+'</strong></div></div>'+htmlTable(rows);
    window.__miveyarCurrentReport=rows;
  };
  window.exportCSV=function(){
    const rows=window.__miveyarCurrentReport||allRows();if(!rows.length){toast('ابتدا گزارش را بساز یا فیلترها را بررسی کن');return}
    const keys=[...new Set(rows.flatMap(x=>Object.keys(x)))],data=[keys,...rows.map(r=>keys.map(k=>r[k]??''))];
    const csv='\ufeff'+data.map(r=>r.map(v=>'"'+String(v??'').replace(/"/g,'""')+'"').join(',')).join('\r\n');
    download(csv,'miveyar-report-'+today().replaceAll('/','-')+'.csv','text/csv;charset=utf-8');
  };
  window.exportReportPDF=function(){
    const rows=window.__miveyarCurrentReport||allRows();if(!rows.length){toast('ابتدا گزارش را بساز یا فیلترها را بررسی کن');return}
    const title='گزارش میوه‌یار · '+(typeEl?.selectedOptions[0]?.textContent||'تمام ثبت‌ها');
    const keys=[...new Set(rows.flatMap(x=>Object.keys(x)))];
    const html='<!doctype html><html lang="fa" dir="rtl"><head><meta charset="utf-8"><title>'+esc(title)+'</title><style>body{font-family:Tahoma,Arial,sans-serif;direction:rtl;padding:18px;color:#172b25}h2{margin:0 0 8px}p{color:#52645d}table{border-collapse:collapse;width:100%;font-size:10px}th,td{border:1px solid #aebbb5;padding:6px;text-align:right;vertical-align:top;word-break:break-word}th{background:#e9f3ee}button{padding:8px 14px;margin-bottom:14px}@media print{button{display:none}body{padding:0}@page{size:A4 landscape;margin:10mm}}</style></head><body><button onclick="window.print()">چاپ / ذخیره PDF</button><h2>'+esc(title)+'</h2><p>تاریخ تهیه: '+esc(timestamp())+' · تعداد رکورد: '+rows.length+'</p><table><thead><tr>'+keys.map(k=>'<th>'+esc(k)+'</th>').join('')+'</tr></thead><tbody>'+rows.map(r=>'<tr>'+keys.map(k=>'<td>'+esc(r[k]??'')+'</td>').join('')+'</tr>').join('')+'</tbody></table><script>window.onload=()=>setTimeout(()=>window.print(),350)<\/script></body></html>';
    const w=window.open('','_blank');if(!w){toast('مرورگر پنجره گزارش را مسدود کرد؛ اجازه بازشدن پنجره را بده');return}w.document.open();w.document.write(html);w.document.close();
  };
  // Date stamps for staff/driver records are attached at creation where possible without changing prior records.
  const oldSaveForm=saveForm;window.saveForm=function(e){const prior=formType;oldSaveForm(e);if(['person','driver'].includes(prior)){const key=prior==='person'?'people':'drivers';const latest=db[key]?.[0];if(latest&&!latest.createdAt){latest.createdAt=timestamp();persist()}}};
  renderPeople();renderAdvances();

  // Visual-only emerald and gold refresh. No financial data or calculations are touched.
  const visualTheme=document.createElement('style');
  visualTheme.id='miveyar-emerald-gold-theme';
  visualTheme.textContent=`
    :root{
      --green:#087f5b;--deep:#064e3b;--gold:#c89b3c;--bg:#f5f8f5;
      --card:#fff;--ink:#18352c;--muted:#71847c;--line:#e2e9e1;
      --shadow:0 10px 28px #064e3b12;
    }
    body{background:radial-gradient(ellipse at 5% 0%,#e2f2e7 0,transparent 34%),linear-gradient(180deg,#f5f8f5 0%,#f8faf8 52%,#eff5f0 100%);color:var(--ink)}
    .app{max-width:1240px}
    .top{padding-top:20px;padding-bottom:17px}
    .logo{background:linear-gradient(145deg,#0b9b6d,#064e3b);border:1px solid #d8b35b;box-shadow:0 5px 0 #064e3b14,0 10px 22px #087f5b28}
    .brand h1{letter-spacing:-.35px}
    .datebox{border-color:#e6d7ad;box-shadow:0 5px 18px #6b51100a}
    .hero{background:linear-gradient(120deg,#043e30 0%,#087f5b 62%,#0b9669 100%)!important;border:1px solid #d4b15b!important;box-shadow:inset 0 1px 0 #ffffff40,0 5px 0 #064e3b18,0 18px 36px #064e3b22!important}
    .hero h2{letter-spacing:-.45px}
    .quick button,.metric,.panel,.item{border-color:#e3eae3;box-shadow:0 4px 0 #064e3b06,0 9px 24px #064e3b08;transition:transform .18s ease,border-color .18s ease,box-shadow .18s ease}
    .quick button{border-top:2px solid #d2ad54;font-size:12px}
    .quick button span{filter:saturate(.9)}
    .metric{position:relative;overflow:hidden;background:linear-gradient(150deg,#fff,#fbfdfb)}
    .metric:before{background:linear-gradient(90deg,#d4ad50,#0b9669,#064e3b)}
    .metric strong{color:#075c46;font-variant-numeric:tabular-nums}
    .sectionhead h3{position:relative;padding-right:11px}
    .sectionhead h3:before{content:"";position:absolute;right:0;top:4px;bottom:4px;width:3px;border-radius:4px;background:linear-gradient(#d5ad4f,#087f5b)}
    .btn{background:linear-gradient(135deg,#0b9669,#087f5b 60%,#064e3b);box-shadow:inset 0 1px 0 #ffffff35,0 3px 0 #064e3b16,0 7px 14px #087f5b14}
    .btn.secondary{background:linear-gradient(135deg,#f5f8ed,#e6f2e8);color:#075c46;border:1px solid #dce8d8;box-shadow:0 3px 0 #064e3b08}
    .btn.danger{border:1px solid #f2d8d3}
    .field label{color:#315748}
    .field input,.field select,.field textarea,.searchrow input{border-color:#dce6dc;background:#fcfefc;border-radius:12px}
    .field input:focus,.field select:focus,.field textarea:focus,.searchrow input:focus{border-color:#b89a4d;box-shadow:0 0 0 3px #c89b3c20,0 5px 12px #064e3b08}
    .chip{background:#edf5ed;color:#315f4a;border:1px solid #e0ebdd}
    .amount{color:#075c46}
    .nav{background:#fffffff2;border-top:1px solid #e2e9e1;box-shadow:0 -7px 25px #064e3b0c}
    .nav button.active{background:linear-gradient(145deg,#e9f4e9,#dcefe3);color:#075c46;box-shadow:inset 0 0 0 1px #d3e6d5}
    .nav button.active span{filter:none}
    .overlay{background:#092c22a8}
    .modal{border:1px solid #d7e5d9;border-bottom:0;box-shadow:0 -12px 40px #042c201c}
    .modalhead{padding-bottom:10px;border-bottom:1px solid #e5ece5}
    .x{background:#eef4ec;color:#315748}
    .notice{background:#fff9e9;border-color:#ead7a4;color:#73591d}
    th{background:#edf5ed;color:#244c3c}
    td,th{border-color:#e2e9e1}
    @media(hover:hover) and (pointer:fine){
      .quick button:hover,.metric:hover,.panel:hover,.item:hover{transform:translateY(-2px);border-color:#d2bd7b;box-shadow:0 5px 0 #064e3b08,0 14px 26px #064e3b12}
      .btn:hover{filter:brightness(1.04)}
    }
    @media(max-width:699px){
      .top{padding-top:14px}
      .hero{border-radius:21px}
      .quick{gap:8px}
      .metric{padding:14px 12px}
      .nav button{min-width:0;flex:1}
      .nav button.active{border:1px solid #d9e8d9}
    }
    @media(prefers-reduced-motion:reduce){.quick button,.metric,.panel,.item,.btn{transition:none!important}}
  `;
  document.head.appendChild(visualTheme);

})();