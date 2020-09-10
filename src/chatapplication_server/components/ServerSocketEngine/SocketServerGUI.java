/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components.ServerSocketEngine;

import chatapplication_server.ChatApplicationServerEngine;
import chatapplication_server.ComponentManager;
import chatapplication_server.components.ConfigManager;
import chatapplication_server.components.base.IComponent;
import chatapplication_server.exception.ComponentInitException;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author atgianne
 */
public class SocketServerGUI extends JFrame implements IComponent, ActionListener, WindowListener
{
    /** Instance of the ConfigManager component */
    ConfigManager configManager;
    
    /** Instance of the SocketServerEngine */
    private SocketServerEngine server;
    
    /** The Stop and Start buttons for the ChatApplication Server */
    private JButton stopStart;
    
   /** JTextArea for the chat room and the events */
   private JTextArea chat, event;
   
   /** The Server port number */
   private JTextField tPortNumber;
    
     /**
     * Singleton instance of the SocketServerEngine component
     */
    private static SocketServerGUI componentInstance = null;
    
    /**
     * Creates a new instance of SocketServerEngine
     */
    public SocketServerGUI() {
        /** Title of the Server window */
        super( "Chat Server" );
    }
    
    /**
     * Make sure that we can only get one instance of the SocketServerEngine component.
     * Implementation of the static getInstance() method.
     */
    public static SocketServerGUI getInstance()
    {
        if ( componentInstance == null )
            componentInstance = new SocketServerGUI();
        
        return componentInstance;
    }
    
    private void createWindow()
    {
        /** Add in the North Panel the Stop/Start button and the Port Number */
        JPanel north = new JPanel();
	north.add( new JLabel("Port number: ") );
	tPortNumber = new JTextField(configManager.getValue( "Server.PortNumber" ));
	north.add(tPortNumber);
        
        /** to stop or start the server, we start with "Start" */
	stopStart = new JButton("Start");
	stopStart.addActionListener( this );
	north.add( stopStart );
	add(north, BorderLayout.NORTH);
        
        /** the event and chat room */
	JPanel center = new JPanel(new GridLayout( 2,1 ));
	chat = new JTextArea(80,80);
	chat.setEditable( false );
	appendRoom("Chat room.\n");
	
        center.add(new JScrollPane(chat));
	event = new JTextArea(80,80);
	event.setEditable(false);
	appendEvent("Events log.\n");
	center.add(new JScrollPane(event));	
	add(center);
        
        /** need to be informed when the user click the close button on the frame */
	addWindowListener( this );
	setSize( 400, 600 );
	setVisible( true );
    }
    
    public void initialize() throws ComponentInitException
    {
        /** Get the running instance of the Configuration Manager component */
        configManager = ConfigManager.getInstance();
        
        server = SocketServerEngine.getInstance();
        
        /** Initialize the Server window to be displayed... */
        createWindow();
    }
    
    /**
     * Method for handling the Start/Stop actions when the respective button is clicked from the user
     * 
     * @param e The event capturing either the Start or the Stop operation
     */
    public void actionPerformed ( ActionEvent e )
    {
        /** If the SocketServerEngine is running...we have to notify it to stop */
        if ( server.getIsRunning() )
        {
            tPortNumber.setEditable( true );
            stopStart.setText( "Start" );
            server.shutdown();
            return;
        }

        /** Otherwise, start the Socket Server... */
        int port;
	try {
            port = Integer.parseInt(tPortNumber.getText().trim());
	}
	catch( Exception er ) 
        {
            appendEvent("Invalid port number");
            return;
	}
        
        /** Start the Socket Server... */
        try
        {
            if ( !tPortNumber.getText().equals( "" ) )
            {
                configManager.setValue("Server.PortNumber", tPortNumber.getText());
            }
            server.initialize();
            stopStart.setText( "Stop" );
            tPortNumber.setEditable( false );
        }
        catch ( ComponentInitException ie )
        {
            /** Safely shut down the system */
            ComponentManager.getInstance().fatalException( ie );
        }
    }
    
    /**
     * Method for adding string messages, to be displayed, to the text area of the center panel of the Server GUI.
     * 
     * @param str The String message to be added 
     */
    public void appendRoom( String str ) 
    {
        //System.out.println("append room: "+str);
	chat.append(str);
                
	chat.setCaretPosition(chat.getText().length() - 1);
    }
    
    /**
     * Method for adding string messages, to be displayed, to the event area of the center panel of the Server GUI.
     * 
     * @param str The String message to be added 
     */
    public void appendEvent( String str ) 
    {
        synchronized ( event )
        {
            event.append(str);
            event.setCaretPosition(chat.getText().length() - 1);
        }
    }
    
    /*
    * If the user click the X button to close the application
    * I need to close the connection with the server to free the port
    */
   public void windowClosing( WindowEvent e ) 
   {
        // if my Server exist
        if( server != null && server.isRunning ) 
        {
            server.shutdown();
        }
        // dispose the frame
        ComponentManager.getInstance().stopComponents();
   }
   // I can ignore the other WindowListener method
   @Override
   public void windowClosed(WindowEvent e) {}
   @Override
   public void windowOpened(WindowEvent e) {}
   public void windowIconified(WindowEvent e) {}
   public void windowDeiconified(WindowEvent e) {}
   public void windowActivated(WindowEvent e) {}
   public void windowDeactivated(WindowEvent e) {}
   
   /**
    * Method for overriding the IComponent.componentMain() method.
    */
   public void componentMain(){
       
   }
   
   /**
    * Method overriding the IComponetn.shutDown() method.
    * This is basically for disposing the ChatApplication Server window.
    */
   public void shutdown()
   {
       dispose();
   }
}
