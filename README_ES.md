# Evania Manicura Pedicura & Spa para Android

Proyecto fuente para abrir en Android Studio y compilar una APK. El HTML adjunto se incluye en `app/src/main/assets/index.html`.

## Compilar
1. Descomprime el ZIP.
2. Abre la carpeta `NailStudioManager_Android` en Android Studio.
3. Usa JDK 17 y espera a que Gradle termine de sincronizar (requiere Internet para descargar las herramientas).
4. Ve a **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
5. Android Studio indicará dónde quedó el APK.

## Notas
- Este ZIP contiene el HTML que se adjuntó en esta conversación; no garantiza que sea la última versión modificada previamente.
- La aplicación usa WebView con JavaScript y `localStorage` habilitados.
- Los datos permanecen en el almacenamiento de la app en ese dispositivo. Desinstalarla o borrar sus datos puede eliminarlos.
- Los enlaces de WhatsApp se abren en una aplicación externa.
- En Reportes > Respaldo completo puedes descargar un JSON con todos los datos y las fotos; al compilar como app Android, el sistema te permite elegir dónde guardarlo. También puedes seleccionar ese archivo para restaurarlo.
- Se entrega el proyecto fuente, no un APK compilado ni el Gradle Wrapper.
