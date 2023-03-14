import javax.swing.JOptionPane;
import java.net.URL;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;

/**
  @author Atij Mahesh
  WeatherReport main class that creates the user interface for
  displaying weather given the user's choice of location. After the user
  inputs the location, the porgram will output the forecast of weather
  from the weather.gov api for various days, ranging from today to a
  week from now.
*/

public class WeatherReport {

  /**
    This main method uses JOptionPane to create a user interface. It asks
    the user for their choice of location: zip code, latitude and longitude, or
    city and state. It then uses the weather.gov api and test the readJson methods
    to get and display the weather forecast for the week.
  */
  public static void main(String[] a) throws Exception {
  
    boolean validPrompt = false;
    String type = "";
    //prompt for which type, and start of geocoding sequence
    while(!validPrompt) {
      try{
        type = JOptionPane.showInputDialog("Will you be inputting "
                                                  + "a Zip Code (enter 1), "
                                                  + "City and State (enter 2), "
                                                  + "or coordinates (enter 3)?");
        if(!(type.equals("1") || type.equals("2") || type.equals("3"))
          || type == null) {
          throw new Exception();
        }
        validPrompt = true;
      } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Please input either 1, 2, or 3.");
      }
    }
    //variables used across program
    String resp = "";
    double longitude = 0;
    double latitude = 0;
    String finalCoords = "";
    URL JsonUrl = null;
    final int INITIAL_UNNECESSARY_CHARS = 14;
    final int FINAL_UNNECESSARY_CHARS = 4;
    
    //case for Zip Code
    if(type.equals("1")) {
      resp = JOptionPane.showInputDialog("Please input a 5 digit "
                                                + "zip code");
      try {
        int zipCode = Integer.parseInt(resp);
        final int ZIPCODE_LENGTH = 5;
        JsonUrl = new URL("https://api.mapbox.com/geocoding/v5/mapbox.places/"
                        + zipCode + ".json?access_token=pk"
                        + ".eyJ1IjoiYXRpam1haGVzaCIsImEiOiJja3V5aTM0dmMy"
                        + "ZjF3MnVvZGNtZmV2d21xIn0.e3Yla1A1nI3JGwEvjrqOrg");
        if(Integer.toString(zipCode).length() != ZIPCODE_LENGTH)
          throw new Exception();
      } catch(Exception e) {
        JOptionPane.showMessageDialog(null, "The given input is invalid.");
        System.exit(0);
      }
      
      //parse through JSON and retrieve coordinates data
      BufferedReader in = new BufferedReader(new InputStreamReader(
                                             JsonUrl.openStream()));
      String inputLine;
      
      while ((inputLine = in.readLine()) != null) {
        if(inputLine != null) {
          finalCoords = inputLine.substring(inputLine.indexOf("coordinates")
                                            + INITIAL_UNNECESSARY_CHARS,
                                            inputLine.indexOf("context")
                                            - FINAL_UNNECESSARY_CHARS);
          break;
        }
      }
      try {
        longitude = Double.parseDouble(finalCoords.substring(
                                                 0, finalCoords.indexOf(",")));
        latitude = Double.parseDouble(finalCoords.substring(
                                                finalCoords.indexOf(",")+1));
      } catch (NumberFormatException e) {
        longitude = Double.parseDouble(finalCoords.substring(
                                                 0, finalCoords.indexOf(",")));
        latitude = Double.parseDouble(finalCoords.substring(
                                                finalCoords.indexOf(",")+1,
                                                finalCoords.length()-1));
      }
    }
    
    //case for City, State format
    else if(type.equals("2")) {
      resp = JOptionPane.showInputDialog("Please input a City "
                                         + "and state in the "
                                         + "following format: "
                                         + "City,State");
      try {
        String city = resp.substring(0, resp.indexOf(","));
        String state = resp.substring(resp.indexOf(",")+1);
        if(city.contains(" "))
          city.replace(" ", "%20");
        if(state.contains(" "))
          state.replace(" ", "%20");
        JsonUrl = new URL("https://api.mapbox.com/geocoding/v5/mapbox.places/"
                          + city + "%2C" + state + ".json?access_token=pk"
                          + ".eyJ1IjoiYXRpam1haGVzaCIsImEiOiJja3V5aTM0dmMy"
                          + "ZjF3MnVvZGNtZmV2d21xIn0.e3Yla1A1nI3JGwEvjrqOrg");
      } catch(Exception e) {
        JOptionPane.showMessageDialog(null, "The given input is invalid.");
        System.exit(0);
      }
      
      //parse through JSON and retrieve coordinates data
      BufferedReader in = new BufferedReader(new InputStreamReader(
                                             JsonUrl.openStream()));
      String inputLine;
      while ((inputLine = in.readLine()) != null) {
        if(inputLine != null) {
          finalCoords = inputLine.substring(inputLine.indexOf("coordinates")
                                            + INITIAL_UNNECESSARY_CHARS,
                                            inputLine.indexOf("context")
                                            - FINAL_UNNECESSARY_CHARS);
          break;
        }
      }
      in.close();
      
      //replace all strange symbols in coordinates
      finalCoords = finalCoords.replaceAll("[^\\d.,]", "");
      finalCoords = finalCoords.replaceAll(",$", "");
      /*
        need to convert longitude to negative since it gets cut off
        and all US coordinates have a negative longitude
      */
      longitude = -Double.parseDouble(finalCoords.substring(0,
                                               finalCoords.indexOf(",")));
      latitude = Double.parseDouble(finalCoords.substring(
                                              finalCoords.indexOf(",")+1));
    }
    
