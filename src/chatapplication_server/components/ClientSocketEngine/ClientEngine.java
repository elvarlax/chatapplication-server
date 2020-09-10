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
import chatapplication_server.components.base.GenericThreadedComponent;
import chatapplication_server.exception.ComponentInitException;
import chatapplication_server.statistics.ServerStatistics;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.net.*;
import java.util.Scanner;

/**
 *
 * @author atgianne
 */
public class ClientEngine extends GenericThreadedComponent 
{
     /** Instance of the ConfigManager component */
    ConfigManager configManager;
    
    /** Object for printing the secure socket server configuration properties */
    ServerStatistics lotusStat;
    
    /** Flag indicating whether the Socket Server is running.... */
    boolean isRunning;
    
    /** The Socket connection to the Chat Application Server */
    private Socket socket;
    
    /** Socket Stream reader/writer that will be used throughout the whole connection... */
    private ObjectOutputStream socketWriter;
    private ObjectInputStream socketReader;
    
    /**
     * Singleton instance of the SocketServerEngine component
     */
    private static ClientEngine componentInstance = null;
    
    /**
     * Creates a new instance of SocketServerEngine
     */
    public ClientEngine() {
        isRunning = false;
    }
    
    /**
     * Make sure that we can only get one instance of the SocketServerEngine component.
     * Implementation of the static getInstance() method.
     */
    public static ClientEngine getInstance()
    {
        if ( componentInstance == null )
            componentInstance = new ClientEngine();
        
        return componentInstance;
    }
    
     /**
     * Implementation of IComponent.initialize method().
     * This method is called upon initialize of the ClientEngine component and handles any configuration that needs to be
     * done in the client before it connects to the Chat Application Server.
     * 
     * @see IComponent interface.
     */
    public void initialize() throws ComponentInitException
    {
        /** Get the running instance of the Configuration Manager component */
        configManager = ConfigManager.getInstance();
                
        /** For printing the configuration properties of the secure socket server */
        lotusStat = new ServerStatistics();
        
        /** Try and connect to the server... */
        try
        {
            socket = new Socket( configManager.getValue( "Server.Address" ), configManager.getValueInt( "Server.PortNumber" ) );
        }
        catch ( Exception e )
        {
            display( "Error connecting to the server:" + e.getMessage() + "\n" );
            ClientSocketGUI.getInstance().loginFailed();
            return;
        }
        
        /** Print that the connection was accepted */
        display( "Connection accepted: " + socket.getInetAddress() + ":" + socket.getPort() + "\n" );
        
        /** Create the read/write object streams... */
        try
        {
            /** Set up the stream reader/writer for this socket connection... */
            socketWriter = new ObjectOutputStream( socket.getOutputStream() );
            socketReader = new ObjectInputStream( socket.getInputStream() );
            
            /** Start the ListeFromServer thread... */
            new ListenFromServer().start();
        }
        catch ( IOException ioe )
        {
            display( "Exception creating new Input/Output Streams: " + ioe + "\n");
            ComponentManager.getInstance().fatalException(ioe);
        }
        
        /** Send our username to the server... */
        try
        {
            socketWriter.writeObject( configManager.getValue( "Client.Username" ) );
        }
        catch ( IOException ioe )
        {
            display( "Exception during login: " + ioe );
            shutdown();
            ComponentManager.getInstance().fatalException(ioe);
        }
        
        super.initialize();
    }
    
    /**
     * Method for displaying a message in the Client GUI
     * 
     * @msg The string message to be displayed
     */
    private void display( String msg )
    {
        ClientSocketGUI.getInstance().append( msg );
    }
    
    /**
     * Method for sending a message to the server
     * 
     * @param msg The message to be sent
     */
    public void sendMessage( ChatMessage msg )
    {
        try 
        {
            socketWriter.writeObject(msg);
        }
        catch( IOException e ) 
        {
            System.out.println("Aloha");
            display( "Exception writing to server: " + e );
        }
    }
    
    /**
     * Method holding the main logic of the Client Engine. It basically waits for inputs from the user to be sent to the Server.
     */
    public void componentMain()
    {
        while ( !mustShutdown )
        {
            /** Wait messages from the user... */
            try
            {
                Thread.sleep( 7000 );
            }
            catch ( InterruptedException ie )
            {
                
            }
            
            // read message from user
            //String msg = scan.nextLine();
            String msg = ClientSocketGUI.getInstance().getPublicMsgToBeSent();
            if ( msg.equals( "" ) )
                continue;
                
            // logout if message is LOGOUT
            if(msg.equalsIgnoreCase("LOGOUT")) {
                    sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
                    // break to do the disconnect
                    break;
            }
            // message WhoIsIn
            else if(msg.equalsIgnoreCase("WHOISIN")) {
                    sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));				
            }
            else if (msg.equalsIgnoreCase("PRIVATEMESSAGE")){				// default to ordinary message
                    sendMessage(new ChatMessage(ChatMessage.PRIVATEMESSAGE, msg));
            }
            else  {				// default to ordinary message
                    sendMessage(new ChatMessage(ChatMessage.MESSAGE, msg));
            }
        }
        
        shutdown();
    }
    
    public ObjectInputStream getStreamReader()
    {
        return socketReader;
    }
    
     /**
     * Override GenericThreadedComponent.shutdown() method.
     * Signal and wait until the ClientEngine thread, holding the secure socket connection, stops.
     * 
     * 
     * @see GenericThreadedComponent
     */
    public void shutdown() 
    {
         /** Close the secure socket server */
        try
        {
            synchronized ( socket)
            {                
                /** Shut down the Client Socket */
                socketReader.close();
                socketWriter.close();
                socket.close();
                
                isRunning = false;
                
                
                /** Print in the Event area of the Server Windows GUI the close operation of the Socket Server... */
                ClientSocketGUI.getInstance().append( "[CCEngine]:: Shutting down the Client Engine....COMPLETE (" + lotusStat.getCurrentDate() + ")\n" );
            }
        }
        catch ( Exception e )
        {
            /** Print to the logging stream that shutting down the Central System socket server failed */
            ClientSocketGUI.getInstance().append("[CCEngine]: Failed shutting down CS socket server -- " + e.getMessage() + " (" + lotusStat.getCurrentDate() + ")\n");
        }
        
        /** Invoke our parent's method to stop the thread running the secure socket server... */
        super.shutdown();
    }
}
