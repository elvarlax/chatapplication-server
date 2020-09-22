/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dtu.appliedcrypto.chatapplication_server.components.ClientSocketEngine;

import dtu.appliedcrypto.chatapplication_server.ComponentManager;
import dtu.appliedcrypto.chatapplication_server.crypto.StreamCipher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.GeneralSecurityException;

/**
 * @author atgianne
 */
public class ListenFromServer extends Thread {

    private final StreamCipher cipher;

    public ListenFromServer(StreamCipher cipher) {
        super();
        this.cipher = cipher;
    }

    public void run() {
        while (true) {
            ObjectInputStream sInput = ClientEngine.getInstance().getStreamReader();

            synchronized (sInput) {
                try {
                    byte[] cipherText = (byte[]) sInput.readObject();

                    String msg = cipher.decrypt(cipherText);

                    if (msg.contains("#")) {
                        ClientSocketGUI.getInstance().appendPrivateChat(msg + "\n");
                    } else {
                        ClientSocketGUI.getInstance().append(msg + "\n");
                    }
                } catch (IOException e) {
                    ClientSocketGUI.getInstance().append("Server has closed the connection: " + e.getMessage() + "\n");
                    ComponentManager.getInstance().fatalException(e);
                } catch (ClassNotFoundException cfe) {
                    ClientSocketGUI.getInstance().append("Server has closed the connection: " + cfe.getMessage());
                    ComponentManager.getInstance().fatalException(cfe);
                } catch (GeneralSecurityException e) {
                    ClientSocketGUI.getInstance().append("Descryption error: " + e.getMessage());
                }
            }
        }
    }
}
