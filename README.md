# Chocolatería artesanal

Aplicación Android desarrollada con Kotlin y Jetpack Compose para explorar un catálogo de
chocolates guatemaltecos, guardar favoritos, reservar existencias, completar un checkout y
consultar pedidos enviados a un servidor MockAPI.

El proyecto reúne los laboratorios de navegación, MVVM, listas eficientes, checkout reactivo,
persistencia local, Retrofit y el laboratorio final de **Cloud Firestore + Repository**.

## Funcionalidades

- Catálogo de 500 productos con búsqueda y orden local por orden original, nombre o precio.
- Detalle de chocolate, perfil de chocolatero y favoritos persistentes.
- Pedido en curso persistido localmente mediante Room.
- Reserva de stock en Firestore al agregar una unidad y devolución al disminuir o eliminar una línea.
- Checkout con validación reactiva, facturación CF/NIT, recibo y confirmación contra MockAPI.
- Pantallas «Mis pedidos» y detalle remoto.
- Actualizaciones de existencias en tiempo real, estado de carga, error y reintento.
- Herramienta visible de prueba de concurrencia: 20 intentos de reserva simultáneos.

## Tecnologías

| Área | Tecnologías |
|---|---|
| Interfaz | Jetpack Compose, Material 3, Coil |
| Arquitectura | MVVM, `StateFlow`, Repository, inyección manual |
| Navegación | Navigation 3 Runtime/UI con claves serializables |
| Datos locales | Room 3 y DataStore Preferences |
| Datos remotos | Retrofit 3, Kotlin Serialization y MockAPI |
| Backend compartido | Firebase Authentication anónimo y Cloud Firestore |
| Concurrencia | Coroutines y transacciones de Firestore |
| Pruebas | JUnit, MockK, Compose UI Test y UI Automator |

Las versiones centralizadas están en [`gradle/libs.versions.toml`](gradle/libs.versions.toml).
Firebase se configura con Google Services `4.4.3` y Firebase BoM `34.19.0`.

## Arquitectura

La app usa **MVVM**: los composables renderizan estado y envían eventos; los ViewModels aplican
la lógica de presentación; los Repositories ocultan el origen de los datos.

```text
Compose screens + Navigation 3
            │ eventos / StateFlow
            ▼
StoreViewModel · OrdersViewModel · ProductDetailViewModel
            │ contratos de dominio
            ▼
Repository interfaces
            │ implementaciones
            ├── FirestoreProductRepository ── Cloud Firestore
            ├── FirebaseAuthRepository ───── Firebase Authentication
            ├── RoomFavoriteRepository ───── Room / SQLite
            ├── RoomCartRepository ───────── Room / SQLite
            ├── DataStoreCatalogPreferencesRepository ─ DataStore
            ├── MockApiOrderRepository ───── Retrofit / MockAPI
            └── InMemoryChocolatierRepository
```

### Inyección manual

[`StoreApplication`](app/src/main/java/uvg/edu/laboratorio09/StoreApplication.kt) crea un único
[`AppContainer`](app/src/main/java/uvg/edu/laboratorio09/AppContainer.kt). El contenedor
construye las implementaciones concretas de los Repositories. Cada ViewModel recibe los
contratos necesarios por constructor usando su `ViewModelProvider.Factory`.

Esto evita que una pantalla o un ViewModel importe Firebase, Room, DataStore o Retrofit. La capa
de presentación trabaja exclusivamente con modelos de dominio como `Chocolate`, `OrderLine`,
`RemoteOrder` y `Chocolatier`.

## Estructura del proyecto

```text
app/src/main/java/uvg/edu/laboratorio09/
├── AppContainer.kt                  # composición de dependencias
├── StoreApplication.kt              # aplicación e inicio de sesión anónimo
├── MainActivity.kt                  # raíz Compose y Navigation 3
├── data/
│   ├── firebase/                    # Auth, listener y transacciones Firestore
│   ├── local/                       # Room, DAO, entities y DataStore
│   ├── remote/                      # implementación Retrofit del Repository
│   └── inmemory/                    # perfiles estáticos de chocolateros
├── domain/
│   ├── CheckoutValidators.kt        # validaciones puras del checkout
│   ├── OrderRules.kt                # cálculos de pedido
│   └── repository/                  # contratos de datos
├── model/                           # modelos inmutables de dominio y UI
├── navigation/StoreNavKey.kt        # destinos serializables Navigation 3
├── remote/                          # API Retrofit y DTOs privados de datos
├── remoteorders/                    # estados, ViewModel y pantallas de pedidos remotos
├── ui/screens/                      # composables reutilizables y pantallas
└── viewmodel/                       # StoreViewModel y ProductDetailViewModel

docs/
├── firestore.rules                  # reglas definitivas de Firestore
├── lab-final-firebase-repository.md # decisiones y comprobaciones finales
└── PR_DESCRIPTION.md                # plantilla de evidencia para el PR
```

