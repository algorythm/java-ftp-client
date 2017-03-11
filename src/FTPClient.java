import javax.imageio.IIOException;
import java.io.IOException;
import java.util.Scanner;

/**
 * Created by AndersWOlsen on 10-03-2017.
 */
public class FTPClient {
    private String ipAddress;
    private int port;
    private TCPClient tcp;
    private TCPClient transfer;

    public FTPClient(String ipAddress) {
        this(ipAddress, 21);
    }

    public FTPClient(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.tcp = new TCPClient();
        this.transfer = new TCPClient();
    }

    public void connect() {
        try {
            tcp.connect(ipAddress, 21);
        } catch (IOException e) {
            System.err.printf("An error has occured when trying to connect to server: %s\n", e.getMessage());
            return;
        }

        System.out.printf("Conntected to %s.\n", ipAddress);

        String receivedMessage = null;
        try {
            receivedMessage = tcp.receive();
        } catch (IOException e) {
            System.out.printf("Could not receive message from server: %s\n", e.getMessage());
            return;
        }

        if (!receivedMessage.startsWith("220"))
            System.out.println("An error occured when connecting to server.");

        System.out.println(receivedMessage);

        authenticate();
    }

    private void authenticate() {
        String username = "anonymous";
        String password = "anonymous";
        System.out.printf("Name (%s:%s): ", ipAddress, username);

        Scanner scn = new Scanner(System.in);
        username = scn.nextLine();

        if (username.equals(""))
            username = password;

        try {
            tcp.send("USER " + username);
            System.out.println(tcp.receive());
        } catch (IOException e) {
            System.err.printf("An error occured during authentication: %s\n", e.getMessage());
        }

        System.out.printf("Password: ");
        password = scn.nextLine();

        if (password.equals(""))
            password = username;

        try {
            tcp.send("PASS " + password);
            System.out.println(tcp.receive());
        } catch (IOException e) {
            System.err.printf("An error occured during authentication: %s", e.getMessage());
        }
    }

    public String readFile(String filename) {
        String receivedMessage = null;

        passiveMode();

        try {
            tcp.send("RETR " + filename);
            receivedMessage = transfer.receive();
            tcp.receive();
            System.out.println(tcp.receive());
            transfer.close();
        } catch (IOException e) {
            System.out.printf("Could not download file: %s\n", e.getMessage());
        }
        return receivedMessage;
    }

    public String listFiles() {
        String receivedMessage = null;

        passiveMode();

        try {
            tcp.send("LIST");
            receivedMessage = "";
            System.out.println(receivedMessage);

            String transferReceive = "";
            while (transferReceive != null) {
                transferReceive = transfer.receive();
                if (transferReceive != null)
                    receivedMessage += transferReceive + "\n";
            }
            tcp.receive();
            tcp.receive();
            transfer.close();
        } catch (IOException e) {
            System.err.printf("Could not list files: %s\n");
        }
        return receivedMessage;
    }

    public String pwd() {
        String receivedMessage = null;
        try {
            tcp.send("PWD");
            receivedMessage = tcp.receive();
        } catch (IOException e) {
            System.err.printf("Could not get working directory: %s\n", e.getMessage());
        }
        return receivedMessage;
    }

    public String cwd(String path) {
        String receivedMessage = null;
        try {
            tcp.send("CWD " + path);
            receivedMessage = tcp.receive();
        } catch (IOException e) {
            System.err.printf("Could not change working directory: %s\n", e.getMessage());
        }
        return receivedMessage;
    }

    public void passiveMode() {
        try {
            tcp.send("PASV");
            String receivedMessage = tcp.receive();
            int port = calculatePassivePort(receivedMessage);
            transfer.connect(ipAddress, port);
        } catch (IOException e) {
            System.err.printf("Could not enter passive mode: %s\n", e.getMessage());
        }
    }

    public static int calculatePassivePort(String passiveMessage) {
        String newString = passiveMessage.replace("227 Entering Passive Mode (", "");
        newString = newString.replace(").", "");

        String[] splittedString = newString.split(",");
        return Integer.parseInt(splittedString[4]) * 256 + Integer.parseInt(splittedString[5]);
    }

    public void disconnect() {
        try {
            tcp.send("QUIT");
            System.out.printf(tcp.receive());
            tcp.close();
        } catch (IOException e) {
            System.err.printf("Could not disconnect server: %s\n", e.getMessage());
        }
    }
}
