#!/usr/bin/env python3
"""Corrige una copia del XML nativo de Astah; conserva los identificadores existentes."""
import sys, copy, hashlib, json
from pathlib import Path
from urllib.parse import quote_plus, unquote_plus
import xml.etree.ElementTree as E
U='org.omg.xmi.namespace.UML'; J='http://objectclub.esm.co.jp/Jude/namespace/'
E.register_namespace('UML',U); E.register_namespace('JUDE',J)
ns={'u':U,'j':J}
def u(s):return '{'+U+'}'+s
def j(s):return '{'+J+'}'+s
source=Path(sys.argv[1]); output=Path(sys.argv[2]); tree=E.parse(source); root=tree.getroot()
ids={e.get('xmi.id'):e for e in root.iter() if e.get('xmi.id')}
classes={unquote_plus(e.get('name','')):e for e in ids.values() if e.tag in (u('Class'),u('Interface')) and e.get('name')}
parents={c:p for p in root.iter() for c in p}
serial=0
changes=[]
def fresh():
 global serial
 serial+=1
 return 'sysac-completado-'+str(serial)
def elem(parent,tag,**attrs):return E.SubElement(parent,tag,attrs)
def reference(parent,tag,child,ident):
 p=elem(parent,tag);return elem(p,child,**{'xmi.idref':ident})
def cloned(e):
 c=copy.deepcopy(e); mapping={x.get('xmi.id'):fresh() for x in c.iter() if x.get('xmi.id')}
 for x in c.iter():
  if x.get('xmi.id'):x.set('xmi.id',mapping[x.get('xmi.id')])
  if x.get('xmi.idref') in mapping:x.set('xmi.idref',mapping[x.get('xmi.idref')])
 return c

def ends(a):return a.findall('u:Association.connection/u:AssociationEnd',ns)
def set_multiplicity(e,lo,hi):
 m=e.find('u:StructuralFeature.multiplicity',ns)
 if m is None:m=elem(e,u('StructuralFeature.multiplicity'))
 v=m.find('u:Multiplicity',ns)
 if v is None:v=elem(m,u('Multiplicity'),**{'xmi.id':fresh()})
 rang=v.find('u:Multiplicity.range',ns)
 if rang is None:rang=elem(v,u('Multiplicity.range'))
 rr=rang.find('u:MultiplicityRange',ns)
 if rr is None:rr=elem(rang,u('MultiplicityRange'),**{'xmi.id':fresh()})
 rr.attrib.update(lower=str(lo),upper=str(hi),lowerValue='',upperValue='')

def association(prefix,spec):
 a=next(x for key,x in ids.items() if key.startswith(prefix+'-'))
 for e,(lo,hi,aggregation) in zip(ends(a),spec):
  set_multiplicity(e,lo,hi);e.set('aggregation',aggregation)
 changes.append('Corregida relación '+prefix)
 return a

# Composición: el rombo se sitúa en el extremo del propietario.
association('1g7',[(1,1,'composite'),(0,-1,'none')])
a=association('1j3',[(1,1,'composite'),(1,1,'none')])
ends(a)[0].set('name','usuario');ends(a)[0].set('navigableType','navigable')
association('1sr',[(1,1,'composite'),(1,1,'none')])
association('1nr',[(1,1,'composite'),(0,-1,'none')])
association('1t5',[(0,-1,'none'),(1,1,'composite')])
association('1ph',[(0,-1,'none'),(1,1,'composite')])
association('1ob',[(0,-1,'none'),(0,1,'none')])
association('1e9',[(0,-1,'none'),(1,1,'none')])
association('1ib',[(0,1,'none'),(1,1,'none')])
association('1hj',[(1,1,'none'),(0,1,'none')])
association('1nd',[(0,-1,'none'),(1,1,'none')])
association('1rf',[(0,-1,'none'),(0,-1,'none')])

