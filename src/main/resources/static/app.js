'use strict';
const $=s=>document.querySelector(s);
const esc=v=>String(v??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const date=v=>v?new Date(v).toLocaleString('es-PE',{dateStyle:'medium',timeStyle:'short'}):'Sin fecha de fin';
const badges=items=>items.map(x=>'<span class="badge">'+esc(x)+'</span>').join('');
const estado=s=>'<span class="badge '+(s==='ACTIVA'?'good':'bad')+'">'+esc(s)+'</span>';
const input=(name,label,type='text',value='',extra='')=>'<label>'+label+'<input name="'+name+'" type="'+type+'" value="'+esc(value)+'" '+extra+'></label>';
const title=(label,desc,eyebrow='PORTAL INSTITUCIONAL')=>'<div class="eyebrow">'+eyebrow+'</div><h1>'+label+'</h1><p class="lead">'+desc+'</p>';
const passwordHelp='<small class="help">Entre 8 y 72 caracteres. Incluya mayúscula, minúscula, número y símbolo. No puede reutilizar contraseñas anteriores.</small>';
let csrf,me=null,noticeTimer,routeVersion=0;
function notify(message,error=false){const n=$('#notice');n.textContent=message;n.className=error?'error':'';n.hidden=false;clearTimeout(noticeTimer);noticeTimer=setTimeout(()=>n.hidden=true,6500);}
async function api(path,method='GET',body){
  if(!csrf)csrf=await fetch('/api/csrf').then(r=>r.json());
  const response=await fetch('/api'+path,{method,headers:{'Content-Type':'application/json',[csrf.headerName]:csrf.token},body:body===undefined?undefined:JSON.stringify(body)});
  const data=await response.json().catch(()=>({mensaje:'No se pudo completar la operación.'}));
  if(!response.ok){if(response.status===401 && !['/login','/recuperacion','/restablecer'].includes(path)){me=null;navigation();location.hash='login';}throw new Error(data.mensaje||'Operación no autorizada. Recargue la página e inténtelo nuevamente.');}
  return data;
}
const can=p=>me?.permisos.includes(p);
function navigation(){
  const group=(label,links)=>'<h3>'+label+'</h3>'+links.map(([id,text])=>'<a href="#'+id+'" class="'+(location.hash.slice(1)===id?'active':'')+'">'+text+'</a>').join('');
  $('#nav').innerHTML=me?group('MI ESPACIO',[['inicio','Panel principal'],['sesiones','Mis sesiones'],['contrasena','Cambiar contraseña']])+
    (can('ADMINISTRAR')?group('ADMINISTRACIÓN',[['usuarios','Buscar usuarios'],['registro','Registrar usuario']]):'')+
    (can('AUDITAR')||can('CONFIGURAR')?group('SEGURIDAD',[...(can('AUDITAR')?[['auditoria','Bitácora de auditoría']]:[]),...(can('CONFIGURAR')?[['politica','Parámetros de seguridad']]:[])]):'')+
    group('CUENTA',[['salir','Cerrar sesión']]):group('ACCESO',[['login','Iniciar sesión'],['recuperacion','Solicitar recuperación'],['restablecer','Restablecer con token']]);
  $('#identity').textContent=me?me.usuario.nombres:'Acceso seguro';
}
function form(id,callback){
  const element=document.getElementById(id);
  element.addEventListener('submit',async e=>{
    e.preventDefault();const submit=element.querySelector('[type=submit]');submit.disabled=true;
    try{await callback(Object.fromEntries(new FormData(element)),element);}
    catch(error){notify(error.message,true);}
    finally{submit.disabled=false;}
  });
}
function actions(){
  document.querySelectorAll('[data-action]').forEach(button=>button.addEventListener('click',async()=>{
    button.disabled=true;
    try{await action(button.dataset);}catch(e){notify(e.message,true);}finally{button.disabled=false;}
  }));
}
async function confirmAction(message,callback){
  const dialog=$('#dialog');
  $('#dialog-body').innerHTML='<h2>Confirmar operación</h2><p>'+esc(message)+'</p><div class="actions"><button id="confirm-action">Confirmar</button></div><br>';
  $('#confirm-action').onclick=async()=>{const b=$('#confirm-action');b.disabled=true;try{await callback();dialog.close();}catch(e){notify(e.message,true);b.disabled=false;}};
  dialog.showModal();
}
$('#dialog-close').onclick=()=>$('#dialog').close();
async function action(d){
  if(d.action==='cuenta')return confirmAction('¿Desea '+d.op+' esta cuenta? La intervención quedará registrada.',async()=>{const r=await api('/admin/usuarios/'+d.id+'/'+d.op,'POST',{});notify(r.mensaje);await render();});
  if(d.action==='cerrar')return confirmAction('Se cerrará únicamente la sesión seleccionada.',async()=>{await api((d.admin==='true'?'/admin':'')+'/sesiones/'+d.id+'/cerrar','POST',{});notify('Sesión cerrada.');await render();});
  if(d.action==='retirar')return confirmAction('El rol dejará de otorgar permisos. Se conservará su historial.',async()=>{await api('/admin/usuarios/'+d.id+'/roles/'+d.assignment,'DELETE');notify('Rol retirado.');await render();});
  if(d.action==='vigencia'){
    const u=await api('/admin/usuarios/'+d.id);const a=u.asignaciones.find(x=>x.idAsignacion===d.assignment);
    $('#dialog-body').innerHTML='<h2>Modificar vigencia</h2><form id="vigencia">'+input('inicio','Inicio','datetime-local',localDate(a.inicioVigencia),'required')+input('fin','Fin (opcional)','datetime-local',localDate(a.finVigencia))+'<button type="submit">Guardar vigencia</button></form><br>';
    form('vigencia',async values=>{await api('/admin/usuarios/'+d.id+'/roles/'+d.assignment,'PUT',{inicio:new Date(values.inicio).toISOString(),fin:values.fin?new Date(values.fin).toISOString():null});$('#dialog').close();notify('Vigencia actualizada.');await render();});
    $('#dialog').showModal();
  }
}
function localDate(value){if(!value)return '';const d=new Date(value);return new Date(d.getTime()-d.getTimezoneOffset()*60000).toISOString().slice(0,16);}
function table(headers,rows){return '<div class="table-wrap"><table><thead><tr>'+headers.map(x=>'<th>'+x+'</th>').join('')+'</tr></thead><tbody>'+rows.join('')+'</tbody></table>'+(rows.length?'':'<div class="empty">No se encontraron registros.</div>')+'</div>';}
function sessionsTable(list,admin=false){
  return table(['Equipo / Dirección','Inicio','Última actividad','Estado','Acción'],list.map(s=>'<tr><td>'+esc(s.equipo)+(s.idSesion===me?.sesion.idSesion?' <span class="badge">Actual</span>':'')+'<small>'+esc(s.direccionRed)+'</small></td><td>'+date(s.iniciadaEn)+'</td><td>'+date(s.ultimaActividad)+'</td><td>'+estado(s.estado)+'</td><td>'+(s.estado==='ACTIVA'?'<button class="secondary" data-action="cerrar" data-id="'+s.idSesion+'" data-admin="'+admin+'">Cerrar sesión</button>':'—')+'</td></tr>'));
}
async function render(){
  const version=++routeVersion;
  let route=location.hash.slice(1)||'inicio';
  if(!me && !['login','recuperacion','restablecer'].includes(route))route='login';
  navigation();const main=$('#main');main.innerHTML='<div class="loading">Cargando…</div>';
  $('#breadcrumb').textContent=({'inicio':'Panel principal','login':'Iniciar sesión','usuarios':'Usuarios','registro':'Registrar usuario','sesiones':'Sesiones','contrasena':'Contraseña','auditoria':'Auditoría','politica':'Seguridad','recuperacion':'Recuperación','restablecer':'Restablecer acceso'}[route]||'Ficha de usuario');
  const show=html=>{if(version!==routeVersion)return false;main.innerHTML=html;return true;};
  try{
    if(route==='salir'){await api('/logout','POST',{});me=null;location.hash='login';return;}
    if(route==='login'){
      show('<div class="login-grid"><section class="login-art"><div class="eyebrow">SU UNIVERSIDAD, CONECTADA</div><div class="monogram">U.</div><div><h2>Un solo acceso.<br>Todos sus servicios.</h2><p>Ingrese de forma segura a su espacio universitario, desde cualquier equipo.</p></div></section><section class="card login-card">'+title('Bienvenido de nuevo','Ingrese sus credenciales institucionales.','ACCESO INSTITUCIONAL')+'<form id="login">'+input('identificacion','Identificación institucional','text','','required autocomplete="username" maxlength="40" placeholder="Su identificación"')+input('contrasena','Contraseña','password','','required autocomplete="current-password" maxlength="72" placeholder="Ingrese su contraseña"')+input('equipo','Nombre de este equipo','text','Mi equipo','required maxlength="120"')+'<button type="submit">Iniciar sesión →</button></form><div class="login-links"><a href="#recuperacion">Olvidé mi contraseña</a><a href="#restablecer">Tengo un token</a></div><div class="hint">¿Aún no tiene una cuenta? Solicite su registro al administrador de la institución.</div></section></div>');
      form('login',async d=>{await api('/login','POST',d);me=await api('/me');location.hash='inicio';notify('Bienvenido, '+me.usuario.nombres+'.');});return;
    }
    if(route==='recuperacion'){
      show(title('Recupere su acceso','Enviaremos un token temporal al correo registrado en su cuenta.')+'<section class="card form"><form id="recuperar">'+input('identificacion','Identificación institucional','text','','required maxlength="40"')+'<button type="submit">Solicitar recuperación</button></form><div class="hint">Cada nuevo token invalida el anterior. Si su cuenta está bloqueada, contacte con un administrador para desbloquearla.</div><a href="#restablecer">Ya tengo un token →</a></section>');
      form('recuperar',async d=>notify((await api('/recuperacion','POST',d)).mensaje));return;
    }
    if(route==='restablecer'||route==='contrasena'){
      const reset=route==='restablecer';
      show(title(reset?'Restablecer contraseña':'Cambiar mi contraseña','Proteja su cuenta con una contraseña que no haya utilizado antes.')+'<section class="card form"><form id="clave">'+input(reset?'token':'actual',reset?'Token recibido por correo':'Contraseña actual',reset?'text':'password','','required autocomplete="off" maxlength="'+(reset?'100':'72')+'"')+input('nueva','Nueva contraseña','password','','required minlength="8" maxlength="72" autocomplete="new-password"')+input('confirmacion','Confirmar nueva contraseña','password','','required minlength="8" maxlength="72" autocomplete="new-password"')+passwordHelp+'<button type="submit">Guardar contraseña</button></form></section>');
      form('clave',async(d,f)=>{if(d.nueva!==d.confirmacion)throw new Error('Las contraseñas no coinciden.');notify((await api(reset?'/restablecer':'/contrasena','POST',d)).mensaje);f.reset();});return;
    }
    me=await api('/me');navigation();
    if(route==='inicio'){
      const list=await api('/sesiones');const active=list.filter(s=>s.estado==='ACTIVA').length;
      const services=[['SERVICIOS_ESTUDIANTE','Servicios estudiantiles','Su rol le permite acceder a los servicios estudiantiles institucionales.','E'],['SERVICIOS_DOCENTE','Servicios docentes','Su rol le permite acceder a los servicios docentes institucionales.','D'],['SERVICIOS_ADMINISTRATIVO','Servicios administrativos','Su rol le permite acceder a los servicios administrativos institucionales.','A']];
      show(title('Bienvenido, '+esc(me.usuario.nombres),'Administre su acceso y consulte las opciones disponibles para sus roles vigentes.','MI ESPACIO UNIVERSITARIO')+
        badges(me.usuario.rolesVigentes)+(!me.usuario.rolesVigentes.length?'<div class="hint">Su cuenta aún no tiene roles vigentes. Puede gestionar su contraseña y sesiones. Un administrador puede asignarle los servicios que correspondan.</div>':'')+
        '<div class="stats"><div class="stat"><span>Estado de la cuenta</span><strong>'+esc(me.usuario.estado)+'</strong></div><div class="stat"><span>Roles vigentes</span><strong>'+me.usuario.rolesVigentes.length+'</strong></div><div class="stat"><span>Sesiones activas</span><strong>'+active+'</strong></div></div><div class="grid">'+
        services.filter(s=>can(s[0])).map(s=>'<section class="card"><div class="service-icon">'+s[3]+'</div><h2>'+s[1]+'</h2><p>'+s[2]+'</p><span class="badge good">Permiso vigente</span></section>').join('')+
        (can('ADMINISTRAR')?'<section class="card"><div class="service-icon">↗</div><h2>Administración de usuarios</h2><p>Gestione las cuentas, los roles y el acceso de la comunidad universitaria.</p><a href="#usuarios">Consultar usuarios →</a></section>':'')+
        '<section class="card"><div class="service-icon">◎</div><h2>Seguridad de mi cuenta</h2><p>Revise sus equipos conectados y mantenga actualizada su contraseña.</p><a href="#sesiones">Ver mis sesiones →</a></section></div>');return;
    }
    if(route==='sesiones'){show(title('Mis sesiones','Cada equipo mantiene una sesión independiente. Puede cerrar la que ya no utilice.')+'<section class="card">'+sessionsTable(await api('/sesiones'))+'</section>');actions();return;}
    if(route==='usuarios'){
      show('<div class="section-top">'+title('Usuarios','Busque por identificación o nombre y consulte la ficha de cada usuario.','ADMINISTRACIÓN')+'<a class="badge" href="#registro">+ Registrar usuario</a></div><section class="card"><form id="buscar" class="search"><input name="q" aria-label="Buscar usuarios" placeholder="Identificación o nombre"><button type="submit">Buscar</button></form><div id="resultados"></div></section>');
      const search=async q=>{const users=await api('/admin/usuarios?q='+encodeURIComponent(q));if(!$('#resultados'))return;$('#resultados').innerHTML=table(['Usuario','Tipo de persona','Roles vigentes','Estado',''],users.map(u=>'<tr><td><strong>'+esc(u.nombres)+'</strong><small>'+esc(u.identificacion)+'</small></td><td>'+esc(u.tipoPersona)+'</td><td>'+badges(u.rolesVigentes)+'</td><td>'+estado(u.estado)+'</td><td><a href="#usuario/'+u.idUsuario+'">Ver ficha →</a></td></tr>'));};
      form('buscar',d=>search(d.q));await search('');return;
    }
    if(route==='registro'){
      show(title('Registrar usuario','La cuenta se creará sin roles asignados. Podrá asignarlos desde su ficha.','ADMINISTRACIÓN')+'<section class="card form"><form id="registro">'+input('identificacion','Identificación institucional','text','','required pattern="[a-zA-Z0-9._-]{3,40}" maxlength="40"')+input('nombres','Nombres y apellidos','text','','required maxlength="120"')+input('medioContacto','Correo de contacto','email','','required maxlength="200"')+'<label>Tipo de persona<select name="tipoPersona"><option>ESTUDIANTE</option><option>DOCENTE</option><option>ADMINISTRATIVO</option></select></label><label><input type="checkbox" name="administrativoAutorizado">Trabajador administrativo expresamente autorizado</label>'+input('contrasena','Contraseña inicial','password','','required minlength="8" maxlength="72" autocomplete="new-password"')+passwordHelp+'<button type="submit">Crear cuenta</button></form></section>');
      form('registro',async d=>{d.administrativoAutorizado=d.administrativoAutorizado==='on';const u=await api('/admin/usuarios','POST',d);notify('Usuario registrado sin roles asignados.');location.hash='usuario/'+u.idUsuario;});return;
    }
    if(route.startsWith('usuario/')){
      const id=route.split('/')[1];const [u,roles,sessions]=await Promise.all([api('/admin/usuarios/'+id),api('/admin/roles'),api('/admin/usuarios/'+id+'/sesiones')]);
      const roleRows=u.asignaciones.map(a=>'<tr><td>'+esc(a.rol.nombre)+'</td><td>'+date(a.inicioVigencia)+'</td><td>'+date(a.finVigencia)+'</td><td>'+(a.retiradaEn?'Retirado':new Date(a.inicioVigencia)>new Date()?'Programado':a.finVigencia&&new Date(a.finVigencia)<=new Date()?'Vencido':'Vigente')+'</td><td>'+(!a.retiradaEn?'<button class="secondary" data-action="vigencia" data-id="'+id+'" data-assignment="'+a.idAsignacion+'">Editar</button> <button class="danger" data-action="retirar" data-id="'+id+'" data-assignment="'+a.idAsignacion+'">Retirar</button>':'—')+'</td></tr>');
      show(title(esc(u.nombres),'Identificación: '+esc(u.identificacion),'FICHA DE USUARIO')+'<div class="grid"><section class="card"><h2>Datos básicos</h2><form id="editar">'+input('nombres','Nombres y apellidos','text',u.nombres,'required maxlength="120"')+input('medioContacto','Correo de contacto','email',u.medioContacto,'required maxlength="200"')+'<button type="submit">Guardar datos</button></form></section><section class="card"><h2>Cuenta y acceso</h2><div class="detail-line"><span>Estado</span>'+estado(u.estado)+'</div><div class="detail-line"><span>Tipo de persona</span>'+esc(u.tipoPersona)+'</div><div class="detail-line"><span>Autorización administrativa</span>'+(u.administrativoAutorizado?'Sí':'No')+'</div><div class="detail-line"><span>Fallos consecutivos</span>'+u.fallosConsecutivos+'</div><div class="actions">'+(u.estado==='BLOQUEADA'?'<button class="secondary" data-action="cuenta" data-op="desbloquear" data-id="'+id+'">Desbloquear</button>':'')+(u.estado!=='DESHABILITADA'?'<button class="secondary" data-action="cuenta" data-op="recuperar" data-id="'+id+'">Enviar recuperación</button><button class="danger" data-action="cuenta" data-op="deshabilitar" data-id="'+id+'">Deshabilitar</button>':'')+'</div><div class="hint">Cada intervención queda registrada en la bitácora de auditoría.</div></section></div><br><section class="card"><h2>Roles y vigencia</h2>'+table(['Rol','Desde','Hasta','Estado','Acciones'],roleRows)+'<br><form id="asignar"><div class="form-row"><label>Nuevo rol<select name="rol">'+roles.map(r=>'<option value="'+r.idRol+'">'+esc(r.nombre)+'</option>').join('')+'</select></label>'+input('inicio','Inicio','datetime-local',localDate(new Date()),'required')+'</div>'+input('fin','Fin (opcional)','datetime-local')+'<button type="submit">Asignar rol</button></form></section><section class="card"><h2>Sesiones del usuario</h2>'+sessionsTable(sessions,true)+'</section>');
      form('editar',async d=>{notify((await api('/admin/usuarios/'+id,'PUT',d)).mensaje);await render();});
      form('asignar',async d=>{await api('/admin/usuarios/'+id+'/roles','POST',{rol:d.rol,inicio:new Date(d.inicio).toISOString(),fin:d.fin?new Date(d.fin).toISOString():null});notify('Rol asignado.');await render();});actions();return;
    }
    if(route==='politica'){
      const p=await api('/admin/politica');
      show(title('Parámetros de seguridad','Los cambios se aplican a las operaciones siguientes y quedan registrados en auditoría.','ÁREA DE SEGURIDAD')+'<section class="card form"><form id="politica">'+input('maxIntentos','Máximo de intentos fallidos','number',p.maxIntentos,'required min="1" max="20"')+input('inactividadMaxima','Inactividad máxima (minutos)','number',p.inactividadMaxima,'required min="1" max="1440"')+input('vigenciaToken','Vigencia del token (minutos)','number',p.vigenciaToken,'required min="1" max="1440"')+input('cantidadHistoricas','Contraseñas históricas a validar','number',p.cantidadHistoricas,'required min="1" max="24"')+'<div class="hint">'+esc(p.reglasContrasena)+'</div><button type="submit">Guardar parámetros</button></form></section>');
      form('politica',async d=>{Object.keys(d).forEach(k=>d[k]=Number(d[k]));notify((await api('/admin/politica','PUT',d)).mensaje);});return;
    }
    if(route==='auditoria'){
      show(title('Bitácora de auditoría','Historial de intentos de acceso, intervenciones administrativas y cierres de sesión.','ÁREA DE SEGURIDAD')+'<section class="card"><form id="filtros"><div class="filter">'+input('usuario','Identificación del usuario')+'<label>Resultado<select name="resultado"><option value="">Todos los eventos</option><option>EXITOSO</option><option>RECHAZADO</option></select></label>'+input('contexto','Equipo o contexto')+input('desde','Desde','datetime-local')+input('hasta','Hasta','datetime-local')+'</div><div class="actions"><button type="submit">Aplicar filtros</button><button type="reset" class="secondary">Limpiar campos</button></div></form></section><section class="card" id="eventos"></section>');
      const query=async values=>{const params=new URLSearchParams();Object.entries(values).filter(([,v])=>v).forEach(([k,v])=>params.set(k,k==='desde'||k==='hasta'?new Date(v).toISOString():v));const events=await api('/auditoria?'+params);if(!$('#eventos'))return;$('#eventos').innerHTML=table(['Fecha y hora','Usuario objetivo','Evento / Resultado','Contexto','Ejecutor / Detalle'],events.map(e=>'<tr><td>'+date(e.fechaHora)+'</td><td>'+esc(e.usuarioObjetivo||'Sistema')+'</td><td>'+esc(e.tipo)+'<small>'+esc(e.resultado||e.accion||e.motivo)+'</small></td><td>'+esc(e.contexto)+'</td><td>'+esc(e.ejecutor||'')+'<small>'+esc(e.motivoRechazo||e.detalle||'')+'</small></td></tr>'));};
      form('filtros',query);await query({});return;
    }
    show(title('Página no encontrada','Seleccione una opción del menú.'));
  }catch(e){if(version===routeVersion){show(title('No se pudo cargar la página',esc(e.message))+'<a href="#inicio">Volver al inicio →</a>');notify(e.message,true);}}
}
window.addEventListener('hashchange',render);
(async()=>{try{me=await api('/me');}catch{}await render();})();
let lastActivity=Date.now();
['pointerdown','keydown'].forEach(event=>document.addEventListener(event,()=>{if(me && Date.now()-lastActivity>60000){lastActivity=Date.now();api('/me').catch(e=>notify(e.message,true));}}));
setInterval(async()=>{if(me){try{me=await api('/estado');navigation();}catch(e){notify(e.message,true);}}},30000);

