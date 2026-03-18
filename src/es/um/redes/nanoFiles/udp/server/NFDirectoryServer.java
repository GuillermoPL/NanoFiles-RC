package es.um.redes.nanoFiles.udp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoFiles.application.Directory;
import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.udp.message.DirMessage;
import es.um.redes.nanoFiles.udp.message.DirMessageOps;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFDirectoryServer {
	/**
	 * Número de puerto UDP en el que escucha el directorio
	 */
	public static final int DIRECTORY_PORT = 6868;
	
	/**
	 * Socket de comunicación UDP con el cliente UDP (DirectoryConnector)
	 */
	private DatagramSocket socket = null;
	/*
	 * DONE: Añadir aquí como atributos las estructuras de datos que sean necesarias
	 * para mantener en el directorio cualquier información necesaria para la
	 * funcionalidad del sistema nanoFilesP2P: ficheros publicados, servidores
	 * registrados, etc.
	 */
	private Map<FileInfo, Set<InetSocketAddress>> IPsPorFichero;
	private Map<InetSocketAddress,	Set<FileInfo>> ficherosPorIp;
	private Map<InetSocketAddress, InetSocketAddress> direccionesDirPeer;
	/**
	 * Probabilidad de descartar un mensaje recibido en el directorio (para simular
	 * enlace no confiable y testear el código de retransmisión)
	 */
	private double messageDiscardProbability;

	public NFDirectoryServer(double corruptionProbability) throws SocketException {
		/*
		 * Guardar la probabilidad de pérdida de datagramas (simular enlace no
		 * confiable)
		 */
		messageDiscardProbability = corruptionProbability;
		/*
		 * DONE: (Boletín SocketsUDP) Inicializar el atributo socket: Crear un socket
		 * UDP ligado al puerto especificado por el argumento directoryPort en la
		 * máquina local,
		 */
		socket = new DatagramSocket(DIRECTORY_PORT);
		/*
		 * DONE: (Boletín SocketsUDP) Inicializar atributos que mantienen el estado del
		 * servidor de directorio: ficheros, etc.)
		 */
		IPsPorFichero = new HashMap<FileInfo, Set<InetSocketAddress>>();
		ficherosPorIp = new HashMap<InetSocketAddress,	Set<FileInfo>>();
		direccionesDirPeer = new HashMap<InetSocketAddress, InetSocketAddress>();

		if (NanoFiles.testModeUDP) {
			if (socket == null) {
				System.err.println("[testMode] NFDirectoryServer: code not yet fully functional.\n"
						+ "Check that all TODOs in its constructor and 'run' methods have been correctly addressed!");
				System.exit(-1);
			}
		}
	}

	@SuppressWarnings("unused")
	public DatagramPacket receiveDatagram() throws IOException {
		DatagramPacket datagramReceivedFromClient = null;
		boolean datagramReceived = false;
		while (!datagramReceived) {
			/*
			 * DONE: (Boletín SocketsUDP) Crear un búfer para recibir datagramas y un
			 * datagrama asociado al búfer (datagramReceivedFromClient)
			 */
			byte buffer[] = new byte[DirMessage.PACKET_MAX_SIZE];
			datagramReceivedFromClient = new DatagramPacket(buffer, buffer.length);

			/*
			 * DONE: (Boletín SocketsUDP) Recibimos a través del socket un datagrama
			 */
			socket.receive(datagramReceivedFromClient);

			if (datagramReceivedFromClient == null) {
				System.err.println("[testMode] NFDirectoryServer.receiveDatagram: code not yet fully functional.\n" + "Check that all TODOs have been correctly addressed!");
				System.exit(-1);
			} else {
				// Vemos si el mensaje debe ser ignorado (simulación de un canal no confiable)
				double rand = Math.random();
				if (rand < messageDiscardProbability) {
					System.err.println("Directory ignored datagram from " + datagramReceivedFromClient.getSocketAddress());
				} else {
					datagramReceived = true;
					System.out
							.println("Directory received datagram from " + datagramReceivedFromClient.getSocketAddress() + " of size " + datagramReceivedFromClient.getLength() + " bytes.");
				}
			}

		}

		return datagramReceivedFromClient;
	}

	public void runTest() throws IOException {

		System.out.println("[testMode] Directory starting...");

		System.out.println("[testMode] Attempting to receive 'ping' message...");
		DatagramPacket rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);

		System.out.println("[testMode] Attempting to receive 'ping&PROTOCOL_ID' message...");
		rcvDatagram = receiveDatagram();
		sendResponseTestMode(rcvDatagram);
	}

	private void sendResponseTestMode(DatagramPacket pkt) throws IOException {
		/*
		 * DONE: (Boletín SocketsUDP) Construir un String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración.
		 */
		String receivedMessage = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("DATA RECEIVED: " + receivedMessage);
		/*
		 * DONE: (Boletín SocketsUDP) Después, usar la cadena para comprobar que su
		 * valor es "ping"; en ese caso, enviar como respuesta un datagrama con la
		 * cadena "pingok". Si el mensaje recibido no es "ping", se informa del error y
		 * se envía "invalid" como respuesta.
		 */
		InetSocketAddress clientAddr = (InetSocketAddress) pkt.getSocketAddress();
		String messageToClient;
		if(receivedMessage.equals("ping")) {
			messageToClient = new String("pingok");
		}else if(receivedMessage.startsWith("ping&")) {
			if(receivedMessage.equals("ping&"+NanoFiles.PROTOCOL_ID)) {
				messageToClient = new String ("welcome");
			}else{
				messageToClient = new String ("denied");
			}
		}
		else {
			messageToClient = new String ("invalid");
		}
		byte dataToClient[] = messageToClient.getBytes();
		DatagramPacket packetToClient = new DatagramPacket(dataToClient, dataToClient.length, clientAddr);
		socket.send(packetToClient);
		/*
		 * DONE: (Boletín Estructura-NanoFiles) Ampliar el código para que, en el caso
		 * de que la cadena recibida no sea exactamente "ping", comprobar si comienza
		 * por "ping&" (es del tipo "ping&PROTOCOL_ID", donde PROTOCOL_ID será el
		 * identificador del protocolo diseñado por el grupo de prácticas (ver
		 * NanoFiles.PROTOCOL_ID). Se debe extraer el "protocol_id" de la cadena
		 * recibida y comprobar que su valor coincide con el de NanoFiles.PROTOCOL_ID,
		 * en cuyo caso se responderá con "welcome" (en otro caso, "denied").
		 */

		String messageFromClient = new String(pkt.getData(), 0, pkt.getLength());
		System.out.println("Data received: " + messageFromClient);



	}

	public void run() throws IOException {

		System.out.println("Directory starting...");

		while (socket!=null && !socket.isClosed()) { // Bucle principal del servidor de directorio TODO: NO SERÍA WHILE TRUE...
			DatagramPacket rcvDatagram = receiveDatagram();
			sendResponse(rcvDatagram);
		}
	}

	private void sendResponse(DatagramPacket pkt) throws IOException {
		/*
		 * DONE: (Boletín MensajesASCII) Construir String partir de los datos recibidos
		 * en el datagrama pkt. A continuación, imprimir por pantalla dicha cadena a
		 * modo de depuración. Después, usar la cadena para construir un objeto
		 * DirMessage que contenga en sus atributos los valores del mensaje. A partir de
		 * este objeto, se podrá obtener los valores de los campos del mensaje mediante
		 * métodos "getter" para procesar el mensaje y consultar/modificar el estado del
		 * servidor.
		 */
		String datosDatagrama = new String(pkt.getData(), 0, pkt.getLength());
//		System.out.println(datosDatagrama);
		DirMessage  mensajeDatagram = DirMessage.fromString(datosDatagrama);
		InetSocketAddress direccionCliente = (InetSocketAddress) pkt.getSocketAddress();
		/*
		 * DONE: Una vez construido un objeto DirMessage con el contenido del datagrama
		 * recibido, obtener el tipo de operación solicitada por el mensaje y actuar en
		 * consecuencia, enviando uno u otro tipo de mensaje en respuesta.
		 */
		String operation = null;
		if(mensajeDatagram!=null) {
			operation = mensajeDatagram.getOperation(); // DONE: Cambiar!
			if(Directory.DIRECTORY_VERBOSE_MODE){
				System.out.println("Recibido mensaje del tipo: " + mensajeDatagram.getOperation());
			}
		}else{
			System.err.println("El servidor acaba de recibir un mensaje NULO");
		}
		
		/*
		 * DONE: (Boletín MensajesASCII) Construir un objeto DirMessage (msgToSend) con
		 * la respuesta a enviar al cliente, en función del tipo de mensaje recibido,
		 * leyendo/modificando según sea necesario el "estado" guardado en el servidor
		 * de directorio (atributos files, etc.). Los atributos del objeto DirMessage
		 * contendrán los valores adecuados para los diferentes campos del mensaje a
		 * enviar como respuesta (operation, etc.)
		 */


		DirMessage msgToSend = null;
		switch (operation) {
		case DirMessageOps.OPERATION_PING: {
			/*
			 * DONE: (Boletín MensajesASCII) Comprobamos si el protocolId del mensaje del
			 * cliente coincide con el nuestro.
			 */
			/*
			 * DONE: (Boletín MensajesASCII) Construimos un mensaje de respuesta que indique
			 * el éxito/fracaso del ping (compatible, incompatible), y lo devolvemos como
			 * resultado del método.
			 */
			String protocolID = mensajeDatagram.getProtocolId();
			if(protocolID.equals(NanoFiles.PROTOCOL_ID)) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_OK);
				direccionesDirPeer.putIfAbsent((InetSocketAddress) pkt.getSocketAddress(), null);
			}else {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_PING_BAD);
			}
			/*
			 * DONE: (Boletín MensajesASCII) Imprimimos por pantalla el resultado de
			 * procesar la petición recibida (éxito o fracaso) con los datos relevantes, a
			 * modo de depuración en el servidor
			 */
			System.out.println("Ping recibido con id de protocolo = " + protocolID);
			break;
		}
		case DirMessageOps.OPERATION_FILELIST_REQUEST: {
			if(IPsPorFichero==null || IPsPorFichero.isEmpty()) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_FILELIST_RESPONSE_BAD);
			}else{	
				msgToSend = new DirMessage(DirMessageOps.OPERATION_FILELIST_RESPONSE_OK, IPsPorFichero, true);
			}
			break;
		}
		case DirMessageOps.OPERATION_USERLIST_REQUEST: {
		    if (direccionesDirPeer == null || direccionesDirPeer.isEmpty()) {
		        msgToSend = new DirMessage(DirMessageOps.OPERATION_USERLIST_RESPONSE_BAD);
		    } else {
		        Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> mapa = new HashMap<>();
		        for (InetSocketAddress direccion : direccionesDirPeer.keySet()) {
		            InetSocketAddress direccionServidor = direccionesDirPeer.get(direccion);
		            if (direccionServidor != null) {
		                // Crear el mapa interno para este peer
		                Map<InetSocketAddress, Set<String>> mapaFicheros = new HashMap<>();
		                // Obtener los archivos del servidor asociado (si existen)
						Set<String> nombresFicheros = new HashSet<>();
		                Set<FileInfo> archivos = ficherosPorIp.get(direccionServidor);
						for(FileInfo archivo : archivos){
							nombresFicheros.add(archivo.fileName);
						}
		                if (archivos != null) {
		                    mapaFicheros.put(direccionServidor, nombresFicheros);
		                } else {
		                    mapaFicheros.put(direccionServidor, new HashSet<>()); // Conjunto vacío si no hay archivos
		                }
		                mapa.put(direccion, mapaFicheros);
		            } else {
		                mapa.put(direccion, new HashMap<>()); // Mapa vacío si no hay servidor asociado
		            }
		        }
		        msgToSend = new DirMessage(DirMessageOps.OPERATION_USERLIST_RESPONSE_OK, mapa);
		    }
		    break;
		}
		 case DirMessageOps.OPERATION_SERVE_REQUEST: {
            Map<FileInfo, Set<InetSocketAddress>> archivos = mensajeDatagram.getInfoArchivos();
            InetSocketAddress direccionDelMensaje = (InetSocketAddress) pkt.getSocketAddress();
            InetSocketAddress direccionServidor = new InetSocketAddress(direccionDelMensaje.getAddress(), mensajeDatagram.getPuertoServe());
            // Actualizar direccionesDirPeer
            direccionesDirPeer.put(direccionDelMensaje, direccionServidor);
            // Actualizar IPsPorFichero y ficherosPorIp
            if (archivos != null && !archivos.isEmpty()) {
                // Actualizar ficherosPorIp
                Set<FileInfo> archivosServidor = ficherosPorIp.getOrDefault(direccionServidor, new HashSet<>()); //REUSABILIDAD DEL CODIGO, POR SI QUIESEMOS "SERVIR" MAS FICHEROS, FUNCIONALIDAD NO IMPLEMENTADA.
                archivosServidor.addAll(archivos.keySet());
                ficherosPorIp.put(direccionServidor, archivosServidor);
                // Actualizar IPsPorFichero
                for (FileInfo archivo : archivos.keySet()) {
                    Set<InetSocketAddress> servidores = IPsPorFichero.getOrDefault(archivo, new HashSet<>());
                    servidores.add(direccionServidor);
                    IPsPorFichero.put(archivo, servidores);
                }
                msgToSend = new DirMessage(DirMessageOps.OPERATION_SERVE_OK);
            } else {
                System.err.println("Archivos nulos o vacíos en OPERATION_SERVE_REQUEST");
                msgToSend = new DirMessage(DirMessageOps.OPERATION_SERVE_BAD);
                direccionesDirPeer.remove(direccionDelMensaje);
            }
            break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD_REQUEST:{
			String cadena = mensajeDatagram.getCadenaDownload();
			//FileInfo[] archivos = FileInfo.lookupFilenameSubstring( NanoFiles.db.getFiles(), cadena);
			Set<FileInfo> archivos = IPsPorFichero.keySet();
			Map<String, Set<InetSocketAddress>> coincidencias = new HashMap<String, Set<InetSocketAddress>>();
			String hash = null;
			for(FileInfo archivo: archivos) {
				if(archivo.fileName.contains(cadena)) {
					hash = archivo.fileHash;
					if(coincidencias.containsKey(hash)){
						coincidencias.get(hash).addAll(IPsPorFichero.get(archivo));
					}else{
						coincidencias.put(hash, IPsPorFichero.get(archivo));
					}
				}
			}
			if(coincidencias.size()>1) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_AMBIGUOUS, coincidencias.size());
			}else if(coincidencias.size()==1) {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_OK, coincidencias.get(hash));
			}else {
				msgToSend = new DirMessage(DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_NOT_FOUND);
			}
			break;
		}
		case DirMessageOps.OPERATION_QUIT: {
            InetSocketAddress direccionDelMensaje = (InetSocketAddress) pkt.getSocketAddress();
            if (direccionesDirPeer.containsKey(direccionDelMensaje)) {
                InetSocketAddress direccionServidor = direccionesDirPeer.get(direccionDelMensaje);
                direccionesDirPeer.remove(direccionDelMensaje);
                // Eliminar archivos del servidor de IPsPorFichero
                Iterator<Map.Entry<FileInfo, Set<InetSocketAddress>>> iterator = IPsPorFichero.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<FileInfo, Set<InetSocketAddress>> entry = iterator.next();
                    Set<InetSocketAddress> servidores = entry.getValue();
                    if (servidores.contains(direccionServidor)) {
                        servidores.remove(direccionServidor);
                        if (servidores.isEmpty()) {
                            iterator.remove();
                        }
                    }
                }
                // Eliminar archivos del servidor de ficherosPorIp
                ficherosPorIp.remove(direccionServidor);
                msgToSend = new DirMessage(DirMessageOps.OPERATION_QUIT_OK);
            } else {
                msgToSend = new DirMessage(DirMessageOps.OPERATION_QUIT_BAD);
            }
            break;
		}
		default:
			System.err.println("Unexpected message operation: \"" + operation + "\"");
			System.exit(-1);
		}

		/*
		 * DONE: (Boletín MensajesASCII) Convertir a String el objeto DirMessage
		 * (msgToSend) con el mensaje de respuesta a enviar, extraer los bytes en que se
		 * codifica el string y finalmente enviarlos en un datagrama
		 */
		if(msgToSend!=null) {
			String msgToSendStr = msgToSend.toString();
			if(Directory.DIRECTORY_VERBOSE_MODE){
				System.out.println("Enviando mensaje del tipo: " + msgToSend.getOperation());
			}
			DatagramPacket msgToSendPkt = new DatagramPacket(msgToSendStr.getBytes(), msgToSendStr.length(), direccionCliente);
			socket.send(msgToSendPkt);
		}

	}
}
