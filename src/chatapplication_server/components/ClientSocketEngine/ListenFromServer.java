/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components.ClientSocketEngine;

import chatapplication_server.ComponentManager;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 *
 * @author atgianne
 */
public class ListenFromServer extends Thread 
{
    public void run()
    {
        while(true) {
                ObjectInputStream sInput = ClientEngine.getInstance().getStreamReader();
                
                synchronized( sInput )
                {
                    try
                    {
                        String msg = (String) sInput.readObject();
                    
                        if(msg.contains( "#" ))
                        {
                            ClientSocketGUI.getInstance().appendPrivateChat(msg + "\n");
                        }
                        else
                        {
                            ClientSocketGUI.getInstance().append(msg + "\n");
                        }
                    }
                    catch(IOException e) 
                    {
                        ClientSocketGUI.getInstance().append( "Server has closed the connection: " + e.getMessage() +"\n" );
                        ComponentManager.getInstance().fatalException(e);
                    }
                    catch(ClassNotFoundException cfe) 
                    {
                        ClientSocketGUI.getInstance().append( "Server has closed the connection: " + cfe.getMessage() );
                        ComponentManager.getInstance().fatalException(cfe);
                    }
                }
        }
    }
}
