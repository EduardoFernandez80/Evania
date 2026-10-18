# Evania · Nail Studio Manager

Aplicación Android para gestionar un estudio de uñas, pedicura y spa: clientes, atenciones, servicios, fotos del trabajo, fidelidad y reportes. Funciona sin conexión y guarda todo en el propio teléfono.

La app es una página web (`index.html`) empaquetada dentro de un `WebView` nativo de Android.

## Funciones

- **Clientes y atenciones**: registro de entradas, duración y estado de cada atención.
- **Servicios y precios**: servicios realizados en cada atención, con subtotal.
- **Fotos del trabajo**: fotos de cada atención, guardadas en el teléfono.
- **Catálogo** de servicios.
- **Fidelidad** de clientes.
- **Reportes**.
- **Copias de seguridad**: exportar y restaurar los datos (incluidas las fotos) en un archivo `.json`.
- **Enlaces externos**: abre WhatsApp, llamadas y correo desde la app.

## Estructura del proyecto

```
app/src/main/
├── AndroidManifest.xml
├── assets/index.html                      # Toda la interfaz y la lógica de la app
└── java/com/evania/nailstudiomanager/
    ├── MainActivity.java                  # WebView, selector de archivos y guardado de respaldos
    └── ImagenUtil.java                    # Reduce las fotos antes de pasarlas a la página
```

## Compilar

Requisitos: JDK y Gradle (también funciona desde Termux).

```bash
gradle assembleDebug
```

El APK queda en:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Fotos

- El selector acepta cualquier imagen (`accept="image/*"`), de a una foto por vez.
- Antes de entregarlas a la página, `ImagenUtil` reduce las fotos a un máximo de unos 2048 px por lado, corrige la rotación y las convierte a JPEG. Esto evita fallos en teléfonos con poca memoria al subir fotos pesadas.
- Dentro de la página, las fotos se vuelven a comprimir y se guardan en IndexedDB.

## Datos y copias de seguridad

- Los datos (clientes, atenciones, servicios) y las fotos viven en el almacenamiento del WebView, solo en el dispositivo.
- Desinstalar la app o borrar sus datos **elimina la información**. Haz copias de seguridad periódicas desde la app.
- El respaldo se guarda como archivo `.json` mediante el selector de documentos de Android.

## Privacidad

Este repositorio **no debe contener datos reales de clientes**. El `.gitignore` excluye APKs, llaves de firma, carpetas de compilación y archivos de respaldo (`*Respaldo*.json`). No subas copias de seguridad ni capturas con información personal.

## Notas técnicas

- Paquete: `com.evania.nailstudiomanager`
- Interfaz: HTML, CSS y JavaScript en un solo archivo, sin librerías externas.
- Puente nativo: `window.Android.saveBackup(nombre, base64)` guarda el respaldo desde la página.
- Si cambias el origen desde el que se carga la página (por ejemplo, de `file://` a otro), los datos guardados no se trasladan: haz un respaldo antes.
- 
