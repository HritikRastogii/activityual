import {LitElement, html, css} from 'lit';
export class ActivityCard extends LitElement {
  static properties = {name:{type:String}, category:{type:String}, frequency:{type:String}, notes:{type:String}};
  declare name: string;
  declare category: string;
  declare frequency: string;
  declare notes: string;
  constructor() {
    super();
    this.name = 'Activity';
    this.category = 'General';
    this.frequency = 'Daily';
    this.notes = '';
  }
  static styles = css`
    :host{display:block;background:white;border:1px solid #e5e7eb;border-radius:14px;padding:18px;color:#172033}
    h3{margin:0 0 8px;font-size:17px}p{margin:5px 0;color:#667085;font-size:13px}
    .tag{display:inline-block;background:#eef2ff;color:#4338ca;padding:4px 9px;border-radius:20px;font-size:11px}
  `;
  render(){return html`<h3>${this.name}</h3><span class="tag">${this.category}</span><p>Frequency: ${this.frequency}</p>${this.notes?html`<p>${this.notes}</p>`:''}`;}
}
if(!customElements.get('activity-card')) customElements.define('activity-card',ActivityCard);