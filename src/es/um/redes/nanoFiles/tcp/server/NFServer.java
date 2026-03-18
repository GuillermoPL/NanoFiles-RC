package es.um.redes.nanoFiles.tcp.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.tcp.client.NFDownloadThread;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.util.FileDatabase;
import es.um.redes.nanoFiles.util.FileInfo;




public class NFServer implements Runnable {
	public static final int DEFAULT_PORT = 10000;
	public int port = 0; //Añadido atributo. Mejora puerto efímero.
	private ServerSocket serverSocket = null; //sirve para modelar tambien si el sv está activo (not null) y el socket no cerrado...
	//upload 
	private static Map<Socket, FileOutputStream> uploadingFiles = new ConcurrentHashMap<>();
	private static Map<Socket, String> uploadingFileNames = new ConcurrentHashMap<>();

	public NFServer() throws IOException {
		this(DEFAULT_PORT);
	}

	public NFServer(int port) throws IOException {
		/*
		 * DONE: (Boletín SocketsTCP) Crear una direción de socket a partir del puerto
		 * especificado (PORT)
		 */
		this.port = port;
		InetSocketAddress serverSocketAddress = new InetSocketAddress(port);
		/*
		 * DONE: (Boletín SocketsTCP) Crear un socket servidor y ligarlo a la dirección
		 * de socket anterior
		 */
		serverSocket = new ServerSocket();
		serverSocket.bind(serverSocketAddress);
	}
	public int getPuerto() {
		return port;
	}
	/**
	 * Método para ejecutar el servidor de ficheros en primer plano. Sólo es capaz
	 * de atender una conexión de un cliente. Una vez se lanza, ya no es posible
	 * interactuar con la aplicación.
	 * 
	 */
	public void test() {
		if (serverSocket == null || !serverSocket.isBound()) {
			System.err.println(
					"[fileServerTestMode] Failed to run file server, server socket is null or not bound to any port");
			return;
		} else {
			System.out
					.println("[fileServerTestMode] NFServer running on " + serverSocket.getLocalSocketAddress() + ".");
		}

		while (serverSocket!=null && !serverSocket.isClosed()) {
			/*
			 * DONE: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
			 * otros peers que soliciten descargar ficheros.
			 */
			/*
			 * DONE: (Boletín SocketsTCP) Tras aceptar la conexión con un peer cliente, la
			 * comunicación con dicho cliente para servir los ficheros solicitados se debe
			 * implementar en el método serveFilesToClient, al cual hay que pasarle el
			 * socket devuelto por accept.
			 */
			boolean connectionOk = false;
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				connectionOk= true;
			}catch(IOException e) {
				System.err.println("Connection refused:" + e.getMessage());
			}
			if(connectionOk) {
				try {
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
					//ejs 1-6
					int integerReceived = dis.readInt();
					System.out.println("Integer received: " + integerReceived);
					int integerToSend = integerReceived+1;
					System.out.println("Sending... " + integerToSend);
					dos.writeInt(integerToSend);
					//ej 7 
					/*PeerMessage msgIn = PeerMessage.readMessageFromInputStream(dis);
					System.out.println("Message received: " + PeerMessageOps.opcodeToOperation(msgIn.getOpcode()));
					PeerMessage msgOut;
					if(msgIn.getOpcode()==PeerMessageOps.OPCODE_DOWNLOAD_FILE) {
						msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_METADATA);
						msgOut.setFileSize(6128412);
						msgOut.setFileHash("5c768e1b1fca4d0b331364c8cb92a108c6249255");
						msgOut.writeMessageToOutputStream(dos);
					}else {
						msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND);
					}*/
					socket.close();
				}catch(IOException e) {
					System.err.println("Communication exception:" + e.getMessage());
				}
			}

		}
	}

	/**
	 * Método que ejecuta el hilo principal del servidor en segundo plano, esperando
	 * conexiones de clientes.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		/*
		 * DONE: (Boletín SocketsTCP) Usar el socket servidor para esperar conexiones de
		 * otros peers que soliciten descargar ficheros
		 */
		/*
		 * DONE: (Boletín SocketsTCP) Al establecerse la conexión con un peer, la
		 * comunicación con dicho cliente se hace en el método
		 * serveFilesToClient(socket), al cual hay que pasarle el socket devuelto por
		 * accept
		 */
		/*
		 * DONE: (Boletín TCPConcurrente) Crear un hilo nuevo de la clase
		 * NFServerThread, que llevará a cabo la comunicación con el cliente que se
		 * acaba de conectar, mientras este hilo vuelve a quedar a la escucha de
		 * conexiones de nuevos clientes (para soportar múltiples clientes). Si este
		 * hilo es el que se encarga de atender al cliente conectado, no podremos tener
		 * más de un cliente conectado a este servidor.
		 */
		while(serverSocket!=null && !serverSocket.isClosed()) {
			boolean connectionOk = false;
			Socket socket = null;
			try {
				socket = serverSocket.accept();
				connectionOk = true;
			}catch(IOException e) {
				System.err.println("Connection refused: " + e.getMessage());
			}
			if(connectionOk) {
				System.out.println("New client connected: " + socket.getInetAddress().toString() + ":" + socket.getPort());
				NFServerThread connectionThread = new NFServerThread(socket);
				connectionThread.start();
			}
		}
	}
	/*
	 * DONE: (Boletín SocketsTCP) Añadir métodos a esta clase para: 1) Arrancar el
	 * servidor en un hilo nuevo que se ejecutará en segundo plano 2) Detener el
	 * servidor (stopserver) 3) Obtener el puerto de escucha del servidor etc.
	 */
	public void startServer() {
		new Thread(this).start();
	}
	
	public void stopServer() {
		if(isOn()) {
			try {	
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			serverSocket = null;
		}
	}
	public boolean isOn(){
		return serverSocket!=null;
	}
	public int getPort() {
		if(serverSocket != null) {
			return serverSocket.getLocalPort();
		}else{
			return 0;
		}
	}


	/**
	 * Método de clase que implementa el extremo del servidor del protocolo de
	 * transferencia de ficheros entre pares.
	 * 
	 * @param socket El socket para la comunicación con un cliente que desea
	 *               descargar ficheros.
	 */





				/*
				 * DONE: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
				 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
				 * compartidos. Los ficheros compartidos se pueden obtener con
				 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
				 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
				 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
				 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
				 * de su hash completo.
				 */
public static void serveFilesToClient(Socket socket) {
    try {
		/*
		 * DONE: (Boletín SocketsTCP) Crear dis/dos a partir del socket
		 */
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
		/*
		 * DONE: (Boletín SocketsTCP) Mientras el cliente esté conectado, leer mensajes
		 * de socket, convertirlo a un objeto PeerMessage y luego actuar en función del
		 * tipo de mensaje recibido, enviando los correspondientes mensajes de
		 * respuesta.
		 */
        while (socket != null && !socket.isClosed()) {
            PeerMessage msgOut = null;
            PeerMessage msgIn = null;

            try {
                //Leer mensaje entrante
                msgIn = PeerMessage.readMessageFromInputStream(dis);
                
                //Verificar si se alcanzó el fin del flujo (cliente cerró conexión)
                if (msgIn == null || msgIn.getOpcode() == PeerMessageOps.OPCODE_INVALID_CODE) {
					if(NFDownloadThread.DOWNLOAD_DEBUG_MODE){
                    	System.out.println("Client closed connection gracefully.");
					}
                    break; //Salir del bucle si el cliente cerró la conexión
                }

                System.out.println("Message received: " + PeerMessageOps.opcodeToOperation(msgIn.getOpcode()));
				/*
				 * DONE: (Boletín SocketsTCP) Para servir un fichero, hay que localizarlo a
				 * partir de su hash (o subcadena) en nuestra base de datos de ficheros
				 * compartidos. Los ficheros compartidos se pueden obtener con
				 * NanoFiles.db.getFiles(). Los métodos lookupHashSubstring y
				 * lookupFilenameSubstring de la clase FileInfo son útiles para buscar ficheros
				 * coincidentes con una subcadena dada del hash o del nombre del fichero. El
				 * método lookupFilePath() de FileDatabase devuelve la ruta al fichero a partir
				 * de su hash completo.
				 */
                switch (msgIn.getOpcode()) {
                    case PeerMessageOps.OPCODE_DOWNLOAD_FILE: //Opcode 1
                        FileInfo[] matchingFiles = FileInfo.lookupFilenameSubstring(NanoFiles.db.getFiles(), msgIn.getFileName());
                        if (matchingFiles == null || matchingFiles.length == 0) {
                            msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_NOT_FOUND); //Opcode 4
                        } else if (matchingFiles.length == 1) {
                            System.out.println("File found: " + matchingFiles[0].fileName + ", size: " + matchingFiles[0].fileSize +", hash: " + matchingFiles[0].fileHash);
                            msgOut = new PeerMessage(PeerMessageOps.OPCODE_FILE_METADATA, matchingFiles[0].fileSize, matchingFiles[0].fileHash); //Opcode 2
                        } else {
                            msgOut = new PeerMessage(PeerMessageOps.OPCODE_AMBIGUOUS); //Opcode 3
                        }
                        break;

                    case PeerMessageOps.OPCODE_CHUNK_REQUEST: //Opcode 5
                        String fileHash = msgIn.getFileHash();
                        int start = msgIn.getChunkInicio();
                        int end = msgIn.getChunkFin();
                        String filePath = NanoFiles.db.lookupFilePath(fileHash);
                        if (filePath == null) {
                            msgOut = new PeerMessage(PeerMessageOps.OPCODE_CHUNK_NOT_FOUND); //Opcode 8
                            break;
                        }
                        try {
                            File file = new File(filePath);
                            int chunkLength = end - start + 1;
                            if (start < 0 || end < start || end >= file.length()) {
                                msgOut = new PeerMessage(PeerMessageOps.OPCODE_END_OF_FILE); //Opcode 7
                                break;
                            }
                            byte[] chunkData = new byte[chunkLength];
                            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                                raf.seek(start);
                                int bytesRead = raf.read(chunkData, 0, chunkLength);
                                if (bytesRead == -1) {
                                    msgOut = new PeerMessage(PeerMessageOps.OPCODE_END_OF_FILE); //Opcode 7
                                } else {
                                    if (bytesRead < chunkLength) {
                                        byte[] trimmedChunk = new byte[bytesRead];
                                        System.arraycopy(chunkData, 0, trimmedChunk, 0, bytesRead);
                                        chunkData = trimmedChunk;
                                    }
									if(NFDownloadThread.DOWNLOAD_DEBUG_MODE){
                                    	System.out.println("Sending chunk [" + start + "-" + end + "]: ");
									}
                                    msgOut = new PeerMessage(PeerMessageOps.OPCODE_CHUNK_RESPONSE, start, chunkData); //Opcode 6
                                }
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading chunk for hash " + fileHash + ": " + e.getMessage());
                            msgOut = new PeerMessage(PeerMessageOps.OPCODE_CHUNK_NOT_FOUND); //Opcode 8
                        }
                        break;

                    case PeerMessageOps.OPCODE_INVALID_CODE: //Opcode 0
                        break;
					case PeerMessageOps.OPCODE_UPLOAD_REQUEST:
					   //Verificar si ya existe el fichero
					   FileInfo[] existingFiles = NanoFiles.db.getFiles();
					   boolean exists = false;
					   for (FileInfo info : existingFiles) {
					       if (info.fileHash.equals(msgIn.getFileHash()) || info.fileName.equals(msgIn.getFileName())) {
					           exists = true;
					           break;
					       }
					   }
					   if (exists) {
					       msgOut = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_REJECTED);
					       break;
					   } else {
					       msgOut = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_ACCEPTED);
					       //Preparar almacenamiento en disco
					       String newFilePath = NanoFiles.sharedDirname + File.separator + msgIn.getFileName();
					       FileOutputStream fos = new FileOutputStream(newFilePath);
					       uploadingFiles.put(socket, fos); //Guardar el flujo de salida asociado a este socket
					       uploadingFileNames.put(socket, msgIn.getFileName()); //Guardar nombre para registrar al terminar
					   }
					   break;
					case PeerMessageOps.OPCODE_UPLOAD_DATA:
					   FileOutputStream fileOut = uploadingFiles.get(socket);
					   if (fileOut != null) {
					       fileOut.write(msgIn.getChunk());
					   }
					   break;
					case PeerMessageOps.OPCODE_UPLOAD_COMPLETE:
						FileOutputStream finishedOut = uploadingFiles.remove(socket);
						String uploadedFileName = uploadingFileNames.remove(socket);
						if (finishedOut != null) {
					       	try {
    							finishedOut.close();
							} catch (IOException e) {
    							System.err.println("Error closing file output stream: " + e.getMessage());
							}
					       //Actualizar la bd
					       NanoFiles.db = new FileDatabase(NanoFiles.sharedDirname);
					       System.out.println("* File uploaded successfully: " + uploadedFileName);
					   }
					   break;
                    default:
                        System.err.println("Unknown opcode received: " + msgIn.getOpcode());
                        msgOut = new PeerMessage(PeerMessageOps.OPCODE_INVALID_CODE); //Opcode 0
                }
                //Enviar mensaje de respuesta
                if (msgOut != null) {
                    try {
                        msgOut.writeMessageToOutputStream(dos);
                        dos.flush(); //Asegurar que los datos se envíen
                    } catch (IOException e) {
                        System.err.println("Error sending response: " + e.getMessage());
                        break; //Salir si no se puede enviar la respuesta
                    }
                }

            } catch (EOFException e) {
				if(NFDownloadThread.DOWNLOAD_DEBUG_MODE){
                	System.out.println("Client closed connection (EOF).");
				}
                break;
            } catch (SocketException e) {
                System.out.println("Client connection reset: " + e.getMessage());
                break;
            } catch (IOException e) {
                System.err.println("IOException while processing message: " + e.getMessage());
                e.printStackTrace(); //Imprimir stack trace para depuración
                break;
            }
        }

    } catch (IOException e) {
        System.err.println("Error initializing streams: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try {
            socket.close();
		if(NFDownloadThread.DOWNLOAD_DEBUG_MODE){
            System.out.println("Socket closed.");
		}
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }
}




}