# Eliminar la segunda relación Usuario-CuentaAcceso y sus presentaciones/referencias.
duplicate=next(e for k,e in ids.items() if k.startswith('1rz-'))
removed={e.get('xmi.id') for e in duplicate.iter() if e.get('xmi.id')}
parents[duplicate].remove(duplicate)
for p in list(root.iter()):
 if p.tag==j('AssociationPresentation') and p.get('xmi.id'):
  m=p.find('j:UPresentation.semanticModel/*',ns)
  if m is not None and m.get('xmi.idref') in removed:
   removed.update(x.get('xmi.id') for x in p.iter() if x.get('xmi.id')); parents[p].remove(p)
for p in root.iter():
 for c in list(p):
  if c.get('xmi.idref') in removed:p.remove(c)
changes.append('Unificadas las dos asociaciones Usuario–CuentaAcceso en una composición bidireccional 1:1.')

# Actualizar las etiquetas de multiplicidad que Astah almacena como presentación.
label_template=next(e for e in root.iter() if e.tag==j('LabelPresentation') and e.get('xmi.id'))
for p in root.iter():
 if p.tag!=j('AssociationPresentation') or not p.get('xmi.id'):continue
 model=p.find('j:UPresentation.semanticModel/*',ns)
 if model is None or model.get('xmi.idref') not in ids:continue
 a=ids[model.get('xmi.idref')]
 for suffix,e in zip(('A','B'),ends(a)):
  rr=e.find('.//u:MultiplicityRange',ns)
  if rr is None or rr.get('lower') is None:continue
  lo,hi=rr.get('lower'),rr.get('upper'); hi='*' if hi=='-1' else hi
  value=lo if lo==hi else lo+'..'+hi
  label=p.find('j:AssociationPresentation.multiplicity'+suffix+'Presentation/j:LabelPresentation',ns)
  if label is None:
   container=elem(p,j('AssociationPresentation.multiplicity'+suffix+'Presentation'))
   label=cloned(label_template)
   for ch in list(label):
    if ch.tag==j('JomtPresentation.localMovement'):label.remove(ch)
   par=label.find('j:JomtPresentation.compositeParent/*',ns)
   if par is not None:par.set('xmi.idref',p.get('xmi.id'))
   else:reference(label,j('JomtPresentation.compositeParent'),j('UPresentation'),p.get('xmi.id'))
   servers=p.find('j:UPresentation.servers',ns)
   if servers is not None and len(servers)==2:
    node=ids[servers[0 if suffix=='A' else 1].get('xmi.idref')]
    location=node.find('j:JomtPresentation.location',ns)
    own=label.find('j:JomtPresentation.location',ns)
    if own is not None and location is not None:
     own[0].text=str(float(location[0].text)+float(node.get('width'))/2+8)
     own[1].text=str(float(location[1].text)-20)
   label.set('width','42.0');label.set('height','16.344114303588867');label.set('visibility','true')
   container.append(label)
  label.set('label',quote_plus(value))

# Clonar estructuras nativas para conservar las convenciones de Astah.
diagrams=[e for e in root.iter() if e.tag==j('Diagram') and e.get('xmi.id')]
domain=next(d for d in diagrams if d.get('name')=='Class Diagram1')
services=next(d for d in diagrams if d.get('name')=='Class Diagram2')
usage=next(e for e in root.iter() if e.tag==u('Usage') and e.get('xmi.id'))
usage_pres=next(e for e in root.iter() if e.tag==j('UsagePresentation') and e.get('xmi.id'))
service_namespace=classes['ServicioAdministracion'].find('u:ModelElement.namespace/*',ns).get('xmi.idref')
domain_namespace=classes['Usuario'].find('u:ModelElement.namespace/*',ns).get('xmi.idref')
def owned(namespace):return ids[namespace].find('u:Namespace.ownedElement',ns)
def presentations(d):return d.find('j:Diagram.presentations',ns)
def cp(d,name):
 ident=classes[name].get('xmi.id')
 return next(p for p in presentations(d) if p.tag==j('ClassifierPresentation') and p.find('j:UPresentation.semanticModel/*',ns).get('xmi.idref')==ident)
