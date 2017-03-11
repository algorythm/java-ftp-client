import java.net.*;
import java.io.*;

/**
 * Created by AndersWOlsen on 06-03-2017.
 */
public class TCPClient {
    private Socket socket;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;

    public TCPClient() {
    }

    public synchronized void connect(String host, int port) throws IOException {
        System.out.println("Connecting to " + host + " on port " + port);
        if (socket != null)
            throw new IOException("Already connected. Disconnect first.");

        socket = new Socket(host, port);
        reader = new BufferedReader(new InputStreamReader((socket.getInputStream())));
        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public void send(String line) throws IOException {
        if (socket == null)
            throw new IOException("Not connected");
        try {
            writer.write(line + "\r\n");
            writer.flush();
        } catch (IOException e) {
            socket = null;
            throw e;
        }
    }

    public String receive() throws IOException {
        String line = reader.readLine();
        return line;
    }

    public void close() throws IOException {
        socket.close();
        socket = null;
    }
}
