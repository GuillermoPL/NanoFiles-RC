package es.um.redes.nanoFiles.logic;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import es.um.redes.nanoFiles.tcp.client.NFConnector;
import es.um.redes.nanoFiles.tcp.client.NFDownloadThread;
import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;
import es.um.redes.nanoFiles.application.NanoFiles;



import es.um.redes.nanoFiles.tcp.server.NFServer;
import es.um.redes.nanoFiles.util.FileDatabase;
import es.um.redes.nanoFiles.util.FileDigest;
import es.um.redes.nanoFiles.util.FileInfo;

public class NFControllerLogicP2P {
	/*
	 * DONE: Se necesita un atributo NFServer que actuar�� como servidor de ficheros
	 * de este peer
	 */
	private NFServer fileServer = null;


	protected NFControllerLogicP2P() {
	}

	/**
	 * M��todo para ejecutar un servidor de ficheros en segundo plano. Debe arrancar
	 * el servidor en un nuevo hilo creado a tal efecto.
	 * 
	 * @return Verdadero si se ha arrancado en un nuevo hilo con el servidor de
	 *         ficheros, y est�� a la escucha en un puerto, falso en caso contrario.
	 * 
	 */
	protected boolean startFileServer(int port) { //A��adido par��metro para puerto ef��mero.
		boolean serverRunning = false;
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya est�� en marcha.
		 */
		if (fileServer != null) {
			System.err.println("File server is already running");
		} else {

			/*
			 * DONE: (Bolet��n Servidor TCP concurrente) Arrancar servidor en segundo plano
			 * creando un nuevo hilo, comprobar que el servidor est�� escuchando en un puerto
			 * v��lido (>0), imprimir mensaje informando sobre el puerto de escucha, y
			 * devolver verdadero. Las excepciones que puedan lanzarse deben ser capturadas
			 * y tratadas en este m��todo. Si se produce una excepci��n de entrada/salida
			 * (error del que no es posible recuperarse), se debe informar sin abortar el
			 * programa
			 * 
			 */
			try {
				fileServer = new NFServer(port);
				fileServer.startServer();
			} catch (IOException e) {
				fileServer=null;
				System.err.println("Excepcion de entrada y salida por imposibilidad de bindear el puerto.");
				System.err.println("Probablemente el puerto que pretendes usar ya est�� en uso por otro NanoFiles en tu propia m��quina...");
				e.printStackTrace();
				return false;
			}
			System.out.println("Servidor de archivos escuchando en el puerto: " + fileServer.getPort());
			serverRunning = true;

		}
		return serverRunning;

	}

	protected void testTCPServer() {
		assert (NanoFiles.testModeTCP);
		/*
		 * Comprobar que no existe ya un objeto NFServer previamente creado, en cuyo
		 * caso el servidor ya est�� en marcha.
		 */
		assert (fileServer == null);
		try {

			fileServer = new NFServer();
			/*
			 * (Bolet��n SocketsTCP) Inicialmente, se crear�� un NFServer y se ejecutar�� su
			 * m��todo "test" (servidor minimalista en primer plano, que s��lo puede atender a
			 * un cliente conectado). Posteriormente, se desactivar�� "testModeTCP" para
			 * implementar un servidor en segundo plano, que se ejecute en un hilo
			 * secundario para permitir que este hilo (principal) siga procesando comandos
			 * introducidos mediante el shell.
			 */
			fileServer.test();
			//Este c��digo es inalcanzable: el m��todo 'test' nunca retorna...
		} catch (IOException e1) {
			e1.printStackTrace();
			System.err.println("Cannot start the file server");
			fileServer = null;
		}
	}

