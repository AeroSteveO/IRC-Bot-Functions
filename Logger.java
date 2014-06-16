/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package Wheatley;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.ActionEvent;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
 * 
 * Simple logging listener, updates the log file every 100 lines of chat, so to not spam the file
 * To use: 
 * --Before the configuration lines
 * BackgroundListenerManager BackgroundListener = new BackgroundListenerManager();
 * 
 * --In configuration
 * .setListenerManager(BackgroundListener) //Put all your .addListener(new listener()) statements after this
 * 
 * --After configuration lines
 * BackgroundListener.addListener(new Logger(),true);
 * 
 * Activate commands with
 *      !save logs
 *          Saves all currently in ram lines to log
 */
public class Logger extends ListenerAdapter{
    ArrayList<String> log = new ArrayList<>();
    Boolean success = false;
    String BotOwner = "nick";
    public void onMessage(MessageEvent event) throws IOException {
        String message = Colors.removeFormattingAndColors(event.getMessage());
        log.add("<"+event.getUser().getNick()+"> "+message);
//        System.out.printf(log.get(log.size()-1)+"\n");
        if(log.size()>100||(message.equalsIgnoreCase("!save logs")&&event.getUser().getNick().equalsIgnoreCase(BotOwner))){
            success = saveToFile(log);
            if(!success)
                event.getBot().sendIRC().notice(BotOwner,"Log file failed to save");
            log.clear();
        }
    }
    public void onAction(ActionEvent event) {
        String action = Colors.removeFormattingAndColors(event.getMessage());
        log.add("* "+event.getUser().getNick()+" "+action);
//        System.out.printf(log.get(log.size()-1)+"\n");
    }
    private Boolean saveToFile(ArrayList<String> log) throws IOException {
        Boolean isSaved = false;
        File file =new File("WheatleyLogs.plog");
        if(!file.exists()){
            file.createNewFile();
        }
        try{
            for(String addition: log){
                FileWriter fileWritter = new FileWriter(file.getName(),true);
                BufferedWriter bufferWritter = new BufferedWriter(fileWritter);
                bufferWritter.write("\n"+addition);
                bufferWritter.close();   
            }
            isSaved=true;
        }catch(IOException e){
            e.printStackTrace();
        }
        
        return(isSaved);
    }
}
