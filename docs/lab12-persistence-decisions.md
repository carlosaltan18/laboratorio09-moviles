# Decisiones de persistencia — Laboratorio 12

| Dato | Ubicación | Justificación |
| --- | --- | --- |
| Favoritos | Room | Es una colección modificada producto por producto y debe sobrevivir al cierre. |
| Líneas del pedido | Room | Es una lista con productos, cantidades, inserciones, actualizaciones y eliminaciones. |
| Orden del catálogo | DataStore | Es una única preferencia clave-valor. |
| Catálogo de 500 productos | No se guarda | Se regenera de forma idéntica con IDs estables y semilla fija. |
| Consulta de búsqueda | ViewModel | Debe sobrevivir a rotación, pero no es necesario recordarla al volver otro día. |
| Posición del scroll | UI | Es un estado visual administrado por `LazyGridState`. |
| Nombre y teléfono | ViewModel | Se conservan durante rotación, pero no deben persistirse innecesariamente. |
| Errores e `isTouched` | ViewModel | Forman parte del estado temporal del formulario. |
| Último recibo | ViewModel | Se necesita para confirmación y rotación; no se implementará historial de compras. |

La preferencia persistida será el orden del catálogo: original, por nombre o por precio.
