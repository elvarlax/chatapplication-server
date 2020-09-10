/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.exception;

/**
 * This kind of exception is thrown whenever a ChatApplication system component fails to initialize correctly.
 * 
 * @author atgianne
 */
public class ComponentInitException extends Exception
{
    /** The message contained in the exception */
    String msg;
    
    /** 
     * Creates a new instance of ComponentInitException 
     *
     * @param m String describing the type of exception error
     */
    public ComponentInitException( String m ) 
    {
        msg = m;
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
     * Standard Exception.getMessage override implementation
     *
     * @return The exception message
     */
    public String getMessage()
    {
        return msg;
    }
}
