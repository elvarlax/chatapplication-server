package SocketActionMessages;

import chatapplication_server.crypto.StreamCipher;

import java.io.Serializable;
import java.security.GeneralSecurityException;

public class EncryptedChatMessage implements Serializable {
    protected static final long serialVersionUID = 1112122200L;
    private final byte[][] type;

    private final byte[][] message;

    public EncryptedChatMessage(ChatMessage cm, StreamCipher sc) throws GeneralSecurityException {

        this.type = sc.encrypt(Integer.toString(cm.getType()));

        this.message = sc.encrypt(cm.getMessage());

    }

    public byte[][] getEncryptedType() {

        return type;

    }

    public byte[][] getEncryptedMessage() {

        return message;

    }
}