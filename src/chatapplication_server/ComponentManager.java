/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server;

import java.lang.reflect.Method;
import java.util.LinkedList;

import chatapplication_server.components.base.*;
import chatapplication_server.exception.ComponentInitException;
import chatapplication_server.exception.PropertyLoadException;
import java.lang.reflect.InvocationTargetException;

/**
 * The component manager handles the starting and stopping of all system components.
 * It also handles gracefull error recovery by terminating any running components
 * and then quitting the program with an exit code equal to 1.
 * 
 * @author atgianne
 */
public class ComponentManager 
{
    /** The list of all active system components */
    LinkedList activeComponents = new LinkedList();
    
    /**
     * Boolean flag set to TRUE if we are throwing a fatalException
     * This avoids cascading loops of fatalExceptions
     */
    boolean handlingFatalException;
    boolean shuttingDown;
    
    /** Singleton instance of this component */
    private static ComponentManager componentInstance = null;
    
    /** 
     * Creates a new instance of ComponentManager.
     * Make sure that we can only get an instance of the ComponentManger by only invoking the
     * getInstance() method. 
     */
    private ComponentManager() 
    {
        handlingFatalException = false;
        shuttingDown = false;
    }
    
    /**
     * Implementation of the getInstance() method.
     * It returns the running instance of the ComponentManager
     */
    public static ComponentManager getInstance()
    {
        if ( componentInstance == null )
            componentInstance = new ComponentManager();
        
        return componentInstance;
    }
    
    /** 
     * Start the given list of components.
     *
     * @param componentNames A list containing the names of the components to be started
     * @retun FALSE on error; TRUE otherwise
     */
    public boolean startComponentsList( LinkedList componentNames )
    {
        String nextComponent;
        
        try
        {
            for ( int i = 1; i < componentNames.size(); i++ )
            {
                nextComponent = ( String )componentNames.get( i );
                
                /** Try to instansiate the component and get its IComponent interface */
                Class c = Class.forName( nextComponent );
                Method m = c.getMethod( "getInstance" );
                IComponent com = ( IComponent )m.invoke( null );
                
                /** Try to start the component */
                System.err.println( "[ChatApplicationServer_ComponentManager]: Starting " + nextComponent );
                com.initialize();
                
                /** Add to active components list in reverse order */
                activeComponents.addFirst( com );
            }
        }
        catch (ComponentInitException c)
        {
            fatalException( c );
        }
        catch ( PropertyLoadException p )
        {
            fatalException( p );
        }
        catch( ClassNotFoundException e )
        {
            fatalException( e );
        }
        catch(NoSuchMethodException e)
        {
            fatalException( e );
        }
        catch(IllegalAccessException e)
        {
            fatalException( e );
        }
        catch(InvocationTargetException e)
        {
            fatalException( e );
        }
        
        return true;
    }
    
    /** 
     * Start the component corresponding to the given name.
     *
     * @param componentNames String name of the component to be started
     * @retun FALSE on error; TRUE otherwise
     */
    public boolean startComponent( String componentName )
    {
        try
        {
            /** Try to instansiate the component and get its IComponent interface */
            Class c = Class.forName( componentName );
            Method m = c.getMethod( "getInstance" );
            IComponent com = ( IComponent )m.invoke( null );

            /** Try to start the component */
            System.err.println( "[ChatApplicationServer_ComponentManager]: Starting " + componentName );
            com.initialize();

            /** Add to active components list in reverse order */
            activeComponents.addFirst( com );
        }
        catch( ComponentInitException e )
        {
            fatalException( e );
        }
        catch ( PropertyLoadException e )
        {
            fatalException( e );
        }
        catch( ClassNotFoundException e )
        {
            fatalException( e );
        }
        catch(NoSuchMethodException e)
        {
            fatalException( e );
        }
        catch(IllegalAccessException e)
        {
            fatalException( e );
        }
        catch(InvocationTargetException e)
        {
            fatalException( e );
        }
        
        return true;
    }
    
    /**
     * Shut down all the active ChatApplicationServer components in the reverse order from which 
     * they were started.
     */
    public void stopComponents()
    {
        /** Signal also the ComponentManger that it must terminate its execution */
        synchronized ( this )
        {
            shuttingDown = true;
        }
        
        synchronized ( activeComponents )
        {
            for ( int i = (activeComponents.size() - 1); i >= 0; i-- )
            {                
                /** Get the IComponent interface for this component */
                IComponent com = ( IComponent )activeComponents.get( i );
                
                /** Stop the component */
                System.out.println( "[ChatApplicationServer_ComponentManger]: Stopping " + com.getClass().getName() );
                com.shutdown();
            }
            
            /** Clear the activeComponent list */
            activeComponents.clear();
            
            System.exit( 1 );
        }
    }
    
    /**
     * Method invoked when a fatal exception occurs. Cleanly shut down the LSCS system and exit.
     *
     * @param e A reference to the occurred exception
     */
    public synchronized void fatalException( Exception e )
    {
        synchronized ( this )
        {
            if ( handlingFatalException )
                System.exit( 1 );
            
            handlingFatalException = true;
        }
        
        System.err.println("-----------------------------------");
        System.err.println("[ChatApplicationServer_ComponentManager]: FATAL EXCEPTION...\n" + e.getMessage() + "\n");
        //e.printStackTrace();
        
        /** Shut down all the component */
        if ( !shuttingDown )
            stopComponents();
        
        /** Exit the LSCS system */
        System.exit( 1 );
    }
}
