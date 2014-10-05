/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/

package testbot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.pircbotx.Colors;
import org.pircbotx.hooks.ListenerAdapter;
import org.pircbotx.hooks.events.MessageEvent;

/**
 *
 * @author Steve-O
 * Utilizes the OMDb API for imdb movie information
 * http://www.omdbapi.com/
 * 
 * Activate Command with:
 *      !IMDb [movie]
 *          Responds with the IMDB ratings for the input movie
 *      !IMDb [movie] [year]
 *          Responds with the IMDB ratings for the input movie
 * 
 * 
 */
public class IMDB extends ListenerAdapter {
    public void onMessage(MessageEvent event) throws Exception {
        String message = Colors.removeFormattingAndColors(event.getMessage().trim());
        if (message.toLowerCase().matches("!imdb\\s[a-z\\s]+\\s[0-9]{4}")){
            String[] msgSplit = message.split(" ");
            String year = msgSplit[msgSplit.length-1];
            String movieTitle = message.split(" ",2)[1].split(year)[0];
            event.getBot().sendIRC().message(event.getChannel().getName(),parseImdbMovieSearch(imdbUrlWithYear(movieTitle,year)));
            
        }
        else if (message.toLowerCase().startsWith("!imdb ")){
            String movieTitle = message.split(" ",2)[1];
            event.getBot().sendIRC().message(event.getChannel().getName(),parseImdbMovieSearch(imdbUrl(movieTitle)));
        }
        
    }
    private String imdbUrl(String movieTitle) throws UnsupportedEncodingException{
        return ("http://www.omdbapi.com/?t="+URLEncoder.encode(movieTitle.trim(), "UTF-8"));
    }
    private String imdbUrlWithYear(String movieTitle, String year) throws UnsupportedEncodingException{
        return ("http://www.omdbapi.com/?t="+URLEncoder.encode(movieTitle.trim(), "UTF-8")+"&y="+URLEncoder.encode(year.trim(), "UTF-8"));
    }
    
    private String parseImdbMovieSearch(String url) throws Exception{
        String movieSearchJSON = readUrl(url);
        JSONParser parser = new JSONParser();
        
        try{
            JSONObject movieJSON = (JSONObject) parser.parse(movieSearchJSON);
            String title = (String) (String) movieJSON.get("Title");
            String mpaaRating = (String) (String) movieJSON.get("Rated");
            String imdbRating = (String) (String) movieJSON.get("imdbRating");
            String id = (String) (String) movieJSON.get("imdbID");
            String release = (String) (String) movieJSON.get("Released");
            String link = "http://imdb.com/title/"+id+"/";
            String response = Colors.BOLD+title+": "+Colors.NORMAL+"("+mpaaRating+") "+Colors.BOLD+"IMDb: "+Colors.NORMAL+imdbRating+"/10 "+Colors.BOLD+"Release Date: "+Colors.NORMAL+formatImdbDate(release)+Colors.BOLD+" Link: "+Colors.NORMAL+link;
            return(response);
        }
        catch (Exception ex){
            try{
                JSONObject movieJSON = (JSONObject) parser.parse(movieSearchJSON);
                String error = (String) (String) movieJSON.get("Error");
                return(error);
            }
            catch(Exception e){
                e.printStackTrace();
                return("ERROR");
            }
        }
    }
    private String formatImdbDate(String date){
        String[] dateSplit = date.split(" ");
        return(dateSplit[1]+" "+dateSplit[0]+", "+dateSplit[2]);
    }
    
    //converts URL to string, primarily used to string-ify json text
    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);
            return buffer.toString();
        } finally {
            if (reader != null)
                reader.close();
        }
    }
}
