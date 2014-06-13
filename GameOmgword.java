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
import org.joda.time.*;
import org.pircbotx.Colors;
import org.pircbotx.hooks.*;
import org.pircbotx.hooks.events.*;

/**
 *
 * @author Steve-O
 * Based on the C# IRC bot, CasinoBot
 * which is really unstable and breaks all the time
 * 
 * Activate command with:
 *      !omgword
 *
 */
public class GameOmgword extends ListenerAdapter {
    // Initialize needed variables
    static ArrayList<String> wordls = null;
    static ArrayList<String> activechan = new ArrayList<String>();
    boolean isactive = false;
    String blockedChan = "#dtella";
    int time = 30;
    @Override
    public void onMessage(MessageEvent event) throws FileNotFoundException{
        String message = Colors.removeFormattingAndColors(event.getMessage());
        String gameChan = event.getChannel().getName();
        // keep the spammy spammy out of main, could move to XML/Global.java at some point
        if (message.equalsIgnoreCase("!omgword")&&!gameChan.equals(blockedChan)) {
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
                String scrambled = shuffle(chosenword);
                boolean running = true;
                event.getBot().sendIRC().message(gameChan, "You have "+time+" seconds to solve this: " + Colors.BOLD+Colors.RED +scrambled.toUpperCase() + Colors.NORMAL);
                //setup amount of given time
                DateTime dt = new DateTime();
                DateTime end = dt.plusSeconds(time);
                WaitForQueue queue = new WaitForQueue(event.getBot());
                while (running){  //magical BS timer built into a waitforqueue, only updates upon message event
                    try {
                        MessageEvent CurrentEvent = queue.waitFor(MessageEvent.class);
                        String currentChan = CurrentEvent.getChannel().getName();
                        dt = new DateTime();
                        if (dt.isAfter(end)){
                            event.getBot().sendIRC().message(currentChan,"You did not guess the solution in time, the correct answer would have been "+chosenword.toUpperCase());
                            running = false;
                            queue.close();
                        }
                        else if (CurrentEvent.getMessage().equalsIgnoreCase(chosenword)&&currentChan.equalsIgnoreCase(gameChan)){
                            event.getBot().sendIRC().message(gameChan, CurrentEvent.getUser().getNick() + ": You have entered the solution! Correct answer was " + chosenword.toUpperCase());
                            running = false;
                            queue.close();
                        }
                        else if ((CurrentEvent.getMessage().equalsIgnoreCase("!fuckthis")||(CurrentEvent.getMessage().equalsIgnoreCase("I give up")))&&currentChan.equals(gameChan)){
                            event.getBot().sendIRC().message(gameChan, CurrentEvent.getUser().getNick() + ": You have given up! Correct answer was " + chosenword.toUpperCase());
                            running = false;
                            queue.close();
                        }
                    } catch (InterruptedException ex) {
                        //      activechan.remove(currentChan);
                        ex.printStackTrace();
                    }
                }
                activechan.remove(gameChan);
            }
            else
                isactive=false;
        }
    }
    // Stupid freaking warning is wrong, breaks code when implemented
    @SuppressWarnings("SizeReplaceableByIsEmpty")
    // Shuffle up the chosen word string
    public static String shuffle(String input){
        List<Character> characters = new ArrayList<Character>();
        for(char c:input.toCharArray()){
            characters.add(c);
        }
        StringBuilder output = new StringBuilder(input.length());
        while(characters.size()!=0){
            int randPicker = (int)(Math.random()*characters.size());
            output.append(characters.remove(randPicker));
        }
        return(output.toString());
    }
    // Grabs the wordlist and loads into variable
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
}