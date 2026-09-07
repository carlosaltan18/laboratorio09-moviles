# Prueba de aceptación — Persona 3

Rama: lab09/tienda

## Secuencia ejecutada

1. Abrir el segundo producto desde el catálogo.
2. Marcarlo como favorito desde el detalle.
3. Expandir la ficha técnica efímera.
4. Rotar el dispositivo.
5. Confirmar que se conserva el mismo detalle.
6. Confirmar que el favorito continúa activo.
7. Confirmar que la ficha técnica vuelve a estar contraída.
8. Regresar al catálogo y confirmar que el segundo producto muestra el favorito.
9. Abrir nuevamente el segundo producto.
10. Abrir su perfil asociado.
11. Regresar al detalle y luego al catálogo.
12. Usar el regreso del sistema desde el catálogo.
13. Confirmar que la ruta raíz no se retira desde BackHandler.

## Evidencia de implementación

- StoreViewModel es la única fuente de verdad y expone uiState como StateFlow de solo lectura.
- El favorito se actualiza con copy y permanece al recrear la actividad por rotación.
- La ficha técnica usa remember, por lo que su estado expandido es efímero y se reinicia al recrear la pantalla.
- La raíz obtiene el ViewModel una sola vez y conecta el mismo callback de favorito al catálogo y al detalle.
- Detalle y perfil resuelven sus claves con find; un ID inexistente muestra un destino seguro en lugar de lanzar una excepción.
- El BackHandler solo está habilitado cuando backStack.size > 1, evitando retirar la ruta raíz.

## Resultado observado

La secuencia se ejecutó en el AVD Pixel_10:

- [x] El segundo producto abrió su detalle.
- [x] El favorito permaneció activo después de rotar el dispositivo.
- [x] La ficha técnica se reinició contraída después de rotar.
- [x] El catálogo mostró el favorito al regresar.
- [x] El perfil Dulce Antigua abrió y regresó al detalle y al catálogo.
- [x] El regreso del sistema desde el catálogo volvió al launcher sin retirar una ruta raíz ni producir errores fatales.

Verificaciones automatizadas: testDebugUnitTest y assembleDebug finalizaron con BUILD SUCCESSFUL.
