/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components.ServerSocketEngine;

import chatapplication_server.ComponentManager;
import chatapplication_server.components.ConfigManager;
import chatapplication_server.components.base.GenericThreadedComponent;
import chatapplication_server.exception.ComponentInitException;
import chatapplication_server.statistics.ServerStatistics;
import java.net.ServerSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import java.util.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;

/**
 *
 * @author atgianne
 */
public class SocketServerEngine extends GenericThreadedComponent
{
    /** Instance of the ConfigManager component */
    ConfigManager configManager;
    
    /** Flag indicating whether the Socket Server is running.... */
    boolean isRunning;
    
    /** Object for printing the secure socket server configuration properties */
    ServerStatistics lotusStat;
    
    /** Vector pool for keeping all the ConnectionHandlers in idle state waiting for new socket connections */
    Vector connectionHandlingPool;

    /** Vector holding the references to the connection handlers that are occupied by an established connection */
    Vector connHandlerOccp;
    
     /**
     * Singleton instance of the SocketServerEngine component
     */
    private static SocketServerEngine componentInstance = null;
    
    /** Object providing the secure server socket of the ChatApplication Central System  */
    ServerSocket ChatApplication_Server;
    
    /**
     * Creates a new instance of SocketServerEngine
     */
    public SocketServerEngine() {
        isRunning = false;
    }
    
    /**
     * Make sure that we can only get one instance of the SocketServerEngine component.
     * Implementation of the static getInstance() method.
     */
    public static SocketServerEngine getInstance()
    {
        if ( componentInstance == null )
            componentInstance = new SocketServerEngine();
        
        return componentInstance;
    }
    
    /**
     * Method for printing some information about the established (if any) socket connections to the CA socket server.
     * Actually, it checks the occupance pool to see how many connection handlers are assigned to sockets and then
     * it prints the corresponding information.
     *
     */
    public void printEstablishedSocketInfo()
    {
        /** Vector that will temporarily hold a clone of the occupance pool... */
        Vector occupance = new Vector();

        /** Take a clone of the occupance pool in order not to block it for a long period of time and loose any new incomaing connections */
        synchronized ( connHandlerOccp )
        {
            occupance = connHandlerOccp;
        }

        /** If there is no established connection...print it to the logging stream */
        if ( occupance.size() == 0 )
        {
            SocketServerGUI.getInstance().appendEvent("[SSEngine]:: There aren't any established client connections to the CA server (" + lotusStat.getCurrentDate() + ")\n" );
            return;
        }

        /** Then, for each connection handler that is occupied..print some information */
        for ( int i = 0; i < occupance.size(); i++ )
        {
            /** Get a Connection Handler reference... */
            SocketConnectionHandler sch = ( SocketConnectionHandler )occupance.get( i );

            /** Print the information... */
            sch.printSocketInfo();
        }
    }
    
    /**
     * Implementation of IComponent.initialize method().
     * This method is called upon initialize of the SocketServerEngine component and handles any configuration that needs to be
     * done in the socket server before it is ready for accepting chat-application client connections.
     * Actually, it creates the ConnectionHandling pool and adds the fired Connectionhandlers that are ready for
     * handling any connection attempts.
     *
     * Also, it starts the socket server.
     * 
     * @see IComponent interface.
     */
    public void initialize() throws ComponentInitException
    {
        /** Get the running instance of the Configuration Manager component */
        configManager = ConfigManager.getInstance();
                
        /** For printing the configuration properties of the secure socket server */
        lotusStat = new ServerStatistics();
        
        /** Initialize the vector holding information about the connection handlers... */
        connectionHandlingPool = new Vector();
        connHandlerOccp = new Vector();
        
        /** Set the default value of the number of SSLConnectionHandlers waiting in the connectionHandling pool */
        configManager.setDefaultValue( "ConnectionHandlers.Number", new Integer( 6 ).toString() );
        
        /** Start the connection handlers and add them in the pool... */
        SocketServerGUI.getInstance().appendEvent("[SSEngine]:: ConnectionHandling Pool (" + configManager.getValue( "ConnectionHandlers.Number" ) + ") fired up (" + lotusStat.getCurrentDate() + ")\n" );
        for ( int i = 0; i < configManager.getValueInt( "ConnectionHandlers.Number" ); i++ )
        {
            SocketConnectionHandler handler = new SocketConnectionHandler();
            
            /** Fire him up... */
            ( new Thread( handler, "CH #" + i ) ).start();
            
            /** Give an identifier name to this Connectionhandler thread... */
            handler.setHandlerIdentifierName( "CH #" + (i + 1) );
                        
            /** Add him in the pool... */
            connectionHandlingPool.addElement( handler );
        }
        SocketServerGUI.getInstance().appendEvent( "\n" );
        
        /** Set up the server for the Chat Application... */
        try
        {
            /** In case of  failure shut down the system */
            ChatApplication_Server = getServer();
            
            /** Indicate the start of the Socket Server Engine.... */
            isRunning = true;
        }
        catch( Exception e )
        {
            /** First print the exception to the current logging stream... */
            SocketServerGUI.getInstance().appendEvent("[SSEngine]:: Failed upon initialization of the CS socket server -- " + e.getMessage() + " (" + lotusStat.getCurrentDate() + ")\n");
            
            /** Safely shut down the system */
            ComponentManager.getInstance().fatalException( e );
        }
        
        /** Invoke our parent intialization method for starting the thread */
        super.initialize();
    }
    
