/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package testbot;

import org.pircbotx.PircBotX;

/**
 *
 * @author Steve-O
 * YES GASP I KNOW GLOBAL VARIABLES ARE REALLY NICE SOMETIMES
 *
 */
public class Global {
    public static String botOwner = new String(); //Updated in the Main .java file from Setings.XML
    public static String mainNick = new String(); //Updated in the Main .java file from Setings.XML
    public static String nickPass = new String(); //Updated in the Main .java file from Setings.XML
    public static boolean reconnect = true;
    public static PircBotX bot;
    public static String commandPrefix = "!";       // Not implemented yet in other functions
}