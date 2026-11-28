const bases:Record<string,string> = {
  auth: import.meta.env.VITE_AUTH_API ?? '/auth-api',
  activities: import.meta.env.VITE_ACTIVITY_API ?? '/activity-api',
  tracking: import.meta.env.VITE_TRACKING_API ?? '/tracking-api',
  analytics: import.meta.env.VITE_ANALYTICS_API ?? '/analytics-api',
  recommendations: import.meta.env.VITE_RECOMMENDATION_API ?? '/recommendation-api'
};
export let authToken = '';
export function setToken(token:string){authToken=token;}
export async function api(service:string,path:string,options:RequestInit={}) {
  const headers = new Headers(options.headers);
  headers.set('Content-Type','application/json');
  if(authToken) headers.set('Authorization',`Bearer ${authToken}`);
  const response=await fetch(`${bases[service]}${path}`,{...options,headers});
  if(response.status===204) return null;
  const text=await response.text();
  let body:any=null; try{body=text?JSON.parse(text):null;}catch{body=text;}
  if(!response.ok) throw new Error(body?.message??body?.error??`${response.status} ${response.statusText}: ${text}`);
  return body;
}
export function asArray(value:any):any[] {
  if(Array.isArray(value)) return value;
  if(Array.isArray(value?.content)) return value.content;
  if(Array.isArray(value?.data)) return value.data;
  if(Array.isArray(value?.items)) return value.items;
  return [];
}