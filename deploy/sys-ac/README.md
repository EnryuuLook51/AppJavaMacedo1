# Despliegue en asus

URL: https://sys-ac.soyfranco.org.pe

- Servicio y código: `/opt/apps/services/sys-ac` (código en `source/`).
- Base persistente: `/opt/apps/data/sys-ac/acceso.mv.db`.
- Configuración privada: `/opt/apps/services/sys-ac/.env` (permiso 600).
- Usuario inicial: `admin`. Consulte ADMIN_PASSWORD en `.env` vía SSH; cámbiela desde la aplicación. Esa variable no cambia contraseñas ya registradas.
- Nginx: `/etc/nginx/sites-available/sys-ac.soyfranco.org.pe`.
- Puerto: `127.0.0.1:8400` → contenedor 8080. HTTPS mediante Cloudflare Tunnel.

## Operar

```sh
cd /opt/apps/services/sys-ac
sudo docker compose ps
sudo docker compose logs --tail=100 app
sudo docker compose restart app
```

## Actualizar

Ejecute `./mvnw -B verify` en el proyecto. Copie el JAR de `target/acceso-1.0.0.jar` a la carpeta del servicio, conservando una copia del JAR anterior. Ejecute `sudo docker compose up -d --build`. Conserve `.env` y la carpeta de datos.

## Respaldo consistente

Detenga temporalmente el servicio antes de copiar H2 y vuelva a iniciarlo:

```sh
cd /opt/apps/services/sys-ac
sudo docker compose stop app
sudo tar -czf /opt/apps/backups/sys-ac-$(date +%Y%m%d-%H%M%S).tar.gz -C /opt/apps/data sys-ac
sudo docker compose start app
```

## Correo

Configure SMTP_HOST, SMTP_PORT, SMTP_USER, SMTP_PASSWORD, SMTP_AUTH, SMTP_TLS y MAIL_FROM en `.env` con su proveedor SMTP. Luego ejecute `sudo docker compose up -d --force-recreate`. Sin SMTP, la recuperación por correo no está disponible. Actualice también el correo del usuario admin.
