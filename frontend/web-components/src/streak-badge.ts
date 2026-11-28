import {LitElement, html, css} from 'lit';
export class StreakBadge extends LitElement {
  static properties={count:{type:Number}};
  count=0;
  static styles=css`:host{display:inline-flex;align-items:center;gap:7px;background:#fff7ed;color:#c2410c;border-radius:24px;padding:9px 14px;font-weight:700;font-size:13px}`;
  render(){return html`🔥 ${this.count} day streak`;}
}
if(!customElements.get('streak-badge')) customElements.define('streak-badge',StreakBadge);