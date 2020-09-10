/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components.ClientSocketEngine;

import SocketActionMessages.ChatMessage;
import chatapplication_server.ComponentManager;
import chatapplication_server.components.ConfigManager;
import chatapplication_server.components.ServerSocketEngine.SocketServerEngine;
import chatapplication_server.components.ServerSocketEngine.SocketServerGUI;
import chatapplication_server.components.base.IComponent;
import chatapplication_server.exception.ComponentInitException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

/**
 *
 * @author atgianne
 */
public class ClientSocketGUI extends JFrame implements IComponent, ActionListener, WindowListener 
{
     /** Instance of the ConfigManager component */
    ConfigManager configManager;
    
    /** will first hold "Username:", later on "Enter message" */
    private JLabel label;
    
    /** to hold the Username and later on the messages */
    private JTextField tf;
	
    /** to hold the server address an the port number */
    private JTextField tfServer, tfPort,textPortNo;
    
    /** The Client socket to be connected to the server... */
    private ClientEngine client;
    
    /** to Logout and get the list of the users */
    /** whoIsIn refers to online users */
    private JButton login, logout, whoIsIn,PrivateChat,PublicChat,SendButton, p2pClient;
    
    /** for the chat room */
    private JTextArea ta;
    private JTextArea ta2,ta3;
    private JFrame f;
    
     /**
     * Singleton instance of the SocketServerEngine component
     */
    private static ClientSocketGUI componentInstance = null;
    
    /**
     * Creates a new instance of SocketServerEngine
     */
    public ClientSocketGUI() {
        /** Title of the Server window */
        super( "Chat Client" );
    }
    
    /**
     * Make sure that we can only get one instance of the SocketServerEngine component.
     * Implementation of the static getInstance() method.
     */
    public static ClientSocketGUI getInstance()
    {
        if ( componentInstance == null )
            componentInstance = new ClientSocketGUI();
        
        return componentInstance;
    }
    
