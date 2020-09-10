/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.exception;

/**
 * This kind of exception is thrown whenever an attempt to load an invalid configuration property given by the user when booting up the ChatApplication Server side.
 * 
 * This mainly has to do with the loading of the server IP and the port number.
 * 
 * @author atgianne
 */
public class PropertyLoadException extends Exception
{
     /** The message contained in the exception */
    String msg;
    
    /** The kind of property for which an exception was thrown */
    String property;
    
    /** 
     * Creates a new instance of PropertyLoadException
     *
     * @param propertyID String describing the kind of loaded property
     * @param m String describing the type of exception error
     */
    public PropertyLoadException( String propertyID, String m ) 
    {        
        /** Get the suitable property given by the user */
        if ( propertyID.equals( "Server.PortNumber" ) )
            property = "'Port'";
        else if (propertyID.equals( "ConnectionHandlers.Number" ))
            property = "'ConnectionHandlers'";
        else
            property = "Unspecified Property Name";
    }
    
    /**
     * Standard Object.toString implementation.
     *
     * @return A string representation of the exception message 
     */
    public String toString()
    {
        return msg;
    }
    
    /**
     * Method for returning the correct name of the property value as used in the lotus-system.propery file.
     *
     * @return A string representation of the property name
     */
    public String propertyToString()
    {
        return property;
    }
    
    /**
     * Standard Exception.getMessage override implementation
     *
     * @return The exception message
     */
    public String getMessage()
    {
        return msg;
    }
}
