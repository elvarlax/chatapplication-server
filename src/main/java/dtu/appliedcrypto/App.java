package dtu.appliedcrypto;

import java.io.IOException;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import dtu.appliedcrypto.chatapplication_server.ChatApplicationServerEngine;
import dtu.appliedcrypto.chatapplication_server.components.ConfigManager;

public class App {
    public static void setupSecurity() {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static ConfigManager parseArgs(String[] args) {
        ConfigManager config = ConfigManager.getInstance();

        String mode = ChatApplicationServerEngine.getCommandLineArgPasswd(args, "mode");
        String prop = ChatApplicationServerEngine.getCommandLineArgPasswd(args, "props", "chatapplication.properties");

        System.out.println(args[0]);
        System.out.println(mode);
        config.setValue("Mode", mode);

        System.out.println(args[0]);
        System.out.println(prop);
        config.setValue("PropertiesFile.Folder", prop);

        return config;
    }

    public static void main(String[] args) {
        setupSecurity();
        ConfigManager config = parseArgs(args);
        String mode = config.getValue("Mode");

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
