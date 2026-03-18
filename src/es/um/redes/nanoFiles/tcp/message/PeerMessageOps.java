package es.um.redes.nanoFiles.tcp.message;

import java.util.Map;
import java.util.TreeMap;

public class PeerMessageOps {

	public static final byte OPCODE_INVALID_CODE = 0;
	public static final byte OPCODE_DOWNLOAD_FILE = 1;
	public static final byte OPCODE_FILE_METADATA = 2;
	public static final byte OPCODE_AMBIGUOUS = 3;
	public static final byte OPCODE_FILE_NOT_FOUND = 4;
	public static final byte OPCODE_CHUNK_REQUEST = 5;
	public static final byte OPCODE_CHUNK_RESPONSE = 6;
	public static final byte OPCODE_END_OF_FILE = 7;
	public static final byte OPCODE_CHUNK_NOT_FOUND = 8;
	public static final byte OPCODE_UPLOAD_REQUEST = 9;
	public static final byte OPCODE_UPLOAD_ACCEPTED = 10;
	public static final byte OPCODE_UPLOAD_REJECTED = 11;
	public static final byte OPCODE_UPLOAD_DATA = 12;
	public static final byte OPCODE_UPLOAD_COMPLETE = 13;

	/*
	 * DONE: (Boletín MensajesBinarios) Añadir aquí todas las constantes que definen
	 * los diferentes tipos de mensajes del protocolo de comunicación con un par
	 * servidor de ficheros (valores posibles del campo "operation").
	 */
	public static final int TAMANIO_HASH = 40;

	/*
	 * DONE: (Boletín MensajesBinarios) Definir constantes con nuevos opcodes de
	 * mensajes definidos anteriormente, añadirlos al array "valid_opcodes" y añadir
	 * su representación textual a "valid_operations_str" EN EL MISMO ORDEN.
	 */
	private static final Byte[] _valid_opcodes = { 
    	OPCODE_INVALID_CODE, OPCODE_DOWNLOAD_FILE, OPCODE_FILE_METADATA, OPCODE_AMBIGUOUS,
    	OPCODE_FILE_NOT_FOUND, OPCODE_CHUNK_REQUEST, OPCODE_CHUNK_RESPONSE, OPCODE_END_OF_FILE, 
    	OPCODE_CHUNK_NOT_FOUND,
    	OPCODE_UPLOAD_REQUEST, OPCODE_UPLOAD_ACCEPTED, OPCODE_UPLOAD_REJECTED, OPCODE_UPLOAD_DATA, OPCODE_UPLOAD_COMPLETE
	};

	private static final String[] _valid_operations_str = { 
	    "INVALID_OPCODE", "DOWNLOAD_FILE", "FILE_METADATA", "AMBIGUOUS_NAME",
	    "FILE_NOT_FOUND", "CHUNK_REQUEST", "CHUNK_RESPONSE", "END_OF_FILE", 
	    "CHUNK_NOT_FOUND",
	    "UPLOAD_REQUEST", "UPLOAD_ACCEPTED", "UPLOAD_REJECTED", "UPLOAD_DATA", "UPLOAD_COMPLETE"
	};

	private static Map<String, Byte> _operation_to_opcode;
	private static Map<Byte, String> _opcode_to_operation;

	static {
		_operation_to_opcode = new TreeMap<>();
		_opcode_to_operation = new TreeMap<>();
		for (int i = 0; i < _valid_operations_str.length; ++i) {
			_operation_to_opcode.put(_valid_operations_str[i].toLowerCase(), _valid_opcodes[i]);
			_opcode_to_operation.put(_valid_opcodes[i], _valid_operations_str[i]);
		}
	}

	/**
	 * Transforma una cadena en el opcode correspondiente
	 */
	protected static byte operationToOpcode(String opStr) {
		return _operation_to_opcode.getOrDefault(opStr.toLowerCase(), OPCODE_INVALID_CODE);
	}

	/**
	 * Transforma un opcode en la cadena correspondiente
	 */
	public static String opcodeToOperation(byte opcode) {
		return _opcode_to_operation.getOrDefault(opcode, null);
	}
}
