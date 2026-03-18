package es.um.redes.nanoFiles.udp.message;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import es.um.redes.nanoFiles.application.NanoFiles;
import es.um.redes.nanoFiles.util.FileInfo;

/**
 * Clase que modela los mensajes del protocolo de comunicación entre pares para
 * implementar el explorador de ficheros remoto (servidor de ficheros). Estos
 * mensajes son intercambiados entre las clases DirectoryServer y
 * DirectoryConnector, y se codifican como texto en formato "campo:valor".
 * 
 * @author rtitos
 *
 */
public class DirMessage {
	public static final int PACKET_MAX_SIZE = 65507; //65535 - 8 (UDP header) - 20 (IP header)

	private static final char DELIMITER = ':'; //Define el delimitador
	private static final char END_LINE = '\n'; //Define el carácter de fin de línea

	/**
	 * Nombre del campo que define el tipo de mensaje (primera línea)
	 */
	private static final String FIELDNAME_OPERATION = "operation";

	/*
	 * DONE: (Boletín MensajesASCII) Definir de manera simbólica los nombres de
	 * todos los campos que pueden aparecer en los mensajes de este protocolo
	 * (formato campo:valor)
	 */
	private static final String FIELDNAME_PROTOCOL = "protocol";
	private static final String FIELDNAME_NUMERO_ARCHIVOS = "num archivos";
	private static final String FIELDNAME_ARCHIVOS = "archivo";
	private static final String FIELDNAME_PUERTO_SERVE = "puerto";
	private static final String FIELDNAME_CADENA_DOWNLOAD = "cadena";
	private static final String FIELDNAME_COINCIDENCIAS_AMBIGUO_DOWNLOAD = "coincidencias";
	private static final String FIELDNAME_DIRECCIONES_DOWNLOAD = "direcciones";
	private static final String FIELDNAME_DIRECCIONES_DOWNLOAD_NUM = "num direcciones";
	private static final String FIELDNAME_USERLIST = "userlist";
	/**
	 * Tipo del mensaje, de entre los tipos definidos en PeerMessageOps.
	 */
	private String operation = DirMessageOps.OPERATION_INVALID;
	/**
	 * Identificador de protocolo usado, para comprobar compatibilidad del directorio.
	 */
	private String protocolId;
	/*
	 * DONE: (Boletín MensajesASCII) Crear un atributo correspondiente a cada uno de
	 * los campos de los diferentes mensajes de este protocolo.
	 */

	private Map<FileInfo, Set<InetSocketAddress>> archivos;
	private int puertoServe;
	private String cadenaDownload;
	private Set<InetSocketAddress> direcciones;
	private int coincidenciasAmbiguedad;
	private Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> userList;

	public DirMessage(String op) {
		operation = op;
	}


	/*
	 * DONE: (Boletín MensajesASCII) Crear diferentes constructores adecuados para
	 * construir mensajes de diferentes tipos con sus correspondientes argumentos
	 * (campos del mensaje)
	 */

	public DirMessage(String op, String cadena) {
		this(op);
		if(op.equals(DirMessageOps.OPERATION_PING)) {
			protocolId = cadena;
		}else if(op.equals(DirMessageOps.OPERATION_DOWNLOAD_REQUEST)){
			cadenaDownload = cadena;
		}else {
			throw new RuntimeException(
					"DirMessage: constructor called for message of unexpected type (" + op + ")");
		}
		
	}
	
	public DirMessage(String op, Map<FileInfo, Set<InetSocketAddress>> archivos, boolean isArchivos) {
		this(op);
		this.archivos = archivos;
	}

	public DirMessage(String op,  Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> userList) {
		this(op);
		this.userList = userList;
	}
	
	public DirMessage(String op, int coincidencias) {
		this(op);
		this.coincidenciasAmbiguedad = coincidencias;
	}
	
	
	public DirMessage(String op, Set<InetSocketAddress> direcciones) {
		this(op);
		this.direcciones = direcciones;
	}
	
	public DirMessage(String op, Map<FileInfo, Set<InetSocketAddress>> archivos, int puerto) {
		this(op, archivos, true);
		this.puertoServe = puerto;
	}


	public String getOperation() {
		return operation;
	}
	