    public ServerSocket getServer() throws Exception
    {
        /** The Socket used by the server */
        ServerSocket s = new ServerSocket( configManager.getValueInt( "Server.PortNumber" ) );
        
        return s;
    }
    
    /**
     * Method for removing a connection handler reference from the occupance pool holding all the handlers that are
     * currently assigned to a specific connection.
     * This method is called whenever a connection handler finished handling a connection and wants to get back in the
     * ConnectionHandling pool (in idle state) waiting for new connections. At the same time, it must be removed from the
     * occupied handlers.
     *
     * IMPORTANT NOTE It must run in a synchronized block
     *
     * @param sch The identification name of the Connection Handler that must be removed from the occupance pool
     */
    public void removeConnHandlerOccp( String sch )
    {
        /** A SocketConnectionHandler reference used for fetching already running handlers... */
        SocketConnectionHandler occupiedH = null;

        synchronized ( connHandlerOccp )
        {
            /** For each connection handler that is currently occupied... */
            for ( int i = 0; i < connHandlerOccp.size(); i++ )
            {
                /** Get its reference... */
                occupiedH = ( SocketConnectionHandler )connHandlerOccp.get( i );

                /** If this is the correct connection handler...remove it from the occupance pool */
                if ( occupiedH.getHandlerIdentifierName().equals( sch ) )
                {
                    connHandlerOccp.remove( i );
                    break;
                }
            }
        }
    }
    
    /**
     * Method for getting the reference to the connectionHandling pool containing all the handlers in idle state.
     * This method is called by a connection handler that was assigned to a socket connection that finished its work
     * and want to go back to idle state and thus add itself to the pool for future work.
     */
    public Vector getConnectionHandlingPool()
    {
        return connectionHandlingPool;
    }
    
    /**
     * Method for adding a Connection Handler with the specified name to the already existing ConnectionHandling
     * pool. This method is actually called by a Connection Handler which, for some reason, wants to shut down.
     * Also, this method is responsible for removing this Connection Handler reference from the occupance pool since it
     * finished handling a specific connection.
     * 
     * IMPORTANT NOTE The addition to the ConnectionHandling pool must be done in a synchronized block.
     *
     * @param handlerName The String identifier of the Connectionhandler to be added in the pool
     */
    public void addConnectionHandlerToPool( String handlerName )
    {
        /** Create a new Connection Handler... */
        SocketConnectionHandler handler = new SocketConnectionHandler();

        /** Fire him up... */
        ( new Thread( handler, handlerName ) ).start();

        /** Give an identifier name to this Connectionhandler thread... */
        handler.setHandlerIdentifierName( handlerName );
        
        synchronized ( connectionHandlingPool )
        {
            /** Add him in the pool... */
            connectionHandlingPool.addElement( handler );
            SocketServerGUI.getInstance().appendEvent("[SSEngine]:: " + handler.getHandlerIdentifierName() + " terminated...New reference back in the pool (" + lotusStat.getCurrentDate() + ")\n" );
        }
    }
    
    /**
     * The main logic of the SocketServerEngine. This mainly includes the acceptance of various client connections and assigns them to specific Connection Handlers
     */
    public void componentMain()
    {
        /** Object representing a secure socket connection that tries to get established */
        Socket s = null;
        
        /** In case of failure during establishment of a connection, type to the log the reason of rejection */
        try
        {
            while ( !mustShutdown )
            {
                /** Format message saying that we are waiting for new connections... */
                SocketServerGUI.getInstance().appendEvent( "[SSEngine]:: Waiting for clients on port " + configManager.getValue( "Server.PortNumber" ) + "\n" );
                
                s = ( Socket )ChatApplication_Server.accept();
                
                 /** Assign to it a connection handler from the pool... */
                SocketConnectionHandler socketHandler = null;
                
                /** Find the first idle handler in the pool */
                synchronized ( connectionHandlingPool )
                {
                    /** Check to see if the pool is full;in that case ignore the connection... */
                    if ( connectionHandlingPool.isEmpty() )
                    {
                        /** Keep track of this event in the logging stream... */
                        SocketServerGUI.getInstance().appendEvent("[SSEngine]:: No more ConnHandlers available for (" + s.getRemoteSocketAddress() + ") (" + lotusStat.getCurrentDate() + ")\n" );
                        
                        /** Continue your work... */
                        continue;
                    }
                    
                     /** Otherwise assign this handler to the incoming connection... */
                    socketHandler = ( SocketConnectionHandler )connectionHandlingPool.elementAt( 0 );
                    connectionHandlingPool.removeElementAt( 0 );
                    socketHandler.setSocketConnection( s );

                    /** Also put the reference of this occupied connection handler to the corresponding pool... */
                    synchronized ( connHandlerOccp )
                    {
                        connHandlerOccp.add( socketHandler );
                    }
                }
            }
        }
        catch ( SocketException se )
        {
            /** This kind of exception is thrown whenever the socket server is shutting down;so close the socket blocked */
            try
            {
                /** First we have to check if any connection has been created... */
                if ( s != null )
                    s.close();
            }
            catch ( Exception ex )
            {
                /** This exception is thrown whenever the blocked socket (waiting for new connections) could not be shut down... */
                SocketServerGUI.getInstance().appendEvent("[SSEngine]:: Failed shutting down blocked socket connection --" + ex.getMessage() + " (" + lotusStat.getCurrentDate() + ")\n" );
            }
        }
        catch ( Exception e )
        {
            /** This exception is thrown whenever (for some reason) the incoming connection could not be established... */
            SocketServerGUI.getInstance().appendEvent("[SSEngine]:: Failed establishing Connection (" + s.getRemoteSocketAddress() + ") -- " + e.getMessage() + " (" + lotusStat.getCurrentDate() + ")\n");
        }
    }
    