    //case for latitude and longitude input
    else if(type.equals("3")) {
      resp = JOptionPane.showInputDialog("Please input a set of "
                                         + "coordinates in the "
                                         + "following format: "
                                         + "longitude,longitude");
      try {
        latitude = Double.parseDouble(resp.substring(0,
                                                resp.indexOf(",")));
        longitude = Double.parseDouble(resp.substring(
                                                 resp.indexOf(",")+1));
      } catch(Exception e) {
        JOptionPane.showMessageDialog(null, "The given input is invalid.");
        System.exit(0);
      }
    }
    //end of geocoding sequence
    
    //start of weather sequencing
    while(true) {
      try {
        //first, retrieve location data
        String weatherURL2 = "https://api.weather.gov/points/" + latitude +","
                             + longitude;
        //retrieve forcast from location data
        JSONObject json = readJsonFromUrl(weatherURL2);
        JSONObject properties = (JSONObject) json.get("properties");
        String forecastURL = properties.get("forecast").toString();
        JSONObject json2 = readJsonFromUrl(forecastURL);
        String forecastHourlyURL = properties.get("forecastHourly").toString();
        JSONObject json3 = readJsonFromUrl(forecastHourlyURL);
        JSONObject propertiesForecast = (JSONObject) json2.get("properties");
        JSONArray periodsForecast = (JSONArray) propertiesForecast.get("periods");
        
        //print weather data for 10 periods
        final int NUMBER_PERIODS = 10;
        for(int i = 0; i < NUMBER_PERIODS; i++) {
          printForecast((JSONObject) periodsForecast.get(i));
          System.out.println();
        }
        break;
      } catch(Exception e) {
        JOptionPane.showMessageDialog(null, "There is no retrievable weather"
                                      + " data for the coordinates: "
                                      + latitude + ", " + longitude
                                      + ".\nInput: " + resp);
        System.exit(0);
      }
    }
  }
 
  /**
    @param JSONObject period (period of time with forecast)
    This method displays the weather forecast given the JSONObject
    for a certain time with the weather. It displays a detailed forecast,
    the average temperature, the wind, and the time of day of this weather.
    It also displays an image of what the forecast is for that period
  */
  public static void printForecast(JSONObject period) {
    ImageIcon icon = null;
  
    //unreachable catch statement
    try {
      URL imageLink = new URL(period.get("icon").toString());
      icon = new ImageIcon(ImageIO.read(imageLink));
    } catch (Exception e) {
      System.exit(0);
    }
  
    //printing out nicely formatted weather
    JOptionPane.showMessageDialog(null, "Forecast: " + period.get("detailedForecast")
                                  + "\nTemperature from "
                                  + period.get("startTime") + " to "
                                  + period.get("endTime") + ": "
                                  + period.get("temperature") + " degrees Fahrenheit\n"
                                  + "Wind: " + period.get("windSpeed") + " "
                                  + period.get("windDirection"),
                                  "Weather for " + period.get("name"),
                                  JOptionPane.INFORMATION_MESSAGE,icon);
  }

  /**
    @param String link of URL to be parsed
    @returns JSONObject of the parsed api json document
    This method converts the given string link to a URl and makes
    a connection using that URL. It then uses the BufferedReader class to
    parse the contents of the api using the readAll method below. It converts the
    String text from readAll() to JSON format.
  */
  public static JSONObject readJsonFromUrl(String link) throws IOException,
  JSONException {
    URL url = new URL(link);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.setDoOutput(true);
    connection.connect();
    InputStream is = url.openStream();
    try {
      BufferedReader rd = new BufferedReader(new InputStreamReader(is,
                                             Charset.forName("UTF-8")));
      String jsonText = readAll(rd);
      JSONObject json = new JSONObject(jsonText);
      return json;
    } finally {
      is.close();
    }
  }
  
  /**
    @param Reader rd which is the BufferedReader created in the readJsonFromURl method.
    @returns String text with the contents of the api.
    This methods reads all the contents of a url using the BufferedReader class
    and creates a string with all the content.
  */
  public static String readAll(Reader rd) throws IOException {
    StringBuilder sb = new StringBuilder();
    int cp;
    while ((cp = rd.read()) != -1) {
      sb.append((char) cp);
    }
    return sb.toString();
  }
}