	public void testTCPClient() {

		assert (NanoFiles.testModeTCP);
		/*
		 * (Bolet��n SocketsTCP) Inicialmente, se crear�� un NFConnector (cliente TCP)
		 * para conectarse a un servidor que est�� escuchando en la misma m��quina y un
		 * puerto fijo. Despu��s, se ejecutar�� el m��todo "test" para comprobar la
		 * comunicaci��n mediante el socket TCP. Posteriormente, se desactivar��
		 * "testModeTCP" para implementar la descarga de un fichero desde m��ltiples
		 * servidores.
		 */
		/*
		try {
			NFConnector nfConnector = new NFConnector(new InetSocketAddress(NFServer.DEFAULT_PORT));
			nfConnector.test();
		} catch (IOException e) {
			//TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	}

	/**
	 * M��todo para descargar un fichero del peer servidor de ficheros
	 * 
	 * @param serverAddressList       La lista de direcciones de los servidores a
	 *                                los que se conectar��
	 * @param targetFileNameSubstring Subcadena del nombre del fichero a descargar
	 * @param localFileName           Nombre con el que se guardar�� el fichero
	 *                                descargado
	 */
	protected boolean downloadFileFromServers(InetSocketAddress[] serverAddressList, String targetFileNameSubstring, String localFileName, int tamanio) {
	    boolean downloaded = false;
	    if (serverAddressList == null || serverAddressList.length == 0) {
	        System.err.println("* Cannot start download - No list of server addresses provided");
	        return false;
	    }
		//Llevar registro de donde se descarga cada cosa
		ConcurrentHashMap<Integer, InetSocketAddress> mapaDescargas = new ConcurrentHashMap<>();
   		//Validate chunk size
    	if (tamanio <= 0) {
    	    System.err.println("* Invalid chunk size: " + tamanio + ". Chunk size must be greater than 0.");
    	    return false;
    	}
    	if (tamanio < 1024) {
    	    System.err.println("* Chunk size too small: " + tamanio + ". Minimum chunk size is 1024 bytes.");
    	    return false;
    	}
	
	    //Construir la ruta completa del archivo usando NanoFiles.sharedDirname
	    String newFilePath = NanoFiles.sharedDirname + File.separator + localFileName;
	
	    //Verificar si el archivo ya existe localmente
	    File localFile = new File(newFilePath);
	    if (localFile.exists()) {
	        System.err.println("* File already exists: " + newFilePath);
	        return false;
	    }
	
	    //Paso 1: Identificar servidores con el fichero (mismo hash y tama��o)
	    String hash = null;
	    long size = 0;
	    ArrayList<InetSocketAddress> direccionesDisponibles = new ArrayList<>();
	    for (InetSocketAddress direccion : serverAddressList) {
	        try (NFConnector connector = new NFConnector(direccion)) {
	            PeerMessage fileRequest = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FILE, (byte) targetFileNameSubstring.getBytes().length, targetFileNameSubstring);
	            if (NFDownloadThread.DOWNLOAD_DEBUG_MODE) {
	                System.out.println("Sending DOWNLOAD_FILE request to " + direccion + " for file: " + targetFileNameSubstring);
	            }
	            fileRequest.writeMessageToOutputStream(connector.getDos());
	            PeerMessage fileResponse = PeerMessage.readMessageFromInputStream(connector.getDis());
	            if (NFDownloadThread.DOWNLOAD_DEBUG_MODE) {
	                System.out.println("Received response from " + direccion + ": " + PeerMessageOps.opcodeToOperation(fileResponse.getOpcode()));
	            }
	            if (fileResponse.getOpcode() == PeerMessageOps.OPCODE_FILE_METADATA) {
	                if (hash == null) {
	                    hash = fileResponse.getFileHash();
	                    size = fileResponse.getFileSize();
	                    direccionesDisponibles.add(direccion);
	                } else if (fileResponse.getFileHash().equals(hash) && size == fileResponse.getFileSize()) {
	                    direccionesDisponibles.add(direccion);
	                }
	            }
	        } catch (IOException e) {
	            System.err.println("* Error connecting to server " + direccion + ": " + e.getMessage());
	        }
	    }
	
	    if (direccionesDisponibles.isEmpty()) {
	        System.err.println("* No hay servidores v��lidos con el fichero.");
	        return false;
	    } else {
	        System.out.println("Pidiendo el fichero solicitado a los diferentes Peers que disponen de él.");
	    }
		if (size > Integer.MAX_VALUE) {
    		System.out.println("El fichero es demasiado pesado, >2 GiB");
		}
	    //Paso 2: Preparar la descarga paralela
	    byte[] ficheroCompleto = new byte[(int) size]; //Buffer compartido para almacenar el fichero
	    int chunkSize = tamanio; //Tama��o de cada fragmento
	    int chunks = (int) Math.ceil((double) size / chunkSize); //N��mero total de fragmentos
		//limitar máximos hilos a 2 por core.
    	int maxThreads = Math.min(direccionesDisponibles.size()*4, Runtime.getRuntime().availableProcessors() * 4);
    	if (chunks > maxThreads) {
       		System.out.println("* Warning: Too many chunks (" + chunks + "). Adjusting chunk size to limit threads.");
        	chunkSize = (int) Math.ceil((double) size / maxThreads);
        	chunks = (int) Math.ceil((double) size / chunkSize);
   		}
	    Thread[] downloadThreads = new Thread[chunks]; //Un hilo por fragmento
	    boolean[] chunkSuccess = new boolean[chunks]; //Estado de cada fragmento (descargado o no)
	    Object lock = new Object(); //Para sincronizaci��n
	
	    //Paso 3: Crear hilos para descargar fragmentos
	    for (int chunk = 0; chunk < chunks; chunk++) {
	        final int chunkIndex = chunk;
	        final int inicio = chunk * chunkSize;
	        final int fin = (int) Math.min(size - 1, inicio + chunkSize - 1);
			InetSocketAddress servidorAsignado = direccionesDisponibles.get(chunk % direccionesDisponibles.size()); //roundrobin
			downloadThreads[chunk] = new NFDownloadThread(
			    servidorAsignado, // Cambiar servidorParaEsteHilo por servidorAsignado
			    direccionesDisponibles.toArray(new InetSocketAddress[0]), // Pasar todos los servidores
			    hash, chunkIndex, inicio, fin,
			    ficheroCompleto, chunkSuccess, lock,
			    mapaDescargas // Agregar mapaDescargas
			);
    		downloadThreads[chunk].start();
		}
		//Paso 4: Esperar a que todos los hilos terminen
	    for (Thread thread : downloadThreads) {
	        try {
	            thread.join();
	        } catch (InterruptedException e) {
	            System.err.println("* Interrupted while waiting for download threads: " + e.getMessage());
	            return false;
	        }
	    }
	
	    //Paso 5: Verificar que todos los fragmentos se descargaron correctamente
	    for (int i = 0; i < chunks; i++) {
	        if (!chunkSuccess[i]) {
	            System.err.println("* Failed to download chunk " + i + " from assigned server");
	            return false;
	        }
	    }
	
	    //Paso 6: Guardar el fichero localmente
	    try (FileOutputStream fos = new FileOutputStream(localFile)) {
	        fos.write(ficheroCompleto);
	    } catch (IOException e) {
	        System.err.println("* Failed to save file locally: " + e.getMessage());
	        return false;
	    }
	
	    //Paso 7: Verificar el hash del fichero descargado
	    String hashLocal = FileDigest.computeFileChecksumString(newFilePath);
	    if (!hashLocal.equals(hash)) {
	        System.err.println("* ERROR: hash doesnt match.");
	        return false;
	    }
	
		//Paso 8: Resumen de la descarga
		System.out.println("File downloaded successfully: " + newFilePath);
		System.out.println("Size: " + size + " bytes");
		System.out.println("Hash: " + hash);
		
		//Mostrar fragmentos descargados de los servidores
		if (NFDownloadThread.DOWNLOAD_DEBUG_MODE) {
		    System.out.println("Fragmentos descargados de los siguientes servidores:");
		    for (int i = 0; i < chunks; i++) {
		        InetSocketAddress server = mapaDescargas.get(i); //Obtener el servidor asociado al fragmento
		        System.out.println(" - Chunk " + i + " [" + (i * chunkSize) + "-" + Math.min(size - 1, (i * chunkSize) + chunkSize - 1) + "] desde " + server);
		    }
		}
	    NanoFiles.db = new FileDatabase(NanoFiles.sharedDirname); //reconstruimos bd para que salga en myfiles o poder servirlo si decidimos empezar a hacerlo en un futuro.... O uploadearlo a algún sitio..
	    downloaded = true;
	    return downloaded;
	}

