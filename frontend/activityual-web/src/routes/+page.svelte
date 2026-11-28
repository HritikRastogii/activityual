<script lang="ts">
  import {onMount} from 'svelte';
  import {api,asArray,setToken} from '$lib/api';
  let token=''; let fullName=''; let email=''; let password=''; let register=false;
  let busy=false; let error=''; let notice='';
  let activities:any[]=[]; let trackings:any[]=[]; let analytics:any=null; let recommendations:any[]=[];
  let name=''; let category='Personal'; let frequency='DAILY'; let notes='';
  let activeTab='dashboard';
  let today=new Date().toISOString().slice(0,10);
  let streak=0;

  onMount(()=>{
    token=localStorage.getItem('activityual_token')??'';
    if(token){setToken(token); loadDashboard();}
  });
  async function authenticate(){
    busy=true; error='';
    try{
      const result=await api('auth',register?'/api/v1/auth/register':'/api/v1/auth/login',{
        method:'POST',
        body:JSON.stringify(register?{fullName,email,password}:{email,password})
      });
      const jwt=result?.token??result?.accessToken??result?.jwt??result?.data?.token;
      if(!jwt) throw new Error('Authentication response did not contain a JWT. Check the Auth API response format.');
      token=jwt; setToken(jwt); email=result?.user?.email??email; localStorage.setItem('activityual_token',jwt);
      await loadDashboard();
    }catch(e:any){error=e.message??'Authentication failed';}
    finally{busy=false;}
  }
  function logout(){localStorage.removeItem('activityual_token');token='';setToken('');activities=[];trackings=[];analytics=null;recommendations=[];}
  async function loadDashboard(){
    busy=true; error='';
    try{
      const results=await Promise.allSettled([
        api('activities','/api/v1/activities'),
        api('tracking','/api/v1/tracking'),
        api('analytics','/api/v1/analytics'),
        api('recommendations','/api/v1/recommendations')
      ]);
      if(results[0].status==='fulfilled') activities=asArray(results[0].value);
      else error='Activities: '+results[0].reason.message;
      if(results[1].status==='fulfilled') trackings=asArray(results[1].value);
      else error=(error?error+' · ':'')+'Tracking: '+results[1].reason.message;
      if(results[2].status==='fulfilled') analytics=results[2].value;
      if(results[3].status==='fulfilled') recommendations=asArray(results[3].value);
      streak=trackings.filter((x:any)=>(x.status??'').toUpperCase()==='COMPLETED').length;
    }catch(e:any){error=e.message??'Could not load dashboard';}
    finally{busy=false;}
  }
  async function createActivity(){
    error='';notice='';
    try{
      await api('activities','/api/v1/activities',{method:'POST',body:JSON.stringify({name,category,frequency,notes,description:notes})});
      name='';notes='';notice='Activity created';await loadDashboard();
    }catch(e:any){error=e.message;}
  }
  async function track(activity:any,status:string){
    error='';notice='';
    try{
      await api('tracking','/api/v1/tracking',{method:'POST',body:JSON.stringify({
        activityId:activity.id??activity.activityId,trackingDate:today,status
      })});
      notice=`${activity.name??'Activity'} marked ${status.toLowerCase()}`;await loadDashboard();
    }catch(e:any){error=e.message;}
  }
  async function acceptRecommendation(id:string){
    try{await api('recommendations',`/api/v1/recommendations/${id}/accept`,{method:'POST'});notice='Recommendation accepted';await loadDashboard();}
    catch(e:any){error=e.message;}
  }
  function chartData(){
    const counts=new Map<string,number>();
    for(const entry of trackings){
      if((entry.status??'').toUpperCase()!=='COMPLETED') continue;
      const id=entry.activityId??'unknown';
      counts.set(id,(counts.get(id)??0)+1);
    }
    return JSON.stringify(activities.slice(0,7).map((activity:any,i:number)=>({
      label:(activity.name??activity.title??`#${i+1}`),
      value:counts.get(activity.id??activity.activityId??'unknown')??0
    })));
  }
</script>

<svelte:head>
  <title>Activityual — Build better habits</title>
  <meta name="description" content="Track habits, build streaks and understand your progress with Activityual." />
</svelte:head>

