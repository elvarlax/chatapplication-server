/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components.base;

import chatapplication_server.exception.ComponentInitException;

/**
 * This class serves as the basis for a multithreaded component that does NOT receive any
 * network messages. Classes that extend this class need to implement the componentMain()
 * method which serves as the main thread loop for the component.
 * In addition you can override the initialize() and shutdown() methods which implement
 * the IComponent interface to provide additional initialization/shutdown code for your
 * component. 
 * IMPORTANT If you do override them, please make sure that when your custom initialization and shutting down
 * is complete that you invoke the original methods that takes care of threading.
 * 
 * @author atgianne
 */
public abstract class GenericThreadedComponent implements IComponent, Runnable 
{
     /** Did we receive a signal to shut down */
    protected boolean mustShutdown = false;
    
    /** The internal thread handle for this component */
    protected Thread localThread;
    
    /**
     * Default implementation of the IComponent.initialize() method.
     * It just starts a thread for this component.
     * If you override this DO NOT forget to invoke it after you do your own
     * initialization.
     *
     * @see IComponent
     */
    public void initialize() throws ComponentInitException
    {
        mustShutdown = false;
        
        localThread = new Thread( this );
        localThread.start();
    }
    
    /**
     * Default implementation of IComponent.shutdown()
     *
     * @see IComponent
     */
    public void shutdown()
    {
        synchronized ( this )
        {
            mustShutdown = true;
        }
    }
    
    /**
     * Java thread entry point. Default action is to
     * invoke the componentMain function of the IComponent
     * interface.
     */
    public void run()
    {
        componentMain();
    }  
}
