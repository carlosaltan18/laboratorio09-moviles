# Contrato de pedidos remotos — Laboratorio 13

## URL base

```text
https://6acc4ac7ef37b150407867e3.mockapi.io/api/v1/
```

## Rutas

| Operación | Ruta | Uso en la aplicación |
| --- | --- | --- |
| Crear pedido | `POST /orders` | Enviar un pedido confirmado. |
| Listar pedidos | `GET /orders?sortBy=createdAtEpochMillis&order=desc` | Mostrar los más recientes primero. |
| Consultar detalle | `GET /orders/{id}` | Mostrar un pedido remoto por su ID. |

## Esquema del recurso `orders`

| Campo | Tipo | Propósito |
| --- | --- | --- |
| `id` | String | Lo asigna MockAPI; no se envía al crear. |
| `createdAtEpochMillis` | Number | Fecha numérica para ordenar correctamente. |
| `customerName` | String | Nombre del cliente; se usan únicamente datos ficticios. |
| `billingType` | String | `CF` o `NIT`. |
| `paymentMethod` | String | Método mostrado en el recibo. |
| `totalCents` | Number | Total exacto en centavos. |
| `status` | String | Estado inicial, por ejemplo `received`. |
| `lines` | Array | Copia histórica de los productos comprados. |

Cada objeto de `lines` contiene `productId`, `productName`,
`unitPriceCents`, `quantity` y `subtotalCents`. Se conserva el nombre y el
precio además del ID para reconstruir el pedido aunque cambie el catálogo.

## Respuesta real validada

El 11 de octubre de 2026 se comprobó `GET /orders` con el recurso vacío. Se
creó el pedido ficticio siguiente mediante `POST /orders`, se consultó por
`GET /orders/1` y luego se eliminó; el recurso quedó nuevamente sin registros.

```json
{
  "createdAtEpochMillis": 1791773975385,
  "customerName": "Cliente de prueba Lab 13",
  "billingType": "CF",
  "paymentMethod": "Efectivo contra entrega",
  "totalCents": 14200,
  "status": "received",
  "lines": [
    {
      "productId": "chocolate_01",
      "productName": "Chocolate Oscuro 70%",
      "unitPriceCents": 4500,
      "quantity": 2,
      "subtotalCents": 9000
    },
    {
      "productId": "chocolate_02",
      "productName": "Chocolate con Café",
      "unitPriceCents": 5200,
      "quantity": 1,
      "subtotalCents": 5200
    }
  ],
  "id": "1"
}
```

No se enviarán teléfonos, NIT, datos bancarios, credenciales ni datos
personales reales, ya que la URL de MockAPI es pública.
