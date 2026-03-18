package es.um.redes.nanoFiles.util;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;

import es.um.redes.nanoFiles.shell.NFShell;

/**
 * @author rtitos
 * Modificado por: Willy y Jorge.
 * 
 *         Utility class with static methods to abstract handling of file
 *         metadata, loading shared failes, search by name substring, etc.
 */
public class FileInfo {
	public String fileHash;
	public String fileName;
	public String filePath;
	public long fileSize = -1;

	public FileInfo(String hash, String name, long size, String path) {
		fileHash = hash;
		fileName = name;
		fileSize = size;
		filePath = path;
	}

	public FileInfo() {
	}

	public String toString() {
		StringBuffer strBuf = new StringBuffer();

		strBuf.append(String.format("%1$-30s", fileName));
		strBuf.append(String.format("%1$10s", fileSize));
		strBuf.append(String.format(" %1$-45s", fileHash));
		return strBuf.toString();
	}	
	public String toStringWithServers(Set<InetSocketAddress> sv) {
	    StringBuffer strBuf = new StringBuffer();
	    //Agregar información del archivo (nombre, tamaño, hash)
	    strBuf.append(String.format("%1$-30s", fileName));
	    strBuf.append(String.format("%1$10s", fileSize));
	    strBuf.append(String.format(" %1$-45s", fileHash));
	    //Agregar los servidores
	    if (sv != null && !sv.isEmpty()) {
	        StringBuilder serverList = new StringBuilder();
	        //Recorrer los servidores y agregar cada uno en formato "host:port"
	        for (InetSocketAddress address : sv) {
	            serverList.append(address.getHostString())
	                    .append(":")
	                    .append(address.getPort())
	                    .append(", ");
	        }
	        //Eliminar la última coma y espacio extra
	        if (serverList.length() > 0) {
	            serverList.setLength(serverList.length() - 2);  //Eliminar la última coma y espacio
	        }
	        //Agregar los servidores a la cadena
	        strBuf.append(serverList.toString());
	    } else {
	        strBuf.append(String.format("%1$-20s", "No servers"));
	    }
	    return strBuf.toString();
	}
	public static void printToSysout(FileInfo[] files) {
	    if (files == null || files.length == 0) {
	        System.out.println("No files available.");
	        return;
	    }

	    //Definir encabezados
	    String[] headers = {"Name", "Size", "Hash"};
	    int[] maxWidths = new int[headers.length];
	    for (int i = 0; i < headers.length; i++) {
	        maxWidths[i] = headers[i].length();
	    }

	    //Calcular anchos máximos para cada columna
	    for (FileInfo file : files) {
	        if (file == null) continue; //Evitar NullPointerException

	        //Parsear la salida de toString
	        String fileStr = file.toString();
	        String name = fileStr.substring(0, 30).trim();
	        String size = fileStr.substring(30, 40).trim();
	        String hash = fileStr.substring(40, 85).trim();

	        //Actualizar anchos máximos
	        maxWidths[0] = Math.max(maxWidths[0], name.length());
	        maxWidths[1] = Math.max(maxWidths[1], size.length());
	        maxWidths[2] = Math.max(maxWidths[2], hash.length());
	    }

	    //Crear formato de fila
	    String format = "| %-" + maxWidths[0] + "s | %-" + maxWidths[1] + "s | %-" +
	                    maxWidths[2] + "s |\n";

	    //Crear separador
	    StringBuilder separator = new StringBuilder("+");
	    for (int width : maxWidths) {
	        separator.append("-".repeat(width + 2)).append("+");
	    }
	    separator.append("\n");

	    //Imprimir tabla
	    System.out.print(separator);
	    System.out.printf(format, headers[0], headers[1], headers[2]);
	    System.out.print(separator);

	    for (FileInfo file : files) {
	        if (file == null) continue;

	        //Parsear la salida de toString
	        String fileStr = file.toString();
	        String name = fileStr.substring(0, 30).trim();
	        String size = fileStr.substring(30, 40).trim();
	        String hash = fileStr.substring(40, 85).trim();

	        System.out.printf(format, name, size, hash);
	    }
	    System.out.print(separator);
	}
public static void printToSysoutWithServers(Map<FileInfo, Set<InetSocketAddress>> files) {
    if (files == null || files.isEmpty()) {
        System.out.println("No files available.");
        return;
    }

    String[] headers = {"Name", "Size", "Hash", "Servers"};
    int[] maxWidths = new int[headers.length];

    //Inicializar con longitudes de encabezado
    for (int i = 0; i < headers.length; i++) {
        maxWidths[i] = headers[i].length();
    }

    //Recoger todas las filas
    List<String[]> rows = new ArrayList<>();
    for (Map.Entry<FileInfo, Set<InetSocketAddress>> entry : files.entrySet()) {
        String[] row = entry.getKey().toArrayWithServers(entry.getValue());
        rows.add(row);
        for (int i = 0; i < row.length; i++) {
            maxWidths[i] = Math.max(maxWidths[i], row[i].length());
        }
    }

    //Crear formato de fila
    StringBuilder formatBuilder = new StringBuilder("|");
    for (int width : maxWidths) {
        formatBuilder.append(" %-" + width + "s |");
    }
    formatBuilder.append("\n");
    String format = formatBuilder.toString();

    //Crear separador
    StringBuilder separator = new StringBuilder("+");
    for (int width : maxWidths) {
        separator.append("-".repeat(width + 2)).append("+");
    }
    separator.append("\n");

    //Imprimir tabla
    System.out.print(separator);
    System.out.printf(format, (Object[]) headers);
    System.out.print(separator);
    for (String[] row : rows) {
        System.out.printf(format, (Object[]) row);
    }
    System.out.print(separator);
}

