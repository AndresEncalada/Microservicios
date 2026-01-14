# Usamos una imagen ligera de Python
FROM python:3.9-slim

# Directorio de trabajo dentro del contenedor
WORKDIR /app

# Copiamos los requisitos e instalamos
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copiamos el código
COPY . .

# Exponemos el puerto 8082 [cite: 38]
EXPOSE 8082

# Comando para iniciar la app
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8082"]