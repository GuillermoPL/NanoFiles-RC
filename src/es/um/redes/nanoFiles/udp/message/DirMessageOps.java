package es.um.redes.nanoFiles.udp.message;

public class DirMessageOps {

	/*
	 * DONE: (Boletín MensajesASCII) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con el
	 * directorio (valores posibles del campo "operation").
	 */
	public static final String OPERATION_INVALID = "invalid_operation";
	public static final String OPERATION_PING = "ping";
	public static final String OPERATION_PING_OK = "pingOk";
	public static final String OPERATION_PING_BAD = "pingBad";
	public static final String OPERATION_FILELIST_REQUEST = "filelistRequest";
	public static final String OPERATION_FILELIST_RESPONSE_OK = "filelistResponseOk";
	public static final String OPERATION_FILELIST_RESPONSE_BAD = "filelistResponseBad";
	public static final String OPERATION_USERLIST_REQUEST = "userlistRequest";
	public static final String OPERATION_USERLIST_RESPONSE_OK = "userlistResponseOk";
	public static final String OPERATION_USERLIST_RESPONSE_BAD = "userlistResponseBad";
	public static final String OPERATION_SERVE_REQUEST= "serveRequest";
	public static final String OPERATION_SERVE_OK = "serveOk";
	public static final String OPERATION_SERVE_BAD = "serveBad";
	public static final String OPERATION_DOWNLOAD_REQUEST = "downloadRequest";
	public static final String OPERATION_DOWNLOAD_RESPONSE_OK = "downloadResponseOk";
	public static final String OPERATION_DOWNLOAD_RESPONSE_AMBIGUOUS = "downloadResponseAmbiguous";
	public static final String OPERATION_DOWNLOAD_RESPONSE_NOT_FOUND = "downloadResponseNotFound";
	public static final String OPERATION_QUIT = "quit";
	public static final String OPERATION_QUIT_OK = "quitOk";
	public static final String OPERATION_QUIT_BAD = "quitBad";
}
