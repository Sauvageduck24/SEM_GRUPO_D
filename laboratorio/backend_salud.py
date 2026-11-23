"""Ejecuta el backend docente con datos ficticios de AppSalud en localhost."""
import importlib.util
from pathlib import Path
import ssl
import sys


def arrancar(ruta):
    spec = importlib.util.spec_from_file_location("backend_docente", Path(ruta))
    docente = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(docente)
    docente.ELEMENTOS = {
        "ana": [
            {
                "id": 1,
                "titulo": "Antecedente de prueba",
                "contenido": "Registro ficticio para el laboratorio, sin datos clínicos reales.",
                "fecha": "2026-11-01",
            },
            {
                "id": 2,
                "titulo": "Consulta de prueba",
                "contenido": "Cita ficticia introducida manualmente.",
                "fecha": "2026-11-02",
            },
        ],
        "luis": [
            {
                "id": 3,
                "titulo": "Informe de prueba",
                "contenido": "Documento ficticio de otra cuenta.",
                "fecha": "2026-11-03",
            },
        ],
        "admin": [],
    }

    if bool(docente.CERT) != bool(docente.CLAVE):
        raise SystemExit("Configura CERT y CLAVE juntos para activar HTTPS.")
    with docente.ThreadingHTTPServer(("127.0.0.1", docente.PUERTO), docente.Manejador) as servidor:
        esquema = "http"
        if docente.CERT:
            contexto = ssl.SSLContext(ssl.PROTOCOL_TLS_SERVER)
            contexto.minimum_version = ssl.TLSVersion.TLSv1_2
            contexto.load_cert_chain(docente.CERT, docente.CLAVE)
            servidor.socket = contexto.wrap_socket(servidor.socket, server_side=True)
            esquema = "https"
        print(f"Backend de salud ficticio en {esquema}://127.0.0.1:{servidor.server_port}", flush=True)
        servidor.serve_forever()


if __name__ == "__main__":
    if len(sys.argv) != 2:
        raise SystemExit("Uso: python3 backend_salud.py /ruta/al/backend/servidor.py")
    arrancar(sys.argv[1])
