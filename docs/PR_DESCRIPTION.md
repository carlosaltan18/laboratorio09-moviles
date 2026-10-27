# Laboratorio 13: Pedidos en servidor (MockAPI + Retrofit)

## Integrantes
- [Nombre y Carné - Integrante 1]
- [Nombre y Carné - Integrante 2]
- [Nombre y Carné - Integrante 3]

---

## 1. Contrato de la API

**URL base:** `https://6acc4ac7ef37b150407867e3.mockapi.io/api/v1/`

### Rutas
- `POST /orders`: Envía pedido confirmado.
- `GET /orders?sortBy=createdAtEpochMillis&order=desc`: Lista pedidos ordenados desde el servidor.
- `GET /orders/{id}`: Detalle de pedido por ID.

### Esquema del recurso `orders`
| Campo | Tipo | Origen | Propósito |
|---|---|---|---|
| `id` | String | Servidor | ID único asignado por MockAPI. |
| `createdAtEpochMillis` | Number | App | Milisegundos Unix para ordenar numéricamente. |
| `customerName` | String | App | `"Anonymous customer"` (protección de datos). |
| `billingType` | String | App | `"CONSUMER_FINAL"` o `"NIT"`. |
| `paymentMethod` | String | App | `"NOT_STORED"` (no se guardan datos de pago). |
| `totalCents` | Number | App | Total en centavos. |
| `status` | String | App | `"CREATED"`. |
| `lines` | Array | App | Copia histórica de productos (`productId`, `productName`, `unitPriceCents`, `quantity`, `subtotalCents`). Permite reconstruir el pedido aunque cambie el catálogo. |

### JSON real devuelto por la API
```json
{
  "createdAtEpochMillis": 1791809817391,
  "customerName": "Anonymous customer",
  "billingType": "CONSUMER_FINAL",
  "paymentMethod": "NOT_STORED",
  "totalCents": 4800,
  "status": "CREATED",
  "lines": [
    {
      "productId": "chocolate_03",
      "productName": "Chocolate con Cardamomo",
      "unitPriceCents": 4800,
      "quantity": 1,
      "subtotalCents": 4800
    }
  ],
  "id": "2"
}
```

---

## 2. Justificación técnica

1. **¿Por qué el pedido se vacía solo tras la respuesta del servidor?**  
   Si se vaciara antes y la petición fallara (sin red o error de servidor), el usuario perdería los productos seleccionados. Al vaciarlo únicamente con respuesta 2xx exitosa, el pedido queda intacto ante errores y permite reintentar.

2. **Ubicación de datos y riesgos de API pública:**  
   - **Room (Lab 12):** Vive en SQLite local dentro del dispositivo; se borra al desinstalar la app.  
   - **MockAPI (Lab 13):** Vive en la nube; persiste tras reinstalar la app.  
   - **Riesgo:** Al ser pública y sin auth, cualquiera con la URL puede leer o borrar pedidos. Por ello, la app no envía datos personales reales (nombres, teléfonos, NIT ni datos de pago).

---

## 3. Comprobaciones manuales obligatorias

| # | Prueba | Esperado | Observado |
|---|---|---|---|
| 1 | **Envío** | Indicador de carga, bloquea doble toque, muestra ID del servidor y vacía el pedido tras éxito. | Cumple. Responde 201 Created con ID numérico y vacía carrito. |
| 2 | **Sin conexión** | En modo avión falla con mensaje amigable, conserva pedido y reintenta exitosamente al reconectar. | Cumple. Captura `IOException`, conserva formulario y envía al reintentar. |
| 3 | **Red lenta y rotación** | Muestra carga. Rotar pantalla no duplica la petición en Network Inspector. | Cumple. ViewModel en raíz (`MainActivity`) mantiene estado y corrutina activa. |
| 4 | **Reinstalar** | Al desinstalar y reinstalar, «Mis pedidos» sigue mostrando el listado ordenado. | Cumple. Datos persistidos en MockAPI; se listan ordenados por fecha desc. |
| 5 | **Pedido eliminado (404)** | Si se borra un pedido en MockAPI y se toca en la app, informa que ya no existe y permite volver. | Cumple. Captura HTTP 404 y emite `OrderDetailUiState.NotFound`. |
| 6 | **Vacío** | Sin pedidos en MockAPI, muestra estado vacío con botón al catálogo, no error. | Cumple. Servidor devuelve `[]` y emite `OrdersUiState.Empty`. |

---

## 4. Evidencias

- **Captura MockAPI (Vista Data):**  
  *(Adjuntar captura del dashboard de MockAPI con los pedidos)*

- **Video continuo (≤ 3 min):**  
  **Link:** [Pegar enlace a Drive / Loom / YouTube]  
  *(Muestra: envío exitoso, modo avión + reintento, mis pedidos con rotación, detalle, error 404 al borrar pedido, y persistencia tras reinstalar).*

---

## 5. Declaración de IA y fuentes
- **IA:** Se utilizó asistencia de IA para estructuración de corrutinas con Retrofit, jerarquías de estados `sealed` y configuración de pruebas unitarias.
- **Fuentes:** Documentación oficial de Android (Retrofit, StateFlow), documentación de MockAPI y guías de Plataformas Móviles UVG.
