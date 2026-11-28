import {LitElement, html, css} from 'lit';
export class AnalyticsChart extends LitElement {
  static properties={data:{type:String}};
  declare data: string;
  constructor() {
    super();
    this.data = '[]';
  }
  static styles=css`:host{display:block}.bars{display:flex;align-items:end;gap:12px;height:150px;padding:12px 4px 0}.bar{flex:1;min-width:25px;background:#6366f1;border-radius:7px 7px 0 0;position:relative}.bar span{position:absolute;bottom:-24px;left:0;right:0;text-align:center;font-size:11px;color:#667085}.legend{font-size:12px;color:#667085;margin-top:30px}`;
  render(){
    let rows:any[]=[]; try{rows=JSON.parse(this.data)}catch{}
    const max=Math.max(1,...rows.map(x=>Number(x.value)||0));
    return html`<div class="bars">${rows.map(x=>html`<div class="bar" style="height:${Math.max(3,(Number(x.value)||0)/max*130)}px"><span>${x.label??''}</span></div>`)}</div><div class="legend">Activity completion overview</div>`;
  }
}
if(!customElements.get('analytics-chart')) customElements.define('analytics-chart',AnalyticsChart);