    public void writeMsgSpecificClient( int PortNo, String msg )
    {
        /** Vector that will temporarily hold a clone of the occupance pool... */
        Vector occupance = new Vector();

        /** Take a clone of the occupance pool in order not to block it for a long period of time and loose any new incomaing connections */
        synchronized ( connHandlerOccp )
        {
            occupance = connHandlerOccp;
        }

        /** If there is no established connection...print it to the logging stream */
        if ( occupance.size() == 0 )
        {
            SocketServerGUI.getInstance().appendEvent("[SSEngine]:: There aren't any established client connections to the CA server (" + lotusStat.getCurrentDate() + ")\n" );
            return;
        }

        /** Then, for each connection handler that is occupied..print some information */
        for ( int i = 0; i < occupance.size(); i++ )
        {
            /** Get a Connection Handler reference... */
            SocketConnectionHandler sch = ( SocketConnectionHandler )occupance.get( i );

            /** If this is the correct client... */
            if ( sch.getHandleSocket().getPort() == PortNo )
                sch.writeMsg( msg );
        }
    }
    
    /**
     * Method for broadcasting an event/message to all connected clients
     * 
     * @param message The message to be broadcasted
     */
    public void broadcast( String message )
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "HH:mm:ss" );
        
        // add HH:mm:ss and \n to the message
        String time = sdf.format(new Date());
        String messageLf = time + " " + message + "\n";
        
        /** Print the message on the Server GUI */
        SocketServerGUI.getInstance().appendRoom( messageLf );
        
        /** Vector that will temporarily hold a clone of the occupance pool... */
        Vector occupance = new Vector();

        /** Take a clone of the occupance pool in order not to block it for a long period of time and loose any new incomaing connections */
        synchronized ( connHandlerOccp )
        {
            occupance = connHandlerOccp;
        }

        /** If there is no established connection...print it to the logging stream */
        if ( occupance.size() == 0 )
        {
            SocketServerGUI.getInstance().appendEvent("[SSEngine]:: There aren't any established client connections to the CA server (" + lotusStat.getCurrentDate() + ")\n" );
            return;
        }

        /** Then, for each connection handler that is occupied..print some information */
        for ( int i = 0; i < occupance.size(); i++ )
        {
            /** Get a Connection Handler reference... */
            SocketConnectionHandler sch = ( SocketConnectionHandler )occupance.get( i );

           sch.writeMsg( messageLf );
        }
    }
    
    public boolean getIsRunning()
    {
        return isRunning;
    }
    
    /**
     * Override GenericThreadedComponent.shutdown() method.
     * Signal and wait until the SocketServerEngine thread, holding the secure socket server, stops.
     * First, we have to stop all the connection handlers running in an idle state in the connection pool.
     * 
     * 
     * @see GenericThreadedComponent
     */
    public void shutdown() 
    {
        /** Close the secure socket server */
        try
        {
            synchronized ( ChatApplication_Server )
            {                
                /** Shut down the Socket Server */
                ChatApplication_Server.close();
                isRunning = false;
                
                
                /** Print in the Event area of the Server Windows GUI the close operation of the Socket Server... */
                SocketServerGUI.getInstance().appendEvent( "[SSEngine]:: Shutting down the Socket Server....COMPLETE (" + lotusStat.getCurrentDate() + ")\n" );
            }
        }
        catch ( Exception e )
        {
            /** Print to the logging stream that shutting down the Central System socket server failed */
            SocketServerGUI.getInstance().appendEvent("[SSEngine]: Failed shutting down CS socket server -- " + e.getMessage() + " (" + lotusStat.getCurrentDate() + ")\n");
        }
        
        /** Invoke our parent's method to stop the thread running the secure socket server... */
        super.shutdown();
    }
}
