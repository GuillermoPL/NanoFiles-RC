package es.um.redes.nanoFiles.tcp.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import es.um.redes.nanoFiles.tcp.message.PeerMessage;
import es.um.redes.nanoFiles.tcp.message.PeerMessageOps;

public class NFDownloadThread extends Thread {
    public static final boolean DOWNLOAD_DEBUG_MODE = true;
    private static final int MAX_RETRIES = 3; //Número máximo de reintentos
    private static final int RETRY_DELAY_MS = 500; //Espera entre reintentos
    private final InetSocketAddress servidorPrincipal;
    private final InetSocketAddress[] todosLosServidores;
    private final String fileHash;
    private final int chunkIndex;
    private final int inicio;
    private final int fin;
    private final byte[] sharedBuffer;
    private final boolean[] chunkSuccess;
    private final Object lock;
    private final ConcurrentMap<Integer, InetSocketAddress> mapaDescargas;

    //Constructor actualizado para aceptar el servidor principal y todos los servidores
    public NFDownloadThread(InetSocketAddress servidorPrincipal, InetSocketAddress[] todosLosServidores, String fileHash, int chunkIndex, int inicio, int fin, byte[] sharedBuffer, boolean[] chunkSuccess, Object lock, ConcurrentMap<Integer, InetSocketAddress> mapaDescargas) {
        this.servidorPrincipal = servidorPrincipal;
        this.todosLosServidores = todosLosServidores;
        this.fileHash = fileHash;
        this.chunkIndex = chunkIndex;
        this.inicio = inicio;
        this.fin = fin;
        this.sharedBuffer = sharedBuffer;
        this.chunkSuccess = chunkSuccess;
        this.lock = lock;
        this.mapaDescargas = mapaDescargas;
    }

    @Override
    public void run() {
        int attempt = 0;
        boolean chunkDownloaded = false;

        //Intentar descargar el fragmento hasta alcanzar el número máximo de reintentos
        while (attempt < MAX_RETRIES && !chunkDownloaded) {
            attempt++;
            //Primero intentamos con el servidor principal, luego con los demás servidores
            InetSocketAddress[] ordenDeIntento = Stream.concat(Stream.of(servidorPrincipal), Arrays.stream(todosLosServidores).filter(s -> !s.equals(servidorPrincipal))
            ).toArray(InetSocketAddress[]::new);
            //Intentar descargar desde los servidores en el orden definido
            for (InetSocketAddress serverAddress : ordenDeIntento) {
                try (NFConnector connector = new NFConnector(serverAddress)) {
                    if (DOWNLOAD_DEBUG_MODE) {
                        System.out.println("[Attempt " + attempt + "] Thread " + chunkIndex +  ": Sending CHUNK_REQUEST to " + serverAddress);
                    }
                    //Solicitar el chunk
                    PeerMessage chunkRequest = new PeerMessage(PeerMessageOps.OPCODE_CHUNK_REQUEST, fileHash, inicio, fin);
                    chunkRequest.writeMessageToOutputStream(connector.getDos());
                    PeerMessage respuestaChunk = PeerMessage.readMessageFromInputStream(connector.getDis());

                    if (respuestaChunk.getOpcode() == PeerMessageOps.OPCODE_CHUNK_RESPONSE) {
                        byte[] datos = respuestaChunk.getChunk();
                        synchronized (lock) {
                            //Guardar el fragmento en el buffer compartido
                            System.arraycopy(datos, 0, sharedBuffer, inicio, datos.length);
                            chunkSuccess[chunkIndex] = true;
                        }
                        //Registrar el servidor que descargó este fragmento
                        mapaDescargas.put(chunkIndex, serverAddress);

                        if (DOWNLOAD_DEBUG_MODE) {
                            System.out.println("[SUCCESS] Thread " + chunkIndex + ": Chunk [" + inicio + "-" + fin + "] downloaded from " + serverAddress);
                        }
                        chunkDownloaded = true;
                        break; //Salir del bucle de servidores
                    } else {
                        System.err.println("[ERROR] Thread " + chunkIndex + ": Unexpected response " + PeerMessageOps.opcodeToOperation(respuestaChunk.getOpcode()));
                    }
                } catch (IOException e) {
                    System.err.println("[ERROR] Thread " + chunkIndex + ": Connection failed to " + serverAddress + " - " + e.getMessage());
                    //Intentar con otro servidor si falla
                }
            }
            //Si el fragmento no se descargó, esperar un poco antes de reintentar
            if (!chunkDownloaded) {
                if (attempt < MAX_RETRIES) {
                    if (DOWNLOAD_DEBUG_MODE) {
                        System.out.println("[Retrying] Thread " + chunkIndex + ": waiting " + RETRY_DELAY_MS + " ms before retry...");
                    }
                    try {
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (InterruptedException e) {
                        //ignorar
                    }
                }
            }
        }

        //Si no se descargó después de los intentos, mostrar un error
        if (!chunkDownloaded) {
            System.err.println("[FAILED] Thread " + chunkIndex + ": Failed to download chunk [" + inicio + "-" + fin + "] after " + MAX_RETRIES + " attempts.");
        }
    }
}
