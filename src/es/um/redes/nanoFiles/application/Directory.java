package es.um.redes.nanoFiles.application;

import java.io.IOException;
import java.net.SocketException;

import es.um.redes.nanoFiles.udp.server.NFDirectoryServer;

public class Directory {
	public static boolean DIRECTORY_VERBOSE_MODE = true;
	public static final double DEFAULT_CORRUPTION_PROBABILITY = 0.0;

	public static void main(String[] args) {
		double datagramCorruptionProbability = DEFAULT_CORRUPTION_PROBABILITY;

		/**
		 * Command line argument to directory is optional, if not specified, default
		 * value is used: -loss: probability of corruption of received datagrams
		 */
		String arg;

		//Analizamos si hay parámetro
		if (args.length > 0 && args[0].startsWith("-")) {
			arg = args[0];
			//Examinamos si es un parámetro válido
			if (arg.equals("-loss")) {
				if (args.length == 2) {
					try {
						//El segundo argumento contiene la probabilidad de descarte
						datagramCorruptionProbability = Double.parseDouble(args[1]);
					} catch (NumberFormatException e) {
						System.err.println("Wrong value passed to option " + arg);
						return;
					}
				} else
					System.err.println("option " + arg + " requires a value");
			} else {
				System.err.println("Illegal option " + arg);
				System.exit(-1); //Añadido
			}
		}
		if (datagramCorruptionProbability < 0 || datagramCorruptionProbability > 1) { //Añadido
   			System.err.println("Loss probability must be between 0 and 1");
    		System.exit(-1);
		}
		System.out.println("Probability of corruption for received datagrams: " + datagramCorruptionProbability);
		try {
			NFDirectoryServer dir = new NFDirectoryServer(datagramCorruptionProbability);
			if (NanoFiles.testModeUDP) {
				dir.runTest();
			} else {
				dir.run();
			}
		} catch (SocketException e) {
			System.err.println("Directory cannot create UDP socket");
			System.err.println("Most likely a Directory process is already running and listening on that port...");
			System.exit(-1);
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Unexpected I/O error when running NFDirectoryServer.run");
			System.exit(-1);
		}

	}

}
