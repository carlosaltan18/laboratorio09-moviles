# Laboratorio final: Firebase y Repository

## Arquitectura implementada

`StoreApplication` crea una única instancia de `AppContainer`. El contenedor arma las
implementaciones y las entrega a los `ViewModel` mediante sus `Factory`. Las pantallas
solamente observan estados y envían eventos; no conocen Firebase, Room, DataStore,
Retrofit ni sus modelos internos.

| Repository | Contrato que consume la presentación | Implementación y responsabilidad |
|---|---|---|
| `ProductRepository` | `observeProducts(): Flow<List<Chocolate>>`, `reserveUnit()` y `releaseUnit()` devuelven `Result<Unit>` | `FirestoreProductRepository` observa `products` en tiempo real y usa transacciones para modificar `stock`. |
| `AuthenticationRepository` | `ensureSignedIn(): Result<Unit>` | `FirebaseAuthRepository` crea o reutiliza una sesión anónima. |
| `FavoriteRepository`, `CartRepository` y `CatalogPreferencesRepository` | Flujos de modelos de dominio y operaciones suspendidas con `Result` | Room conserva favoritos y líneas reservadas; DataStore conserva el orden del catálogo. |
| `RemoteOrderRepository` | Pedidos de dominio y operaciones suspendidas con `Result` | `MockApiOrderRepository` adapta los DTO de Retrofit para confirmar, listar y consultar pedidos. |
| `ChocolatierRepository` | `observeChocolatiers(): Flow<List<Chocolatier>>` | `InMemoryChocolatierRepository` conserva los dos perfiles asociados al catálogo. |

El catálogo no se genera dentro de la aplicación: `FirestoreProductRepository` recibe la
colección `products` con un `addSnapshotListener` y la expone como `Flow`. Cada cambio de
existencias publicado en Firestore actualiza la lista sin reabrir la pantalla. La búsqueda
y el orden se aplican sobre esa lista ya descargada.

## Decisiones de diseño

| Decisión | Solución elegida | Alternativa descartada y motivo | Fuente consultada |
|---|---|---|---|
| Separar el acceso a datos | Contratos de Repository con modelos de dominio, `AppContainer` en `Application` e inyección manual por `ViewModelProvider.Factory`. | Permitir que un ViewModel cree DAO, Retrofit o Firebase; acoplaría la presentación al origen de los datos y dificultaría las pruebas. | [Android: data layer](https://developer.android.com/topic/architecture/data-layer), [manual DI](https://developer.android.com/training/dependency-injection/manual) |
| Reservar existencias concurrentemente | Transacción de Firestore: lee el documento y reduce `stock` sólo si sigue siendo mayor que cero. | Leer el stock en la app y después llamar `update`; dos clientes podrían leer el mismo valor antes de escribir y vender más unidades que las disponibles. | [Firestore transactions](https://firebase.google.com/docs/firestore/manage-data/transactions) |
| Seguridad del catálogo | Reglas por campo: usuario autenticado, sólo cambia `stock`, que debe ser entero y no negativo. | Reglas abiertas o con fecha de vencimiento; permitirían crear, borrar o alterar precio, nombre y demás campos desde un cliente modificado. | [Firestore security rules](https://firebase.google.com/docs/firestore/security/rules-fields) |

Una transacción es segura frente a intentos simultáneos porque Firestore vuelve a ejecutar la
función cuando un documento leído cambió antes de confirmar. Por eso cada transacción vuelve a
evaluar el stock guardado y únicamente las que encuentran una unidad disponible terminan con
éxito.

## Reglas definitivas

El archivo fuente es [`firestore.rules`](firestore.rules). Debe publicarse literalmente en
**Firestore Database → Reglas**:

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

Estas reglas no impiden que una persona autenticada reserve inventario y abandone el pedido:
las unidades seguirán reservadas hasta que esa persona las devuelva desde su carrito. Tampoco
pueden demostrar que una actualización proviene de una reserva legítima, por lo que un cliente
autenticado y modificado todavía podría fijar `stock` en cualquier entero no negativo. Para este
laboratorio no se usan Cloud Functions ni vencimientos automáticos, por lo que ambas limitaciones
se documentan como parte del diseño.

## Comprobaciones para registrar antes de entregar

| Comprobación | Resultado esperado | Resultado observado / evidencia pendiente |
|---|---|---|
| Búsqueda de imports | En paquetes de pantallas y ViewModels, cero coincidencias de `androidx.room3`, `androidx.datastore`, `retrofit2` y `com.google.firebase`. | Verificado en el código; adjuntar captura de la búsqueda. |
| Actualización en tiempo real | Al editar `stock` en la consola, la tarjeta cambia sin reabrir el catálogo. | Ejecutar con la app conectada y adjuntar captura/video. |
| Reserva y devolución | Con stock 5: agregar 2 deja 3; quitar 1 deja 4; eliminar la línea deja 5. Con stock 0, aparece “Agotado” y no cambia el pedido. | Ejecutar y registrar los valores observados en la consola. |
| Concurrencia | Con stock 5 y 20 intentos: “Éxitos: 5 · Rechazos: 15” y el documento termina en 0. | Ejecutar en detalle del producto y adjuntar captura junto con la consola. |

## Evidencia manual pendiente

1. En Rules Playground prueben sobre un producto existente: una reducción válida con sesión
   (permitida), `stock = -1` con sesión (denegada) y la reducción válida sin sesión
   (denegada). Guarden las tres capturas.
2. Confirmen en la consola que `products` tiene los 500 IDs estables del Laboratorio 10.
3. La colección ya fue cargada antes de esta migración. Esta versión no contiene un seeder ni
   un botón de carga temporal; agreguen al PR el SHA real en que su grupo retiró ese código, si
   existió en su historial.
4. Vacíen el carrito que existiera antes de ejecutar esta versión. Esas líneas antiguas no
   tienen una reserva de Firestore asociada.
