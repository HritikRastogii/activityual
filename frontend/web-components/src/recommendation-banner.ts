import {LitElement, html, css} from 'lit';

export class RecommendationBanner extends LitElement {
  static properties = {
    message: {type: String},
    recommendationId: {type: String, attribute: 'recommendation-id'}
  };

  declare message: string;
  declare recommendationId: string;

  constructor() {
    super();
    this.message = 'Build a consistent habit';
    this.recommendationId = '';
  }

  static styles = css`:host{display:block;background:#eef2ff;border:1px solid #c7d2fe;border-radius:14px;padding:16px;color:#312e81}p{margin:0 0 10px}button{border:0;border-radius:8px;background:#4338ca;color:white;padding:8px 12px;cursor:pointer}`;

  render() {
    return html`<p>💡 ${this.message}</p><button @click=${() => this.dispatchEvent(new CustomEvent('recommendation-accept', {detail: {id: this.recommendationId}, bubbles: true, composed: true}))}>Got it</button>`;
  }
}

if (!customElements.get('recommendation-banner')) {
  customElements.define('recommendation-banner', RecommendationBanner);
}
