/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dtu.appliedcrypto.SocketActionMessages;

import java.io.Serializable;

/**
 * This class defines the different type of messages that will be exchanged
 * between the Clients and the Server. When talking from a Java Client to a Java
 * Server a lot easier to pass Java objects, no need to count bytes or to wait
 * for a line feed at the end of the frame
 *
 * @author atgianne
 */
public class ChatMessage implements Serializable {
    protected static final long serialVersionUID = 1112122200L;

    // The different types of message sent by the Client

    // WHOISIN to receive the list of the users connected

    // MESSAGE an ordinary message

    // LOGOUT to disconnect from the Server

    public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2, PRIVATEMESSAGE = 3, INIT_KEY_EXCHANGE = 4,
            CONFIRM_KEY_EXCHANGE = 5;

    private final int type;

    private final byte[] message;

    private final String id;

    /**
     * Payload exchanged between entities
     * 
     * @param id      originId - represents sender in form of IP:PORT
     * @param type
     * @param message payload - encrypted/plaintext based on message type
     */
    public ChatMessage(String id, int type, byte[] message) {
        this.type = type;
        this.id = id;
        this.message = message;
    }

    public ChatMessage(String id, int type) {
        this.type = type;
        this.id = id;
        this.message = null;
    }

    // getters

    public String getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public byte[] getMessage() {
        return message;
    }
}
