"""Carga el backend docente externo y cambia solo datos ficticios y bind local."""
import importlib.util,sys
from pathlib import Path
if len(sys.argv)!=2:raise SystemExit("Uso: python3 backend_salud.py /ruta/al/backend/servidor.py")
spec=importlib.util.spec_from_file_location("backend_docente",Path(sys.argv[1]))
mod=importlib.util.module_from_spec(spec);spec.loader.exec_module(mod)
mod.ELEMENTOS={"ana":[{"id":1,"titulo":"Antecedente de prueba","contenido":"Registro ficticio para el laboratorio, sin datos clínicos reales.","fecha":"2026-11-01"},{"id":2,"titulo":"Consulta de prueba","contenido":"Cita ficticia introducida manualmente.","fecha":"2026-11-02"}],"luis":[{"id":3,"titulo":"Informe de prueba","contenido":"Documento ficticio de otra cuenta.","fecha":"2026-11-03"}],"admin":[]}
print("Backend de salud ficticio en 127.0.0.1:8080",flush=True)
mod.ThreadingHTTPServer(("127.0.0.1",8080),mod.Manejador).serve_forever()
