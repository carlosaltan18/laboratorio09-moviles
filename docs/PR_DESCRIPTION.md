# Laboratorio final: Catálogo compartido con Firestore y Repository

> Reemplacen los campos entre corchetes y marquen solamente las comprobaciones que el grupo
> haya ejecutado sobre el SHA final. Este archivo es una plantilla para pegar en el PR abierto
> desde `feature/lab-final-firebase-repository` hacia `main`.

## Datos de entrega

- Integrantes: [nombres y carnés]
- Rama base: `main`
- Rama de trabajo: `feature/lab-final-firebase-repository`
- SHA final evaluable: `[SHA]`
- Eliminación del cargador temporal: `[SHA del commit que eliminó CatalogSeeder y su botón, si existió]`

## Resumen

- Se migró el catálogo de 500 productos con IDs estables a Cloud Firestore.
- La sesión anónima se inicia al abrir la aplicación.
- Los ViewModels reciben Repositories por constructor desde `AppContainer`; no importan
  Firebase, Room, DataStore ni Retrofit.
- Agregar una unidad ejecuta una transacción en Firestore antes de modificar el carrito local;
  quitar o eliminar una línea devuelve las reservas.
- Los pedidos confirmados siguen viajando a MockAPI y favoritos, carrito y preferencia siguen
  persistidos localmente a través de sus Repository.

## Decisiones de diseño

| Decisión | Solución elegida | Alternativa descartada y por qué | Fuente consultada |
|---|---|---|---|
| Repository y contratos | Contratos con `Flow` para observación y `Result` para acciones; modelos de dominio fuera de la capa de datos. | Acceder a Firebase, DAO o Retrofit desde un ViewModel expondría detalles de implementación. | [Android data layer](https://developer.android.com/topic/architecture/data-layer) |
| Existencias simultáneas | Transacción de Firestore que lee y actualiza `stock` de forma atómica. | Lectura y escritura separadas desde la app deja una condición de carrera. | [Firestore transactions](https://firebase.google.com/docs/firestore/manage-data/transactions) |
| Reglas y límites | Sólo usuarios autenticados pueden leer y actualizar únicamente un `stock` entero no negativo. | Reglas temporales de creación o reglas abiertas permitirían modificar el catálogo. Una reserva abandonada sigue ocupada hasta que se devuelva y un cliente autenticado modificado aún podría fijar un stock no negativo arbitrario. | [Firestore field rules](https://firebase.google.com/docs/firestore/security/rules-fields) |

## Reglas definitivas publicadas

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /products/{productId} {
      allow read: if request.auth != null;
      allow update: if request.auth != null
        && request.resource.data.diff(resource.data)
          .affectedKeys().hasOnly(['stock'])
        && request.resource.data.stock is int
        && request.resource.data.stock >= 0;
      allow create, delete: if false;
    }
  }
}
```

## Comprobaciones

| Comprobación | Esperado | Observado | Evidencia |
|---|---|---|---|
| Imports de datos | Cero imports de Room, DataStore, Retrofit o Firebase en pantallas/ViewModels. | [completar] | [captura] |
| Actualización en tiempo real | El cambio de `stock` en consola se refleja sin reabrir catálogo. | [completar] | [captura/video] |
| Reserva y devolución | 5 → 3 al agregar 2; 3 → 4 al quitar 1; 4 → 5 al eliminar; con 0 aparece “Agotado”. | [completar] | [captura/video] |
| Concurrencia | 20 intentos con stock 5 producen 5 éxitos, 15 rechazos y stock final 0. | [completar] | [captura junto a la consola] |

## Evidencia adjunta

- [ ] Colección `products` y documento con campos e ID estable.
- [ ] Rules Playground: actualización válida autenticada permitida.
- [ ] Rules Playground: `stock = -1` autenticado denegado.
- [ ] Rules Playground: actualización válida sin sesión denegada.
- [ ] Búsqueda de imports.
- [ ] Prueba de concurrencia y consola mostrando stock final 0.
- [ ] Video continuo (máximo 3 minutos): cambio remoto de stock, reserva/devolución, concurrencia y confirmación con Mis pedidos.

## Uso de inteligencia artificial

Se utilizó asistencia de IA para revisar la estructura, proponer implementaciones y redactar
documentación. El grupo revisó el código, configuró Firebase, publicó las reglas y ejecutó las
pruebas/evidencias manuales de esta entrega.
