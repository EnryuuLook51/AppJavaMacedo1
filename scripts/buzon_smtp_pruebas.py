#!/usr/bin/env python3
"""Buzón SMTP desechable para probar recuperación en localhost sin servicios externos."""
from __future__ import annotations

from email import policy
from email.parser import BytesParser
from html import escape
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
import socketserver
import threading
import time


MAX_MENSAJES = 100
MAX_CORREO = 1024 * 1024
MENSAJES: list[dict[str, str]] = []
BLOQUEO = threading.Lock()


def recibir(raw: bytes) -> None:
    parsed = BytesParser(policy=policy.default).parsebytes(raw)
    body = parsed.get_body(preferencelist=("plain",))
    text = body.get_content() if body else "(El correo no tiene texto plano.)"
    with BLOQUEO:
        MENSAJES.insert(0, {
            "id": str(time.time_ns()),
            "para": str(parsed.get("To", "")),
            "de": str(parsed.get("From", "")),
            "asunto": str(parsed.get("Subject", "(sin asunto)")),
            "fecha": str(parsed.get("Date", "")),
            "texto": str(text),
        })
        del MENSAJES[MAX_MENSAJES:]


class SMTP(socketserver.StreamRequestHandler):
    def handle(self) -> None:
        self.wfile.write(b"220 Buzon local de pruebas listo\r\n")
        while True:
            line = self.rfile.readline(8192)
            if not line:
                return
            command = line.decode("ascii", "replace").strip().split(None, 1)[0].upper()
            if command in {"EHLO", "HELO"}:
                self.wfile.write(b"250 Buzon local\r\n")
            elif command in {"MAIL", "RCPT", "RSET", "NOOP"}:
                self.wfile.write(b"250 OK\r\n")
            elif command == "DATA":
                self.wfile.write(b"354 Finalice el mensaje con un punto\r\n")
                chunks: list[bytes] = []
                size = 0
                while True:
                    part = self.rfile.readline(8192)
                    if not part or part == b".\r\n":
                        break
                    if part.startswith(b".."):
                        part = part[1:]
                    size += len(part)
                    if size > MAX_CORREO:
                        self.wfile.write(b"552 Mensaje demasiado grande\r\n")
                        return
                    chunks.append(part)
                recibir(b"".join(chunks))
                self.wfile.write(b"250 Mensaje guardado solo en memoria\r\n")
            elif command == "QUIT":
                self.wfile.write(b"221 Hasta luego\r\n")
                return
            else:
                self.wfile.write("502 Comando no admitido por el buzón de pruebas\r\n".encode("utf-8"))


class SMTPServer(socketserver.ThreadingTCPServer):
    allow_reuse_address = True
    daemon_threads = True


PAGE = """<!doctype html><html lang="es"><head><meta charset="utf-8">
<meta name="viewport" content="width=device-width,initial-scale=1">
<title>Buzón de pruebas · Universidad Nacional del Pacífico</title>
<style>
:root{color-scheme:light;--navy:#103547;--teal:#087c7c;--gold:#bd8752;--paper:#f6f5ef;--ink:#193c49;--muted:#667e85;--line:#dce3df}
*{box-sizing:border-box}body{margin:0;background:var(--paper);color:var(--ink);font:15px "Trebuchet MS",sans-serif}
header{background:var(--navy);color:white;padding:30px max(24px,calc((100vw - 1000px)/2));border-bottom:4px solid var(--gold)}
.eyebrow{color:#bdd4cd;font-size:11px;letter-spacing:2px;text-transform:uppercase}h1{font:30px Georgia,serif;margin:9px 0 5px}header p{margin:0;color:#c1d1d0;font-size:13px}
main{max-width:1000px;margin:38px auto;padding:0 20px}.notice{padding:13px 16px;background:#eaf2ed;border-left:3px solid var(--teal);color:#42625e;font-size:13px;margin-bottom:20px}
.list{display:grid;gap:13px}.mail{background:white;border:1px solid var(--line);border-radius:7px;padding:18px 20px;box-shadow:0 3px 12px #12364208}
.top{display:flex;justify-content:space-between;gap:12px}.subject{font-weight:700}.date{color:var(--muted);font-size:12px}.meta{margin:9px 0 15px;color:var(--muted);font-size:13px}
button{border:0;border-radius:4px;padding:9px 13px;background:var(--teal);color:white;font:inherit;cursor:pointer}button:hover{background:#075f62}
pre{white-space:pre-wrap;overflow-wrap:anywhere;background:#f6f8f6;border:1px solid var(--line);padding:15px;border-radius:5px;line-height:1.6;font:13px/1.6 ui-monospace,monospace}
.empty{background:white;border:1px dashed #b8c7c1;border-radius:7px;text-align:center;padding:55px;color:var(--muted)}footer{padding:28px 0;text-align:center;color:var(--muted);font-size:12px}
</style></head><body><header><div class="eyebrow">Universidad Nacional del Pacífico · Entorno de demostración</div>
<h1>Buzón de correo de pruebas</h1><p>Correos de recuperación recibidos en esta computadora</p></header><main>
<div class="notice"><strong>Solo desarrollo:</strong> servidor local sin autenticación. Los mensajes se guardan temporalmente en memoria, no se escriben en disco.</div>
<section class="list">{{mensajes}}</section><footer>Escucha en localhost · Al detener el servidor, el buzón se vacía.</footer></main>
<script>document.querySelectorAll('[data-show]').forEach(b=>b.addEventListener('click',()=>{let p=document.getElementById(b.dataset.show);p.hidden=!p.hidden;b.textContent=p.hidden?'Mostrar contenido para copiar el token':'Ocultar token';}));</script>
</body></html>"""


def render() -> bytes:
    with BLOQUEO:
        snapshot = [item.copy() for item in MENSAJES]
    if snapshot:
        cards = []
        for item in snapshot:
            body_id = "body-" + item["id"]
            cards.append(
                '<article class="mail"><div class="top"><span class="subject">'
                + escape(item["asunto"]) + '</span><span class="date">' + escape(item["fecha"])
                + '</span></div><div class="meta">Para: ' + escape(item["para"])
                + ' · De: ' + escape(item["de"]) + '</div><button data-show="' + body_id
                + '">Mostrar contenido para copiar el token</button><pre id="' + body_id
                + '" hidden>' + escape(item["texto"]) + '</pre></article>'
            )
        messages = "".join(cards)
    else:
        messages = '<div class="empty">Todavía no hay mensajes.<br>Solicita una recuperación en la aplicación para que llegue aquí.</div>'
    return PAGE.replace("{{mensajes}}", messages).encode("utf-8")


class HTTP(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        if self.path not in {"/", "/index.html"}:
            self.send_error(404)
            return
        payload = render()
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(payload)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.end_headers()
        self.wfile.write(payload)

    def log_message(self, format: str, *args: object) -> None:
        pass


def main() -> None:
    smtp = SMTPServer(("127.0.0.1", 1025), SMTP)
    web = ThreadingHTTPServer(("127.0.0.1", 8025), HTTP)
    threading.Thread(target=smtp.serve_forever, daemon=True).start()
    print("Buzón SMTP de pruebas para Universidad Nacional del Pacífico")
    print("SMTP: localhost:1025 · HTTP: http://localhost:8025")
    print("Correos y tokens solo en memoria. Ctrl+C detiene y vacía el buzón.")
    try:
        web.serve_forever()
    except KeyboardInterrupt:
        print("\nBuzón de pruebas detenido.")
    finally:
        web.server_close()
        smtp.shutdown()
        smtp.server_close()


if __name__ == "__main__":
    main()
