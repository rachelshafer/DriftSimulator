//Rachel Shafer
//Drift calculator

import java.io.File;
import java.lang.Math;
import java.util.Random;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;


public class DriftCalculatorworks
{
  //empty constructor
  public DriftCalculatorworks()
  {}
  
  //Takes the start latitude, longitude, and window of time along with the object type.
  public static double[] Drift(double startlat, double startlong, String starttime, String endtime, String objectname, int[][] intlandarray, double[][][] oceandataarray, double[][][] windbeforedataarray, double[][][] windafterdataarray, double[][][] windafterdataarray2, boolean onexacthour, double[] objectdata)
  {
    double[] newlatandlong = new double[2];
    double latitude = startlat;
    double longitude = startlong;
    double watertemp=0, water_x=0, water_y=0, airtemp=0, wind_x=0, wind_y=0, object_cd=0, object_freeboard=0, object_draft=0, object_leeway=0;
    double latdelta = 0, longdelta = 0;
    double unitmin =5;  //unit of time in minutes between data collection and calculations
    boolean landho = false;//true if object hit land
    Random rand = new Random();
    
    //parse starttime and endtime                                                                          time manipulation
    String year = starttime.substring(0,4); //get year
    String month = starttime.substring(5,7); //get month
    String day = starttime.substring(8,10);  //get day
    String hour = starttime.substring(11,13); // get hour
    String min = starttime.substring(14,16); // get min
    
    String endyear = endtime.substring(0,4); //get year
    String endmonth = endtime.substring(5,7); //get month
    String endday = endtime.substring(8,10);  //get day
    String endhour = endtime.substring(11,13); // get hour
    String endmin = endtime.substring(14,16); // get min
    
    //convert strings into floats
    double doubleyear = Double.parseDouble(year);
    double doublemonth = Double.parseDouble(month);
    double doubleday = Double.parseDouble(day);
    double doublehour = Double.parseDouble(hour);
    double doublemin = Double.parseDouble(min);

    double doubleendyear = Double.parseDouble(endyear);
    double doubleendmonth = Double.parseDouble(endmonth);
    double doubleendday = Double.parseDouble(endday);
    double doubleendhour = Double.parseDouble(endhour);
    double doubleendmin = Double.parseDouble(endmin);
    
    //converts months and days into days in a year
    double daysinyear = getdaysinyear(doubleyear, doublemonth, doubleday);
    double enddaysinyear = getdaysinyear(doubleendyear, doubleendmonth, doubleendday);
    
    //convert hour and minutes into minutes in a day
    double minutesofyear = (doublehour*60) + doublemin + (daysinyear*24*60);
    double endminutesofyear = (doubleendhour*60) + doubleendmin + (enddaysinyear*24*60);
    

    
    double[] extrapolateddata = new double[5];
    
    double[] monthandday = getmonthandday(doubleyear, daysinyear);
      
    doublemonth = monthandday[0];
    doubleday = monthandday[1];

   
    
    landho = landcheck(latitude, longitude, intlandarray);
    //Calculates the drift for each unit of time and then gets new data for next unit of time.
    while(doubleyear <= doubleendyear && minutesofyear < endminutesofyear &&landho == false)
    {
      monthandday = getmonthandday(doubleyear, daysinyear);
      
      doublemonth = monthandday[0];
      doubleday = monthandday[1];
      //get data
      int index = windafterdataarray.length-1;
      try{
      //Holds:Ocean temperature, current u, current v, wind u, wind v.
        if(doubleday>windafterdataarray[index][1][0]||doublehour>windafterdataarray[index][2][0])//Changes data if needed
        {extrapolateddata = extrapolateddata(doublemonth, doubleday, doublehour, doublemin, latitude, longitude, oceandataarray, windbeforedataarray, windafterdataarray, onexacthour);}
        else 
        {extrapolateddata = extrapolateddata(doublemonth, doubleday, doublehour, doublemin, latitude, longitude, oceandataarray, windafterdataarray, windafterdataarray2, onexacthour);}
    }
    catch(Exception e){ System.out.println("problem");}

      //lable data
      watertemp = extrapolateddata[0];
      water_x = extrapolateddata[1];
      water_y = extrapolateddata[2];
      //airtemp = extrapolatedwinddata[0];
      wind_x = extrapolateddata[3];
      wind_y = extrapolateddata[4];
      object_cd = objectdata[0];
      object_freeboard = objectdata[1];
      object_draft = objectdata[2];
      object_leeway = objectdata[3];
      //calculate the change in position (meters) x and y 
      //true curent and wind vectors  (treat the water as if it is still)        to find drag
      double twv_x = wind_x - water_x;
      //true curent and wind vectors
      double twv_y = wind_y - water_y;
      
      //Air speed magnitude in relation to the water (Pythagorean theorem)
      double airspeedmag = Math.sqrt(Math.pow(twv_x, 2) + Math.pow(twv_y, 2));
      //binary search function to find the object velocity with respect to the water  
      double os = binarysearchforvelocity(object_cd, airspeedmag, object_freeboard, object_draft);
      
      //Calaulate the x and y of the object speed (using the proportions of a similar triangle)
      double os_x = (os*twv_x)/airspeedmag;
      double os_y = (os*twv_y)/airspeedmag;
      
      //calculate leeway of boat
      double angle = 0, rad = 0;
      double randomnum = rand.nextDouble();
      if(objectname=="Boat")
      {
        double hyp = Math.sqrt(Math.pow(os_x, 2) + Math.pow(os_y, 2));
        //find the angle without leeway
        if(os_x>0 && os_y>0)
        {angle = Math.atan(Math.abs(os_y/os_x));}
        if(os_x<0 && os_y>0)
        {angle = (90 - Math.atan(Math.abs(os_y/os_x)))+90;}
        if(os_x<0 && os_y<0)
        {angle = 180 + Math.atan(Math.abs(os_y/os_x));}  
        if(os_x>0 && os_y<0)
        {angle = 360 - Math.atan(Math.abs(os_y/os_x));}
        
        //Randomly pick the direction of leeway
        if(randomnum<0.5)
        {angle = angle + object_leeway;}
        else
        {angle = angle + object_leeway;}
        
        if(angle>360)
        {angle = angle - 360;}
        if(angle<0)
        {angle = 360 + angle;}
        
        rad = Math.toRadians(angle);
        
        os_x = Math.cos(rad)*hyp;
        os_y = Math.sin(rad)*hyp;

      }
      //now add the x and y of the object speed with respect to the water to the x and y of the ocean current speed to get the final x and y speed 
      double final_x_speed = os_x + water_x;
      double final_y_speed = os_y + water_y;

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////increment time
      
      //increment minutes, hours, days, months, and year if necessary
      doublemin = doublemin + unitmin;
      doublehour = doublehour + doublemin/60 - (doublemin%60)/60;
      doublemin = doublemin%60;
            
      if(doublehour>=24) 
      {
        daysinyear = daysinyear + (doublehour/24 - (doublehour%24)/24);
        doublehour = doublehour%24;
      }
      boolean leap = false;
      if(doubleyear%4 ==0&&doubleyear%100 != 0)
      {leap = true;}
      if(doubleyear%400 ==0)
      {leap = true;}
      if(leap == true)
      if(leap == true)
      {
        if(daysinyear > 366)
        {doubleyear = doubleyear+1;
         daysinyear = daysinyear - 366;}
      }
      else
      {
        if(daysinyear > 365)
        {doubleyear = doubleyear+1;
         daysinyear = daysinyear - 365;}
      }
        
      minutesofyear = (doublehour*60) + doublemin + (daysinyear*24*60);
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      //check if increment went past the end time and then calculate the last portion of the drift for the remaning time
      double overdays =0, overminutes=0, totaloverminutes=0;
      //if unit time increment went past the end time, then find exact time left before end time to calculate the distance traveled for that time.
      if(doubleyear <= doubleendyear)
      {
        if(minutesofyear > endminutesofyear)
        {
          unitmin = unitmin - (minutesofyear - endminutesofyear);//(minutesofyear < endminutesofyear) not possible
        }
        if(minutesofyear == endminutesofyear)
        {
          unitmin=0;
        }
      }
      
      //Change in lat for given time (unitmin)     meters in a lat deg ~ 111,111
      latdelta = (final_y_speed*unitmin*60)/111111;
      

      //find the start lat for this loop and add half of the latdelta to it
      //latmidpoint is the lat point in the middle of the distance travled for this calculated change in lat
      //(useing latmidpoint results in a more accurate approximation of the meters in a longitude degree at the current coordinates)
      double latmidpoint = latitude + (latdelta/2);
      double latradians = Math.toRadians(latmidpoint);      
      //Change in long (meters) at the current lat for given time (unitmin)
      longdelta = (final_x_speed*unitmin*60)/111111*(Math.cos(latradians));
      
      
      //increment lat and long
      latitude = latitude + latdelta;
      longitude = longitude + longdelta;
      
      landho = landcheck(latitude, longitude, intlandarray);      
    }
      
    newlatandlong[0] = latitude;
    newlatandlong[1] = longitude;
    
      
    return newlatandlong;
    
  }
  //Takes the start latitude, longitude, and window of time along with the object type.
  public static double[] singleDrift(double startlat, double startlong, String starttime, String endtime, String objectname, int[][] intlandarray)
  {
    double[] newlatandlong = new double[2];
    double latitude = startlat;
    double longitude = startlong;
    double watertemp=0, water_x=0, water_y=0, airtemp=0, wind_x=0, wind_y=0, object_cd=0, object_freeboard=0, object_draft=0, object_leeway=0;
    double latdelta = 0, longdelta = 0;
    double unitmin =5;  //unit of time in minutes between data collection and calculations
    boolean landho = false;//true if object hit land
    Random rand = new Random();
    
    //parse starttime and endtime                                                                          time manipulation
    String year = starttime.substring(0,4); //get year
    String month = starttime.substring(5,7); //get month
    String day = starttime.substring(8,10);  //get day
    String hour = starttime.substring(11,13); // get hour
    String min = starttime.substring(14,16); // get min
    
    String endyear = endtime.substring(0,4); //get year
    String endmonth = endtime.substring(5,7); //get month
    String endday = endtime.substring(8,10);  //get day
    String endhour = endtime.substring(11,13); // get hour
    String endmin = endtime.substring(14,16); // get min
    
    //convert strings into floats
    double doubleyear = Double.parseDouble(year);
    double doublemonth = Double.parseDouble(month);
    double doubleday = Double.parseDouble(day);
    double doublehour = Double.parseDouble(hour);
    double doublemin = Double.parseDouble(min);

    double doubleendyear = Double.parseDouble(endyear);
    double doubleendmonth = Double.parseDouble(endmonth);
    double doubleendday = Double.parseDouble(endday);
    double doubleendhour = Double.parseDouble(endhour);
    double doubleendmin = Double.parseDouble(endmin);
    
    //converts months and days into days in a year
    double daysinyear = getdaysinyear(doubleyear, doublemonth, doubleday);
    double enddaysinyear = getdaysinyear(doubleendyear, doubleendmonth, doubleendday);
    
    //convert hour and minutes into minutes in a day
    double minutesofyear = (doublehour*60) + doublemin + (daysinyear*24*60);
    double endminutesofyear = (doubleendhour*60) + doubleendmin + (enddaysinyear*24*60);
    
    double[] extrapolateddata = new double[5];
    
    double[] monthandday = getmonthandday(doubleyear, daysinyear);
      
    doublemonth = monthandday[0];
    doubleday = monthandday[1];

    double objectdata[] = new double[4];
    double oceandataarray[][][] = new double[100][100][3];
    
    double[][][] windbeforedataarray = new double[100][100][2];
    double[][][] windafterdataarray = new double[100][100][2];
    double[][][] windafterdataarray2 = new double[100][100][2];
    
     ///////////////////////////////////////////////////////////////////////////
    double doublenextday = doubleday;
    double hourbefore = 24;
    double hourafter = 24;
    double hourafter2 =24;
    double exacthour = 0;
    boolean onexacthour = false;
    
    
    if(doublehour > 0 && doublehour <6)                      // Find data for time before and after or for exact time
    {hourbefore = 0; hourafter = 6;}
    if(doublehour > 6 && doublehour <12)
    {hourbefore = 6; hourafter = 12;}
    if(doublehour > 12 && doublehour <18)
    {hourbefore = 12; hourafter = 18;}
    if(doublehour > 18 && doublehour <24)
    {hourbefore = 18; hourafter = 0; doublenextday = doublenextday + 1;}
        
    if(doublehour == 0)
    {exacthour =0; onexacthour = true;}
    if(doublehour == 6)
    {exacthour =6; onexacthour = true;}
    if(doublehour == 12)
    {exacthour =12; onexacthour = true;}
    if(doublehour == 18)
    {exacthour =18; onexacthour = true;}
    
    try{
    if(onexacthour = true)
    {windbeforedataarray = winddataarray(doublemonth, doubleday, exacthour);}
    else
    {
      windbeforedataarray = winddataarray(doublemonth, doubleday, hourbefore);
      windafterdataarray = winddataarray(doublemonth, doublenextday, hourafter);
      hourafter2 = hourafter + 6;
      if(hourafter2==24)
      {windafterdataarray2 = winddataarray(doublemonth, doublenextday+1, 0);}
      else
      {windafterdataarray2 = winddataarray(doublemonth, doublenextday, hourafter2);}
    }
      oceandataarray = oceandataarray(1, 16, 0);
      objectdata = getobjectdata(objectname);
    }
    catch(Exception e){ System.out.println("problem");}
    /////////////////////////////////////////////////////////////////////////////////////
    
    
    
    
    
    
    
    landho = landcheck(latitude, longitude, intlandarray);
    //Calculates the drift for each unit of time and then gets new data for next unit of time.
    while(doubleyear <= doubleendyear && minutesofyear < endminutesofyear &&landho == false)
    {
      
      monthandday = getmonthandday(doubleyear, daysinyear);
      
      doublemonth = monthandday[0];
      doubleday = monthandday[1];
      //get data
      int index = windafterdataarray.length-1;
      try{
      //Holds:Ocean temperature, current u, current v, wind u, wind v.
        if(doubleday>windafterdataarray[index][1][0]||doublehour>windafterdataarray[index][2][0])//Change data if needed
        {extrapolateddata = extrapolateddata(doublemonth, doubleday, doublehour, doublemin, latitude, longitude, oceandataarray, windbeforedataarray, windafterdataarray, onexacthour);}
        else 
        {extrapolateddata = extrapolateddata(doublemonth, doubleday, doublehour, doublemin, latitude, longitude, oceandataarray, windafterdataarray, windafterdataarray2, onexacthour);}

      
    }
    catch(Exception e){ System.out.println("problem");}
      //lable data
      watertemp = extrapolateddata[0];
      water_x = extrapolateddata[1];
      water_y = extrapolateddata[2];
      wind_x = extrapolateddata[3];
      wind_y = extrapolateddata[4];
      object_cd = objectdata[0];
      object_freeboard = objectdata[1];
      object_draft = objectdata[2];
      object_leeway = objectdata[3];
      //calculate the change in position (meters) x and y 
      //true curent and wind vectors  (treat the water as if it is still)        to find drag
      double twv_x = wind_x - water_x;
      //true curent and wind vectors
      double twv_y = wind_y - water_y;
      
      //Air speed magnitude in relation to the water (Pythagorean theorem)
      double airspeedmag = Math.sqrt(Math.pow(twv_x, 2) + Math.pow(twv_y, 2));
      //binary search function to find the object velocity with respect to the water  
      double os = binarysearchforvelocity(object_cd, airspeedmag, object_freeboard, object_draft);
      
      //Calaulate the x and y of the object speed (using the proportions of a similar triangle)
      double os_x = (os*twv_x)/airspeedmag;
      double os_y = (os*twv_y)/airspeedmag;
      
      //calculate leeway of boat
      double angle = 0, rad = 0;
      double randomnum = rand.nextDouble();
      if(objectname=="Boat")
      {
        double hyp = Math.sqrt(Math.pow(os_x, 2) + Math.pow(os_y, 2));
        //find the angle without leeway
        if(os_x>0 && os_y>0)
        {angle = Math.atan(Math.abs(os_y/os_x));}
        if(os_x<0 && os_y>0)
        {angle = (90 - Math.atan(Math.abs(os_y/os_x)))+90;}
        if(os_x<0 && os_y<0)
        {angle = 180 + Math.atan(Math.abs(os_y/os_x));}  
        if(os_x>0 && os_y<0)
        {angle = 360 - Math.atan(Math.abs(os_y/os_x));}
        
        //Randomly pick the direction of leeway
        if(randomnum<0.5)
        {angle = angle + object_leeway;}
        else
        {angle = angle + object_leeway;}
        
        if(angle>360)
        {angle = angle - 360;}
        if(angle<0)
        {angle = 360 + angle;}
        
        rad = Math.toRadians(angle);
        
        os_x = Math.cos(rad)*hyp;
        os_y = Math.sin(rad)*hyp;

      }
      //now add the x and y of the object speed with respect to the water to the x and y of the ocean current speed to get the final x and y speed 
      double final_x_speed = os_x + water_x;
      double final_y_speed = os_y + water_y;
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////increment time
     
      //increment minutes, hours, days, months, and year if necessary
      doublemin = doublemin + unitmin;
      doublehour = doublehour + doublemin/60 - (doublemin%60)/60;
      doublemin = doublemin%60;
            
      if(doublehour>=24) 
      {
        daysinyear = daysinyear + (doublehour/24 - (doublehour%24)/24);
        doublehour = doublehour%24;
      }
      boolean leap = false;
      if(doubleyear%4 ==0&&doubleyear%100 != 0)
      {leap = true;}
      if(doubleyear%400 ==0)
      {leap = true;}
      if(leap == true)
      if(leap == true)
      {
        if(daysinyear > 366)
        {doubleyear = doubleyear+1;
         daysinyear = daysinyear - 366;}
      }
      else
      {
        if(daysinyear > 365)
        {doubleyear = doubleyear+1;
         daysinyear = daysinyear - 365;}
      }
        
      minutesofyear = (doublehour*60) + doublemin + (daysinyear*24*60);
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      //check if increment went past the end time and then calculate the last portion of the drift for the remaning time
      double overdays =0, overminutes=0, totaloverminutes=0;
      //if unit time increment went past the end time, then find exact time left before end time to calculate the distance traveled for that time.
      if(doubleyear <= doubleendyear)
      {
        if(minutesofyear > endminutesofyear)
        {
          unitmin = unitmin - (minutesofyear - endminutesofyear);//(minutesofyear < endminutesofyear) not possible
        }
        if(minutesofyear == endminutesofyear)
        {
          unitmin=0;
        }
      }
      
      //Change in lat for given time (unitmin)     meters in a lat deg ~ 111,111
      latdelta = (final_y_speed*unitmin*60)/111111;
      

      //find the start lat for this loop and add half of the latdelta to it
      //latmidpoint is the lat point in the middle of the distance travled for this calculated change in lat
      //(useing latmidpoint results in a more accurate approximation of the meters in a longitude degree at the current coordinates)
      double latmidpoint = latitude + (latdelta/2);
      double latradians = Math.toRadians(latmidpoint);      
      //Change in long (meters) at the current lat for given time (unitmin)
      longdelta = (final_x_speed*unitmin*60)/111111*(Math.cos(latradians));
      //increment lat and long
      latitude = latitude + latdelta;
      longitude = longitude + longdelta;
      
      landho = landcheck(latitude, longitude, intlandarray);      
    }
      
    newlatandlong[0] = latitude;
    newlatandlong[1] = longitude;
    
      
    return newlatandlong;
    
  }
  //Checks if an object hit land
  public static boolean landcheck(double lat, double longi, int[][] landarr)
  {
    boolean landho = false;
    //Map is 50x50 degrees lat and long (50/600=0.0833333333)
    double latlongdelta = 0.0833333333333;
    //shift values by half of the delta
    double startlat = 60 - (latlongdelta/2);
    double startlong = 260 + (latlongdelta/2);
    //find the difference between start position of land array(index[0][0]) and current position
    double latdelta = startlat - lat;
    double longdelta = longi - startlong;
    //find the closest index in the land array that represtents the current position
    double latindex = Math.round(latdelta/latlongdelta);
    double longindex = Math.round(longdelta/latlongdelta);
    //if index value is 1 then the object hit land
    if(landarr[(int)latindex][(int)longindex]==1)
    {
      landho = true;
    }
    return landho;
  }
  
  
  public static double drag(double cd, double density, double velocity, double area)
  {
    double tempdrag = cd*density*(Math.pow(velocity, 2))*area*0.5; 
    return tempdrag; 
  }
  
  
  
