package es.um.redes.nanoFiles.tcp.message;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class PeerMessage {

	public static final byte HASH_LENGHT = 40;
	private static final boolean ENCRYPT = true;

	private byte opcode;

	/*
	 * DONE: (Boletín MensajesBinarios) Añadir atributos u otros constructores
	 * específicos para crear mensajes con otros campos, según sea necesario
	 * 
	 */
	//download
	private byte longFileName;
	private String fileName;
	//metadata
	private long fileSize;
	private String fileHash; //tmb para chunkRequest
	//chunkRequest
	private int chunkInicio;
	private int chunkFin;
	//chunkResponse
	private int posicionChunk;
	private byte[] chunk;


    public PeerMessage() {
        opcode = PeerMessageOps.OPCODE_INVALID_CODE;
    }

    //Constructor con opcode
    public PeerMessage(byte op) {
        opcode = op;
    }


    //Constructores existentes
    public PeerMessage(byte op, byte longName, String name) {
        this(op);
        longFileName = longName;
        fileName = new String(name);
    }

    public PeerMessage(byte op, long size, String hash) {
        this(op);
        fileSize = size;
        fileHash = new String(hash);
    }

    public PeerMessage(byte op, long size, String hash, byte longName, String name) {
        this(op, size, hash);
        longFileName = longName;
        fileName = new String(name);
    }

    public PeerMessage(byte op, String hash, int chunkInicial, int chunkFinal) {
        this(op);
        fileHash = hash;
        chunkInicio = chunkInicial;
        chunkFin = chunkFinal;
    }

    public PeerMessage(byte op, int posicionChunk, byte[] chunk) {
        this(op);
        this.posicionChunk = posicionChunk;
        this.chunk = chunk;
    }

	/*
	 * DONE: (Boletín MensajesBinarios) Crear métodos getter y setter para obtener
	 * los valores de los atributos de un mensaje. Se aconseja incluir código que
	 * compruebe que no se modifica/obtiene el valor de un campo (atributo) que no
	 * esté definido para el tipo de mensaje dado por "operation".
	 */
	public byte getOpcode() {
		return opcode;
	}
	
	public int getLongFileName() {
		return longFileName;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public long getFileSize() {
		return fileSize;
	}
	
	public String getFileHash() {
		return fileHash;
	}
	
	public int getChunkInicio() {
		return chunkInicio;
	}
	
	public int getChunkFin() {
		return chunkFin;
	}
	
	public byte[] getChunk() {
		return chunk;
	}
	
	public int getPosicionChunk() {
		return posicionChunk;
	}
	public void setLongFileName(byte longitud) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_DOWNLOAD_FILE))|| PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_UPLOAD_REQUEST))) {
			longFileName = longitud;
		}else {
			throw new RuntimeException(
					"PeerMessage: setLongFileName called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}

	public void setFileName(String name) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_DOWNLOAD_FILE))|| PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_UPLOAD_REQUEST))) {
			fileName = name;
		}else {
			throw new RuntimeException(
					"PeerMessage: setFileName called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
	
	public void setFileHash(String hash) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_FILE_METADATA)) || PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_CHUNK_REQUEST))|| PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_UPLOAD_REQUEST))) {
			fileHash = hash;
		}else {
			throw new RuntimeException(
					"PeerMessage: setFileHash called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
	
	public void setFileSize(long size) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_FILE_METADATA)) || PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_UPLOAD_REQUEST))) {
			fileSize = size;
		}else {
			throw new RuntimeException(
					"PeerMessage: setFileSize called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
	
	public void setChunkInicio(int chunkInicial) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_CHUNK_REQUEST))) {
			chunkInicio = chunkInicial;
		}else {
			throw new RuntimeException(
					"PeerMessage: setChunkInicio called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
	
	public void setChunkFin(int chunkFinal) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_CHUNK_REQUEST))) {
			chunkFin = chunkFinal;
		}else {
			throw new RuntimeException(
					"PeerMessage: setChunkFin called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
	
	public void setChunk(byte[] chunk) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_CHUNK_RESPONSE)) || PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_UPLOAD_DATA))) {
			this.chunk = chunk;
		}else {
			throw new RuntimeException(
					"PeerMessage: setChunk called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
	
	public void setPosicionChunk(int posicionChunk) {
		if(PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_CHUNK_RESPONSE)) || PeerMessageOps.opcodeToOperation(opcode).equals(PeerMessageOps.opcodeToOperation(PeerMessageOps.OPCODE_UPLOAD_DATA))) {
			this.posicionChunk = posicionChunk;
		}else {
			throw new RuntimeException(
					"PeerMessage: setPosicionChunk called for message of unexpected type (" + PeerMessageOps.opcodeToOperation(opcode) + ")");
		}
	}
	/**
	 * Método de clase para parsear los campos de un mensaje y construir el objeto
	 * PeerMessage que contiene los datos del mensaje recibido
	 * 
	 * @param data El array de bytes recibido
	 * @return Un objeto de esta clase cuyos atributos contienen los datos del
	 *         mensaje recibido.
	 * @throws IOException
	 */


 	public static PeerMessage readMessageFromInputStream(DataInputStream dis) throws IOException {
		/*
		 * DONE: (Boletín MensajesBinarios) En función del tipo de mensaje, leer del
		 * socket a través del "dis" el resto de campos para ir extrayendo con los
		 * valores y establecer los atributos del un objeto PeerMessage que contendrá
		 * toda la información del mensaje, y que será devuelto como resultado. NOTA:
		 * Usar dis.readFully para leer un array de bytes, dis.readInt para leer un
		 * entero, etc.
		 */
        try {
            //Leer la longitud del mensaje
            int length = dis.readInt();
            byte[] data = new byte[length];
            dis.readFully(data);

            //Desencriptar si está habilitado
            if (ENCRYPT) {
                data = CryptoUtils.decrypt(data);
            }

            //Crear un DataInputStream para parsear los datos
            DataInputStream newDis = new DataInputStream(new ByteArrayInputStream(data));
            byte opcode = newDis.readByte();
            PeerMessage message = new PeerMessage(opcode);
            switch (opcode) {
                case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
                    byte longitudNombre = newDis.readByte();
                    byte[] nombre = new byte[longitudNombre];
                    newDis.readFully(nombre);
                    String nombreCadena = new String(nombre);
                    message.setLongFileName(longitudNombre);
                    message.setFileName(nombreCadena);
                    break;
                case PeerMessageOps.OPCODE_FILE_METADATA:
                    long tamanio = newDis.readLong();
                    byte[] hash = new byte[PeerMessageOps.TAMANIO_HASH];
                    newDis.readFully(hash);
                    String hashCadena = new String(hash);
                    message.setFileSize(tamanio);
                    message.setFileHash(hashCadena);
                    break;
                case PeerMessageOps.OPCODE_CHUNK_REQUEST:
                    byte[] hashh = new byte[PeerMessageOps.TAMANIO_HASH];
                    newDis.readFully(hashh);
                    String hashChain = new String(hashh);
                    int inicio = newDis.readInt();
                    int fin = newDis.readInt();
                    message.setFileHash(hashChain);
                    message.setChunkInicio(inicio);
                    message.setChunkFin(fin);
                    break;
                case PeerMessageOps.OPCODE_CHUNK_RESPONSE:
                    inicio = newDis.readInt();
                    int longitud = newDis.readInt();
                    byte[] datos = new byte[longitud];
                    newDis.readFully(datos);
                    message.setPosicionChunk(inicio);
                    message.setChunk(datos);
                    break;
                case PeerMessageOps.OPCODE_INVALID_CODE:
                case PeerMessageOps.OPCODE_END_OF_FILE:
                    break;
                case PeerMessageOps.OPCODE_UPLOAD_REQUEST:
                    long size = newDis.readLong();
                    byte[] hashBytes = new byte[PeerMessageOps.TAMANIO_HASH];
                    newDis.readFully(hashBytes);
                    String hashStr = new String(hashBytes);
                    byte longName = newDis.readByte();
                    byte[] nameBytes = new byte[longName];
                    newDis.readFully(nameBytes);
                    String nameStr = new String(nameBytes);
                    message.setFileSize(size);
                    message.setFileHash(hashStr);
                    message.setLongFileName(longName);
                    message.setFileName(nameStr);
                    break;
                case PeerMessageOps.OPCODE_UPLOAD_DATA:
                    inicio = newDis.readInt();
                    int lengthh = newDis.readInt();
                    byte[] chunkData = new byte[lengthh];
                    newDis.readFully(chunkData);
                    message.setPosicionChunk(inicio);
                    message.setChunk(chunkData);
                    break;
                case PeerMessageOps.OPCODE_UPLOAD_COMPLETE:
                case PeerMessageOps.OPCODE_UPLOAD_ACCEPTED:
                case PeerMessageOps.OPCODE_UPLOAD_REJECTED:
                    break;
                default:
                    System.err.println("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: " + opcode);
                    throw new IOException("PeerMessage.readMessageFromInputStream doesn't know how to parse this message opcode: " + opcode);
            }
            return message;
        }catch (EOFException eof) {
                System.out.println("Socket cerrado por el cliente (fin de flujo)");
                PeerMessage invalid = new PeerMessage(PeerMessageOps.OPCODE_INVALID_CODE);
                return invalid;
        }catch (Exception e) {  
            throw new IOException("Error reading or decrypting message: " + e.getMessage(), e);
        }
    }

	public void writeMessageToOutputStream(DataOutputStream dos) throws IOException {
		/*
		 * DONE (Boletín MensajesBinarios): Escribir los bytes en los que se codifica el
		 * mensaje en el socket a través del "dos", teniendo en cuenta opcode del
		 * mensaje del que se trata y los campos relevantes en cada caso. NOTA: Usar
		 * dos.write para leer un array de bytes, dos.writeInt para escribir un entero,
		 * etc.
		 */
        try {
            //Usar ByteArrayOutputStream con DataOutputStream para capturar los datos
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream tempDos = new DataOutputStream(baos);

            //Escribir los datos según el opcode (lógica original)
            tempDos.writeByte(opcode);
            switch (opcode) {
                case PeerMessageOps.OPCODE_AMBIGUOUS:
                case PeerMessageOps.OPCODE_INVALID_CODE:
                case PeerMessageOps.OPCODE_FILE_NOT_FOUND:
                    break;
                case PeerMessageOps.OPCODE_DOWNLOAD_FILE:
                    byte[] nameBytes = fileName.getBytes();
                    tempDos.writeByte(longFileName);
                    tempDos.write(nameBytes);
                    break;
                case PeerMessageOps.OPCODE_FILE_METADATA:
                    tempDos.writeLong(fileSize); //Corregido a writeLong
                    tempDos.write(fileHash.getBytes());
                    break;
                case PeerMessageOps.OPCODE_CHUNK_REQUEST:
                    tempDos.write(fileHash.getBytes());
                    tempDos.writeInt(chunkInicio);
                    tempDos.writeInt(chunkFin);
                    break;
                case PeerMessageOps.OPCODE_CHUNK_RESPONSE:
                    tempDos.writeInt(posicionChunk);
                    tempDos.writeInt(chunk.length);
                    tempDos.write(chunk);
                    break;
                case PeerMessageOps.OPCODE_UPLOAD_REQUEST:
                    tempDos.writeLong(fileSize); //Corregido a writeLong
                    tempDos.write(fileHash.getBytes());
                    tempDos.writeByte(longFileName);
                    tempDos.write(fileName.getBytes());
                    break;
                case PeerMessageOps.OPCODE_UPLOAD_DATA:
                    tempDos.writeInt(posicionChunk);
                    tempDos.writeInt(chunk.length);
                    tempDos.write(chunk);
                    break;
                case PeerMessageOps.OPCODE_UPLOAD_COMPLETE:
                case PeerMessageOps.OPCODE_UPLOAD_ACCEPTED:
                case PeerMessageOps.OPCODE_UPLOAD_REJECTED:
                    break;
                default:
                    System.err.println("PeerMessage.writeMessageToOutputStream found unexpected message opcode " + opcode + "("
                            + PeerMessageOps.opcodeToOperation(opcode) + ")");
            }

            //Obtener los datos escritos
            byte[] data = baos.toByteArray();

            //Encriptar si está habilitado
            if (ENCRYPT) {
                data = CryptoUtils.encrypt(data);
            }

            //Escribir longitud del mensaje y los datos
            dos.writeInt(data.length);
            dos.write(data);
        } catch (Exception e) {
            throw new IOException("Error writing or encrypting message: " + e.getMessage(), e);
        }
    }
}