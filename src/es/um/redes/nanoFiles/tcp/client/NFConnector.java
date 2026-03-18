package es.um.redes.nanoFiles.tcp.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

//Esta clase proporciona la funcionalidad necesaria para intercambiar mensajes entre el cliente y el servidor
public class NFConnector  implements AutoCloseable{
	private Socket socket;
	private InetSocketAddress serverAddr;
	DataInputStream dis = null;
	DataOutputStream dos = null;



	public NFConnector(InetSocketAddress fserverAddr) throws UnknownHostException, IOException {
		serverAddr = fserverAddr;
		/*
		 * DONE: (Boletín SocketsTCP) Se crea el socket a partir de la dirección del
		 * servidor (IP, puerto). La creación exitosa del socket significa que la
		 * conexión TCP ha sido establecida.
		 */
		if (fserverAddr == null) {
			throw new IllegalArgumentException("Server address cant be null");
		}
		socket = new Socket(serverAddr.getAddress(),serverAddr.getPort());
		/*
		 * DONE: (Boletín SocketsTCP) Se crean los DataInputStream/DataOutputStream a
		 * partir de los streams de entrada/salida del socket creado. Se usarán para
		 * enviar (dos) y recibir (dis) datos del servidor.
		 */
		dis = new DataInputStream(socket.getInputStream());
		dos = new DataOutputStream(socket.getOutputStream());

	}
	public DataOutputStream getDos() {
		return dos;
	}
	public DataInputStream getDis() {
		return dis;
	}
	public void test() {
		/*
		 * DONE: (Boletín SocketsTCP) Enviar entero cualquiera a través del socket y
		 * después recibir otro entero, comprobando que se trata del mismo valor.
		 */
		try {
			//Ej 1-6
			int integerToSend = 1;
			int integerReceived;
			System.out.println("Sending..." + integerToSend);
			dos.writeInt(integerToSend);
			integerReceived = dis.readInt();
			System.out.println("Integer received..." + integerReceived);
			//Ej 7
			/*
			PeerMessage msgOut;
			PeerMessage msgIn;
			msgOut = new PeerMessage(PeerMessageOps.OPCODE_DOWNLOAD_FILE, (byte) 10, "test.txt");
			msgOut.writeMessageToOutputStream(dos);
			msgIn = PeerMessage.readMessageFromInputStream(dis);
			System.out.println("Message received: " + PeerMessageOps.opcodeToOperation(msgIn.getOpcode()));
			if(msgIn.getOpcode()==PeerMessageOps.OPCODE_FILE_METADATA) {
				System.out.println("File Size: " + msgIn.getFileSize());
				System.out.println("File Hash: " + msgIn.getFileHash());
			} */
		} catch(IOException e) {
			System.err.println("Communication exception: " + e.getMessage());
		}
	}
	
	public InetSocketAddress getServerAddr() {
		return serverAddr;
	}
	
    @Override
    public void close() throws IOException {
        if (dis != null) {
            dis.close();
        }
        if (dos != null) {
            dos.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

}