  //Function to find the velocity by looking at the drag and wind vectors
  public static double binarysearchforvelocity(double cd, double airspeedmag, double freeboardarea, double draftarea)
  {
    //object speed
    double os = binarysearchforvelocity(cd, airspeedmag, freeboardarea, draftarea, 0, airspeedmag);
    
    return os;
  }
  //Function to find the velocity by looking at the drag and wind vectors
  public static double binarysearchforvelocity(double cd, double airspeedmag, double freeboardarea, double draftarea, double low, double high)
  {
    //object speed (in relation to the water)
    double os = ((high - low)/2) + low;
    //air speed
    double as = airspeedmag - os;
    //water density = 997 kg/m^3        area  = draft area
    double drag_water = drag(cd, 997, os, draftarea);
    //air density = 1.225 kg/m^3        area  = freeboard area
    double drag_air = drag(cd, 1.225, as, freeboardarea);
    
    //Find correct object speed through binary search
    if(drag_water > drag_air - 0.0001&&drag_water < drag_air+ 0.0001)//Margin of error
    {return os;}
    if(drag_water < drag_air)
    {binarysearchforvelocity(cd, airspeedmag, freeboardarea, draftarea, os, high);}
    if(drag_water > drag_air)
    {binarysearchforvelocity(cd, airspeedmag, freeboardarea, draftarea, low, os);}



    return 0;
  }
  
   
  public static double[] getmonthandday(double doubleyear, double daysinyear)
  {
    double[] monthandday = new double[2];

    //check if it is a leap year
    boolean leap= false;
    if(doubleyear%4 ==0&&doubleyear%100 != 0)
    {leap = true;}
    if(doubleyear%400 ==0)
    {leap = true;}

    double month = 0, day = 0;
    if(leap == true)
    {
      if(daysinyear <= 31)
      {month = 1;
       day = daysinyear;}
      if(daysinyear > 31&&daysinyear <= 60)
      {month = 2;
       day = daysinyear-31;}
      if(daysinyear > 60&&daysinyear <= 91)
      {month = 3;
       day = daysinyear-60;}
      if(daysinyear > 91&&daysinyear <= 121)
      {month = 4;
       day = daysinyear-91;}
      if(daysinyear > 121&&daysinyear <= 152)
      {month = 5;
       day = daysinyear-121;}
      if(daysinyear > 152&&daysinyear <= 182)
      {month = 6;
       day = daysinyear-152;}
      if(daysinyear > 182&&daysinyear <= 213)
      {month = 7;
       day = daysinyear-182;}
      if(daysinyear > 213&&daysinyear <= 244)
      {month = 8;
       day = daysinyear-213;}
      if(daysinyear > 244&&daysinyear <= 274)
      {month = 9;
       day = daysinyear-244;}
      if(daysinyear > 274&&daysinyear <= 305)
      {month = 10;
       day = daysinyear-274;}
      if(daysinyear > 305&&daysinyear <= 335)
      {month = 11;
       day = daysinyear-305;}
      if(daysinyear > 335&&daysinyear <= 366)
      {month = 12;
       day = daysinyear-335;}
    }
    else
    {
      if(daysinyear <= 31)
      {month = 1;
       day = daysinyear;}
      if(daysinyear > 31&&daysinyear <= 59)
      {month = 2;
       day = daysinyear-31;}
      if(daysinyear > 59&&daysinyear <= 90)
      {month = 3;
       day = daysinyear-59;}
      if(daysinyear > 90&&daysinyear <= 120)
      {month = 4;
       day = daysinyear-90;}
      if(daysinyear > 120&&daysinyear <= 151)
      {month = 5;
       day = daysinyear-120;}
      if(daysinyear > 151&&daysinyear <= 181)
      {month = 6;
       day = daysinyear-151;}
      if(daysinyear > 181&&daysinyear <= 212)
      {month = 7;
       day = daysinyear-181;}
      if(daysinyear > 212&&daysinyear <= 243)
      {month = 8;
       day = daysinyear-212;}
      if(daysinyear > 243&&daysinyear <= 273)
      {month = 9;
       day = daysinyear-243;}
      if(daysinyear > 273&&daysinyear <= 304)
      {month = 10;
       day = daysinyear-273;}
      if(daysinyear > 304&&daysinyear <= 334)
      {month = 11;
       day = daysinyear-304;}
      if(daysinyear > 334&&daysinyear <= 365)
      {month = 12;
       day = daysinyear-334;}      
    }
    monthandday[0] = month;
    monthandday[1] = day;
    
    return monthandday;
  
  }
  public static double getdaysinyear(double doubleyear, double doublemonth, double doubleday)
  {
    //check if it is a leap year
    boolean leap= false;
    if(doubleyear%4 ==0&&doubleyear%100 != 0)
    {leap = true;}
    if(doubleyear%400 ==0)
    {leap = true;}

    double daysinyear = 0;
    if(doublemonth == 1)
    {daysinyear = doubleday;}
    if(doublemonth == 2)
    {daysinyear = 31 + doubleday;}
    if(doublemonth == 3)                      
    {
      daysinyear = 59 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 4)
    {
      daysinyear = 90 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 5)
    {
      daysinyear = 120 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 6)
     {
      daysinyear = 151 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 7)
    {
      daysinyear = 181 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 8)
    {
      daysinyear = 212 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 9)
    {
      daysinyear = 243 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 10)
    {
      daysinyear = 273 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 11)
    {
      daysinyear = 304 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
    if(doublemonth == 12)
    {
      daysinyear = 334 + doubleday;
      if(leap == true)
      {daysinyear = daysinyear + 1;}
    }
  
  
    return daysinyear;
  }
  //Get data and extrapolate for a specific time.  (Army time)


  public static double[] getobjectdata(String name)throws IOException
  {
    String[] exactsdata = {};
    String[] var = {};
    double[] extrapolateddata = new double[4];
    boolean found = false;

    String tempname = "";

    BufferedReader in = new BufferedReader(new FileReader("Object_List.txt")); 
    String s = in.readLine();
    s = in.readLine();   //Skip titles
    //
    while(found != true&&s != null)      // Go through the entire data set and find the data we need to extrapolate
    {
      var = s.split(",");

      tempname = var[0];
      if(tempname.equals(name))
      {
        exactsdata = var;
        found = true;
      }
      s = in.readLine();
  }
  in.close();
  //convert string data to float
    double cd = Double.parseDouble(exactsdata[1]); 
    double freeboardarea = Double.parseDouble(exactsdata[2]);
    double draftarea = Double.parseDouble(exactsdata[3]);
    double leeway = Double.parseDouble(exactsdata[4]);
    
    extrapolateddata[0] = cd;
    extrapolateddata[1] = freeboardarea;
    extrapolateddata[2] = draftarea;
    extrapolateddata[3] = leeway;
    
    return extrapolateddata; 
  
}
  
  //Given the intended path create the prob position array
  public static double[][] startprobpositionarray(double ipstartlat, double ipstartlong, double ipendlat, double ipendlong, double boatspeedmph)
  {
    //boat speed in meters per second
    double boatspeedms = (boatspeedmph*1000)/(60*60*0.621);
    
    //find the distance from start position to end position
    double latdelta = ipendlat-ipstartlat;
    double longdelta = ipendlong-ipstartlong;
    double distancelatm = Math.abs(latdelta)*111111;
    //lat of the middle of the intended path
    double latmidpoint = ipstartlat + (latdelta/2);
    double latradians = Math.toRadians(latmidpoint);
    double distancelongm = Math.abs(longdelta)*111111/(Math.cos(latradians));
    double distancem = Math.sqrt(Math.pow(distancelatm,2)+Math.pow(distancelongm,2));//pythagorean theorem
    //Travel time in seconds
    double traveltime = distancem/boatspeedms;
    //Time between array points
    double timeincrement = traveltime/999;
    //current object start time
    double currenttime = 0;
    
    double currentlat = ipstartlat;
    double currentlong = ipstartlong;

    
    double latincrement = latdelta/999;
    double longincrement = longdelta/999;
     
    //array with possible objects that each have a lat, long, and object start time
    double[][] probpositions = new double[1000][3];
    
    //Fill array with points along the intended path
    for(int i = 0; i<1000; i++)
    {
      probpositions[i][0] = currentlat;
      probpositions[i][1] = currentlong;
      probpositions[i][2] = currenttime;
      
      
      currentlat = currentlat + latincrement;
      currentlong = currentlong + longincrement;
      currenttime = currenttime + timeincrement;
    }
   
    return probpositions;
  }
  
  //increments every value in the prob position array for a given amount of time.                              elapsedtime = previous elapsed time(sec) before current drift      sec
  public static double[][] probpositionarray(String starttime, String endtime, String objectname, double[][] probpositions, int[][] intlandarray, double elapsedtime, double timeincrement)
  {
    double[] latandlongtemp = new double[2];
    String newstarttime = "";
    
    String year = starttime.substring(0,4); //get year
    String month = starttime.substring(5,7); //get month
    String day = starttime.substring(8,10);  //get day
    String hour = starttime.substring(11,13); // get hour
    String min = starttime.substring(14,16); // get min
    //convert strings into double
    double doubleyear = Double.parseDouble(year);
    double doublemonth = Double.parseDouble(month);
    double doubleday = Double.parseDouble(day);
    double doublehour = Double.parseDouble(hour);
    double doublemin = Double.parseDouble(min);
    double extrapolatedobjectdata[] = new double[4];
    double oceandataarray[][][] = new double[102][101][3];
    
    double[][][] windbeforedataarray = new double[202][201][2];
    double[][][] windafterdataarray = new double[202][201][2];
    double[][][] windafterdataarray2 = new double[202][201][2];
    
 ///////////////////////////////////////////////////////////////////////////
    double doublenextday = doubleday;
    double hourbefore = 24;
    double hourafter = 24;
    double hourafter2 =24;
    double exacthour = 0;
    boolean onexacthour = false;
    
    
    if(doublehour > 0 && doublehour <6)                      // Find data for time before and after or for exact time
    {hourbefore = 0; hourafter = 6;}
    if(doublehour > 6 && doublehour <12)
    {hourbefore = 6; hourafter = 12;}
    if(doublehour > 12 && doublehour <18)
    {hourbefore = 12; hourafter = 18;}
    if(doublehour > 18 && doublehour <24)
    {hourbefore = 18; hourafter = 0; doublenextday = doublenextday + 1;}
        
    if(doublehour == 0)
    {exacthour =0; onexacthour = true;}
    if(doublehour == 6)
    {exacthour =6; onexacthour = true;}
    if(doublehour == 12)
    {exacthour =12; onexacthour = true;}
    if(doublehour == 18)
    {exacthour =18; onexacthour = true;}
    
    try{
    if(onexacthour = true)
    {windbeforedataarray = winddataarray(doublemonth, doubleday, exacthour);}
    else
    {
      windbeforedataarray = winddataarray(doublemonth, doubleday, hourbefore);
      windafterdataarray = winddataarray(doublemonth, doublenextday, hourafter);
      hourafter2 = hourafter + 6;
      if(hourafter2==24)
      {windafterdataarray2 = winddataarray(doublemonth, doublenextday+1, 0);}
      else
      {windafterdataarray2 = winddataarray(doublemonth, doublenextday, hourafter2);}
    }
      oceandataarray = oceandataarray(1, 16, 0);
      extrapolatedobjectdata = getobjectdata(objectname);
    }
    catch(Exception e){ System.out.println("problem");}
    /////////////////////////////////////////////////////////////////////////////////////
    
    
    for(int i = 0; i<1000; i++)
    {
      
      //object start time sec
      double ostime = probpositions[i][2];
      double elapsedend = elapsedtime + timeincrement;
      double timedifference = ostime - elapsedtime;
      
      if(elapsedtime < ostime && ostime < elapsedend)
      {
        try {//Find the string time and date of the drift start time for an object
          SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
          Date d = df.parse(starttime); 
          Calendar cal = Calendar.getInstance();
          cal.setTime(d);
          cal.add(Calendar.SECOND, (int)timedifference);
          newstarttime = df.format(cal.getTime());
        } catch (ParseException parseException){} 
        
        
        latandlongtemp = Drift(probpositions[i][0], probpositions[i][1], newstarttime, endtime, objectname, intlandarray, oceandataarray, windbeforedataarray, windafterdataarray, windafterdataarray2, onexacthour, extrapolatedobjectdata);
        probpositions[i][0] = latandlongtemp[0];
        probpositions[i][1] = latandlongtemp[1];
      }
      if(ostime <= elapsedend)
      {
        latandlongtemp = Drift(probpositions[i][0], probpositions[i][1], starttime, endtime, objectname, intlandarray,  oceandataarray, windbeforedataarray, windafterdataarray, windafterdataarray2, onexacthour, extrapolatedobjectdata);
        probpositions[i][0] = latandlongtemp[0];
        probpositions[i][1] = latandlongtemp[1];
      }

    }
      
    return probpositions;
  }
  
  //provides an array of possible positions for a latitude and longitude based on a random distribution
  public static double[][] waveeffectarray(String objectname, double latitude, double longitude, double stime, double elapsedtime)
  {
    double newlatitude=0,newlongitude=0; 
    Random rand = new Random();
    double[][] waveeffect = new double[1000][2];
    double objectwaveeffectspeedms = 0;
    double yrand = 0, xrand = 0, ymeters = 0, xmeters = 0, latrand = 0, longrand = 0, latmidpoint = 0, latradians = 0;
    if(objectname=="Raft")
    {objectwaveeffectspeedms = (0.25*1000)/(60*60*0.621);}
    else if(objectname=="Boat")
    {objectwaveeffectspeedms = (0.1*1000)/(60*60*0.621);}
    else if(objectname=="PIW")
    {objectwaveeffectspeedms = (0.05*1000)/(60*60*0.621);}
     else if(objectname=="Buoy")
    {objectwaveeffectspeedms = (0.05*1000)/(60*60*0.621);}  
    
     int i=0;
    while(i!=1000)
    { //random numbers from -1 to 1
      yrand =(rand.nextDouble()*2)-1;
      xrand = (rand.nextDouble()*2)-1;
      ymeters = yrand*(objectwaveeffectspeedms*(elapsedtime-stime));
      xmeters = xrand*(objectwaveeffectspeedms*(elapsedtime-stime));
      latrand = (ymeters)/111111;

      //find the start lat for this loop and add half of the latdelta to it
      //latmidpoint is the lat point in the middle of the distance travled for this calculated change in lat
      //(useing latmidpoint results in a more accurate approximation of the meters in a longitude degree at the current coordinates)
      latmidpoint = latitude + (latrand/2);
      latradians = Math.toRadians(latmidpoint);      
      //Change in long (meters) at the current lat for given time (unitmin)
      longrand = (xmeters)/111111*(Math.cos(latradians));
      
            
      //increment lat and long
      newlatitude = latitude + latrand;
      newlongitude = longitude + longrand;
      
      waveeffect[i][0] = newlatitude;
      waveeffect[i][1] = newlongitude;
      
      double num = Math.sqrt(Math.pow(ymeters, 2) + Math.pow(xmeters, 2));
      double radius = objectwaveeffectspeedms*(elapsedtime-stime);
      if(num <= radius)
      {i++;}
    }
    
    return waveeffect;
  }
  
  
  //function to fill the land array to check if the drifting object hit land
  public static int[][] landarray()
  {
    //array to represent map and hold a land(1 or 0) value for each section of the map 
    int landarray[][] = new int[600][600];
    
    try{
    BufferedReader br = new BufferedReader(new FileReader("land.txt")); 
    String st; 

    for(int i = 0; i<600; i++)
    {
      //Land file
      st = br.readLine();
      String[] land = st.split(" ");
      for(int j = 0; j<600; j++)
      {
        landarray[i][j] = Integer.parseInt(land[j]);
      }
    }
    }
    catch(Exception e){}
  
    return landarray;
  }
    
  
  
  
 
 public static double[][][] oceandataarray(double month, double day, double hour)throws IOException
 {
   
      String[] var = {};

      String tempmonth = "";
      
      BufferedReader in = new BufferedReader(new FileReader("Ocean Data.txt")); 
      String s = in.readLine();
      var = s.split("\t");
      int index1 = Integer. parseInt(var[0]);
      int index2 = Integer. parseInt(var[1]);
      double dataincrement = Double.parseDouble(var[2]);
      double startlat = Double.parseDouble(var[3]);
      double startlong = Double.parseDouble(var[4]);
                                      //add 1 for data line
      double dataarray[][][] = new double[index1+1][index2][3];
       
      s = in.readLine();   //Skip titles
      s = in.readLine();   //
      s = in.readLine();   //
// Go through the entire data set and find the data we need to extrapolate


      var = s.split("\t");
      String temp = var[0];
      String tempdate = temp.substring(0,10); //get date
      tempmonth = temp.substring(5,7);//get month
      String tempday = temp.substring(8,10);  //get day
      String temphour = temp.substring(11,13); // get hour

      int inttempmonth = Integer. parseInt(tempmonth); //convert month to int
      int inttempday = Integer. parseInt(tempday); //convert day to int
      int inttemphour = Integer. parseInt(temphour); //convert hour to int
      
      double wtemp = 0, u = 0, v = 0;
      
      
      if(inttempmonth==month&&inttempday==day&&inttemphour==hour)
      {
        for(int i=index1-1; i>(-1); i--)//flip lat values
        {
          for(int j=0; j<index2; j++)///////////////////////////////////////////////////////////////////
          {
            
            wtemp = Double.parseDouble(var[4]);
            u = Double.parseDouble(var[5]);
            v = Double.parseDouble(var[6]);
            
            //System.out.println(wtemp);
            
            dataarray[i][j][0] = wtemp;
            dataarray[i][j][1] = u;
            dataarray[i][j][2] = v;
            s = in.readLine();
            var = s.split("\t");
          }
        }
      
      s = in.readLine();

      }
  in.close();

  //data to store for data extrapolation and data reset
  dataarray[index1][0][0] = month;
  dataarray[index1][1][0] = day;
  dataarray[index1][2][0] = hour;
  dataarray[index1][3][0] = startlat;
  dataarray[index1][4][0] = startlong;
  dataarray[index1][5][0] = dataincrement;

  return dataarray; 
}
  
 
  public static double[][][] winddataarray(double month, double day, double hour)throws IOException
 { 
      String[] var = {};
      BufferedReader in = new BufferedReader(new FileReader("Wind Data.txt")); 
      String s = in.readLine();
      var = s.split("\t");
      int index1 = Integer. parseInt(var[0]);
      int index2 = Integer. parseInt(var[1]);
      double dataincrement = Double.parseDouble(var[2]);
      double startlat = Double.parseDouble(var[3]);
      double startlong = Double.parseDouble(var[4]);
      
      double dataarray[][][] = new double[index1+1][index2][2];


      String tempmonth = "";
      
      
      s = in.readLine();   //Skip titles
      s = in.readLine();   //
      s = in.readLine();   //
      while(s != null)      // Go through the entire data set and find the data we need to extrapolate
      {
      var = s.split("\t");
      String temp = var[0];
      String tempdate = temp.substring(0,10); //get date
      tempmonth = temp.substring(5,7);//get month
      String tempday = temp.substring(8,10);  //get day
      String temphour = temp.substring(11,13); // get hour

      int inttempmonth = Integer. parseInt(tempmonth); //convert month to int
      int inttempday = Integer. parseInt(tempday); //convert day to int
      int inttemphour = Integer. parseInt(temphour); //convert hour to int
      
      double u = 0, v = 0;
      
      
      if(inttempmonth==month&&inttempday==day&&inttemphour==hour)
      {
        for(int i=index1-1; i>(-1); i--)
        {
          for(int j=0; j<index2; j++)///////////////////////////////////////////////////////////////////
          {
            
            u = Double.parseDouble(var[3]);
            v = Double.parseDouble(var[4]);
            
            dataarray[i][j][0] = u;
            dataarray[i][j][1] = v;
            s = in.readLine();
            var = s.split("\t");
          }
        }
        
      }
      
      s = in.readLine();

  }
  in.close();
  dataarray[index1][0][0] = month;
  dataarray[index1][1][0] = day;
  dataarray[index1][2][0] = hour;
  dataarray[index1][3][0] = startlat;
  dataarray[index1][4][0] = startlong;
  dataarray[index1][5][0] = dataincrement;

  return dataarray; 
   
   
  }
  
  
  
  
  public static double[] extrapolateddata(double month, double day, double hour, double min, double lat, double longi, double[][][] oceandataarray, double[][][] windbeforedataarray, double[][][] windafterdataarray, boolean onexacthour)throws IOException
  {
    
    //Holds extrapolated data:Ocean temperature, current u, current v, wind u, wind v.
    double[] extrapolateddata = new double[5];
    
    //find rows in both arrays
    int index1 = oceandataarray.length-1;
    int index2 = windbeforedataarray.length-1;
    
    double latlongdeltaocean = oceandataarray[index1][5][0];//0.5;
    double latlongdeltawind = windbeforedataarray[index2][5][0];//0.25;
    //shift values by half of the delta
    double startlatocean = oceandataarray[index1][3][0];//40.25;
    double startlongocean = oceandataarray[index1][4][0];//280.25;
    double startlatwind = windbeforedataarray[index2][3][0];//39.125;
    double startlongwind = windbeforedataarray[index2][4][0];//280.125;
    //find the difference between start position and current position
    double latdeltaocean = startlatocean - lat;
    double longdeltaocean = longi - startlongocean;
    double latdeltawind = startlatwind - lat;
    double longdeltawind = longi - startlongwind;
    //find the closest index that represtents the current position
    double oceanlatindex = Math.round(latdeltaocean/latlongdeltaocean);
    double oceanlongindex = Math.round(longdeltaocean/latlongdeltaocean);
    double windlatindex = Math.round(latdeltawind/latlongdeltawind);
    double windlongindex = Math.round(longdeltawind/latlongdeltawind);

    
    //Fill the extrapolated data array
    extrapolateddata[0] = oceandataarray[(int)oceanlatindex][(int)oceanlongindex][0];
    extrapolateddata[1] = oceandataarray[(int)oceanlatindex][(int)oceanlongindex][1];
    extrapolateddata[2] = oceandataarray[(int)oceanlatindex][(int)oceanlongindex][2];
    
    if(onexacthour = true)
    {
      extrapolateddata[3] = windbeforedataarray[(int)windlatindex][(int)windlongindex][0];
      extrapolateddata[4] = windbeforedataarray[(int)windlatindex][(int)windlongindex][1];
    }
    else//extrapolate data between data for the hour before and after
    {
      //find weight of of before data and after data on extrapolated data.
      double firstgap = hour - windbeforedataarray[index2][2][0];
      double secondgap = 6 - firstgap;
      double propfirstgap = firstgap/6;
      double propsecondgap = secondgap/6;
      //convert string data to float
      
      double beforewind_u = windbeforedataarray[(int)windlatindex][(int)windlongindex][0];
      double beforewind_v = windbeforedataarray[(int)windlatindex][(int)windlongindex][1];
      
      double afterwind_u = windafterdataarray[(int)windlatindex][(int)windlongindex][0];
      double afterwind_v = windafterdataarray[(int)windlatindex][(int)windlongindex][1];
      //extrapolated wind u, and wind v.
      extrapolateddata[3] = beforewind_u*(propsecondgap) + afterwind_u*(propfirstgap);
      extrapolateddata[4] = beforewind_v*(propsecondgap) + afterwind_v*(propfirstgap);
      
    }

    return extrapolateddata;

  }
   
}