	/*
	 * DONE: (Boletín MensajesASCII) Crear métodos getter y setter para obtener los
	 * valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	
	public void setProtocolID(String protocolIdent) {
		if (!operation.equals(DirMessageOps.OPERATION_PING)) {
			throw new RuntimeException(
					"DirMessage: setProtocolId called for message of unexpected type (" + operation + ")");
		}
		protocolId = protocolIdent;
	}

	public String getProtocolId() {
		return protocolId;
	}
	

	public String getCadenaDownload() {
		return cadenaDownload;
	}

	public Map<FileInfo, Set<InetSocketAddress>> getInfoArchivos() {
		return archivos;
	}
	
	public int getCoincidenciasAmbiguo() {
		return coincidenciasAmbiguedad;
	}
	
	public Set<InetSocketAddress> getDirecciones() {
		return direcciones;
	}
	
	public Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> getUserInfo() {
		return  userList;
	}
	
	//setters coincidencias y direcciones
	public void setCadenaDownload(String cadena) {
		if (!operation.equals(DirMessageOps.OPERATION_DOWNLOAD_REQUEST)) {
			throw new RuntimeException(
					"DirMessage: setCadenaDownload called for message of unexpected type (" + operation + ")");
		}
		cadenaDownload = cadena;
	}
	
	public void setCoincidencias(int coincidencias) {
		if (!operation.equals(DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_AMBIGUOUS)) {
			throw new RuntimeException(
					"DirMessage: setCadenaDownload called for message of unexpected type (" + operation + ")");
		}
		coincidenciasAmbiguedad = coincidencias;
	}
	
	public void setDirecciones(Set<InetSocketAddress> direcciones) {
		if (!operation.equals(DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_OK)) {
			throw new RuntimeException(
					"DirMessage: setCadenaDownload called for message of unexpected type (" + operation + ")");
		}
		this.direcciones = direcciones;
	}

	public void setInfoArchivos(Map<FileInfo, Set<InetSocketAddress>> archivos) {
		if (!(operation.equals(DirMessageOps.OPERATION_FILELIST_RESPONSE_OK) || (operation.equals(DirMessageOps.OPERATION_FILELIST_RESPONSE_BAD)) ||operation.equals(DirMessageOps.OPERATION_SERVE_REQUEST))) {
			throw new RuntimeException(
					"DirMessage: setInfoArchivos called for message of unexpected type (" + operation + ")");
		}
		this.archivos = archivos;
	}
	
	public void addArchivo(FileInfo archivo, Set<InetSocketAddress> dir) {
		if (!(operation.equals(DirMessageOps.OPERATION_SERVE_REQUEST) || operation.equals(DirMessageOps.OPERATION_FILELIST_RESPONSE_OK))) {
			throw new RuntimeException(
					"DirMessage: addArchivo called for message of unexpected type (" + operation + ")");
		}
		archivos.put(archivo, dir);
	}
	
	public void addDireccion(InetSocketAddress direccion) {
		if (!(operation.equals(DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_OK))) {
			throw new RuntimeException(
					"DirMessage: addDireccion called for message of unexpected type (" + operation + ")");
		}
		direcciones.add(direccion);
	}
	
	public int getPuertoServe() {
		return puertoServe;
	}
	
	public void setPuertoServe(int puerto) {
		if (!operation.equals(DirMessageOps.OPERATION_SERVE_REQUEST)) {
			throw new RuntimeException(
					"DirMessage: setPuertoServe called for message of unexpected type (" + operation + ")");
		}
		this.puertoServe = puerto;
	}
	
	public void setUserInfo( Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> info) {
		if (!operation.equals(DirMessageOps.OPERATION_USERLIST_RESPONSE_OK)) {
			throw new RuntimeException(
					"DirMessage: setUserInfo called for message of unexpected type (" + operation + ")");
		}
		this.userList = info;
	}
	/**
	 * Método que convierte un mensaje codificado como una cadena de caracteres, a
	 * un objeto de la clase PeerMessage, en el cual los atributos correspondientes
	 * han sido establecidos con el valor de los campos del mensaje.
	 * 
	 * @param message El mensaje recibido por el socket, como cadena de caracteres
	 * @return Un objeto PeerMessage que modela el mensaje recibido (tipo, valores,
	 *         etc.)
	 */
	public static DirMessage fromString(String message) {
		/*
		 * DONE: (Boletín MensajesASCII) Usar un bucle para parsear el mensaje línea a
		 * línea, extrayendo para cada línea el nombre del campo y el valor, usando el
		 * delimitador DELIMITER, y guardarlo en variables locales.
		 */

		//System.out.println("DirMessage read from socket:");
		 //System.out.println(message);
		String[] lines = message.split(END_LINE + "");
		//Local variables to save data during parsing
		DirMessage m = null;
		for (String line : lines) {
			int idx = line.indexOf(DELIMITER); //Posición del delimitador
			String fieldName = line.substring(0, idx).toLowerCase(); //minúsculas
			String value = line.substring(idx + 1); //trim fuera
			switch (fieldName) {
			case FIELDNAME_OPERATION: {
				assert (m == null);
				m = new DirMessage(value);
				break;
			}
			case FIELDNAME_PROTOCOL: {
				m.setProtocolID(value);
				break;
			}
			case FIELDNAME_DIRECCIONES_DOWNLOAD_NUM: {
				Set<InetSocketAddress> archivos = new HashSet<InetSocketAddress>(Integer.parseInt(value));
				m.setDirecciones(archivos);
				break;
			}
			case FIELDNAME_NUMERO_ARCHIVOS: {
				Map<FileInfo, Set<InetSocketAddress>> archivos = new HashMap<FileInfo, Set<InetSocketAddress>>();
				m.setInfoArchivos(archivos);
				break;
			}
			case FIELDNAME_ARCHIVOS: {
			    if (value.length() < 86) {
			        throw new IllegalArgumentException(
			            "Formato inválido: La cadena debe tener al menos 86 caracteres. Longitud actual: " + value.length() + " value: " + value
			        );
			    }

			    //Extraer los campos básicos
			    String name = value.substring(0, 30).trim();  //Extrae el nombre del archivo (30 caracteres)
			    String size_str = value.substring(30, 40).trim();  //Extrae el tamaño (10 caracteres)
			    String hash = value.substring(40, 86).trim();  //Extrae el hash (45 caracteres con el padding)
			    Long fileSize;
			    try {
			        fileSize = Long.parseLong(size_str);
			    } catch (NumberFormatException e) {
			        throw new IllegalArgumentException("Error al convertir fileSize a long.");
			    }
			    //Crear el objeto FileInfo
			    FileInfo nuevo = new FileInfo(hash, name, fileSize, NanoFiles.DEFAULT_SHARED_DIRNAME);
			    //Procesar los servidores si existe el campo de servidores 
			    if (m.getOperation().equals(DirMessageOps.OPERATION_FILELIST_RESPONSE_OK)) {
			        //Extraer la parte de servidores (todo lo que viene después de los 86 caracteres iniciales)
			        String servers = value.substring(86).trim();  //Extrae los servidores (el resto de la cadena)
			        //Si hay servidores, procesarlos
			        Set<InetSocketAddress> servidores = new HashSet<>();
			        if (!servers.isEmpty()) {
			            //Eliminar corchetes si existen
			            String cleanedServers = servers.startsWith("[") && servers.endsWith("]") ? servers.substring(1, servers.length() - 1) : servers;
			            //Dividir por comas y procesar cada dirección
			            for (String server : cleanedServers.split(",\\s*")) {
			                try {
			                    String trimmedServer = server.trim();
			                    if (!trimmedServer.isEmpty()) {
			                        //Eliminar barra inicial si existe
			                        if (trimmedServer.startsWith("/")) {
			                            trimmedServer = trimmedServer.substring(1);
			                        }
			                        //Parsear dirección individual
			                        int lastColon = trimmedServer.lastIndexOf(':');
			                        if (lastColon == -1) {
			                            throw new IllegalArgumentException("Formato de servidor inválido (falta puerto): " + server);
			                        }
			                        String hostPart = trimmedServer.substring(0, lastColon);
			                        String portPart = trimmedServer.substring(lastColon + 1);
			                        //Manejar direcciones no resueltas
			                        if (hostPart.endsWith("/<unresolved>")) {
			                            String hostname = hostPart.substring(0, hostPart.length() - "/<unresolved>".length());
			                            servidores.add(InetSocketAddress.createUnresolved(hostname, Integer.parseInt(portPart)));
			                        } else {
			                            //Manejar IPv6 si fuera necesario (eliminar corchetes)
			                            if (hostPart.startsWith("[") && hostPart.endsWith("]")) {
			                                hostPart = hostPart.substring(1, hostPart.length() - 1);
			                            }
			                            servidores.add(new InetSocketAddress(InetAddress.getByName(hostPart), Integer.parseInt(portPart)));
			                        }
			                    }
			                } catch (UnknownHostException | NumberFormatException e) {
			                    throw new IllegalArgumentException("Error al parsear dirección del servidor: " + server, e);
			                }
			            }
			        }

			        //Añadir el archivo y sus servidores a la colección
			        m.addArchivo(nuevo, servidores);
			    } else {
			        m.addArchivo(nuevo, null);
			    }
			    break;
			}
			case FIELDNAME_CADENA_DOWNLOAD: {
				m.setCadenaDownload(value);
				break;
			}
			case FIELDNAME_PUERTO_SERVE: {
				m.setPuertoServe(Integer.parseInt(value));
				break;
			}
            case FIELDNAME_USERLIST: {
                try {
                    //Reemplazar \n internos por un delimitador seguro (por ejemplo, ;)
                    String cleanedValue = value.replace("\n", ";");
                    m.setUserInfo(MapSerializer.mapFromString(cleanedValue));
                } catch (IllegalArgumentException e) {
                    System.err.println("Error deserializando userlist: " + value + " - " + e.getMessage());
                    throw e;
                }
                break;
            }
			case FIELDNAME_DIRECCIONES_DOWNLOAD: {
				 try {
					//	System.out.println("Deserializando: " + value);
				        //Si la dirección comienza con '/', la quitamos
				        if (value.startsWith("/")) {
				            value = value.substring(1); //Elimina el primer carácter '/'
				        }

				        //Dividimos la IP y el puerto por el delimitador ":"
				        String[] partes = value.split(":");
				        String ipStr = partes[0].trim(); //IP
				        int puerto = Integer.parseInt(partes[1].trim()); //Puerto
				        
				        //Obtenemos la dirección InetAddress a partir de la IP
				        InetAddress ip = InetAddress.getByName(ipStr);
				        
				        //Creamos el InetSocketAddress con la IP y el puerto
				        InetSocketAddress nuevo = new InetSocketAddress(ip, puerto);
				        
				        //Añadimos la dirección al mensaje
						//System.out.println("Deserializado: " + nuevo);
				        m.addDireccion(nuevo);
				    } catch (Exception e) {
				        throw new IllegalArgumentException("Error al parsear dirección IP y puerto: " + value, e);
				    }
				    break;
			}
			case FIELDNAME_COINCIDENCIAS_AMBIGUO_DOWNLOAD:
				try {
    				m.setCoincidencias(Integer.parseInt(value));
				} catch (NumberFormatException e) {
    				System.err.println("Error al convertir a entero: " + value);
    				m.setCoincidencias(-1);
				}
				break;
			default:
				System.err.println("PANIC: DirMessage.fromString - message with unknown field name " + fieldName);
				System.err.println("Message was:\n" + message);
				System.exit(-1);
			}
		}
		return m;
	}

