import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Created by AndersWOlsen on 06-03-2017.
 */

public class Main {
    public static void main(String[] args) {
        FTPClient ftp = new FTPClient("130.226.195.126");
        ftp.connect();

        Scanner scn = new Scanner(System.in);
        String userInput = null;

        boolean continueConnection = true;

        while (continueConnection) {
            System.out.printf("ftp> ");
            userInput = scn.nextLine();

            String[] userArguments = userInput.split(" ");

            switch (userArguments[0]) {
                case "pwd":
                    System.out.println(ftp.pwd());
                    break;
                case "cd":
                    ftp.cwd(userArguments[1]);
                    break;
                case "ls":
                    if (userArguments.length == 1)
                        System.out.println(ftp.listFiles());
                    else {
                        ftp.cwd(userArguments[1]);
                        System.out.println(ftp.listFiles());
                        ftp.cwd("..");
                    }
                    break;
                case "get":
                    System.out.println(ftp.readFile(userArguments[1]));
                    break;
                case "quit":
                    continueConnection = false;
                    break;
                case "exit":
                    continueConnection = false;
                    break;
            }
        }

        ftp.disconnect();
    }
}
