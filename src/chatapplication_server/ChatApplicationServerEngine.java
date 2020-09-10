/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server;

import java.io.IOException;
import java.util.LinkedList;

/**
 * ChatApplicationServerEngine class is responsible for providing the appropriate interface in order for the system to correctly start up and 
 * shut down all the necessary components.
 * Provided method implementations::
 *     --startUpCAServer(): Boot up the ChatApplicationServer system when 'Mode=Server' is given as argument
 *     --shutDownCAServer(): Safely shut down the ChatApplicationServer system when 'Mode=Server' is given as argument
 *     --startUpCAClient(): Boot up the ChatApplicationClient system when 'Mode=Client' is given as argument
 *     --shutDownCAClient(): Safely shut down the ChatApplicationClient system when 'Mode=Client' is given as argument
 *
 * Furhtermore, the ChatApplicationServerEngine class provides a Main method in order for a user to be able to boot up the system through a
 * terminal command of type java -cp ChatApplication-Server.jar chatapplication_server.Main. 
 * 
 * 
 * @author atgianne
 */
public class ChatApplicationServerEngine 
{
    /** Singleton instance of this object... */
    private static ChatApplicationServerEngine instance = null;
    
    /**
     * Creates a new instance of LSCSEngine
     */
    public ChatApplicationServerEngine() {
    }
    
    /**
     * Get the LSCEngine object singleton instance.
     */
    private static ChatApplicationServerEngine getInstance()
    {
        if ( instance ==null )
            instance = new ChatApplicationServerEngine();
        
        return instance;
    }
    
    /**
     * Method implementation for successfully booting up the ChatApplicationServer system. It starts in a secure manner and order all the 
     * implemented ChatApplicationServer components.
     *
     */
    public void startUpCAServer()
    {
       System.out.println( "[ChatApplicationServer_Engine]: Booting up..." );
        
        /** Fetch the ComponentManager running instance */
        ComponentManager cm = ComponentManager.getInstance();
        
        /** Setup the list of the ChatApplicationServer system components that we want to run */
        /** IMPORTANT Add them in the order that they should be started */
        LinkedList ourComponents = new LinkedList();
        ourComponents.add( "chatapplication_server.components.ConfigManager" );
        ourComponents.add( "chatapplication_server.components.ChatApplicationServerPropertiesLoader" );
        ourComponents.add( "chatapplication_server.components.ServerSocketEngine.SocketServerGUI" );
        
        /** First start the Configuration Manager component since we must load to it the necessary passwords... */
        if ( !cm.startComponent( "chatapplication_server.components.ConfigManager" ) )
            System.exit( 1 );
        
        /** Then fire the other components up... */
        if ( !cm.startComponentsList( ourComponents ) )
            System.exit( 1 );
        
        System.err.println( "[ChatApplicationServer_Engine]: Booting sequence complete" );
    }
    
    /**
     * Method implementation for successfully booting up the ChatApplicationClient system. It starts in a secure manner and order all the 
     * implemented ChatApplicationClient components.
     *
     */
    public void startUpCAClient()
    {
       System.out.println( "[ChatApplicationClient_Engine]: Booting up..." );
        
        /** Fetch the ComponentManager running instance */
        ComponentManager cm = ComponentManager.getInstance();
        
        /** Setup the list of the ChatApplicationServer system components that we want to run */
        /** IMPORTANT Add them in the order that they should be started */
        LinkedList ourComponents = new LinkedList();
        ourComponents.add( "chatapplication_server.components.ConfigManager" );
        ourComponents.add( "chatapplication_server.components.ChatApplicationServerPropertiesLoader" );
        ourComponents.add( "chatapplication_server.components.ClientSocketEngine.ClientSocketGUI" );
        
        /** First start the Configuration Manager component since we must load to it the necessary passwords... */
        if ( !cm.startComponent( "chatapplication_server.components.ConfigManager" ) )
            System.exit( 1 );
        
        /** Then fire the other components up... */
        if ( !cm.startComponentsList( ourComponents ) )
            System.exit( 1 );
        
        System.err.println( "[ChatApplicationClient_Engine]: Booting sequence complete" );
    }
    
     /**
     * Method invoked when we want to normally shut down the ChatApplication system main method.
     * This results in shutting down all the active components of the ChatApplication system.
     */
    public void shutDownCA()
    {
        System.out.println( "[ChatApplication_Engine]: Shutting down..." );
        
        ComponentManager.getInstance().stopComponents();
        
        System.out.println( "[ChatApplication_Engine]: Shut down complete" );
        System.exit( 1 );
    }
    
    /**
     * Method for getting the mode of operation of Chat Application instance to run. Basically it will either fire up as a Server or a Client.
     * 
     * For the first what is expected is 'Mode=Server' whereas for the latter 'Mode=Client'
     * 
     * @param args The command line arguments given by the user
     * 
     * @return String holding the mode of operation; either running the Server or the Client 
     */
    private static String getCommandLineArgPasswd( String[] args )
    {
        /** Auxiliary objects... */
        int delimPos;
        String key, value;
        
        /** Get the passwords from the command line arguments... */
        for ( int i = 0; i < args.length; i++ )
        {
            /** Tokenize into key/value pair... */
            delimPos = args[i].indexOf( '=' );
            
            /** No '=' character found...ignore */
            if ( delimPos == -1 )
                continue;
            
            /** Check the case of the 'KeyStore.Password' key...Ignore upper case */
            if ( args[i].substring( 0, delimPos ).toLowerCase().equals( "mode" ) )
            {
                key = args[i].substring( 0, delimPos ).toLowerCase();
                value = args[i].substring( delimPos + 1 );
                return value;
            }
        }
        
        return "";
    }

  /**
     * Method implementation of the main logic of the ChatApplicationServer system.
     * IMPORTANT This method can only be called by a user in the case of a boot up procedure through a command terminal
     *
     * @param args The command line arguments
     */
    public static void main( String[] args )
    { 
        String mode = getCommandLineArgPasswd( args );
        
        System.out.println( args[0] );
        System.out.println( mode );
        
        if ( mode.equals( "Server" ) )
        {
            /** Boot up the ChatApplicationServer system... */
            ChatApplicationServerEngine.getInstance().startUpCAServer();
        }
        else if ( mode.equals( "Client" ) )
        {
            /** Boot up the ChatApplicationClient system... */
            ChatApplicationServerEngine.getInstance().startUpCAClient();
        }
        else
        {
            System.out.println( "No correct mode given - either 'Mode=Server' or 'Mode=Client'" );
            System.exit(1);
        }
        
        /** Wait for a keypress; prevent other threads from dying */
        try
        {
            System.in.read();
        }
        catch( IOException ioe ) {
        }
        
        /** Safely shut down the LSCS system... */
        ChatApplicationServerEngine.getInstance().shutDownCA();
    }
    
}