	/**
	 * Método que devuelve una cadena de caracteres con la codificación del mensaje
	 * según el formato campo:valor, a partir del tipo y los valores almacenados en
	 * los atributos.
	 * 
	 * @return La cadena de caracteres con el mensaje a enviar por el socket.
	 */
	public String toString() {

		StringBuffer sb = new StringBuffer();
		sb.append(FIELDNAME_OPERATION + DELIMITER + operation + END_LINE); //Construimos el campo
		/*
		 * DONE: (Boletín MensajesASCII) En función de la operación del mensaje, crear
		 * una cadena la operación y concatenar el resto de campos necesarios usando los
		 * valores de los atributos del objeto.
		 */
		switch(operation) {
		case DirMessageOps.OPERATION_PING: {
			sb.append(FIELDNAME_PROTOCOL + DELIMITER + protocolId + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_FILELIST_RESPONSE_OK: {
			sb.append(FIELDNAME_NUMERO_ARCHIVOS + DELIMITER + getInfoArchivos().size() + END_LINE);
			for(FileInfo archivo : archivos.keySet()) {
				sb.append(FIELDNAME_ARCHIVOS + DELIMITER + archivo.toStringWithServers(archivos.get(archivo)) + END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_SERVE_REQUEST:
		{
			sb.append(FIELDNAME_PUERTO_SERVE + DELIMITER + getPuertoServe() + END_LINE);
			sb.append(FIELDNAME_NUMERO_ARCHIVOS + DELIMITER + getInfoArchivos().size() + END_LINE);
			for(FileInfo archivo : archivos.keySet()) {
				sb.append(FIELDNAME_ARCHIVOS + DELIMITER + archivo.toString() +  END_LINE);
			}
			break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD_REQUEST: {
			sb.append(FIELDNAME_CADENA_DOWNLOAD + DELIMITER + cadenaDownload + END_LINE);
			break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_OK:{
			sb.append(FIELDNAME_DIRECCIONES_DOWNLOAD_NUM + DELIMITER + direcciones.size() + END_LINE);
			for(InetSocketAddress direccion : direcciones){
				//System.out.println(direccion);
				sb.append(FIELDNAME_DIRECCIONES_DOWNLOAD + DELIMITER + direccion + END_LINE);
				//System.out.println(sb.toString());
			}
			break;
		}
		case DirMessageOps.OPERATION_DOWNLOAD_RESPONSE_AMBIGUOUS:{
			sb.append(FIELDNAME_COINCIDENCIAS_AMBIGUO_DOWNLOAD + DELIMITER + coincidenciasAmbiguedad + END_LINE);
			break;
		}
        case DirMessageOps.OPERATION_USERLIST_RESPONSE_OK: {
            String userListStr = MapSerializer.mapToString(getUserInfo()).replace("\n", ";");
            //System.out.println("Serializando userlist: [" + userListStr + "]");
            sb.append(FIELDNAME_USERLIST + DELIMITER + userListStr + END_LINE);
            break;
        }
		default:	
		}
	//	if(!(operation!=DirMessageOps.OPERATION_USERLIST_RESPONSE_OK)){
			sb.append(END_LINE); //Marcamos el final del mensaje
	//	}
		
		return sb.toString();
	}

}
