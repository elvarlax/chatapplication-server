/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components.ClientSocketEngine;

import SocketActionMessages.ChatMessage;
import SocketActionMessages.EncryptedChatMessage;
import chatapplication_server.components.ConfigManager;
import chatapplication_server.crypto.StreamCipher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
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
    private final String dhKey = "111111111";
    private StreamCipher streamCipher;


    P2PClient() {
        super("P2P Client Chat");
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

//        ta2 = new JTextArea(80,80);
//        ta2.setEditable(false);
//        centerPanel.add(new JScrollPane(ta2));   
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

//        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
        ObjectOutputStream sOutput;        // to write on the socket
        // try to connect to the server
        try {
            socket = new Socket(tfServer.getText(), Integer.parseInt(tfPort.getText()));
        }
        // if it failed not much I can so
        catch (Exception ec) {
            display("Error connectiong to server:" + ec.getMessage() + "\n");
            return false;
        }

        /* Creating both Data Stream */
        try {
//			sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }
        if (dhKey == null) {
            //Key from diffie hellman
            String dhKey = "111111111";

            //Initialize a StreamCipher instance
            try {
                streamCipher = new StreamCipher(dhKey);
            } catch (NoSuchAlgorithmException nae) {
                display("Exception initializing nae: " + nae);
            }
        }

        try {
            ChatMessage cm = new ChatMessage(str.length(), str);
            EncryptedChatMessage ecm = new EncryptedChatMessage(cm, streamCipher);
            sOutput.writeObject(ecm);
            display("You: " + str);
            sOutput.close();
            socket.close();
        } catch (IOException ex) {
            display("Exception creating new Input/output Streams: " + ex);
        } catch (GeneralSecurityException gse) {
            display("Exception encrypting the message: " + gse);
        }

        return true;
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
                //display("Server is listening on port:"+tfsPort.getText());
                ta.append("Server is listening on port:" + tfsPort.getText() + "\n");
                ta.setCaretPosition(ta.getText().length() - 1);
                // infinite loop to wait for connections
                while (keepGoing) {
                    // format message saying we are waiting

                    Socket socket = serverSocket.accept();    // accept connection

                    ObjectInputStream sInput = null;        // to write on the socket

                    /* Creating both Data Stream */
                    try {
                        sInput = new ObjectInputStream(socket.getInputStream());
                    } catch (IOException eIO) {
                        display("Exception creating new Input/output Streams: " + eIO);
                    }
                    if (dhKey == null) {
                        //Key from diffie hellman
                        String dhKey = "111111111";

                        //Initialize a StreamCipher instance
                        streamCipher = new StreamCipher(dhKey);
                    }

                    try {
                        EncryptedChatMessage ecm = (EncryptedChatMessage) sInput.readObject();
                        int decrType = Integer.parseInt(streamCipher.decrypt(ecm.getEncryptedType()[0], ecm.getEncryptedType()[1]));
                        String decrMessage = streamCipher.decrypt(ecm.getEncryptedMessage()[0], ecm.getEncryptedMessage()[1]);
                        ChatMessage cm = new ChatMessage(decrType, decrMessage);
                        String msg = cm.getMessage();
                        System.out.println("Msg:" + msg);
                        display(socket.getInetAddress() + ": " + socket.getPort() + ": " + msg);
                        sInput.close();
                        socket.close();
                    } catch (IOException ex) {
                        display("Exception creating new Input/output Streams: " + ex);
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(P2PClient.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (GeneralSecurityException gse) {
                        display("Exception decrypting the message: " + gse);
                    }

                }
            }
            // something went bad
            catch (IOException e) {
//            String msg = sdf.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
//			display(msg);
            } catch (NoSuchAlgorithmException e) {
                display("Exception establishing encryption: " + e);
            }
        }
    }
}