## Navegación

La navegación se implementa con `rememberNavBackStack`, `NavDisplay` y las claves selladas de
[`StoreNavKey`](app/src/main/java/uvg/edu/laboratorio09/navigation/StoreNavKey.kt). Las claves
que llevan datos, por ejemplo `Detail(productId)` y `RemoteOrderDetail(orderId)`, son
serializables y no transportan objetos completos.

```text
Catálogo ──► Detalle ──► Perfil de chocolatero
   │             │
   │             └──► Pedido ──► Checkout ──► Confirmación
   │
   └──► Mis pedidos ──► Detalle de pedido remoto
```

El back stack se mantiene en la raíz de Compose. Por eso la navegación no crea un nuevo
`StoreViewModel` por pantalla y el pedido, favoritos y estado de checkout se conservan al rotar.

## Estado y validación

`StoreViewModel` expone `uiState`, `checkoutUiState`, `orderReceipt` y
`orderSubmissionState` como `StateFlow` de sólo lectura. El estado interno es inmutable y se
actualiza con `copy` y `MutableStateFlow.update`.

Las validaciones del checkout viven en Kotlin puro:

- Nombre: al menos tres letras, sin dígitos.
- Teléfono/WhatsApp: exactamente ocho dígitos.
- NIT y razón social: obligatorios únicamente al seleccionar «Factura con NIT».
- Los errores se muestran después de interactuar con cada campo (`isTouched`).

El botón de confirmar usa `derivedStateOf` para combinar formulario válido, pedido con unidades y
ausencia de un envío en progreso. Al cambiar de NIT a Consumidor Final, el ViewModel limpia el
estado fiscal y sus errores en cascada.

## Firebase y Firestore

### Autenticación

Al abrir la app, `StoreApplication` solicita una sesión anónima mediante
`FirebaseAuthRepository`. Las reglas de Firestore requieren un `request.auth` válido; por ello
la primera carga del catálogo espera que esa sesión exista.

### Catálogo en tiempo real

[`FirestoreProductRepository`](app/src/main/java/uvg/edu/laboratorio09/data/firebase/FirestoreProductRepository.kt)
observa la colección `products` con `addSnapshotListener`, la transforma a un
`Flow<List<Chocolate>>` y actualiza la interfaz sin reabrir el catálogo.

Cada documento requiere estos campos:

```text
products/{productId}
├── chocolatierId : String
├── description   : String
├── imageUrl      : String
├── name          : String
├── priceCents    : Int
└── stock         : Int
```

Los IDs son estables (`chocolate_01`, `chocolate_02`, `chocolate_03`,
`generated-chocolate-001`, etc.). Esto permite que los favoritos y las líneas de pedido locales
continúen señalando al mismo producto.

La aplicación no genera ni siembra el catálogo al iniciar. La colección debe contener los 500
documentos cargados una sola vez desde el catálogo estable del proyecto.

### Reserva de existencias y concurrencia

Agregar una unidad no modifica primero el carrito. El flujo correcto es:

```text
Usuario toca «Agregar»
        │
        ▼
Transacción Firestore lee el stock actual del servidor
        │
        ├── stock > 0 ──► reduce stock en 1 ──► aumenta la línea local de Room
        │
        └── stock = 0 ──► rechaza la reserva y muestra «Agotado»
```

Una transacción vuelve a evaluarse si otro cliente modifica el documento antes de confirmarla;
así no pueden terminar más reservas exitosas que unidades disponibles. Disminuir una cantidad o
eliminar una línea devuelve esas unidades con otra transacción. Confirmar un pedido **no** vuelve
a descontar el inventario porque ya quedó reservado.

En el detalle del producto está la acción «Prueba de concurrencia · 20 intentos». Con un stock
inicial de 5, el resultado esperado es `Éxitos: 5 · Rechazos: 15` y stock final `0`.

