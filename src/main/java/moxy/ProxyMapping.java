package moxy;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 */
public class ProxyMapping {


    public Destination map(String url) {
        for (ProxyMapping.Entry entry : entries) {
            if (entry.regex.matcher(url).matches()) {
               return entry.destination;
            }
        }
        return null;
    }

    public static class Destination {
        public String host;
        public int port;

        public Destination(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }

    public static class Entry {
        public Pattern regex;
        public Destination destination;

        public Entry(String regex, Destination destination) {
            this.regex = Pattern.compile(regex, Pattern.DOTALL);
            this.destination = destination;
        }
    }

    private List<Entry> entries = new ArrayList<Entry>();

    public void read(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String line;
        while ((line = reader.readLine()) != null) {
            Entry entry = makeEntry(line);
            if (entry != null) {
                entries.add(entry);
            }
        }
    }

    private Entry makeEntry(String line) {
        line = line.trim();
        if (line.startsWith("#") || line.length() == 0) {
            return null;
        }
        String[] parts = line.split("\\s");
        if (parts.length != 3) {
            throw new RuntimeException("Can't parse " + line);
        }
        return new Entry(parts[0], new Destination(parts[1], Integer.parseInt(parts[2])));
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void dump(PrintStream out) {
        for (Entry entry : entries) {
            out.print(entry.regex);
            out.print(" -> ");
            out.print(entry.destination.host);
            out.print(":");
            out.print(entry.destination.port);
            out.print("\n");
        }
    }
}
