# Usamos Nginx ligero
FROM nginx:alpine

# Borramos la página por defecto de Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copiamos TU página web
COPY index.html /usr/share/nginx/html/index.html

# Nginx expone el puerto 80 por defecto dentro del contenedor