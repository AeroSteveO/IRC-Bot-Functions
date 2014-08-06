/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Wheatley;

import java.io.FileNotFoundException;
import java.util.ArrayList;
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
 * @author Steve-O
 *
 * Requested by: PiTheMathGod
 * Activate Command with:
 *      !Mastermind [length] [chars] [lives]
 *
 *          Options include:
 *              Length: the number of characters in the code [int]
 *              Chars: the number of unique characters to use in the code [int]
 *              Lives: number of guesses you can make before losing [int]
 *
 *
 *
 */
public class GameMasterMind extends ListenerAdapter {
    
    String blockedChan = "#dtella";
    static ArrayList<String> activechan = new ArrayList<String>();
    boolean isactive = false;
    
    public void onMessage(MessageEvent event) throws FileNotFoundException, InterruptedException {
        
        String message = Colors.removeFormattingAndColors(event.getMessage());
        String gameChan = event.getChannel().getName();
        if (message.split(" ")[0].equalsIgnoreCase("!mastermind")&&!gameChan.equals(blockedChan)) {
            
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
                String[] options = message.split(" ");
                int length = 5;
                int charSize = 2;
                int lives = length * charSize;
                
                if (options.length==2){
                    length = Integer.parseInt(options[1]);
                    if (length>10)
                        length=10;
                    lives = length * charSize;
                }
                else if (options.length == 3){
                    length = Integer.parseInt(options[1]);
                    if (length>10){
                        length=10;
                    }
                    charSize = Integer.parseInt(options[2]);
                    if (charSize>10){
                        charSize=10;
                    }
                    lives = length * charSize;
                }
                else if (options.length == 4){
                    length = Integer.parseInt(options[1]);
                    if (length>10)
                        length=10;
                    charSize = Integer.parseInt(options[2]);
                    if (charSize>10)
                        charSize=10;
                    lives = Integer.parseInt(options[3]);
                }
                int time = 30+(charSize+length)*10;
                int scorePositionValue = 0;
                int scoreValue = 0;
                
                ArrayList<Integer> solutionArray = createIntArray(length,charSize);
                String solution = convertIntToString(solutionArray);
                
                boolean running=true;
                int key=(int) (Math.random()*100000+1);
                TimedWaitForQueue timedQueue = new TimedWaitForQueue(Global.bot,time,event.getChannel(),event.getUser(),key);
                event.respond("Try to correctly guess a "+length+" digit code (0-"+Integer.toString(charSize-1)+")");
                //event.respond(""+Integer.toString(solutionArray.size()) + "  "+ solution);
                
                while (running){
                    MessageEvent CurrentEvent = timedQueue.waitFor(MessageEvent.class);
                    String guess = CurrentEvent.getMessage();
                    String currentChan = CurrentEvent.getChannel().getName();
                    if (CurrentEvent.getMessage().equalsIgnoreCase(Integer.toString(key))){
                        event.getBot().sendIRC().message(gameChan,"Game over! You've run out of time. "+Colors.BOLD + solution + Colors.NORMAL + " would have been the solution.");
                        running = false;
                        timedQueue.end();
                    }
                    else if ((CurrentEvent.getMessage().equals("!fuckthis")||(CurrentEvent.getMessage().equalsIgnoreCase("I give up")))&&currentChan.equals(gameChan)){
                        CurrentEvent.respond("You have given up! Correct answer was " + solution);
                        running = false;
                        timedQueue.end();
                    }
                    else if (Pattern.matches("[0-9]{"+length+"}",guess)&&currentChan.equalsIgnoreCase(gameChan)){
                        String[] temp = guess.split("(?!^)");
                        ArrayList<Integer> guessArr = new ArrayList<Integer>();
                        for (int i=0;i<temp.length;i++){
                            guessArr.add(Integer.parseInt(temp[i]));
                        }
                        for (int i = 0;i<guessArr.size()&&i<solutionArray.size();i++){
                            if (guessArr.get(i)==solutionArray.get(i))
                                scorePositionValue++;
                        }
                        for (int i = 0;i<=charSize;i++){
                            if (solutionArray.contains(i)&&guessArr.contains(i)){
                                int solCount = characterCounter(solutionArray,i);
                                int gueCount = characterCounter(guessArr,i);
                                if(solCount>gueCount)
                                    scoreValue = scoreValue + gueCount;
                                else
                                    scoreValue = scoreValue + solCount;
                            }
                        }
                        lives--;
                        if (lives <= 0){
                            CurrentEvent.respond("You've run out of lives, the solution was "+solution);
                            running = false;
                            timedQueue.end();
                        }
                        else if (scorePositionValue == length){
                            event.getBot().sendIRC().message(gameChan,"Congratulations " + CurrentEvent.getUser().getNick() +  ", you've found the code: " + Colors.BOLD + solution + Colors.NORMAL);
                            running = false;
                            timedQueue.end();
                        }
                        else{
                            CurrentEvent.respond("Code has "+scorePositionValue+" digits in the correct place, and uses "+scoreValue+" digits | Lives left: "+lives);
                        }
                        
                        scoreValue = 0;
                        scorePositionValue = 0;
                    }
                }
                activechan.remove(gameChan); //updated current index of the game
            }
        }
    }
    private static ArrayList<Integer> createIntArray(int length, int charSize){
        ArrayList<Integer> numbers = new ArrayList<>();
        
        for(int c=0;c<length;c++){
            numbers.add(createInt(charSize));
        }
        return numbers;
    }
    public String convertIntToString(ArrayList<Integer> chosenNumArray){
        String converted = "";
        for (int i=0;i<chosenNumArray.size();i++){
            converted = converted + Integer.toString(chosenNumArray.get(i));
        }
        return converted;
    }
    private static int createInt(int charSize){
        return (int) (Math.random()*charSize);
    }
    public int characterCounter(ArrayList<Integer> s, int charToFind){
        int counter = 0;
        for( int i=0; i<s.size(); i++ ) {
            if( s.get(i)==(charToFind) ) {
                counter++;
            }
        }
        return counter;
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
