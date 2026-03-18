# NanoFiles P2P System

Sistema distribuido de transferencia de archivos desarrollado en Java que combina arquitectura cliente-servidor con comunicación peer-to-peer (P2P).

## 🚀 Características principales

- Protocolo de comunicación fiable sobre UDP (implementación propia)
- Transferencia de archivos P2P sobre TCP
- Descarga paralela multihilo mediante fragmentación en chunks
- Cifrado AES en comunicaciones entre peers
- Sistema de directorio centralizado para descubrimiento de recursos
- Gestión concurrente de múltiples conexiones

## 🧱 Arquitectura

El sistema se compone de:

- **Directory Server**
  - Mantiene información de archivos y peers
  - Comunicación mediante protocolo fiable sobre UDP

- **Peers (NanoFiles)**
  - Actúan como cliente y servidor
  - Descargan y comparten archivos
  - Comunicación P2P mediante TCP

## 🔌 Protocolos implementados

### 1. Protocolo de control (UDP)
Mensajes tipo:
- `ping`, `filelistRequest`, `serveRequest`, `downloadRequest`, `userlist`
- Sistema basado en mensajes `campo:valor`

Incluye:
- Control de errores
- Compatibilidad de protocolo
- Respuestas estructuradas

### 2. Protocolo de transferencia (TCP)
Mensajes binarios con opcodes:
- `DownloadFile`, `ChunkRequest`, `ChunkResponse`
- `UploadRequest`, `UploadData`, `UploadComplete`

Características:
- Transferencia por fragmentos
- Verificación mediante hash
- Manejo de errores (chunk not found, EOF, etc.)

## ⚡ Descarga paralela

- División de archivos en chunks
- Descarga concurrente desde múltiples peers
- Asignación round-robin
- Sincronización mediante locks
- Validación final por hash

## 🔐 Seguridad

- Cifrado AES en mensajes P2P
- Protección frente a sniffing
- Integración transparente en el protocolo

## 🧵 Concurrencia

- Uso de estructuras concurrentes (maps, arrays sincronizados)
- Control de acceso con locks
- Gestión de múltiples hilos por descarga

## 📈 Limitaciones

- Tamaño máximo de archivo limitado (~2GB)
- Número de hilos limitado para evitar saturación
- Sin detección automática de peers caídos

## 🔮 Futuro trabajo

- Thread pool con ExecutorService
- Uso de RandomAccessFile para archivos grandes
- Detección activa de peers (heartbeat)
- Mejora de estructuras de datos

## 📄 Documentación

- [Design Document](https://github.com/GuillermoPL/NanoFiles-RC/blob/main/docs/Documento%20de%20dise%C3%B1o%20del%20proyecto%20NanoFilesP2P.pdf)
