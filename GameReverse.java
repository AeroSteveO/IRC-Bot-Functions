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
 * which is really unstable and breaks all the time
 *
 * Activate Command with:
 *      !reverse
 *
 */
public class GameReverse extends ListenerAdapter {
    static ArrayList<String> wordls = null;
    static ArrayList<String> activechan = new ArrayList<String>();
    boolean isactive = false;
    int time = 20;
    String blockedChan = "#dtella";
    @Override
    public void onMessage(MessageEvent event) throws FileNotFoundException, InterruptedException{
        String message = Colors.removeFormattingAndColors(event.getMessage());
        String gameChan = event.getChannel().getName();
        // keep the spammy spammy out of main, could move to XML/Global.java at some point
        if (message.equalsIgnoreCase("!reverse")&&!gameChan.equals(blockedChan)) {
            // get the list of words only if theres nothing in the list alread
            if (wordls == null) {
                wordls = getWordList();
            }
            // check if the current active channel list is empty
            if (activechan.isEmpty()){
                activechan.add(gameChan);
            }
            else{ //if its not empty, check if the channel calling the function is already active
                for (int i=0;i<activechan.size();i++){
                    if (activechan.get(i).equals(gameChan)){
                        isactive = true;
                    }
                }
                if (!isactive) { //if its not active, add it to the active channel list, and start the game
                    activechan.add(gameChan);
                }
            }
            if (!isactive){
                //get and shuffle the word
                String chosenword = wordls.get((int) (Math.random()*wordls.size()-1));
                String reversed = reverse(chosenword);
                event.getBot().sendIRC().message(gameChan, "You have "+time+" seconds to reverse this: " + Colors.BOLD+Colors.RED +reversed.toUpperCase() + Colors.NORMAL);
                //setup amount of given time
                boolean running = true;
                int key=(int) (Math.random()*100000+1);
                TimedWaitForQueue timedQueue = new TimedWaitForQueue(Global.bot,time,event.getChannel(),event.getUser(),key);
                
                while (running){  //magical BS timer built into a waitforqueue, only updates upon message event
                    try {
                        MessageEvent CurrentEvent = timedQueue.waitFor(MessageEvent.class);
                        String currentChan = CurrentEvent.getChannel().getName();
                        if (CurrentEvent.getMessage().equalsIgnoreCase(Integer.toString(key))){
                            event.getBot().sendIRC().message(currentChan,"You did not guess the solution in time, the correct answer would have been "+chosenword.toUpperCase());
                            running = false;
                            timedQueue.close();
                        }
                        else if (CurrentEvent.getMessage().equalsIgnoreCase(chosenword)&&currentChan.equalsIgnoreCase(gameChan)){
                            event.getBot().sendIRC().message(gameChan, CurrentEvent.getUser().getNick() + ": You have entered the solution! Correct answer was " + chosenword.toUpperCase());
                            running = false;
                            timedQueue.close();
                        }
                        else if ((CurrentEvent.getMessage().equalsIgnoreCase("!fuckthis")||(CurrentEvent.getMessage().equalsIgnoreCase("I give up")))&&currentChan.equals(gameChan)){
                            event.getBot().sendIRC().message(gameChan, CurrentEvent.getUser().getNick() + ": You have given up! Correct answer was " + chosenword.toUpperCase());
                            running = false;
                            timedQueue.close();
                        }
                    } catch (InterruptedException ex) {
                        //      activechan.remove(CurrentEvent.getChannel().getName());
                        ex.printStackTrace();
                    }
                }
                activechan.remove(gameChan);
            }
            else
                isactive=false;
        }
    }
    public ArrayList<String> getWordList() throws FileNotFoundException{
        try{
            Scanner wordfile = new Scanner(new File("wordlist.txt"));
            ArrayList<String> wordls = new ArrayList<String>();
            while (wordfile.hasNext()){
                wordls.add(wordfile.next());
            }
            wordfile.close();
            return (wordls);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    public static String reverse(String input){
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        for(int i=characters.size();i>0;i--){
            output.append(characters.get(i-1));
        }
        return(output.toString());
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