	public String[] toArrayWithServers(Set<InetSocketAddress> sv) {
    	String name = fileName;
    	String size = String.valueOf(fileSize);
    	String hash = fileHash;
    	String servers;

    	if (sv != null && !sv.isEmpty()) {
    	    StringBuilder serverList = new StringBuilder();
    	    for (InetSocketAddress address : sv) {
    	        serverList.append(address.getHostString())
    	                  .append(":")
    	                  .append(address.getPort())
    	                  .append(", ");
    	    }
    	    //Eliminar la última coma y espacio
    	    if (serverList.length() > 0) {
    	        serverList.setLength(serverList.length() - 2);
    	    }
    	    servers = serverList.toString();
    	} else {
    	    servers = "No servers";
    	}

    	return new String[] { name, size, hash, servers };
	}

	/**
	 * Scans the given directory and returns an array of FileInfo objects, one for
	 * each file recursively found in the given folder and its subdirectories.
	 * 
	 * @param sharedFolderPath The folder to be scanned
	 * @return An array of file metadata (FileInfo) of all the files found
	 */
	public static FileInfo[] loadFilesFromFolder(String sharedFolderPath) {
		File folder = new File(sharedFolderPath);

		Map<String, FileInfo> files = loadFileMapFromFolder(folder);

		FileInfo[] fileinfoarray = new FileInfo[files.size()];
		Iterator<FileInfo> itr = files.values().iterator();
		int numFiles = 0;
		while (itr.hasNext()) {
			fileinfoarray[numFiles++] = itr.next();
		}
		return fileinfoarray;
	}

	/**
	 * Scans the given directory and returns a map of <filehash,FileInfo> pairs.
	 * 
	 * @param folder The folder to be scanned
	 * @return A map of the metadata (FileInfo) of all the files recursively found
	 *         in the given folder and its subdirectories.
	 */
	protected static Map<String, FileInfo> loadFileMapFromFolder(final File folder) {
		Map<String, FileInfo> files = new HashMap<String, FileInfo>();
		scanFolderRecursive(folder, files);
		return files;
	}

	private static void scanFolderRecursive(final File folder, Map<String, FileInfo> files) {
		if (folder.exists() == false) {
			System.err.println("scanFolder cannot find folder " + folder.getPath());
			return;
		}
		if (folder.canRead() == false) {
			System.err.println("scanFolder cannot access folder " + folder.getPath());
			return;
		}

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.isDirectory()) {
				scanFolderRecursive(fileEntry, files);
			} else {
				String fileName = fileEntry.getName();
				String filePath = fileEntry.getPath();
				String fileHash = FileDigest.computeFileChecksumString(filePath);
				long fileSize = fileEntry.length();
				if (fileSize > 0) {
					files.put(fileHash, new FileInfo(fileHash, fileName, fileSize, filePath));
				} else {
					if (fileName.equals(NFShell.FILENAME_TEST_SHELL)) {
						NFShell.enableVerboseShell();
						System.out.println("[Enabling verbose shell]");
					} else {
						System.out.println("Ignoring empty file found in shared folder: " + filePath);
					}
				}
			}
		}
	}

	public static FileInfo[] lookupFilenameSubstring(FileInfo[] files, String filenameSubstr) {
		String needle = filenameSubstr.toLowerCase();
		Vector<FileInfo> matchingFiles = new Vector<FileInfo>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].fileName.toLowerCase().contains(needle)) {
				matchingFiles.add(files[i]);
			}
		}
		FileInfo[] result = new FileInfo[matchingFiles.size()];
		matchingFiles.toArray(result);
		return result;
	}
	//Añadidos para manejo mapas
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;  //Compara las referencias (optimización)
	    if (obj == null || getClass() != obj.getClass()) return false;  //Si son de clases diferentes
	    FileInfo other = (FileInfo) obj;
	    return fileHash.equals(other.fileHash) &&  //Compara los valores de los campos
	           fileName.equals(other.fileName) &&
	           fileSize == other.fileSize;
	}

	@Override
	public int hashCode() {
	    return Objects.hash(fileHash, fileName, fileSize);  //Calcula el hash en base a los campos relevantes
	}
}