def loc(p):return [float(f.text) for f in p.find('j:JomtPresentation.location',ns)]
def set_loc(p,x,y):
 v=p.find('j:JomtPresentation.location',ns)
 if v is None:v=elem(p,j('JomtPresentation.location'));elem(v,'XMI.field');elem(v,'XMI.field')
 v[0].text=str(x);v[1].text=str(y)
def client(p,tag,ident):
 c=p.find('j:UPresentation.clients',ns)
 if c is None:c=elem(p,j('UPresentation.clients'))
 elem(c,tag,**{'xmi.idref':ident})
def dependency(d,src,dst,label,namespace):
 a=cloned(usage);aid=a.get('xmi.id');a.set('name',quote_plus(label));a.set('iconType','0')
 a.find('u:ModelElement.namespace/*',ns).set('xmi.idref',namespace)
 st=a.find('u:ModelElement.stereotype',ns)
 if st is not None:a.remove(st)
 a.find('u:Dependency.client/*',ns).set('xmi.idref',classes[src].get('xmi.id'))
 a.find('u:Dependency.supplier/*',ns).set('xmi.idref',classes[dst].get('xmi.id'))
 owned(namespace).append(a)
 p=cloned(usage_pres);pid=p.get('xmi.id');p.find('j:UPresentation.semanticModel/*',ns).set('xmi.idref',aid)
 p.find('j:UPresentation.diagram/*',ns).set('xmi.idref',d.get('xmi.id'))
 # La exportación original lista proveedor primero y cliente después.
 servers=p.find('j:UPresentation.servers',ns)
 for c in list(servers):servers.remove(c)
 for name in (dst,src):
  node=cp(d,name);elem(servers,j('ClassifierPresentation'),**{'xmi.idref':node.get('xmi.id')});client(node,j('UsagePresentation'),pid)
 sp,tp=cp(d,src),cp(d,dst);sx,sy=loc(sp);tx,ty=loc(tp)
 x=(sx+float(sp.get('width'))/2+tx+float(tp.get('width'))/2)/2
 y=(sy+float(sp.get('height'))/2+ty+float(tp.get('height'))/2)/2
 labelp=p.find('j:PathPresentation.namePresentation/j:LabelPresentation',ns)
 labelp.set('label',quote_plus(label));labelp.set('width',str(max(40,len(label)*7)));set_loc(labelp,x,y)
 presentations(d).append(p)
 changes.append('Dependencia '+src+' → '+dst+' ('+label+').')