{#if !token}
  <main class="auth-shell">
    <section class="auth-card">
      <div class="brand-mark">A</div>
      <p class="eyebrow">YOUR PERSONAL ACTIVITY TRACKER</p>
      <h1>{register?'Create your account':'Welcome back'}</h1>
      <p class="muted">Small actions. Consistent progress.</p>
      <form onsubmit={(e)=>{e.preventDefault();authenticate();}}>
        {#if register}
          <label>Full name<input type="text" bind:value={fullName} required maxlength="100" placeholder="Your full name" /></label>
        {/if}
        <label>Email address<input type="email" bind:value={email} required placeholder="you@example.com" /></label>
        <label>Password<input type="password" bind:value={password} required minlength="8" maxlength="72" placeholder="At least 8 characters" /></label>
        <button class="primary full" disabled={busy}>{busy?'Please wait…':register?'Create account':'Sign in'}</button>
      </form>
      {#if error}<p class="error">{error}</p>{/if}
      <p class="switch">{register?'Already have an account?':'New to Activityual?'} <button class="link" onclick={()=>{register=!register;error='';}}> {register?'Sign in':'Create account'}</button></p>
    </section>
  </main>
{:else}
  <div class="app-shell">
    <aside class="sidebar">
      <div class="brand"><span class="brand-mark small">A</span><span>activityual</span></div>
      <p class="side-label">WORKSPACE</p>
      <button class:active={activeTab==='dashboard'} onclick={()=>activeTab='dashboard'}>▦ <span>Dashboard</span></button>
      <button class:active={activeTab==='activities'} onclick={()=>activeTab='activities'}>◷ <span>My activities</span></button>
      <button class:active={activeTab==='analytics'} onclick={()=>activeTab='analytics'}>▥ <span>Analytics</span></button>
      <button class:active={activeTab==='recommendations'} onclick={()=>activeTab='recommendations'}>✧ <span>Recommendations</span></button>
      <div class="sidebar-bottom"><span class="muted small-text">{email}</span><button onclick={logout}>↪ <span>Sign out</span></button></div>
    </aside>
    <main class="main">
      <header class="topbar"><div><p class="eyebrow">ACTIVITYUAL / OVERVIEW</p><h1>{activeTab==='dashboard'?'Your dashboard':activeTab==='activities'?'My activities':activeTab==='analytics'?'Your analytics':'Recommendations'}</h1></div><button class="refresh" onclick={loadDashboard}>↻ Refresh</button></header>
      {#if error}<div class="alert error">{error}</div>{/if}
      {#if notice}<div class="alert success">{notice}</div>{/if}
      {#if busy}<p class="muted">Loading your activity…</p>{/if}

      {#if activeTab==='dashboard' || activeTab==='analytics'}
        <section class="stats">
          <article class="stat"><span>Total activities</span><strong>{activities.length}</strong><small>Habits you're building</small></article>
          <article class="stat"><span>Completed logs</span><strong>{streak}</strong><small>Total completed activity records</small></article>
          <article class="stat"><span>Recommendations</span><strong>{recommendations.length}</strong><small>Suggestions for you</small></article>
        </section>
        <section class="panel">
          <div class="panel-heading"><div><h2>Consistency overview</h2><p class="muted">Completed activity counts</p></div></div>
          <analytics-chart data={chartData()}></analytics-chart>
        </section>
      {/if}

      {#if activeTab==='dashboard' || activeTab==='activities'}
        <section class="panel">
          <div class="panel-heading"><div><h2>{activeTab==='dashboard'?'Your activities':'All activities'}</h2><p class="muted">Track today's progress</p></div></div>
          <form class="create-form" onsubmit={(e)=>{e.preventDefault();createActivity();}}>
            <input aria-label="Activity name" placeholder="New activity name" bind:value={name} required />
            <select aria-label="Category" bind:value={category}><option>Personal</option><option>Health</option><option>Learning</option><option>Work</option><option>Fitness</option></select>
            <select aria-label="Frequency" bind:value={frequency}><option value="DAILY">Daily</option><option value="WEEKLY">Weekly</option><option value="MONTHLY">Monthly</option><option value="CUSTOM">Custom</option></select>
            <input aria-label="Notes" placeholder="Notes (optional)" bind:value={notes} />
            <button class="primary" disabled={busy}>+ Add</button>
          </form>
          {#if activities.length}
            <div class="activity-grid">
              {#each activities as activity,i (activity.id??activity.activityId??i)}
                <div class="activity-item">
                  <activity-card name={activity.name??activity.title??'Activity'} category={activity.category??'General'} frequency={activity.frequency??'Daily'} notes={activity.notes??''}></activity-card>
                  <div class="activity-actions"><button class="complete" onclick={()=>track(activity,'COMPLETED')}>✓ Complete</button><button class="missed" onclick={()=>track(activity,'MISSED')}>Mark missed</button></div>
                </div>
              {/each}
            </div>
          {:else}<div class="empty"><span>✧</span><h3>No activities yet</h3><p>Create your first habit above to start tracking progress.</p></div>{/if}
        </section>
      {/if}

      {#if activeTab==='dashboard' || activeTab==='recommendations'}
        <section class="panel">
          <div class="panel-heading"><div><h2>Recommended for you</h2><p class="muted">Personalised timing and frequency suggestions</p></div></div>
          {#if recommendations.length}
            <div class="recommendation-list">
              {#each recommendations as rec,i (rec.recommendationId??rec.id??i)}
                <recommendation-banner message={rec.suggestedAction??rec.suggestedAction??rec.message??rec.reason??rec.suggestion??'Try a consistent time for this activity.'} recommendation-id={rec.recommendationId??rec.id??''} onrecommendation-accept={()=>acceptRecommendation(rec.recommendationId??rec.id)}></recommendation-banner>
              {/each}
            </div>
          {:else}<p class="muted">No recommendations available yet. Keep tracking activities to receive suggestions.</p>{/if}
        </section>
      {/if}
      <footer>Activityual · Build consistency, one day at a time.</footer>
    </main>
  </div>
{/if}