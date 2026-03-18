# NanoFiles - P2P File Sharing 🚀

Este proyecto es un sistema de compartición de archivos que utiliza una arquitectura híbrida.

### 🛠️ Lo que implementé:
* **Protocolo UDP Fiable**: Comunicación con el servidor de directorio mediante técnicas de parada y espera.
* **Seguridad (Mejora)**: Encriptación **AES** en las comunicaciones P2P para proteger la privacidad de los datos.
* **Descarga Paralela**: Uso de múltiples hilos para descargar fragmentos de archivos desde varios peers simultáneamente.
* **Multihilo**: Servidor capaz de gestionar múltiples conexiones de clientes al mismo tiempo.