# Fronteras conceptuales de la página 2 de la documentación.
boundaries=[('PantallaAcceso',['iniciarSesion','cerrarSesion','cambiarContrasena','recuperarAcceso'],80,'ServicioAutenticacion'),('PantallaAdministracion',['gestionarUsuarios','gestionarRoles','intervenirCuenta','gestionarSesiones'],650,'ServicioAdministracion'),('PantallaAuditoria',['establecerFiltros','consultarBitacora'],1220,'ServicioAuditoria'),('PantallaPoliticaSeguridad',['mostrarParametros','actualizarParametros'],1790,'ServicioAdministracion')]
void=next((e.get('xmi.id') for e in ids.values() if e.get('name')=='void'),None)
for name,operations,x,target in boundaries:
 c=E.Element(u('Class'),{'xmi.id':fresh(),'name':name,'version':'0','unSolvedFlag':'false','isRoot':'false','isLeaf':'false','isAbstract':'false','isActive':'false'})
 ident=c.get('xmi.id');reference(c,u('ModelElement.namespace'),u('Namespace'),service_namespace)
 elem(c,u('ModelElement.visibility'),**{'xmi.value':'public'})
 st=E.Element(u('Stereotype'),{'xmi.id':fresh(),'name':'boundary','version':'0','unSolvedFlag':'false','isRoot':'false','isLeaf':'false','isAbstract':'false'})
 elem(st,u('Stereotype.baseClass')).text='Class';reference(st,u('Stereotype.extendedElement'),u('ModelElement'),ident)
 reference(c,u('ModelElement.stereotype'),u('Stereotype'),st.get('xmi.id'));elem(c,u('Namespace.ownedElement'))
 features=elem(c,u('Classifier.feature'))
 for op in operations:
  o=elem(features,u('Operation'),**{'xmi.id':fresh(),'name':op,'version':'0','unSolvedFlag':'false','ownerScope':'instance','isQuery':'false','concurrency':'sequential','isRoot':'false','isLeaf':'false','isAbstract':'false'})
  reference(o,u('ModelElement.namespace'),u('Namespace'),ident);elem(o,u('ModelElement.visibility'),**{'xmi.value':'public'})
  reference(o,u('Feature.owner'),u('Classifier'),ident);elem(o,u('Feature.visibility'),**{'xmi.value':'public'})
  if void:
   pars=elem(o,u('BehavioralFeature.parameter'));par=elem(pars,u('Parameter'),**{'xmi.id':fresh(),'name':'','version':'0','unSolvedFlag':'false','kind':'return'})
   reference(par,u('ModelElement.namespace'),u('Namespace'),ident);elem(par,u('ModelElement.visibility'),**{'xmi.value':'public'})
   reference(par,u('Parameter.behavioralFeature'),u('BehavioralFeature'),o.get('xmi.id'));reference(par,u('Parameter.type'),u('Classifier'),void)
 owned(service_namespace).extend([c,st]);classes[name]=c
 p=cloned(cp(services,'GestorExpiracionSesiones'));p.set('label',name);p.set('width','470.0');p.set('height',str(65+len(operations)*22));set_loc(p,x,-520)
 p.find('j:UPresentation.semanticModel/*',ns).set('xmi.idref',ident)
 clients=p.find('j:UPresentation.clients',ns)
 if clients is not None:p.remove(clients)
 presentations(services).append(p)
 dependency(services,name,target,'invoca',service_namespace)
 changes.append('Añadida frontera '+name+' con '+str(len(operations))+' operaciones.')
for src,label in [('Credencial','valida contraseña'),('CuentaAcceso','límite de intentos'),('Sesion','inactividad'),('TokenRecuperacion','vigencia')]:
 dependency(domain,src,'PoliticaSeguridad',label,domain_namespace)
frame=presentations(services).find('j:FramePresentation',ns)
if frame is not None:set_loc(frame,-20,-570);frame.set('height','1450.0');frame.set('width','2490.0')

# Validación estructural: ninguna referencia rota ni ID duplicado.
allids=[e.get('xmi.id') for e in root.iter() if e.get('xmi.id')]
assert len(allids)==len(set(allids)),'IDs duplicados'
missing={e.get('xmi.idref') for e in root.iter() if e.get('xmi.idref') and e.get('xmi.idref') not in set(allids)}
assert not missing,missing
assert len([e for e in root.iter() if e.tag==u('Generalization') and e.get('xmi.id')])==4
assert len(diagrams)==2
output.parent.mkdir(parents=True,exist_ok=True)
E.indent(tree,space='  ')
tree.write(output,encoding='UTF-8',xml_declaration=True)
E.parse(output)
report={'origen':str(source),'sha256_original':hashlib.sha256(source.read_bytes()).hexdigest(),'salida':str(output),'ids':len(allids),'referencias_rotas':len(missing),'diagramas':2,'cambios':changes,'validacion_astah':'Pendiente de abrir en Astah; validación XML y referencias completada.'}
output.with_suffix('.validacion.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n')
print(json.dumps({k:v for k,v in report.items() if k!='cambios'},ensure_ascii=False,indent=2))
