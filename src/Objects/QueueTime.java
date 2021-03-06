/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Objects;

import testbot.Global;
import org.pircbotx.Channel;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Stephen
 * 
 * Requirements:
 * - APIs
 *    N/A
 * - Custom Objects
 *    N/A
 * - Linked Classes
 *    Global
 * 
 */
public  class QueueTime implements Runnable {
    int time;
    User user;
    Channel chan;
    int key;
    PircBotX bot;
    Thread t;
    
    public QueueTime(PircBotX bot, int time, Channel chan, User user, int key) {
        this.time = time;
        this.chan=chan;
        this.user=user;
        this.key=key;
        this.bot=bot;
    }
    
    public QueueTime(MessageEvent event, int time, int key) {
        this.time = time;
        this.chan = event.getChannel();
        this.user = event.getBot().getUserBot();
        this.key = key;
        this.bot = Global.bot;
    }
    
    public void interrupt(){
        this.t.interrupt();
    }
    
    public void giveT(Thread t) {
        this.t = t;
    }
    
    @Override
    public void run() {
        try { // No need to loop for this thread
            Thread.sleep(time*1000);
            bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent(Global.bot,chan,user,Integer.toString(key)));
        } catch (InterruptedException ex) {
            
        }
    }
}