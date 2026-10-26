# E100-1 — Integrador completo

## Componentes implementados

- Remote Config se consulta en `IntegratorWorker` y se guarda en Room (`integrator.db`).
  La app observa Room y conserva la última sincronización exitosa cuando no hay Internet.
- Si los valores ya sincronizados cambian, se guarda una entrada de historial en Room y
  se muestra una notificación local. El primer valor guardado es la línea base.
- La lista compartida usa Firestore `collaborative_items` y `addSnapshotListener`.
  Cada cambio se confirma mediante una transacción con el resumen `collaboration/summary`.
- El resumen contiene `count` y `revision`; la app los refleja en Realtime Database
  `collaborative_counter`. El worker reconcilia el espejo cada vez que se ejecuta.
- El token FCM se guarda en Firestore en `users/{uid}/devices/{installationId}`
  cuando hay un usuario autenticado. `PushService` muestra mensajes en primer plano
  y mensajes de datos recibidos en segundo plano.
- WorkManager programa una sincronización inicial y otra periódica con intervalo
  mínimo de 15 minutos y conexión de red. Android decide el momento exacto.

## Configuración requerida en Firebase

1. Abre el proyecto `UCBP1 Jonathan` en Firebase Console.
2. Crea **Cloud Firestore** si todavía no existe. La ubicación debe elegirse con
   cuidado, porque puede no ser modificable después de crear la base de datos.
3. En Firestore, abre **Reglas**, copia el contenido completo de
   `firebase/firestore.rules` y pulsa **Publicar**.
4. En Realtime Database, abre **Reglas**, reemplázalas por el contenido completo
   de `firebase/database.rules.json` y pulsa **Publicar**. El archivo conserva las
   reglas de notas anteriores y añade el contador compartido.
5. Authentication debe mantener habilitado Correo/contraseña. Remote Config debe
   tener `welcome_message`, `notes_enabled` y `maintenance_mode` publicados.

Las reglas locales son archivos preparados para copiar o desplegar. No se aplican
automáticamente en Firebase al instalar la app.

## Demostraciones

1. Ejecuta la app y regístrate o inicia sesión. Abre **Lista compartida** y crea
   un elemento. Otro dispositivo autenticado debe recibirlo por el listener. El
   contador de RTDB se actualiza cuando se reconcilia el resumen de Firestore.
   Solo el creador de un elemento puede borrarlo.
2. Tras una sincronización exitosa, desactiva Internet y reinicia la app: el
   último valor de configuración se conserva en Room. Firestore puede mostrar
   la lista ya almacenada en su propia caché. Agregar o borrar mediante
   transacciones requiere Internet.
3. Cambia `welcome_message` en Remote Config y publica. La sincronización genera
   una notificación local y guarda un registro en **Ajustes > Historial**. En
   segundo plano, WorkManager puede tardar más de 15 minutos por restricciones
   del sistema. Una notificación requiere permiso de Android.
4. Copia el token FCM en Ajustes y envía un mensaje de prueba desde Firebase
   Console. En datos personalizados agrega `route` con `collaborative`, `notes`,
   `profile`, `settings` o `home`. Al pulsar la notificación la app abre esa ruta.
   Los mensajes con notificación en segundo plano los muestra Android; los datos
   adjuntos llegan al intent al pulsarla. Un mensaje solo de datos pasa por el
   servicio tanto en primer plano como en segundo plano cuando FCM lo entrega.
5. En Android Studio, App Inspection / Background Task Inspector permite observar
   las tareas de WorkManager, y Database Inspector permite inspeccionar
   `integrator.db`, tablas `remote_config` y `config_history`.

## Consistencia y límites

Firestore y Realtime Database no comparten transacciones atómicas. La transacción
Firestore garantiza la actualización conjunta de cada elemento y su resumen;
RTDB es una réplica de consistencia eventual. Si falla la red entre ambas, la
interfaz indica que el contador está pendiente y otro cliente conectado o el
worker lo reconcilia. La revisión evita que un cliente con una revisión vieja
reemplace una más reciente. Como la réplica RTDB es escrita por clientes
autenticados, las reglas del ejemplo no garantizan su integridad frente a un
cliente malicioso. Para producción se necesita un backend confiable que sea el
único autorizado a escribir el contador.

No se han verificado las reglas publicadas ni un envío FCM externo hasta que se
configure Firestore y se ejecuten esas pruebas en la consola del proyecto.
