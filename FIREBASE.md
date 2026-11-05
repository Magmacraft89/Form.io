# Conectar Form.io con Firebase

La app usa **Firebase Authentication** (cuentas de administrador), **Cloud Firestore**
(formularios, respuestas, perfil y plantillas) y **Firebase Hosting** (la página web
que se abre al escanear el QR). Todo funciona con el plan gratuito **Spark**.

## 1. Crear el proyecto
1. Entra a https://console.firebase.google.com y pulsa **Agregar proyecto**.
2. Ponle un nombre (por ejemplo `formio-devcore`). Google Analytics es opcional.

## 2. Registrar la app Android
1. En la página del proyecto pulsa el ícono de **Android**.
2. Nombre del paquete: `com.devcore.formio`
3. Descarga **google-services.json** y cópialo dentro de la carpeta **`app/`**
   (al lado de `app/build.gradle`). Sin este archivo la app no compila.

## 3. Activar el inicio de sesión
**Compilación → Authentication → Comenzar → Correo electrónico/contraseña → Habilitar → Guardar.**

## 4. Crear la base de datos
**Compilación → Firestore Database → Crear base de datos** → elige la ubicación
(`nam5` o la más cercana) → inicia en **modo de producción**.

## 5. Publicar las reglas de seguridad
En **Firestore → Reglas**, borra lo que hay, pega el contenido de
`firebase/firestore.rules` y pulsa **Publicar**.
(También se publican solas con el paso 6.)

## 6. Publicar la página del QR (Firebase Hosting)
Necesitas Node.js instalado. En una terminal, dentro de la carpeta `firebase/`:

```bash
npm install -g firebase-tools
firebase login
firebase use --add          # elige tu proyecto y ponle el alias "default"
firebase deploy             # sube la página y las reglas
```

Al terminar verás la dirección `https://TU-PROYECTO.web.app`. Los QR de la app apuntan
a `https://TU-PROYECTO.web.app/f/CODIGO`; la app arma esa dirección sola con el ID del
proyecto que viene en `google-services.json`.

## 7. Probar
1. Ejecuta la app en Android Studio, crea una cuenta y entra.
2. En el Dashboard pulsa **Cargar ejemplos** (o crea tu propio formulario).
3. Publica un formulario, escanea su QR con otro celular y responde:
   la respuesta aparece al instante en la app.

## Estructura de la base de datos

| Ruta | Contenido |
|---|---|
| `usuarios/{uid}` | nombre, correo, teléfono, organización, foto (Base64) |
| `usuarios/{uid}/plantillas/{id}` | plantillas propias del administrador |
| `formularios/{CODIGO}` | título, preguntas, configuración, visitas y total de respuestas |
| `formularios/{CODIGO}/respuestas/{id}` | número, fecha, duración y valores (anónimas) |

## Problemas comunes
- **"No tienes permiso para esta acción"**: faltan las reglas del paso 5.
- **El QR abre una página "Site Not Found"**: falta el `firebase deploy` del paso 6.
- **Error al compilar `processDebugGoogleServices`**: falta `app/google-services.json`
  o el paquete no es `com.devcore.formio`.