### Reglas de seguridad

Las reglas versionadas están en [`docs/firestore.rules`](docs/firestore.rules). Deben publicarse
en **Firestore Database → Reglas**:

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

Las reglas impiden que el cliente cree, borre o altere nombre, precio, imagen y demás campos.
Como límite conocido, un cliente autenticado y modificado aún puede intentar fijar un stock entero
no negativo arbitrario, y una reserva abandonada permanece ocupada hasta que se devuelva desde el
carrito. El laboratorio no usa Cloud Functions ni vencimientos automáticos.

## Persistencia local y pedidos remotos

| Dato | Almacenamiento | Repository |
|---|---|---|
| Favoritos | Room | `RoomFavoriteRepository` |
| Pedido en curso | Room | `RoomCartRepository` |
| Preferencia de orden | DataStore Preferences | `DataStoreCatalogPreferencesRepository` |
| Pedido confirmado e historial | MockAPI | `MockApiOrderRepository` |
| Catálogo y stock | Cloud Firestore | `FirestoreProductRepository` |

Los DTO de Retrofit no salen de la capa `data/remote`; el resto de la app recibe `RemoteOrder` y
`RemoteOrderLine`. La URL base de desarrollo está encapsulada en
[`OrderNetwork.kt`](app/src/main/java/uvg/edu/laboratorio09/remote/OrderNetwork.kt).

Al confirmar, la aplicación envía el pedido a MockAPI y vacía Room sólo después de recibir una
respuesta exitosa. Así, si falla la red, el pedido reservado permanece disponible para reintentar.
No se envían datos personales reales: el pedido remoto usa un cliente genérico.

## Configuración local

### Requisitos

- Android Studio actualizado.
- JDK 21 para Gradle.
- Dispositivo o emulador con Android API 33 o superior.
- Un proyecto Firebase con Authentication anónimo y Firestore habilitados.

### Pasos

1. Clona el repositorio y ábrelo en Android Studio.
2. Verifica que el `applicationId` sea `uvg.edu.laboratorio09`.
3. En Firebase, registra esa app Android y coloca el archivo descargado como
   `app/google-services.json`.
4. Habilita **Anonymous** en Firebase Authentication.
5. Crea Firestore en modo producción, edición Standard, con la base `(default)`.
6. Carga una única vez los 500 documentos estables en `products`.
7. Publica las reglas de [`docs/firestore.rules`](docs/firestore.rules).
8. Sincroniza Gradle y ejecuta la configuración `app`.

> `google-services.json` identifica el proyecto Firebase; no reemplaza las reglas de seguridad.
> Nunca agregues claves privadas, datos de pago ni datos personales reales al repositorio.

## Verificación

Desde PowerShell:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
.\gradlew.bat testDebugUnitTest assembleDebugAndroidTest lintDebug
```

Antes de entregar, verifica también de forma manual:

1. Cambia un `stock` desde la consola y confirma que el catálogo se actualiza sin reabrirse.
2. Con stock 5, agrega 2 unidades, quita 1 y elimina la línea: `5 → 3 → 4 → 5`.
3. Con stock 0, intenta agregar: debe mostrarse «Agotado» y el pedido no cambia.
4. Ejecuta la prueba de concurrencia con 20 intentos y stock 5.
5. En Rules Playground prueba una reducción válida autenticada, stock negativo y una petición sin
   sesión.
6. Confirma un pedido y revisa «Mis pedidos» y su detalle remoto.

La plantilla para registrar resultados, capturas, video y decisiones está en
[`docs/PR_DESCRIPTION.md`](docs/PR_DESCRIPTION.md).

## Documentación adicional

- [Decisiones y comprobaciones del laboratorio final](docs/lab-final-firebase-repository.md)
- [Contrato y comprobaciones de MockAPI](docs/lab13-remote-orders-contract.md)
- [Decisiones de persistencia local](docs/lab12-persistence-decisions.md)
- [Prueba de aceptación de navegación](ACCEPTANCE.md)

## Convenciones

- Código e identificadores en inglés; textos visibles para la persona usuaria en español.
- Estados inmutables y exposición pública mediante `asStateFlow()`.
- Operaciones observables mediante `Flow`; acciones suspendidas mediante `Result`.
- Sin `!!`, sin logs de depuración y sin acceso directo a fuentes de datos desde pantallas o
  ViewModels.
