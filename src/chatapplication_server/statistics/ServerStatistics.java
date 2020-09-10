/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatapplication_server.statistics;

import java.util.Calendar;

/**
 * This class is just an auxiliary class for logging functionalities. It contains methods for printing statistics
 * related to the socket server running at the Chat Application.
 * 
 * It also provides some utility routines for printing information regarding clients connecting to the socket server.
 * 
 * @author atgianne
 */
public class ServerStatistics 
{
    /** Creates a new instance of ServerStatistics */
    public ServerStatistics() {
    }
    
     /**
     * Method for returning a String representation of the current date and time of the LSCS system.
     *
     * IMPORTANT NOTE Calendar component starts counting months from 0. Thus, an increment by 1 must be done
     *                in each month.
     *
     * @return A String representation of the current date and time
     */
    public String getCurrentDate()
    {
        String date;
        
        /** Create the String representation of the current date and time */
        date = new Integer( Calendar.getInstance().get( Calendar.DAY_OF_MONTH ) ).toString() + "/" +
               new Integer( Calendar.getInstance().get( Calendar.MONTH ) + 1 ).toString() + "/" + 
               new Integer( Calendar.getInstance().get( Calendar.YEAR ) ) + "---" +
               new Integer( Calendar.getInstance().get( Calendar.HOUR_OF_DAY ) ) + ":" + 
               new Integer( Calendar.getInstance().get( Calendar.MINUTE ) ) + ":" + 
               new Integer( Calendar.getInstance().get( Calendar.SECOND ) );
        
        return date;
    }
}
