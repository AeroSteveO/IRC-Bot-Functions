/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package testbot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.hooks.managers.BackgroundListenerManager;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.dcc.ReceiveChat;
import org.pircbotx.Configuration.*;
import org.pircbotx.hooks.*;
import org.pircbotx.hooks.events.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Element;
import java.io.File;
import org.pircbotx.hooks.managers.BackgroundListenerManager;

/**
 *
 * @author Stephen
 */
public class TestBot {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Setup this bot
        BackgroundListenerManager BackgroundListener = new BackgroundListenerManager();
        
        Configuration.Builder configuration = new Configuration.Builder()
                .setName("TestBot") //Set the nick of the bot. CHANGE IN YOUR CODE
                .setLogin("LQ") //login part of hostmask, eg name:login@host
                .setAutoNickChange(true) //Automatically change nick when the current one is in use
                .setCapEnabled(true) //Enable CAP features
                .addAutoJoinChannel("#rapterverse")
//.addCapHandler(new TLSCapHandler(new UtilSSLSocketFactory().trustAllCertificates(), true))
                .setAutoReconnect(true)
                .setMaxLineLength(425)
                .setListenerManager(BackgroundListener)//Allow for logger background listener
                .addListener(new Why())
                .addListener(new Ignite())
                .addListener(new Laser())
                .addListener(new Logger())
                .addListener(new RandChan())
                .addListener(new Shakespeare())
                .addListener(new GameBomb())
                .addListener(new GameMasterMind())
                .addListener(new GameHangman())
                .addListener(new GameOmgword())
                .addListener(new GameReverse())
                .addListener(new GameAltReverse())
                .addListener(new EnglishSayings())
                .addListener(new SimplePing())
                .setServerHostname("irc.stevensnet.info"); //Join the official #pircbotx channel
                //.buildConfiguration();
        BackgroundListener.addListener(new Logger(),true); //Add logger background listener
        Configuration config = configuration.buildConfiguration();
        //bot.connect throws various exceptions for failures
        try {
            Global.bot = new PircBotX(config);
            //PircBotX bot = new PircBotX(configuration);
            //Connect to the freenode IRC network
            Global.bot.startBot();
        } //In your code you should catch and handle each exception seperately,
        //but here we just lump them all togeather for simpliciy
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}