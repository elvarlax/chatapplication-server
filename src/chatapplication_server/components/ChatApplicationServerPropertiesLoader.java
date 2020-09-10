/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components;

import chatapplication_server.ComponentManager;
import chatapplication_server.components.base.IComponent;
import chatapplication_server.exception.ComponentInitException;
import chatapplication_server.exception.PropertyLoadException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * This class file is responsible for loading all configuration properties defined in the chatapplication-server.properties
 * file stored in the "dist" folder of the ChatApplicationServer project. These property values will be passed to
 * the Configuration Manager component instance in order to be accessible from all other implemented components.
 *
 * IMPORTANT NOTE In case of a property with a "null" (or no) value in the chatapplication-server.properties file an 
 *                exception will be thrown. The only difference is for the LogFile property which in case of a "null" value,
 *                the LogViewer component redirects logging to the standard Java output.
 *
 * 
 * @author atgianne
 */
public class ChatApplicationServerPropertiesLoader implements IComponent 
{
    /** Properties class object for holding LSCS configuration information */
    protected static Properties CSProps = new Properties();
    
    /** Instance of the ConfigManager component */
    ConfigManager configManager;
    
    /** Singleton instance of the LSCS PropertiesLoader component */
    private static ChatApplicationServerPropertiesLoader componentInstance = null;
    
    /** Creates a new instance of LSCSPropertiesLoader */
    public ChatApplicationServerPropertiesLoader() {
    }
    
    /**
     * Make sure that we can only get one instance of the LSCSPropertiesLoader component.
     * Implementation of the static getInstance() method.
     */
    public static ChatApplicationServerPropertiesLoader getInstance()
    {
        if ( componentInstance == null )
            componentInstance = new ChatApplicationServerPropertiesLoader();
        
        return componentInstance;
    }
    
     /**
     * Implementation of the IComponent.initialize method.
     * Load all the configuration properties from the lotus-server.properties file and pass them to the Configuration
     * Manager component.
     *
     * @see IComponent interface
     */
    public void initialize() throws ComponentInitException, PropertyLoadException
    {
        /** Get the running instance of the Comfiguration Manager component */
        configManager = ConfigManager.getInstance();
        
        /** The default value of the lotus-server.properties folder is "dist/chatapplication-server.properties" */
        configManager.setDefaultValue( "PropertiesFile.Folder", "chatapplication.properties" );
        
        /** Try to load the chatapplication-server.properties file */
        try
        {
            CSProps.load( new FileInputStream( configManager.getValue( "PropertiesFile.Folder" ) ) );
            
            /** Load the number of Connection Handlers that will reside in the ConnectionHandling poll for handling any received socket connections */
            if ( !checkPropertyValue( "ConnectionHandlers.Number", CSProps.getProperty( "ConnectionHandlers" ) ) )
                throw new PropertyLoadException( "ConnectionHandlers.Number", "\n[ChatApplicationServer.PropertiesLoader]: Exception while loading property " );
            
            /** Load the port number where the server is going to listen; if "null" or "" then the default value is 1500 */
             if ( !checkPropertyValue( "Server.PortNumber", CSProps.getProperty( "ServerPort" ) ) )
                configManager.setValue( "Server.PortNumber", "1500" );
             
             if ( !checkPropertyValue( "Server.Address", CSProps.getProperty( "ServerAddress" ) ) )
                configManager.setValue( "Server.PortNumber", "localhost" );
             
             if ( !checkPropertyValue( "Client.Username", CSProps.getProperty( "ClientUsername" ) ) )
                configManager.setValue( "Server.PortNumber", "Anonymous" );
        }
        catch( IOException e )
        {
            throw new ComponentInitException( "[ChatApplicationServer.PropertiesLoader]: IOException " + e.getMessage() );
        }
    }
    
     /**
     * Method for checking the value of a loaded property from the chatapplication-server.properties file.
     * If the value is not "null", it is passed to the Configuration Manager component in order to be accessible
     * from all other components; otherwise a false statement is returned.
     *
     * @param configID The name of the configuration property to be set
     * @param loadedProp String representation of the loaded property to be checked
     *
     * @return TRUE if the value is not "null"; FALSE otherwise
     */
    public boolean checkPropertyValue( String configValue, String loadedProp )
    {
        /** Check the property value and pass it to the ConfigManager component if not "null" */
        if ( !loadedProp.equals( "null" ) && !loadedProp.equals( "" ) )
        {
            configManager.setValue( configValue, loadedProp );
            return true;
        }
        
        return false;
    }
    
    /**
     * Implementation of the IComponent.componentMain method
     *
     * @see IComponent interface
     */
    public void componentMain() {
    }
    
    /**
     * Implementation of the IComponent.shutdown() method
     *
     * @see IComponent interface
     */
    public void shutdown() {
    }
}
