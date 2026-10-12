# Evidencia de persistencia — Persona 3

## Alcance

La preferencia persistida es el orden del catálogo:

- `original`: conserva el orden determinista de generación.
- `name`: orden alfabético por nombre.
- `price`: precio ascendente y, en empates, ID ascendente.

`Context.storePreferencesDataStore` se declara una sola vez a nivel de archivo en
`StorePreferences.kt`. `StoreViewModel` lee `catalog_sort` como `Flow`, usa
`CatalogSort.ORIGINAL` cuando todavía no existe un valor y escribe mediante `edit`.
Los composables solo reciben el valor y el callback; no acceden a DataStore.

## Verificación automatizada

Comandos ejecutados:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug assembleDebugAndroidTest lintDebug
```

Resultado observado:

- `BUILD SUCCESSFUL`.
- El APK de la aplicación y el APK de pruebas instrumentadas compilan.
- `lintDebug` no reporta problemas en los archivos de Persona 3.
- `CatalogPreferencesTest.viewModelWritesCatalogSortToDataStore` comprueba la
  escritura iniciada desde el ViewModel.
- `CatalogPreferencesTest.newViewModelRestoresPersistedSortAndAppliesItToCatalog`
  crea otro ViewModel y comprueba la restauración y el orden alfabético.

Las pruebas instrumentadas quedaron compiladas, pero su ejecución requiere un
dispositivo o AVD conectado.

## Preparación de la prueba manual

1. Instalar una compilación nueva de la rama sin borrar los datos entre recorridos.
2. Abrir la aplicación y seleccionar `Precio` en el catálogo.
3. Marcar al menos un producto como favorito.
4. Agregar al menos dos unidades al pedido.
5. Anotar los productos, cantidades y orden seleccionado.
6. Abrir **View > Tool Windows > App Inspection > Database Inspector**.
7. Seleccionar el proceso `uvg.edu.laboratorio09` y comprobar:
   - Tabla `favorites`: una fila por favorito.
   - Tabla `order_lines`: una fila por producto del pedido y su cantidad.
8. En **Device Explorer**, localizar:
   `data/data/uvg.edu.laboratorio09/files/datastore/store_preferences.preferences_pb`.

## Cierre real y reapertura

La rotación no demuestra persistencia en disco. Para la evidencia se debe detener
el proceso:

1. Presionar **Stop** en Android Studio, o abrir
   **Ajustes > Aplicaciones > laboratorio09 > Forzar detención**.
2. No volver a ejecutar con el botón Run.
3. Abrir la aplicación desde el icono del launcher.
4. Comprobar que:
   - `Precio` sigue seleccionado.
   - El catálogo continúa ordenado por precio ascendente.
   - Los favoritos siguen marcados.
   - El pedido conserva productos, cantidades, subtotales y total.
5. Confirmar el pedido.
6. Verificar en Database Inspector que `order_lines` queda sin filas y que
   `favorites` conserva sus filas.
7. Detener nuevamente el proceso y abrir desde el launcher.
8. Comprobar que el pedido continúa vacío, los favoritos sobreviven y el orden
   del catálogo sigue restaurado.

## Tabla de resultados manuales

Completar la columna **Observado** únicamente después de realizar el recorrido en
un dispositivo o AVD.

| Comprobación | Resultado esperado | Observado | Evidencia |
| --- | --- | --- | --- |
| Escritura desde ViewModel | Al tocar `Precio`, DataStore guarda `catalog_sort=price`. | Pendiente | Captura del catálogo y archivo DataStore |
| Restauración del orden | Después de forzar cierre y abrir desde el icono, `Precio` continúa seleccionado y aplicado. | Pendiente | Video continuo |
| Favoritos en Room | `favorites` contiene las filas seleccionadas y los iconos sobreviven al cierre. | Pendiente | Database Inspector y video |
| Pedido en Room | `order_lines` conserva productos y cantidades después del cierre. | Pendiente | Database Inspector y video |
| Confirmación persistida | Confirmar elimina todas las filas de `order_lines`; tras otro cierre, el pedido sigue vacío. | Pendiente | Database Inspector y video |
| DataStore único | La aplicación funciona sin `IllegalStateException` y solo existe la declaración de `preferencesDataStore`. | Pendiente | Código y ejecución |

## Guion de video

Duración recomendada: máximo tres minutos y sin cortes.

1. Mostrar el catálogo y seleccionar `Precio`.
2. Marcar un favorito.
3. Agregar productos al pedido.
4. Mostrar `favorites` y `order_lines` en Database Inspector.
5. Detener la aplicación con **Stop**.
6. Abrirla desde el icono.
7. Mostrar que orden, favorito y pedido se restauraron.
8. Confirmar el pedido.
9. Mostrar que `order_lines` quedó vacío.
10. Detener y abrir nuevamente desde el icono.
11. Mostrar pedido vacío, favorito conservado y orden restaurado.

## Datos de la evidencia

| Campo | Valor |
| --- | --- |
| Dispositivo o AVD | Pendiente |
| Versión de Android / API | Pendiente |
| Fecha de la prueba | Pendiente |
| URL del video | Pendiente |
| Captura de Room antes de confirmar | Pendiente |
| Captura de Room después de confirmar | Pendiente |
| Captura de la preferencia | Pendiente |

## Declaración de herramientas

Se utilizó OpenAI Codex para revisar la arquitectura existente, implementar la
preferencia de DataStore, integrarla al estado, preparar pruebas y documentar el
recorrido de verificación. La ejecución manual, las capturas y el video deben ser
realizados y revisados por los integrantes.