    private void createWindow()
    {
        // The NorthPanel with:
        JPanel northPanel = new JPanel(new GridLayout(3,1));
        // the server name anmd the port number
        JPanel serverAndPort = new JPanel(new GridLayout(1,5, 1, 3));
        // the two JTextField with default value for server address and port number
        tfServer = new JTextField( configManager.getValue( "Server.Address" ) );
        tfPort = new JTextField("" + configManager.getValue( "Server.PortNumber" ));
        tfPort.setHorizontalAlignment(SwingConstants.RIGHT);
        
        serverAndPort.add(new JLabel("Server Address:  "));
        serverAndPort.add(tfServer);
        serverAndPort.add(new JLabel("Port Number:  "));
        serverAndPort.add(tfPort);
        serverAndPort.add(new JLabel(""));
        // adds the Server an port field to the GUI
        northPanel.add(serverAndPort);
        
        // the Label and the TextField
        label = new JLabel("Enter your username below", SwingConstants.CENTER);
        northPanel.add(label);
        tf = new JTextField( configManager.getValue( "Client.Username" ) );
        tf.setBackground(Color.WHITE);
        northPanel.add(tf);
        add(northPanel, BorderLayout.NORTH);
        
        // The CenterPanel which is the chat room
        ta = new JTextArea("Public Chat room\n", 80, 80);
        JPanel centerPanel = new JPanel(new GridLayout(2,1));
        centerPanel.add(new JScrollPane(ta));
        ta.setEditable(false);

        ta2 = new JTextArea(80,80);
        ta2.setEditable(false);
        centerPanel.add(new JScrollPane(ta2));

        add(centerPanel, BorderLayout.CENTER);
        
        // the 3 buttons
        login = new JButton("Login");
        login.addActionListener(this);
        PrivateChat = new JButton("Private Chat");
        PrivateChat.addActionListener(this);
        //PublicChat = new JButton("Public Chat");
        //PublicChat.addActionListener(this);
        logout = new JButton("Logout");
        logout.addActionListener(this);
        logout.setEnabled(false);		// you have to login before being able to logout
        whoIsIn = new JButton("Online Users");
        whoIsIn.addActionListener(this);
        whoIsIn.setEnabled(false);		// you have to login before being able to Who is in

        //P2P Client Button
        p2pClient=new JButton("P2P");
        p2pClient.addActionListener(this);
        
        JPanel southPanel = new JPanel();
        southPanel.add(login);
        southPanel.add(logout);
        southPanel.add(whoIsIn);
        southPanel.add(PrivateChat);
        //southPanel.add(PublicChat);
        southPanel.add(p2pClient);
        add(southPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 600);
        setVisible(true);
        tf.requestFocus();
        
        /// PRIVATE CHAT WINDOW ///                                         
        JLabel labelUsername = new JLabel("Enter username: ");
        JLabel labelPassword = new JLabel("Enter Port No : ");
        JTextField textUsername = new JTextField(20);
        textPortNo = new JTextField(20);
        SendButton = new JButton("Send");
        f = new JFrame(configManager.getValue( "Client.Username" ));
        f.setVisible(false);
        f.getContentPane().setLayout(new GridBagLayout ());
        Container contentPane = f.getContentPane();
        GridBagLayout layout = new GridBagLayout();
        contentPane.setLayout(layout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets(10, 10, 10, 10);

        // add components to the panel
        constraints.gridx = 0;
        constraints.gridy = 0;     
        contentPane.add(labelUsername, constraints);

        constraints.gridx = 1;
        contentPane.add(textUsername, constraints);

        constraints.gridx = 0;
        constraints.gridy = 1;     
        contentPane.add(labelPassword, constraints);

        constraints.gridx = 1;
        contentPane.add(textPortNo, constraints);

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        ta3 = new JTextArea("", 30, 30);
        contentPane.add(ta3, constraints);


        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.gridwidth = 3;
        constraints.anchor = GridBagConstraints.CENTER;
        contentPane.add(SendButton, constraints);
        SendButton.addActionListener(this);
        setLocationRelativeTo(null);
        f.pack();
    }
    
     public void initialize() throws ComponentInitException
    {
        /** Get the running instance of the Configuration Manager component */
        configManager = ConfigManager.getInstance();
        
        client = ClientEngine.getInstance();
        
        /** Initialize the Server window to be displayed... */
        createWindow();
    }
     
     /**
      * Method invoked by the Client to append text in the text area
      * 
      * @param str The string message to be displayed
      */
     public void append( String str )
     {
         String NewStr = " "; 
         String NewStr2 = " ";
        int j=0;
        // parse string
         String[] stringArray = str.split(",");
         if ("0".equals(stringArray[0]))
                NewStr="No Online Users \n";
         else if ("10".equals(stringArray[0]))
         {
             for (int i = 1; i < stringArray.length-1; i=i+3) {
                  //j=j+1;
             NewStr = NewStr.concat(stringArray[i] + " at Port No: "+ stringArray[i+1] + "\n" );
             //System.out.println(j);

            }
         }
         else {
             ta.append(str);
             ta.setCaretPosition(ta.getText().length() - 1);
         }
             ta.append(NewStr2);
             ta.setCaretPosition(ta.getText().length() - 1);
             ta2.append(NewStr);
             ta2.setCaretPosition(ta2.getText().length() - 1);
     }
     
     /**
      * Method for printing a message in a private chat when listening from the server.
      * 
      * @param msg The message from the server 
      */
     public void appendPrivateChat ( String msg )
     {
         f.setVisible(true);
         ta3.append(msg);
         ta3.setCaretPosition(ta3.getText().length() - 1);
     }
     
     /**
      * Method for capturing the cliec events of the various buttons of the Client GUI
      * 
      * @param e The Action Event of the button that was clicked
      */
     public void actionPerformed( ActionEvent e )
     {
         String username = "";
         String portNumber = "";
         
         Object o = e.getSource();
         
         /** If it is the logout operation... */
         if ( o == logout )
         {
             client.sendMessage( new ChatMessage(ChatMessage.LOGOUT, "") );
             
             /** Disable the logout and other button */
             logout.setEnabled( false );
             whoIsIn.setEnabled( false );
             
             /** Enable the login button... */
             login.setEnabled( true );
             
             label.setText("Enter your username below");
             return;
         }
         /** if it is the WHOISIN button... */
         if ( o == whoIsIn )
         {
             client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
             return;
         }
         /** If this is for a private chat... */
         if ( o == PrivateChat )
         {
             f.setVisible( true );
             return;
         }
         /** If it is for sending a private message... */
         if ( o == SendButton )
         {
             // obtain a port number from j-label of the user it wants to talk to and send the message to server
            String privateMsg = textPortNo.getText()+ "," + ta3.getText()+ "-" + configManager.getValue( "Client.Username" ) +"-#";
            System.out.println("2nd window : "+ privateMsg );

            client.sendMessage(new ChatMessage(ChatMessage.PRIVATEMESSAGE, privateMsg));

            return;
         }
         
         /** P2P Chat Window */
         if ( o == p2pClient )
         {
             P2PClient p2p = new P2PClient();
         }
         
         /** If this is a login operation... */
         if ( o == login )
         {
             // ok it is a connection request
            username = tf.getText().trim();
           // empty username ignore it
           if(username.length() == 0)
           {
               append( "One of the server address, port number or username are missing - Please try again!!\n" );
               return;
           }
           // empty serverAddress ignore it
           String server = tfServer.getText().trim();
           if(server.length() == 0)
           {
               append( "One of the server address, port number or username are missing - Please try again!!\n" );
               return;
           }
          
           // empty or invalid port numer, ignore it
            portNumber = tfPort.getText().trim();
           if(portNumber.length() == 0)
           {
               append( "One of the server address, port number or username are missing - Please try again!!\n" );
               return;
           }
           
           /** Try to create a new client with the server... */
           try
            {
            /** Get the updates values for the server address, port and client username... */
            configManager.setValue( "Server.Address" , tfServer.getText());
            configManager.setValue( "Server.PortNumber" , tfPort.getText());
            configManager.setValue( "Client.Username" , tf.getText());
            
            tf.setText("");
            label.setText("Enter your message below");
            
            // disable login button
            login.setEnabled(false);
            // enable the 2 buttons
            logout.setEnabled(true);
            whoIsIn.setEnabled(true);
            // disable the Server and Port JTextField
            tfServer.setEditable(false);
            tfPort.setEditable(false);
            // Action listener for when the user enter a message
            tf.addActionListener(this);
            
            client.initialize();
            }
            catch ( ComponentInitException ie )
            {
                /** Safely shut down the system */
                ComponentManager.getInstance().fatalException( ie );
            }
         }
     }
     
     /**
      * Method that is invoked when the user login has failed so as to correctly re-initialize the buttons
      */
     public void loginFailed()
     {
          /** Disable the logout and other button */
        logout.setEnabled( false );
        whoIsIn.setEnabled( false );

        /** Enable the login button... */
        login.setEnabled( true );
        
        // disable the Server and Port JTextField
        tfServer.setEditable(true);
        tfPort.setEditable(true);
        
        label.setText("Enter your username below");
        
        tf.removeActionListener(this);
     }
     
     public String getPublicMsgToBeSent()
     {
         return tf.getText();
     }
     
     /**
    * Method for overriding the IComponent.componentMain() method.
    */
   public void componentMain(){
       
   }
   
   /*
    * If the user click the X button to close the application
    * I need to close the connection with the server to free the port
    */
   public void windowClosing( WindowEvent e ) 
   {
        /** If the Client socket connection is open */
       if ( client != null && client.isRunning )
       {
           client.shutdown();
       }
       
       // dispose the frame
        ComponentManager.getInstance().stopComponents();
   }
   // I can ignore the other WindowListener method
   public void windowClosed(WindowEvent e) {}
   public void windowOpened(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowActivated(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   
   /**
    * Method overriding the IComponetn.shutDown() method.
    * This is basically for disposing the ChatApplication Server window.
    */
   public void shutdown()
   {
       dispose();
   }
}
