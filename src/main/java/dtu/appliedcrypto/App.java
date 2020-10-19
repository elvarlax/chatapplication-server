package dtu.appliedcrypto;

import dtu.appliedcrypto.chatapplication_server.ChatApplicationServerEngine;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.Security;

public class App {
    public static void SetupSecurity() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        String mode = ChatApplicationServerEngine.getCommandLineArgPasswd(args);

        System.out.println(args[0]);
        System.out.println(mode);

        SetupSecurity();

        if (mode.equals("Server")) {
            /** Boot up the ChatApplicationServer system... */
            ChatApplicationServerEngine.getInstance().startUpCAServer();
        } else if (mode.equals("Client")) {
            /** Boot up the ChatApplicationClient system... */
            ChatApplicationServerEngine.getInstance().startUpCAClient();
        } else {
            System.out.println("No correct mode given - either 'Mode=Server' or 'Mode=Client'");
            System.exit(1);
        }

        /** Wait for a keypress; prevent other threads from dying */
        try {
            System.in.read();
        } catch (IOException ioe) {
        }

        /** Safely shut down the LSCS system... */
        ChatApplicationServerEngine.getInstance().shutDownCA();
    }
}
