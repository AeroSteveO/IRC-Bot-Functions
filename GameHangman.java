/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Wheatley;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.PircBotX;
import org.pircbotx.User;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.WaitForQueue;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Stephen
 * Based on the C# IRC bot, CasinoBot
 * which is generally unstable and requires windows to run
 *
 * Activate Command with:
 *      !hangman
 */
public class GameHangman extends ListenerAdapter {
    // Woohooo basic variables for junk
    int baselives = 10;
    int changed = 0;
    int lives = baselives;
    int time = 60;
    ArrayList<String> wordls = null;
    static ArrayList<String> activechan = new ArrayList<String>();
    boolean isactive = false;
    String blockedChan = "#dtella";
    int correct = 0;
    @Override
    public void onMessage(MessageEvent event) throws FileNotFoundException, InterruptedException {
        {
            String message = Colors.removeFormattingAndColors(event.getMessage());
            String gameChan = event.getChannel().getName();
            if (message.equalsIgnoreCase("!hangman")&&!gameChan.equals(blockedChan)) {
                if (wordls == null) {
                    wordls = getWordList();
                }
                
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
                    // Choose a random word from the list
                    String chosenword = wordls.get((int) (Math.random()*wordls.size()-1));
                    String guess = MakeBlank(chosenword);
                    char[] characters = chosenword.toCharArray();
                    event.getBot().sendIRC().message(gameChan, "You have "+time+" seconds to find the following word: " + Colors.BOLD + guess + Colors.NORMAL);
                    boolean running = true;
                    int key=(int) (Math.random()*100000+1);
                    TimedWaitForQueue timedQueue = new TimedWaitForQueue(Global.bot,time,event.getChannel(),event.getUser(),key);
                    while (running){
                        MessageEvent CurrentEvent = timedQueue.waitFor(MessageEvent.class);
                        String currentChan = CurrentEvent.getChannel().getName();
                        changed = 0;
                        if (CurrentEvent.getMessage().equalsIgnoreCase(Integer.toString(key))){
                            event.getBot().sendIRC().message(gameChan,"Game over! "+Colors.BOLD + chosenword.toUpperCase() + Colors.NORMAL + " would have been the solution.");
                            running = false;
                            timedQueue.end();
                        }
                        else if (Pattern.matches("[a-zA-Z]{2,}",CurrentEvent.getMessage())&&currentChan.equalsIgnoreCase(gameChan)){
                            if (CurrentEvent.getMessage().equalsIgnoreCase(chosenword)){
                                event.getBot().sendIRC().message(gameChan,"Congratulations " + CurrentEvent.getUser().getNick() +  ", you've found the word: " + Colors.BOLD + chosenword.toUpperCase() + Colors.NORMAL);
                                running=false;
                                timedQueue.end();
                            }
                            else{
                                lives--;
                                event.getBot().sendIRC().message(gameChan, CurrentEvent.getMessage() + " is incorrect. Lives left: " + lives );
                            }
                        }
                        else if (Pattern.matches("[a-zA-Z]{1}", CurrentEvent.getMessage())&&currentChan.equalsIgnoreCase(gameChan)){
                            for (int i = 0; i<chosenword.length(); i++){
                                if (Character.toString(characters[i]).equalsIgnoreCase(CurrentEvent.getMessage())&&!Character.toString(guess.charAt(i)).equalsIgnoreCase(CurrentEvent.getMessage())){
                                    String temp = guess.substring(0,i)+CurrentEvent.getMessage()+guess.substring(i+1);
                                    guess = temp;
                                    event.getBot().sendIRC().message(gameChan, CurrentEvent.getMessage() + " is correct! " + Colors.BOLD + guess.toUpperCase() + Colors.NORMAL + " Lives left: " +  lives );
                                    correct++;
                                    changed = 1;
                                }
                            }
                            if (changed ==0){
                                lives--;
                                event.getBot().sendIRC().message(gameChan, CurrentEvent.getMessage() + " is wrong or was already guessed. Lives left: " + lives );
                                if(lives == 0){
                                    event.getBot().sendIRC().message(gameChan, "You've run out of lives! The word we looked for was " + Colors.BOLD + Colors.RED + chosenword.toUpperCase() + Colors.NORMAL);
                                    running = false;
                                    timedQueue.end();
                                }
                            }
                            else if (correct == chosenword.length()){
                                event.getBot().sendIRC().message(gameChan,"Congratulations " + CurrentEvent.getUser().getNick() +  ", you've found the word: " + Colors.BOLD + chosenword.toUpperCase() + Colors.NORMAL);
                                running = false;
                                timedQueue.end();
                            }
                        }
                        else if ((CurrentEvent.getMessage().equals("!fuckthis")||(CurrentEvent.getMessage().equalsIgnoreCase("I give up")))&&currentChan.equals(gameChan)){
                            event.respond("You have given up! Correct answer was " + chosenword.toUpperCase());
                            running = false;
                            timedQueue.close();
                        }
                    }
                    correct = 0;
                    changed = 0;
                    lives = baselives;
                    activechan.remove(gameChan);
                }
            }
        }
    }
    public static String MakeBlank(String input){
        String blanks = new String();
        for (int i = 0; i<input.length(); i++){
            blanks = blanks + "_";
        }
        return(blanks);
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
    public class TimedWaitForQueue extends WaitForQueue{
        int time;
        private QueueTime runnable = null;
        Thread t;
        public TimedWaitForQueue(PircBotX bot,int time, Channel chan,User user, int key) throws InterruptedException {
            super(bot);
            this.time=time;
            QueueTime runnable = new QueueTime(Global.bot,time,chan,user,key);
            this.t = new Thread(runnable);
            runnable.giveT(t);
            t.start();
        }
        public void end() throws InterruptedException{
            this.close();
            t.join(1000);
        }
    }
    public class QueueTime implements Runnable {
        int time;
        User user;
        Channel chan;
        int key;
        PircBotX bot;
        Thread t;
        QueueTime(PircBotX bot, int time, Channel chan, User user, int key) {
            this.time = time;
            this.chan=chan;
            this.user=user;
            this.key=key;
            this.bot=bot;
        }
        
        public void giveT(Thread t) {
            this.t = t;
        }
        
        @Override
        public void run() {
            try {
                Thread.sleep(time*1000);
                bot.getConfiguration().getListenerManager().dispatchEvent(new MessageEvent(Global.bot,chan,user,Integer.toString(key)));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}