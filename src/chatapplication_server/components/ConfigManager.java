/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.components;

import chatapplication_server.components.base.IComponent;
import java.util.HashMap;

/**
 * The ConfigManager components manages configuration values in the format [key, value].
 * This component loads config values given by the user during the execution of the ChatApplicationServer
 * system.
 * 
 * @author atgianne
 */
public class ConfigManager implements IComponent 
{
    /** HasMap used for storing all the config values */
    private HashMap configValues;
    
    /** Singleton Instance of this component */
    private static ConfigManager componentInstance = null;
    
    /** Creates a new instance of ConfigManager */
    public ConfigManager() {
    }
   
    /**
     * Implementation of the static getInstance() method.
     * It ensures that only one instance of the ConfigManages component is
     * active at any time. 
     */
    public static ConfigManager getInstance()
    {
        if ( componentInstance == null )
            componentInstance = new ConfigManager();
        
        return componentInstance;
    }
    
    /**
     * Default implementation of the IComponent.initialize()
     *
     * @see IComponent
     */
    public void initialize()
    {
        /** Initialize the map holding all the given system configuration values... */
        configValues = new HashMap();
    }
    
    /**
     * Default implementation of the IComponent.componentMain method.
     *
     * @see IComponent
     */
    public void componentMain() {}
    
    /**
     * Default implementation of the IComponent.shutdown method.
     *
     * @see IComponent
     */
    public void shutdown() {}
    
    /**
     * Method for getting a config value from the map given its key.
     * 
     * @param key The key to use for looking up the value
     *
     * @return A string with the stored value OR an empty string if no such key exists in the map
     */
    public String getValue( String key )
    {
        synchronized ( configValues )
        {
            /** If it is not in the map, return... */
            if ( !configValues.containsKey( key ) )
            {
                System.err.println( "Missing key [" +key + "]" );
                return "";
            }
            
            return ( String )configValues.get( key );
        }
    }
    
    /**
     * Method for getting a config value-as an integer- from the map given its key.
     *
     * @param key The key to use for looking up the value.
     *
     * @return The int value of the key or 0 if not found
     */
    public int getValueInt( String key )
    {
        synchronized( configValues )
        {
            /** Not in map */
            if( !configValues.containsKey(key) )
            {
                System.err.println("Missing key ["+key+"]");
                return 0;
            }
            
            return Integer.parseInt( (String)configValues.get(key) );
        }
    }
    
    /**
     * Method for getting a config value-as long integer- from the map given its key.
     *
     * @param key The key to use for looking up the value.
     *
     * @return The long value of the key or 0 if not found
     */
    public long getValueLong( String key )
    {
        synchronized( configValues )
        {
            /** Not in map */
            if( !configValues.containsKey(key) )
            {
                System.err.println("Missing key ["+key+"]");
                return 0;
            }
            
            return Long.parseLong( (String)configValues.get(key) );
        }            
    }
    
    /**
     * Method for getting a config value-as a float- from the map given its key.
     *
     * @param key The key to use for looking up the value.
     *
     * @return The float value of the key or 0 if not found
     */
    public float getValueFloat( String key )
    {
        synchronized( configValues )
        {
            /** Not in map */
            if( !configValues.containsKey(key) )
            {
                System.err.println("Missing key ["+key+"]");   
                return 0;
            }
            
            return Float.parseFloat( (String)configValues.get(key) );
        }            
    }
    
    /**
     * Set a config value from the map given its key. The method will overwrite
     * any existing value already stored in the map with the given key.
     *
     * @param key The key to use for looking up the value.
     * @param value The value to store in the map.
     */
    public void setValue( String key, String value )
    {
        synchronized( configValues )
        {            
           configValues.put(key, value);
        }            
    }
    
    /**
     * If the given key does not already exist in the map, set it up with the
     * given default key. This method is used by components to establish default
     * values for the initial run of the application.
     *
     * @param key The key to use for looking up the value.
     * @param value The value to store in the map.
     */
    public void setDefaultValue( String key, String value )
    {
        synchronized( configValues )
        {            
            if( !configValues.containsKey(key) )
            {
                configValues.put(key, value);
                
                System.out.println( "[ChatApplicationServer_ConfigManager]: Overriding config entry '" + key + "'" );
            }
        }            
    }    
}
