/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Wheatley;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.joda.time.DateTime;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
 * Based on the C# IRC bot, CasinoBot
 * which is generally unstable and requires windows to run
 *
 * Activate Command with:
 *      !bomb
 */
public class GameBomb extends ListenerAdapter {
    // Woohooo basic variables for junk
    int time = 10;
    String blockedChan = "#dtella";
    ArrayList<String> colorls = null;
    
    @Override
    public void onMessage(MessageEvent event) throws FileNotFoundException, InterruptedException {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        String gameChan = event.getChannel().getName();
        if (colorls == null) {
            colorls = getColorList();
        }
        if (message.equalsIgnoreCase("!bomb")&&!gameChan.equals(blockedChan)){
            String player = event.getUser().getNick();
            List<String> colours = new ArrayList<>();
            String colorlist = "";
            for (int i=0;i<5;i++){
                colours.add(colorls.get((int) (Math.random()*colorls.size()-1)).toLowerCase());
            }
            for (int i=0; i<colours.size()-1;i++){
                colorlist = colorlist + colours.get(i) + ", ";
            }
            
            colorlist = colorlist + colours.get(colours.size()-1);
            boolean running = true;
            String solution = colours.get((int) (Math.random()*colours.size()-1));
            event.respond("You recieved the bomb. You have " + time + " seconds to defuse it by cutting the right cable." + Colors.BOLD + " Choose your destiny:" + Colors.NORMAL);
            event.getBot().sendIRC().message(gameChan,"Wire colors include: " + colorlist);
            int key=(int) (Math.random()*100000+1);
            TimedWaitForQueue timedQueue = new TimedWaitForQueue(Global.bot,time,event.getChannel(),event.getUser(),key);
            while (running){
                MessageEvent CurrentEvent = timedQueue.waitFor(MessageEvent.class);
                
                if (CurrentEvent.getMessage().equalsIgnoreCase(Integer.toString(key))){
                    event.getBot().sendIRC().message(gameChan,"the bomb explodes in front of " + player + ". Seems like you did not even notice the big beeping suitcase.");
                    running = false;
                    timedQueue.close();
                }
                else if (CurrentEvent.getMessage().equalsIgnoreCase(solution)&&CurrentEvent.getUser().getNick().equalsIgnoreCase(player)){
                    event.getBot().sendIRC().message(gameChan, player + " defused the bomb. Seems like he was wise enough to buy a defuse kit." );
                    running = false;
                    timedQueue.close();
                }
                else if (!CurrentEvent.getMessage().equalsIgnoreCase(solution)&&CurrentEvent.getUser().getNick().equalsIgnoreCase(player)) {
                    event.getBot().sendIRC().message(gameChan,"The bomb explodes in " + player + "'s hands. You lost your life.");
                    running = false;
                    timedQueue.close();
                }
            }
            colours.clear();
        }
    }
    public ArrayList<String> getColorList() throws FileNotFoundException{
        try{
            Scanner wordfile = new Scanner(new File("colorlist.txt"));
            ArrayList<String> colorls = new ArrayList<String>();
            while (wordfile.hasNextLine()){
                colorls.add(wordfile.nextLine());
            }
            wordfile.close();
            return (colorls);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public class TimedWaitForQueue extends WaitForQueue{
        int time;
        public TimedWaitForQueue(PircBotX bot,int time, Channel chan,User user, int key) throws InterruptedException {
            super(bot);
            this.time=time;
            Thread.sleep(this.time*1000);
            bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent(Global.bot,chan,user,Integer.toString(key)));
        }
    }
}