	/**
	 * M��todo para obtener el puerto de escucha de nuestro servidor de ficheros
	 * 
	 * @return El puerto en el que escucha el servidor, o 0 en caso de error.
	 */
	protected int getServerPort() {
		int port = 0;
		/*
		 * DONE: Devolver el puerto de escucha de nuestro servidor de ficheros
		 */
		if(fileServer!=null){
			port = fileServer.getPuerto();
		}
		return port;
	}

	/**
	 * M��todo para detener nuestro servidor de ficheros en segundo plano
	 * 
	 */
	protected void stopFileServer() {
		/*
		 * DONE: Enviar se��al para detener nuestro servidor de ficheros en segundo plano
		 */
		if(fileServer!=null){
			fileServer.stopServer();
		}
		
	}

	protected boolean serving() {
		boolean result = false;
		if(fileServer!=null && fileServer.isOn()){
			result = true;
		}
		return result;

	}

	protected boolean uploadFileToServer(FileInfo matchingFile, String uploadToServer, int tamanio) {
	    boolean result = false;
	    InetSocketAddress serverAddress;
   		//Validate chunk size
    	if (tamanio <= 0) {
    	    System.err.println("* Invalid chunk size: " + tamanio + ". Chunk size must be greater than 0.");
    	    return false;
    	}
    	if (tamanio < 1024) {
    	    System.err.println("* Chunk size too small: " + tamanio + ". Minimum chunk size is 1024 bytes.");
    	    return false;
    	}
	    try {
	        String[] parts = uploadToServer.split(":");
	        serverAddress = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
	    } catch (Exception e) {
	        System.err.println("* Invalid server address format: " + uploadToServer);
	        return false;
	    }
	    try (NFConnector connector = new NFConnector(serverAddress)) {
	        File file = new File(matchingFile.filePath);
	        if (!file.exists()) {
	            System.err.println("* File not found locally: " + matchingFile.fileName);
	            return false;
	        }
	
	        //Paso 1: Solicitar permiso de subida
	        PeerMessage uploadRequest = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_REQUEST, file.length(), matchingFile.fileHash, (byte) matchingFile.fileName.length(), matchingFile.fileName);
	        uploadRequest.writeMessageToOutputStream(connector.getDos());
	
	        PeerMessage serverResponse = PeerMessage.readMessageFromInputStream(connector.getDis());
	        if (serverResponse.getOpcode() == PeerMessageOps.OPCODE_UPLOAD_REJECTED) {
	            System.err.println("* Upload rejected by server: file already exists");
	            return false;
	        } else if (serverResponse.getOpcode() != PeerMessageOps.OPCODE_UPLOAD_ACCEPTED) {
	            System.err.println("* Unexpected server response to upload request");
	            return false;
	        }
	
	        //Paso 2: Enviar el fichero troceado
	        int chunkSize = tamanio;
	        try (FileInputStream fis = new FileInputStream(file)) {
	            int bytesRead;
	            byte[] buffer = new byte[chunkSize];
	            int position = 0;
	            while ((bytesRead = fis.read(buffer)) != -1) {
	                byte[] chunk = Arrays.copyOf(buffer, bytesRead);
	                PeerMessage dataMsg = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_DATA, position, chunk);
	                dataMsg.writeMessageToOutputStream(connector.getDos());
	                position += bytesRead;
	            }
	        }
	
	        //Paso 3: Indicar finalizaci��n
	        PeerMessage endMsg = new PeerMessage(PeerMessageOps.OPCODE_UPLOAD_COMPLETE);
	        endMsg.writeMessageToOutputStream(connector.getDos());
	
	        System.out.println("* Upload completed successfully: " + matchingFile.fileName);
	        result = true;
	
	    } catch (IOException e) {
	        System.err.println("* Upload failed: " + e.getMessage());
	    }
	
	    return result;
	}

}
