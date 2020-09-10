/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package SocketActionMessages;

import java.io.Serializable;

/**
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server.
 * When talking from a Java Client to a Java Server a lot easier to pass Java objects, no
 * need to count bytes or to wait for a line feed at the end of the frame
 * 
 * @author atgianne
 */
public class ChatMessage implements Serializable 
{
    protected static final long serialVersionUID = 1112122200L;
 
    // The different types of message sent by the Client

    // WHOISIN to receive the list of the users connected

    // MESSAGE an ordinary message

    // LOGOUT to disconnect from the Server

    public static final int WHOISIN = 0, MESSAGE = 1, LOGOUT = 2,PRIVATEMESSAGE = 3;

    private int type;

    private String message;

     

    // constructor

    public ChatMessage(int type, String message) {

        this.type = type;

        this.message = message;

    }

     

    // getters

    public int getType() {

        return type;

    }

    public String getMessage() {

        return message;

    }
}
