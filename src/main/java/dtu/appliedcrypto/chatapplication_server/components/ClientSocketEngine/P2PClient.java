/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dtu.appliedcrypto.chatapplication_server.components.ClientSocketEngine;

import dtu.appliedcrypto.SocketActionMessages.ChatMessage;
import dtu.appliedcrypto.SocketActionMessages.ChatMessageType;
import dtu.appliedcrypto.chatapplication_server.components.ConfigManager;
import dtu.appliedcrypto.chatapplication_server.crypto.DHState;
import dtu.appliedcrypto.chatapplication_server.crypto.DiffieHellman;
import dtu.appliedcrypto.chatapplication_server.crypto.SymmetricCipher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import java.net.*;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author atgianne
 */
public class P2PClient extends JFrame implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -8356039080334565759L;

    private final String host;
    private final String port;
    private final JTextField tfServer;
    private final JTextField tfPort;
    private final JTextField tfsPort;
    private final JLabel label;
    private final JTextField tf;
    private final JTextArea ta;
    protected boolean keepGoing;
    JButton send, start;

    private String id;
    private final BigInteger secret;
    private Map<String, DHState> dhStates;
    private Map<String, SymmetricCipher> ciphers;
    private Map<String, Queue<String>> messageBuffer;

    P2PClient() {
        super("P2P Client Chat");

        secret = DiffieHellman.generateRandomSecret();
        ciphers = new HashMap<String, SymmetricCipher>();
        dhStates = new HashMap<String, DHState>();
        messageBuffer = new HashMap<String, Queue<String>>();

        host = ConfigManager.getInstance().getValue("Server.Address");
        port = ConfigManager.getInstance().getValue("Server.PortNumber");

        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3, 1));
        // the server name anmd the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1, 5, 1, 3));
        // the two JTextField with default value for server address and port number
        tfServer = new JTextField(host);
        tfPort = new JTextField("" + port);
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);

        tfsPort = new JTextField(5);
        tfsPort.setHorizontalAlignment(SwingConstants.RIGHT);
        start = new JButton("Start");
        start.addActionListener(this);

        serverAndPort.add(new JLabel("Receiver's Port No:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel("Receiver's IP Add:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel(""));
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);

        // the Label and the TextField
        label = new JLabel("Enter message below", SwingConstants.LEFT);
        northPanel.add(label);
        tf = new JTextField();
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);

        // The CenterPanel which is the chat room
        ta = new JTextArea(" ", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(1, 1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);

        // ta2 = new JTextArea(80,80);
        // ta2.setEditable(false);
        // centerPanel.add(new JScrollPane(ta2));
        add(centerPanel, BorderLayout.CENTER);

        send = new JButton("Send");
        send.addActionListener(this);
        JPanel southPanel = new JPanel();
        southPanel.add(send);
        southPanel.add(start);
        JLabel lbl = new JLabel("Sender's Port No:");
        southPanel.add(lbl);
        tfsPort.setText("0");
        southPanel.add(tfsPort);
        add(southPanel, BorderLayout.SOUTH);

        this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        // setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();
        if (o == send) {
            if (tfPort.getText().equals(ConfigManager.getInstance().getValue("Server.PortNumber"))) {
                display("Cannot give the same port number as the Chat Application Server - Please give the port number of the peer client to communicate!\n");
                return;
            }
            this.send(tf.getText());
        }
        if (o == start) {
            new ListenFromClient().start();
        }
    }

    public void display(String str) {
        ta.append(str + "\n");
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    public boolean send(String str) {
        Socket socket;
        String destinationId;
        ChatMessage payload;
        ObjectOutputStream sOutput; // to write on the socket
        // try to connect to the server
        try {
            socket = new Socket(tfServer.getText(), Integer.parseInt(tfPort.getText()));
            destinationId = getId(socket);
        }
        // if it failed not much I can so
        catch (Exception ec) {
            display("Error connectiong to server:" + ec.getMessage() + "\n");
            return false;
        }

        /* Creating both Data Stream */
        try {
            // sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        try {
            switch (dhStates.getOrDefault(destinationId, DHState.NOT_INITIALIZED)) {
                case NOT_INITIALIZED:
                    // initialize DH key exchange
                    byte[] A = DiffieHellman.getPartialKey(this.secret).toByteArray();
                    payload = new ChatMessage(id, ChatMessageType.INIT_KEY_EXCHANGE, A);
                    sOutput.writeObject(payload);
                    dhStates.put(destinationId, DHState.INITIALIZED);
                case INITIALIZED:
                    // enqueue the message for later
                    Queue<String> buffer = getBuffer(destinationId);
                    buffer.add(str);
                    break;
                case ESTABLISHED:
                    SymmetricCipher cipher = ciphers.get(destinationId);
                    payload = new ChatMessage(id, ChatMessageType.SECRET_MESSAGE, cipher.encrypt(str));
                    sOutput.writeObject(payload);
                    display("You: " + str);
                    break;
            }
            sOutput.close();
            socket.close();
        } catch (IOException ex) {
            display("Exception creating new Input/output Streams: " + ex);
        } catch (GeneralSecurityException ex) {
            display("Encryption exception: " + ex);
        }

        return true;
    }

    private String getId(Socket socket) {
        return "localhost:" + socket.getPort();
    }

    private Queue<String> getBuffer(String destinationId) {
        if (!messageBuffer.containsKey(destinationId)) {
            messageBuffer.put(destinationId, new LinkedList<String>());
        }
        return messageBuffer.get(destinationId);
    }

    private class ListenFromClient extends Thread {
        public ListenFromClient() {
            keepGoing = true;
        }

        @Override
        public void run() {
            try {
                // the socket used by the server
                ServerSocket serverSocket = new ServerSocket(Integer.parseInt(tfsPort.getText()));
                // display("Server is listening on port:"+tfsPort.getText());
                ta.append("Server is listening on port:" + tfsPort.getText() + "\n");
                id = "localhost:" + tfsPort.getText();
                ta.setCaretPosition(ta.getText().length() - 1);

                // infinite loop to wait for connections
                while (keepGoing) {
                    // format message saying we are waiting

                    Socket socket = serverSocket.accept(); // accept connection

                    ObjectInputStream sInput = null; // to write on the socket

                    /* Creating both Data Stream */
                    try {
                        sInput = new ObjectInputStream(socket.getInputStream());
                    } catch (IOException eIO) {
                        display("Exception creating new Input/output Streams: " + eIO);
                    }

                    try {
                        ChatMessage message = (ChatMessage) sInput.readObject();
                        String senderId, plainText;

                        switch (message.getType()) {
                            case INIT_KEY_EXCHANGE:
                                // get sender id
                                senderId = message.getId();

                                // send him A
                                byte[] A = DiffieHellman.getPartialKey(secret).toByteArray();
                                ChatMessage payload = new ChatMessage(id, ChatMessageType.CONFIRM_KEY_EXCHANGE, A);
                                send(senderId, payload);
                                dhStates.put(senderId, DHState.INITIALIZED);

                            case CONFIRM_KEY_EXCHANGE:
                                // get sender id
                                senderId = message.getId();

                                // establish Key
                                BigInteger B = new BigInteger(message.getMessage());
                                byte[] K = DiffieHellman.getSharedKey(B, secret);
                                ciphers.put(senderId, new SymmetricCipher(K));
                                dhStates.put(senderId, DHState.ESTABLISHED);

                                // send outstanding messages
                                Queue<String> buffer = getBuffer(senderId);
                                sendBuffer(senderId, buffer);
                                break;
                            case SECRET_MESSAGE:
                                SymmetricCipher cipher = ciphers.get(message.getId());
                                plainText = cipher.decrypt(message.getMessage());
                                System.out.println("Msg:" + plainText);
                                display(socket.getInetAddress() + ":" + socket.getPort() + ": " + plainText);
                                break;
                            default:
                                plainText = new String(message.getMessage());
                                System.out.println("Msg:" + plainText);
                                display(socket.getInetAddress() + ":" + socket.getPort() + ": " + plainText);
                        }
                        sInput.close();
                        socket.close();
                    } catch (IOException ex) {
                        display("Exception creating new Input/output Streams: " + ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(P2PClient.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (GeneralSecurityException ex) {
                        Logger.getLogger(P2PClient.class.getName()).log(Level.SEVERE, null, ex);
                        display("Exception ecrypting/decrypting: " + ex);
                    }

                }
            }
            // something went bad
            catch (IOException e) {
                // String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e
                // + "\n";
                // display(msg);
            }
        }

        private String getSenderIP(String destination) {
            int index = destination.indexOf(":");
            String host = destination.substring(0, index);
            return host;
        }

        private int getSenderPort(String destination) {
            int index = destination.indexOf(":");
            int port = Integer.parseInt(destination.substring(index + 1));
            return port;
        }

        private void send(String destination, ChatMessage message) throws IOException {
            Socket socket = new Socket(getSenderIP(destination), getSenderPort(destination));
            ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
            sOutput.writeObject(message);
            sOutput.close();
            socket.close();
        }

        private void sendBuffer(String destination, Queue<String> buffer) throws IOException, GeneralSecurityException {
            if (buffer.isEmpty()) {
                return;
            }

            Socket socket = new Socket(getSenderIP(destination), getSenderPort(destination));
            ObjectOutputStream sOutput = new ObjectOutputStream(socket.getOutputStream());
            SymmetricCipher cipher = ciphers.get(destination);

            while (!buffer.isEmpty()) {
                String outMessage = buffer.poll();
                byte[] cipherText = cipher.encrypt(outMessage);
                ChatMessage payload = new ChatMessage(id, ChatMessageType.SECRET_MESSAGE, cipherText);
                sOutput.writeObject(payload);
                display("You: " + outMessage);
            }

            sOutput.close();
            socket.close();
        }
    }
}
