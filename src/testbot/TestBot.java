/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package testbot;

import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.Configuration.*;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ConnectEvent;
import org.pircbotx.hooks.events.KickEvent;
import org.pircbotx.hooks.events.MessageEvent;
import org.pircbotx.hooks.managers.BackgroundListenerManager;

/**
 *
 * @author Steve-O
 */
public class TestBot extends ListenerAdapter {
    
    @Override
    public void onMessage(final MessageEvent event) throws Exception {
        // in case something should be done here
    }
    @Override
    // Rejoin on Kick
    public void onKick(KickEvent event) throws Exception {
        if (event.getRecipient().getNick().equals(event.getBot().getNick())) {
            event.getBot().sendIRC().joinChannel(event.getChannel().getName());
        }
    }
    @Override
    // Set mode +B for Bots
    public void onConnect(ConnectEvent event) throws Exception {
        event.getBot().sendRaw().rawLine("mode " + event.getBot().getNick() + " +B");
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //Setup this bot
        BackgroundListenerManager BackgroundListener = new BackgroundListenerManager();
        
        Configuration.Builder configuration = new Configuration.Builder()
                .setName("TestBot")                    //Set the nick of the bot. CHANGE IN YOUR CODE
                .setLogin("LQ")                        // login part of hostmask, eg name:login@host
                .setAutoNickChange(true)               // Automatically change nick when the current one is in use
                .setCapEnabled(true)                   // Enable CAP features
                .addAutoJoinChannel("#rapterverse")
                .setAutoReconnect(true)
                .setMaxLineLength(425)                 // This is for the IRC networks I use, it can be increased/decreased as needed
                .setListenerManager(BackgroundListener)// Allow for logger background listener
                .addListener(new TestBot())
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
                .addListener(new GameGuessTheNumber())
                .addListener(new EnglishSayings())
                .addListener(new SimplePing())
                .addListener(new IMDB())
                .setServerHostname("irc.stevensnet.info"); //Join the official #pircbotx channel
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