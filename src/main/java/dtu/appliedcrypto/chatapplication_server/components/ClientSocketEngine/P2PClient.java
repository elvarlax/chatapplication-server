/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dtu.appliedcrypto.chatapplication_server.components.ClientSocketEngine;

import dtu.appliedcrypto.SocketActionMessages.ChatMessage;
import dtu.appliedcrypto.SocketActionMessages.ChatMessageType;
import dtu.appliedcrypto.chatapplication_server.certs.Certificates;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author atgianne
 */
public class P2PClient extends JFrame implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = -8800761976188484413L;

    private String host;
    private String port;
    private String alias;

    private final JTextField tfServer;
    private final JTextField tfPort;
    private final JTextField tfsPort;
    private final JLabel label;
    private final JTextField tf;
    private final JTextArea ta;
    protected boolean keepGoing;
    JButton Send, stopStart;
    JButton connectStop;

    /** Client Socket and output stream... */
    Socket socket = null;
    ObjectOutputStream sOutput;

    private ListenFromClient clientServer;

    /**
     * Flag indicating whether the Socket Server is running at one of the Clients...
     */
    boolean isRunning;

    /**
     * Flag indicating whether another client is connected to the Socket Server...
     */
    boolean isConnected;

    private final BigInteger secret;
    private Map<String, DHState> dhStates;
    private Map<String, SymmetricCipher> ciphers;
    private Map<String, Queue<String>> messageBuffer;
    // private final Certificates cetrificates;

    P2PClient(String userName) {
        super("P2P Client Chat");

        alias = userName;

        secret = DiffieHellman.generateRandomSecret();
        // cetrificates = new Certificates(alias, "");

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
        stopStart = new JButton("Start");
        stopStart.addActionListener(this);

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

        connectStop = new JButton("Connect");
        connectStop.addActionListener(this);

        Send = new JButton("Send");
        Send.addActionListener(this);
        Send.setVisible(false);
        JPanel southPanel = new JPanel();
        southPanel.add(connectStop);
        southPanel.add(Send);
        southPanel.add(stopStart);
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

        isRunning = false;
        isConnected = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object o = e.getSource();

        if (o == connectStop) {
            if (connectStop.getText().equals("Connect") && isConnected == false) {
                if (tfPort.getText().equals(ConfigManager.getInstance().getValue("Server.PortNumber"))) {
                    display("Cannot give the same port number as the Chat Application Server - Please give the port number of the peer client to communicate!\n");
                    return;
                }

                /** Connect to the Socket Server instantiated by the other client... */
                this.connect();
            } else if (connectStop.getText().equals("Disconnect") && isConnected == true) {
                this.disconnect();
            }
        } else if (o == Send) {
            /**
             * Try to send the message to the other communicating party, if we have been
             * connected...
             */
            if (isConnected == true) {
                this.send(tf.getText());
            }
        } else if (o == stopStart) {
            if (stopStart.getText().equals("Start") && isRunning == false) {
                clientServer = new ListenFromClient();
                clientServer.start();
                isRunning = true;
                stopStart.setText("Stop");
            } else if (stopStart.getText().equals("Stop") && isRunning == true) {
                clientServer.shutDown();
                clientServer.stop();
                isRunning = false;
                stopStart.setText("Start");
            }
        }
    }

    public void display(String str) {
        ta.append(str + "\n");
        ta.setCaretPosition(ta.getText().length() - 1);
    }

    /**
     * Method that is invoked when a client wants to connect to the Socket Server
     * spawn from another client in order to initiate their P2P communication.
     * 
     * @return TRUE if the connection was successful; FALSE otherwise
     */
    public boolean connect() {
        /* Try to connect to the Socket Server... */
        try {
            if (isConnected == false) {
                socket = new Socket(tfServer.getText(), Integer.parseInt(tfPort.getText()));

                sOutput = new ObjectOutputStream(socket.getOutputStream());
                isConnected = true;
                Send.setVisible(true);
                connectStop.setText("Disconnect");

                return true;
            }
        } catch (IOException eIO) {
            display("The Socket Server from the other side has not been fired up!!\nException creating new Input/output Streams: "
                    + eIO.getMessage() + "\n");
            isConnected = false;
            Send.setVisible(false);
            connectStop.setText("Connect");
            return false;
        }
        // if it failed not much I can so
        catch (Exception ec) {
            display("Error connecting to server:" + ec.getMessage() + "\n");
            isConnected = false;
            Send.setVisible(false);
            connectStop.setText("Connect");
            return false;
        }

        return true;
    }

    /**
     * Method that is invoked when we want do disconnect from a Socket Server (spawn
     * by another client); this, basically, reflects the stopping of a P2P
     * communication
     * 
     * @return TRUE if the disconnect was successful; FALSE, otherwise
     */
    public boolean disconnect() {
        /** Disconnect from the Socket Server that we are connected... */
        try {
            if (isConnected == true) {
                /** First, close the output stream... */
                sOutput.close();

                /** Then, close the socket... */
                socket.close();

                /** Re-initialize the parameters... */
                isConnected = false;
                Send.setVisible(false);
                connectStop.setText("Connect");

                return true;
            }
        } catch (IOException ioe) {
            display("Error closing the socket and output stream: " + ioe.getMessage() + "\n");

            /** Re-initialize the parameters... */
            isConnected = false;
            Send.setVisible(false);
            connectStop.setText("Connect");
            return false;
        }

        return true;
    }

    public boolean send(String str) {
        try {
            byte[] message = str.getBytes();
            sOutput.writeObject(new ChatMessage("", ChatMessageType.SECRET_MESSAGE, message));
            display("You: " + str);
            // sOutput.close();
            // socket.close();
        } catch (IOException ex) {
            display("The Client's Server Socket was closed!!\nException creating output stream: " + ex.getMessage());
            this.disconnect();
            return false;
        }

        return true;
    }

    private class ClientWorker extends Thread {
        boolean keepGoing;
        Socket socket;
        ObjectInputStream sInput;

        public ClientWorker(Socket socket) {
            try {
                this.socket = socket;
                this.sInput = new ObjectInputStream(socket.getInputStream());
                this.keepGoing = true;
            } catch (IOException ioe) {
                System.out.println("[P2PClient]:: Error firing up Socket Server " + ioe.getMessage());
            }
        }

        @Override
        public void run() {
            while (keepGoing) {
                try {
                    ChatMessage message = (ChatMessage) sInput.readObject();
                    String msg = new String(message.getMessage());
                    System.out.println("Msg:" + msg);
                    display(socket.getInetAddress() + ": " + socket.getPort() + ": " + msg);
                } catch (IOException ex) {
                    display("Could not ready correctly the messages from the connected client: " + ex.getMessage());
                    // clientConnect = false;
                    this.keepGoing = false;
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(P2PClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private class ListenFromClient extends Thread {
        boolean keepGoing;
        ServerSocket serverSocket;
        List<ClientWorker> clients;

        public ListenFromClient() {
            try {
                // the socket used by the server
                serverSocket = new ServerSocket(Integer.parseInt(tfsPort.getText()));
                ta.append("Server is listening on port:" + tfsPort.getText() + "\n");
                ta.setCaretPosition(ta.getText().length() - 1);
                // clientConnect = false;
                keepGoing = true;
            } catch (IOException ioe) {
                System.out.println("[P2PClient]:: Error firing up Socket Server " + ioe.getMessage());
            }
        }

        @Override
        public void run() {
            // infinite loop to wait for messages
            clients = new LinkedList<ClientWorker>();
            while (keepGoing) {
                /** Wait only when there are no connections... */
                try {
                    Socket socket = serverSocket.accept();
                    ClientWorker client = new ClientWorker(socket);
                    client.start();
                    clients.add(client);
                } catch (IOException ex) {
                    display("The Socket Server was closed: " + ex.getMessage());
                }

            }
        }

        public void shutDown() {
            try {
                keepGoing = false;
                for (ClientWorker clientWorker : clients) {
                    clientWorker.sInput.close();
                    clientWorker.socket.close();
                }
                clients = new LinkedList<ClientWorker>();

                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException ioe) {
                System.out.println("[P2PClient]:: Error closing Socket Server " + ioe.getMessage());
            }
        }
    }
}
