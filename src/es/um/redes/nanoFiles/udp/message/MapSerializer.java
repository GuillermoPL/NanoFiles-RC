package es.um.redes.nanoFiles.udp.message;

import java.net.InetSocketAddress;
import java.util.*;

/**
 * @author Willy
 * @author Jorge
 * 
 * Utility class with static methods to serialize and deserialize a nested map structure 
 * containing network addresses and associated file sets, as well as to print the map in a 
 * formatted table. Provides functionality to convert between string representations and 
 * Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> objects, handling address 
 * parsing, escaping, and validation.
 */


public class MapSerializer {
    public static String mapToString(Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> map) {
        if (map == null || map.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<InetSocketAddress, Map<InetSocketAddress, Set<String>>> outerEntry : map.entrySet()) {
            InetSocketAddress outerKey = outerEntry.getKey();
            if (outerKey == null || outerKey.getPort() <= 0) {
                System.err.println("Ignorando clave externa inválida: " + outerKey);
                continue;
            }
            sb.append(addressToString(outerKey)).append(":");
            Map<InetSocketAddress, Set<String>> innerMap = outerEntry.getValue();
            if (innerMap == null || innerMap.isEmpty()) {
                sb.append("none={}\n");
                continue;
            }
            Map.Entry<InetSocketAddress, Set<String>> innerEntry = innerMap.entrySet().iterator().next();
            InetSocketAddress innerKey = innerEntry.getKey();
            if (innerKey == null) {
                sb.append("none={");
            } else {
                sb.append(addressToString(innerKey)).append("={");
            }
            Set<String> strings = innerEntry.getValue();
            boolean firstString = true;
            for (String str : strings) {
                if (!firstString) {
                    sb.append(",");
                }
                firstString = false;
                sb.append(escape(str));
            }
            sb.append("}\n");
        }
        //Eliminar la última \n si existe
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '\n') {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> mapFromString(String str) throws IllegalArgumentException {
        Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> map = new HashMap<>();
        if (str == null || str.trim().isEmpty()) {
            return map;
        }
        String[] lines = str.split(";");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                System.out.println("Ignorando línea vacía");
                continue;
            }
            int firstColon = line.indexOf(':');
            if (firstColon == -1) {
                System.err.println("Error: no se encontró primer ':' en la línea: " + line);
                continue;
            }
            int secondColon = line.indexOf(':', firstColon + 1);
            if (secondColon == -1) {
                System.err.println("Error: no se encontró segundo ':' en la línea: " + line);
                continue;
            }
            String outerKeyStr = line.substring(0, secondColon);
            InetSocketAddress outerKey;
            try {
                outerKey = parseAddress(outerKeyStr);
            } catch (IllegalArgumentException e) {
                System.err.println("Error parseando clave externa: " + outerKeyStr + " - " + e.getMessage());
                continue;
            }

            String rest = line.substring(secondColon + 1);
            int eqIndex = rest.indexOf('=');
            if (eqIndex == -1) {
                System.err.println("Error: no se encontró '=' en la parte interna: " + rest);
                continue;
            }

            String innerKeyStr = rest.substring(0, eqIndex).trim();
            InetSocketAddress innerKey;
            if (innerKeyStr.equals("none")) {
                innerKey = null;
            } else {
                try {
                    innerKey = parseAddress(innerKeyStr);
                } catch (IllegalArgumentException e) {
                    System.err.println("Error parseando clave interna: " + innerKeyStr + " - " + e.getMessage());
                    continue;
                }
            }

            String filesStr = rest.substring(eqIndex + 1).trim();
            if (!filesStr.startsWith("{") || !filesStr.endsWith("}")) {
                System.err.println("Error: conjunto de ficheros mal formado: " + filesStr);
                continue;
            }
            String inside = filesStr.substring(1, filesStr.length() - 1).trim();

            Set<String> files = new HashSet<>();
            if (!inside.isEmpty()) {
                String[] fileArray = inside.split(",");
                for (String f : fileArray) {
                    files.add(f.trim());
                }
            }

            Map<InetSocketAddress, Set<String>> innerMap = new HashMap<>();
            innerMap.put(innerKey, files);
            map.put(outerKey, innerMap);
        }
        return map;
    }

    public static void printMapAsTable(Map<InetSocketAddress, Map<InetSocketAddress, Set<String>>> map) {
        String[] headers = {"IP", "Serving", "Listening At", "Files"};
        int[] maxWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            maxWidths[i] = headers[i].length();
        }

        for (Map.Entry<InetSocketAddress, Map<InetSocketAddress, Set<String>>> outerEntry : map.entrySet()) {
            String ip = addressToStringSafe(outerEntry.getKey());
            String serving = outerEntry.getValue().isEmpty() || outerEntry.getValue().keySet().iterator().next() == null ? "No" : "Yes";
            String listeningAt = outerEntry.getValue().isEmpty() || outerEntry.getValue().keySet().iterator().next() == null ? "-" : addressToString(outerEntry.getValue().keySet().iterator().next());
            String files = outerEntry.getValue().isEmpty() ? "-" : String.join(",", outerEntry.getValue().values().iterator().next());

            maxWidths[0] = Math.max(maxWidths[0], ip.length());
            maxWidths[1] = Math.max(maxWidths[1], serving.length());
            maxWidths[2] = Math.max(maxWidths[2], listeningAt.length());
            maxWidths[3] = Math.max(maxWidths[3], files.length());
        }

        String format = "| %-" + maxWidths[0] + "s | %-" + maxWidths[1] + "s | %-" + maxWidths[2] + "s | %-" + maxWidths[3] + "s |\n";

        StringBuilder separator = new StringBuilder("+");
        for (int width : maxWidths) {
            separator.append("-".repeat(width + 2)).append("+");
        }
        separator.append("\n");

        System.out.print(separator);
        System.out.printf(format, headers[0], headers[1], headers[2], headers[3]);
        System.out.print(separator);

        for (Map.Entry<InetSocketAddress, Map<InetSocketAddress, Set<String>>> outerEntry : map.entrySet()) {
            String ip = addressToStringSafe(outerEntry.getKey());
            String serving = outerEntry.getValue().isEmpty() || outerEntry.getValue().keySet().iterator().next() == null ? "No" : "Yes";
            String listeningAt = outerEntry.getValue().isEmpty() || outerEntry.getValue().keySet().iterator().next() == null ? "-" : addressToString(outerEntry.getValue().keySet().iterator().next());
            String files = outerEntry.getValue().isEmpty() ? "-" : String.join(",", outerEntry.getValue().values().iterator().next());
            System.out.printf(format, ip, serving, listeningAt, files);
        }
        System.out.print(separator);
    }

    private static String addressToString(InetSocketAddress addr) {
        if (addr == null) {
            throw new IllegalArgumentException("Dirección nula");
        }
        String host = addr.getHostString();
        if (addr.isUnresolved()) {
            host += "/<unresolved>";
        }
        return host + ":" + addr.getPort();
    }

    private static String addressToStringSafe(InetSocketAddress addr) {
        return addr == null ? "none" : addressToString(addr);
    }

    private static InetSocketAddress parseAddress(String str) {
        if (str == null || str.trim().isEmpty()) {
            throw new IllegalArgumentException("Cadena de dirección vacía o nula: " + str);
        }
        if (str.equals("none")) {
            return null;
        }
        String[] parts = str.split(":", -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Formato de dirección inválido, se esperaba host:port, recibido: " + str);
        }
        String hostPart = parts[0].trim();
        String portPart = parts[1].trim();
        if (hostPart.isEmpty() || portPart.isEmpty()) {
            throw new IllegalArgumentException("Host o puerto vacío en dirección: " + str);
        }
        try {
            int port = Integer.parseInt(portPart);
            if (port < 0 || port > 65535) {
                throw new IllegalArgumentException("Puerto fuera de rango (0-65535) en dirección: " + str);
            }
            if (hostPart.endsWith("/<unresolved>")) {
                String hostname = hostPart.substring(0, hostPart.length() - "/<unresolved>".length());
                return InetSocketAddress.createUnresolved(hostname, port);
            } else {
                return new InetSocketAddress(hostPart, port);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Puerto no numérico en dirección: " + str, e);
        }
    }
/*
    private static String[] splitStrings(String content) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '\\') {
                escaped = true;
                continue;
            }
            if (c == ',' && !escaped) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
            escaped = false;
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result.toArray(new String[0]);
    }
*/
    private static String escape(String str) {
        return str.replace(",", "\\,").replace("{", "\\{").replace("}", "\\}");
    }
/*
    private static String unescape(String str) {
        StringBuilder result = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\' && !escaped) {
                escaped = true;
                continue;
            }
            result.append(c);
            escaped = false;
        }
        return result.toString();
    }
    */
}