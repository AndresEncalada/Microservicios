from fastapi import FastAPI
from pydantic import BaseModel
from typing import List

app = FastAPI()

# Definimos el modelo de datos que esperamos recibir (el JSON)
class SolicitudEnvio(BaseModel):
    productos: List[int]  # Lista de IDs de productos
    destino: str          # "Quito", "Guayaquil", etc.

@app.post("/calcular-envio")
def calcular_envio(solicitud: SolicitudEnvio):
    # Algoritmo simple: Costo basado en cantidad de productos
    cantidad = len(solicitud.productos)
    
    # Tarifas simuladas
    if solicitud.destino.lower() == "cuenca":
        costo_unitario = 1.50
    else:
        costo_unitario = 3.00 # Más caro fuera de la ciudad
        
    total_envio = cantidad * costo_unitario

    print(f"Calculando envío para {cantidad} productos a {solicitud.destino}. Total: {total_envio}")

    return {
        "costo_envio": total_envio,
        "moneda": "USD",
        "mensaje": "Cálculo realizado exitosamente"
    }

# Para correrlo localmente si ejecutas el archivo directo
if __name__ == "__main__":
    import uvicorn
    # Puerto 8082 requerido por la guía [cite: 38]
    uvicorn.run(app, host="0.0.0.0", port=8082)