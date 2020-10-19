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
import dtu.appliedcrypto.chatapplication_server.crypto.RSACipher;
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

import org.apache.commons.lang3.SerializationUtils;

import java.net.*;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.security.cert.Certificate;
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
    private String id;
    private String senderId;

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

    private final BigInteger dhSecret;
    private Certificates certs;

    private Map<String, SymmetricCipher> ciphers;
    private Map<String, Queue<String>> messageBuffer;
    private Map<String, DHState> dhStates;

    P2PClient() {
        super("P2P Client Chat");

        ConfigManager config = ConfigManager.getInstance();

        host = config.getValue("Server.Address");
        port = config.getValue("Server.PortNumber");

        String keyStoreFile = config.getValue("KeyStore.File");
        String keyStoreSecret = config.getValue("KeyStore.Secret");
        String caFile = config.getValue("KeyStore.CA");
        String privateCertFile = config.getValue("KeyStore.Cert");
        try {
            if (caFile == null || privateCertFile == null) {
                // just load key store with pre-stored CA and private certificate
                certs = new Certificates(keyStoreFile, keyStoreSecret);
            } else {
                // load and store CA and private certificate
                certs = new Certificates(keyStoreFile, keyStoreSecret, caFile, privateCertFile);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        dhSecret = DiffieHellman.generateRandomSecret();
        // cetrificates = new Certificates(alias, "");

        ciphers = new HashMap<String, SymmetricCipher>();
        messageBuffer = new HashMap<String, Queue<String>>();
        dhStates = new HashMap<String, DHState>();

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

    public void stopClient(ClientWorker client) {
        try {
            client.socket.close();
            client.sInput.close();
        } catch (IOException ioe) {
            display("Could not close client");
        }
        clientServer.clients.remove(client);
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
                senderId = tfPort.getText();
                socket = new Socket(tfServer.getText(), Integer.parseInt(senderId));

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

    public boolean send(String message) {
        try {
            ChatMessage payload;

            switch (dhStates.getOrDefault(senderId, DHState.CERT_EXCHANGE_NOT_INITIALIZED)) {
                case KEY_ESTABLISHED:
                    SymmetricCipher cipher = ciphers.get(senderId);
                    byte[] cipherText = cipher.encrypt(message);
                    payload = new ChatMessage(id, ChatMessageType.SECRET_MESSAGE, cipherText);
                    sOutput.writeObject(payload);
                    display("You: " + message);
                    break;
                case CERT_EXCHANGE_NOT_INITIALIZED:
                    // initialize key exchange
                    payload = new ChatMessage(id, ChatMessageType.INIT_CERT_EXCHANGE, certs.getCert());
                    sOutput.writeObject(payload);
                    dhStates.put(senderId, DHState.CERT_EXCHANGE_INITIALIZED);
                default:
                    // enqueue messages
                    Queue<String> buffer = getBuffer(senderId);
                    buffer.add(message);
                    break;
            }
        } catch (IOException ex) {
            display("The Client's Server Socket was closed!!\nException creating output stream: " + ex.getMessage());
            this.disconnect();
            return false;
        } catch (GeneralSecurityException ex) {
            display("Encryption exception: " + ex);
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
                    String senderId = message.getId();
                    Certificate senderCert;
                    SymmetricCipher cipher;
                    ChatMessage payload;
                    String plainText;
                    byte[] A, B, K;

                    switch (message.getType()) {
                        case INIT_CERT_EXCHANGE:
                            // verify sender
                            senderCert = (Certificate) SerializationUtils.deserialize(message.getMessage());
                            certs.verify(senderCert);
                            certs.addCert(senderId, senderCert);

                            // respond with my certificate
                            payload = new ChatMessage(id, ChatMessageType.CONFIRM_CERT_EXCHANGE, certs.getCert());
                            send(senderId, payload);

                            // update state
                            dhStates.put(senderId, DHState.CERT_EXCHANGE_INITIALIZED);
                            break;
                        case CONFIRM_CERT_EXCHANGE:
                            // verify sender
                            senderCert = (Certificate) SerializationUtils.deserialize(message.getMessage());
                            certs.verify(senderCert);
                            certs.addCert(senderId, senderCert);

                            // send him A encrypted by his public key
                            A = DiffieHellman.getPartialKey(dhSecret).toByteArray();
                            A = RSACipher.encrypt(senderCert.getPublicKey(), A);
                            payload = new ChatMessage(id, ChatMessageType.INIT_KEY_EXCHANGE, A);
                            send(senderId, payload);

                            // update state
                            dhStates.put(senderId, DHState.KEY_EXCHANGE_INITIALIZED);
                            break;
                        case INIT_KEY_EXCHANGE:
                            // decrypt A
                            senderCert = certs.getCert(senderId);
                            A = RSACipher.decrypt(certs.getPrivateKey(), message.getMessage());
                            K = DiffieHellman.getSharedKey(new BigInteger(A), dhSecret);

                            // setup symmetric cipher
                            cipher = new SymmetricCipher(K);
                            ciphers.put(senderId, cipher);

                            // send him B encrypted by his public key
                            B = DiffieHellman.getPartialKey(dhSecret).toByteArray();
                            B = RSACipher.encrypt(senderCert.getPublicKey(), B);
                            payload = new ChatMessage(id, ChatMessageType.CONFIRM_KEY_EXCHANGE, B);
                            send(senderId, payload);

                            // update state
                            dhStates.put(senderId, DHState.KEY_ESTABLISHED);
                            break;
                        case CONFIRM_KEY_EXCHANGE:
                            // decrypt B
                            senderCert = certs.getCert(senderId);
                            B = RSACipher.decrypt(certs.getPrivateKey(), message.getMessage());
                            K = DiffieHellman.getSharedKey(new BigInteger(B), dhSecret);

                            // setup symmetric cipher
                            cipher = new SymmetricCipher(K);
                            ciphers.put(senderId, cipher);

                            // update state
                            dhStates.put(senderId, DHState.KEY_ESTABLISHED);

                            // send all buffered outgoing messages
                            Queue<String> buffer = getBuffer(senderId);
                            sendBuffer(senderId, buffer);
                            break;
                        case SECRET_MESSAGE:
                            // decode
                            cipher = ciphers.get(senderId);
                            plainText = cipher.decrypt(message.getMessage());
                            System.out.println("Msg:" + plainText);
                            display(socket.getInetAddress() + ":" + socket.getPort() + ": " + plainText);
                            break;
                        default:
                            plainText = new String(message.getMessage());
                            System.out.println("Msg:" + plainText);
                            display(socket.getInetAddress() + ": " + socket.getPort() + ": " + plainText);
                    }

                } catch (IOException ex) {
                    display("Could not ready correctly the messages from the connected client: " + ex.getMessage());
                    // clientConnect = false;
                    this.keepGoing = false;
                    stopClient(this);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(P2PClient.class.getName()).log(Level.SEVERE, null, ex);
                } catch (GeneralSecurityException ex) {
                    display("Crypto exception: " + ex.getMessage());
                }
            }
        }

        private void send(String destinationPort, ChatMessage message) throws IOException {
            int port = Integer.parseInt(destinationPort);
            Socket socket = new Socket("localhost", port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.writeObject(message);
            out.close();
            socket.close();
        }

        private void sendBuffer(String destinationPort, Queue<String> buffer)
                throws IOException, GeneralSecurityException {
            if (buffer.isEmpty()) {
                return;
            }

            int port = Integer.parseInt(destinationPort);
            Socket socket = new Socket("localhost", port);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            SymmetricCipher cipher = ciphers.get(destinationPort);

            while (!buffer.isEmpty()) {
                String outMessage = buffer.poll();
                byte[] cipherText = cipher.encrypt(outMessage);
                ChatMessage payload = new ChatMessage(id, ChatMessageType.SECRET_MESSAGE, cipherText);
                out.writeObject(payload);
                display("You: " + outMessage);
            }

            out.close();
            socket.close();
        }
    }

    private Queue<String> getBuffer(String destinationId) {
        if (!messageBuffer.containsKey(destinationId)) {
            messageBuffer.put(destinationId, new LinkedList<String>());
        }
        return messageBuffer.get(destinationId);
    }

    private class ListenFromClient extends Thread {
        boolean keepGoing;
        ServerSocket serverSocket;
        List<ClientWorker> clients;

        public ListenFromClient() {
            try {
                // the socket used by the server
                id = tfsPort.getText();
                serverSocket = new ServerSocket(Integer.parseInt(id));
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
