//Natalie Shafer
//UI
import javax.swing.UIManager;
import java.io.BufferedReader;
import java.io.FileReader; 
import java.io.IOException;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.lang.Object.*;
import javax.swing.InputVerifier;
import javax.swing.text.MaskFormatter;
import java.text.ParseException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Calendar;
import java.lang.Math; 
import java.awt.geom.AffineTransform;

public class OceanDriftSim extends JFrame{
  public static final Color DARK_GREEN = new Color(0 ,153, 0);
  public static final Color LIGHT_BLUE = new Color(51,153,255);
  
  public static final Color PURPLE = new Color(102,0, 153);
  static JFrame f; //frame
  JLabel view ;//map
  BufferedImage surface;//image for map redraw
  JTextField latitude2Input, longitude2Input, dateAndTimeOutPut, boatMphInput;
  String date2InputText, date3InputText, time2InputText, time3InputText, latitude2InputText, longitude2InputText, c1type, objecttype, timeIncrementText, boatMphInputText, starttime, endtime;
  JComboBox<String> c1, datecol, timeIncrement, date2Input, date3Input;
  JTable j;
  JFormattedTextField time2Input, time3Input;
  double spacing, ratio = 1, unit, xmax = 50, xmin= 0, ymax=50, ymin=0, latitude, longitude, totalElapsedTime=0, boatMph= 0; //dimensions for map
  int numbercord, rows=0, timeIncrementint=15;// used for intended path
  int[][] landArray = new int[100][3];
  double[] cord = new double[8], newlatandlong = new double[2]; // used for intended path
  double[][] probpositions = new double[100][3], randarray = new double[1000][2], temprandarray = new double[1000][2];
  double[][][] randarrayHolder = new double[1000][1000][2],oceanarray , windarray;
  boolean oceanArrows=false, windArrows=false, intendedPath=false, probabilityAreas=false, searchPattern=false, simrun =false;
  MaskFormatter mask, mask2;
  
  DriftCalculatorworks model = new DriftCalculatorworks(); 
  
  public static void main(String[] args)
  {
    try { UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");} 
    catch (UnsupportedLookAndFeelException e) {}
    catch (ClassNotFoundException e) {}
    catch (InstantiationException e) {}
    catch (IllegalAccessException e) {}
     new OceanDriftSim(); 
  }
  
  public OceanDriftSim ()
  {
    f = new JFrame("Ocean Drift Simulator"); //FRAME and label for frame
    JPanel p = new JPanel(); //panel that is added to frame,
    f.setSize(1350, 750);
    setResizable(true);
    p.setLayout(null);
    
    JFormattedTextField ftext = new JFormattedTextField();
    try {
      mask = new MaskFormatter("##:##:##");//the # is for numeric values 'Z' stands for Zulu time,The UTC (Coordinated Universal Time)time zone is sometimes denoted bythe letter Z—a reference to the equivalent nautical time zone (GMT),which has been denoted by a Z since about 1950.
      mask.setPlaceholderCharacter('#'); 
    } catch (ParseException e) {e.printStackTrace();}
    
    try {
      mask2 = new MaskFormatter("##:##:##");
      mask2.setPlaceholderCharacter('#');
      mask2.install(ftext);
    } catch (ParseException e) {e.printStackTrace();}
    
//labels 
   JLabel date1 = new JLabel("Date");
   JLabel date2 = new JLabel("Date");
   JLabel time1 = new JLabel("Time");
   JLabel time2 = new JLabel("Time");
   JLabel type = new JLabel("Drifted Object Type");
   JLabel mphlable = new JLabel("Average Spead MPH");
   JLabel latitude1 = new JLabel("Latitude");
   JLabel latitude2 = new JLabel("Latitude"); 
   JLabel longitude1 = new JLabel("Longitude");
   JLabel longitude2 = new JLabel("Longitude");
   JLabel intendlabel = new JLabel("Intended Path");
   JLabel lastKnownlabel = new JLabel("The Last Known Date, Time, Latitude, and Longitude");
   JLabel objectlabel = new JLabel("Drifted Object");
//change font size for major lables
   intendlabel.setFont(new Font("Serif", Font.BOLD, 20));
   lastKnownlabel.setFont(new Font("Serif", Font.BOLD, 20));
   objectlabel.setFont(new Font("Serif", Font.BOLD, 20));
//buttons
   JButton skipback = new JButton("<< Step");
   JButton skipforward = new JButton("Step >>");
   JButton resetMap = new JButton("Reset map");
   JButton resetInfo = new JButton("Reset input");
   JButton sim = new JButton("Simulate");  
//radioButtons
   JRadioButton ocean = new JRadioButton("Ocean Current Arrows");
   JRadioButton wind = new JRadioButton("Wind Arrows");
   JRadioButton path = new JRadioButton("Intended Path");
   JRadioButton probability = new JRadioButton("Probability Areas");
   JRadioButton search = new JRadioButton("Search Pattern");
//comboBoxs
   c1 = new JComboBox<String>(new String[] {" ", "Small Object(Person)", "Small Unpowered(Rafts Dinghys)", "Medium Sailboats(sail-propelled)", "Float Buoy"});
   datecol = new JComboBox<String>(new String[] {" ","2011-01-11", "2011-01-12", "2011-01-13", "2011-01-14", "2011-01-15", "2011-01-16","2011-01-17","2011-01-18","2011-01-19","2011-01-20" });
   date2Input = new JComboBox<String>(new String[] {" ", "2011-01-11", "2011-01-12", "2011-01-13", "2011-01-14", "2011-01-15", "2011-01-16","2011-01-17","2011-01-18","2011-01-19","2011-01-20" });
   timeIncrement = new JComboBox<String>(new String[] {"15min", "30min", "1hour", "2hour", "4hour", "6hour"});
   timeIncrement.setMaximumRowCount(3);
   datecol.setMaximumRowCount(5);
//TextAreaInput
   time2Input = new JFormattedTextField(mask);
   boatMphInput = new JTextField();
   dateAndTimeOutPut= new JTextField();
   dateAndTimeOutPut.setEditable(false);
   latitude2Input = new JTextField();
   latitude2Input.getDocument().putProperty("filterNewlines", Boolean.TRUE);
   longitude2Input = new JTextField();
   longitude2Input.getDocument().putProperty("filterNewlines", Boolean.TRUE);
//Table INPUT
   j = new JTable(2, 4);
   TableColumn dateColumn = j.getColumnModel().getColumn(0);
   dateColumn.setCellEditor(new DefaultCellEditor(datecol));//puts a dropdown in the date column  
   JFormattedTextField ftf = new JFormattedTextField( mask2 );
   DefaultCellEditor dce = new DefaultCellEditor(ftf);
   j.getColumnModel().getColumn(1).setCellEditor(dce);
   j.getColumnModel().getColumn(2).setCellEditor(new CellEditor(new LatVerifier()));
   j.getColumnModel().getColumn(3).setCellEditor(new CellEditor(new LongVerifier()));
   j.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
//Make the initial map
   surface = new BufferedImage(600, 600,BufferedImage.TYPE_INT_RGB);
    = new JLabel(new ImageIcon(surface));
   ratio = 600/(xmax-xmin);
   unit = ((xmax - xmin)/5);
   mapRedraw(ratio, unit);
   
//set up the placement of items on the panel
//labels placement
   date1.setBounds(20, 40, 100, 20);
   date2.setBounds(20, 150, 100, 20);
   time1.setBounds(130, 40, 100, 20); 
   time2.setBounds(130, 150, 100, 20); 
   type.setBounds(20, 250, 200, 20);
   mphlable.setBounds(240, 250, 200, 20);
   latitude1.setBounds(240, 40, 150, 20);
   latitude2.setBounds(240, 150, 150, 20);
   longitude1.setBounds(350, 40, 150, 20);
   longitude2.setBounds(350, 150, 150, 20);
   intendlabel.setBounds(20, 10, 200, 20);
   lastKnownlabel.setBounds(20, 120, 500, 25);
   objectlabel.setBounds(20, 220, 500, 25);
//buttons placement
   skipback.setBounds(500, 630, 100, 20);
   skipforward.setBounds(610, 630, 100, 20);
   resetMap.setBounds(1000, 630, 100, 20);
   resetInfo.setBounds(20, 450, 100, 20);
   sim.setBounds(350, 450, 100, 20);//sim button
//radioButtons placement and ToolTips for the radio buttons
   ocean.setBounds(1150, 20, 200, 20);
   ocean.setToolTipText("Plots Ocean Current Vectors");
   wind.setBounds(1150, 60, 200, 20);
   wind.setToolTipText("Plots Wind Vectors");
   path.setBounds(1150, 100, 200, 20);
   path.setToolTipText("Plots The Intended Path");
   probability.setBounds(1150, 140, 200, 20);
   probability.setToolTipText("Plots Probability Areas");
   search.setBounds(1150, 180, 200, 20);
   search.setToolTipText("Plots Search Pattern");
//comboBoxs placement
   c1.setBounds(20, 270, 215, 20); // drop down
   timeIncrement.setBounds(500, 630, 100, 20);
//TextAreaInput placement
   date2Input.setBounds(20, 170, 100, 20); //date input
  // date3Input.setBounds(20, 370, 100, 20); //date input
   time2Input.setBounds(130, 170, 100, 20); //time input
   time2Input.setToolTipText("UTC (Coordinated Universal Time)");
   //time3Input.setBounds(130, 370, 100, 20); //time input
   latitude2Input.setBounds(240, 170, 100, 20); //lat input
   latitude2Input.setToolTipText("North and South range [10 to 60]");
   longitude2Input.setBounds(350, 170, 100, 20); //long input
   longitude2Input.setToolTipText("East and West range [-100 to -50]");
   dateAndTimeOutPut.setBounds(830, 630, 160, 20); //Date and time output for map
   boatMphInput.setBounds(240, 270, 100, 20);
//Table placement
   j.setBounds(20, 60, 450, 32);//tabel input   
//Map placement
   .setBounds(500, 20, 600, 600); // map 
  
//Adding these components to the Panel   
//Lables Adding  
   p.add(date1);
   p.add(date2);
   p.add(time1);
   p.add(time2);
   p.add(type);
   p.add(latitude1);
   p.add(latitude2);  
   p.add(longitude1);
   p.add(longitude2);
   p.add(intendlabel);
   p.add(lastKnownlabel);
   p.add(objectlabel);
   p.add(mphlable);
//Buttons Adding 
   p.add(skipforward);
   p.add(resetInfo);
   p.add(sim);
//RadioButtons Adding  
   p.add(ocean);
   p.add(wind);
   p.add(path);
//ComboBoxs Adding
   p.add(c1);
   p.add(timeIncrement);
//TextAreaInput Adding
   p.add(date2Input);
   p.add(time2Input);
   p.add(latitude2Input);
   p.add(longitude2Input);
   p.add(dateAndTimeOutPut);
   p.add(boatMphInput);
//Table Adding
   p.add(j);
//Map Adding
   p.add();
   
   
   
   
   
   
//ActionListeners, FocusListeners and KeyListeners for Text Areas!!!!
   latitude2Input.addKeyListener(new KeyAdapter() {
     @Override
     public void keyPressed(KeyEvent e) {
       if (e.getKeyCode() == KeyEvent.VK_TAB||e.getKeyCode() == KeyEvent.VK_ENTER ) {
         if (e.getModifiers() > 0) {
           latitude2Input.transferFocusBackward();
         } else {
           latitude2Input.transferFocus();
         }
         e.consume();
       }
     }
   });
   
   latitude2Input.addFocusListener(new FocusListener() {
     public void focusGained(FocusEvent e) {}
     public void focusLost(FocusEvent e) {
       String value = latitude2Input.getText();
       double d=0;
       try{
         d = Double.parseDouble(value);
       }catch (NumberFormatException n) {}         
       if (d > 60.0 || d < 10.0) { latitude2Input.setText("");
       } 
     }
   });
   
   longitude2Input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
              if (e.getKeyCode() == KeyEvent.VK_TAB||e.getKeyCode() == KeyEvent.VK_ENTER ) {
                if (e.getModifiers() > 0) {
                        longitude2Input.transferFocusBackward();
                } else {
                  longitude2Input.transferFocus();
                }
                e.consume();
                }
            }
   });
   
   longitude2Input.addFocusListener(new FocusListener() {
     public void focusGained(FocusEvent e) {}
     public void focusLost(FocusEvent e) {
      String value = longitude2Input.getText();
      double d=0;
      if (value != "") 
        try{ d = Double.parseDouble(value);}
      catch (NumberFormatException n) {}         
      if (d > -50 || d < -100) { longitude2Input.setText("");} 
     }
   });
   
   time2Input.addFocusListener(new FocusListener() {
    public void focusGained(FocusEvent e) {}
    public void focusLost(FocusEvent e) {
      
      time2InputText = time2Input.getText();
      try {
        Date date = null;
        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        date = timeFormat.parse(time2InputText);
        time2InputText = new SimpleDateFormat("HH:mm:ss").format(date);
      } catch (ParseException parseException) {} 
      time2Input.setText(time2InputText);
    }
   });

//ActionListeners for buttons!!!  
   sim.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e)
     { 
       
       tablerowcheck();
       System.out.println("rows" + rows);
       if(rows == 1||rows == 0)
       {
         simrun = true;
         zoom();   //zoom function
       
         double stlatd, stlongd;
         String test = (String)j.getValueAt(0, 2);
         if(test != null)
         {
         stlatd = Double.parseDouble((String)j.getValueAt(0, 2));
         stlongd = 360+(Double.parseDouble((String)j.getValueAt(0, 3)));
         }
         else
         {
         stlatd = Double.parseDouble(latitude2Input.getText());
         stlongd = 360+ Double.parseDouble(longitude2Input.getText());
         }
         totalElapsedTime = 0;
         Graphics2D g = surface.createGraphics();
         starttime = date2InputText+"T"+time2InputText+"Z";
         System.out.println(starttime);
         
         try {
           endtime = starttime;
           SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
           Date d = df.parse(endtime); 
           Calendar cal = Calendar.getInstance();
           cal.setTime(d);
           cal.add(Calendar.MINUTE, timeIncrementint);
           endtime = df.format(cal.getTime());
         } catch (ParseException parseException) { //Code should be added to handle invalid date format}
         }
         //System.out.println(endtime);
         dateAndTimeOutPut.setText(endtime);
         landArray = model.landarray();

         newlatandlong = model.singleDrift(stlatd, stlongd, starttime, endtime, objecttype, landArray);
          totalElapsedTime = (timeIncrementint*60);
         
         randarray = model.waveeffectarray(objecttype, newlatandlong[0], newlatandlong[1],  0 , totalElapsedTime);
          g.setColor(Color.red);
       
         
         for(int k=0; k<1000; k++)
             {  
               if(model.landcheck(randarray[k][0], randarray[k][1], landArray) == false)
                 g.draw(new Line2D.Double(((((randarray[k][1])-260)-xmin)*ratio),((-((randarray[k][0])-60))-ymin)*ratio,((((randarray[k][1])-260)-xmin)*ratio),((-((randarray[k][0])-60))-ymin)*ratio));
               temprandarray[k][0] = randarray[k][0];
               temprandarray[k][1] = randarray[k][1]; 
             };
           g.dispose();
         view.repaint();
       
       }
       ///////////////////////////////////////////////////////////////////////////////////////////////////////////
       System.out.println(rows+" ");
       if(rows == 2)
       {
         double stlatd, stlongd, nextlatd, nextlongd;
         stlatd = Double.parseDouble((String)j.getValueAt(0, 2));
         stlongd = 360+(Double.parseDouble((String)j.getValueAt(0, 3)));
         nextlatd = Double.parseDouble((String)j.getValueAt(1, 2));
         nextlongd = 360+(Double.parseDouble((String)j.getValueAt(1, 3)));
         totalElapsedTime = 0;
         boatMphInputText = boatMphInput.getText();
         System.out.println(boatMphInputText);
         if(boatMphInput.getText().trim().length() != 0)
         {
           boatMph = (Double.parseDouble(boatMphInputText));
         }
         else
         {
         JOptionPane.showMessageDialog(null, "When you have a intended path there needs to be a speed for the boat traveling the intended path", "Error!", JOptionPane.INFORMATION_MESSAGE);
        return;
         }
         
         
           String holdSlat, holdSlong;
           holdSlat = latitude2Input.getText();
           holdSlong = longitude2Input.getText();
         
         if(holdSlat != "" && holdSlong != "")
         {
           System.out.println(holdSlat+" "+holdSlong);
         stlatd = Double.parseDouble(holdSlat);
         stlongd = 360+ Double.parseDouble(holdSlong);
         System.out.println(stlatd+" "+stlongd);
         }
         
         
         simrun = true;
       zoom();   //zoom function
         
         probpositions = model.startprobpositionarray(stlatd, stlongd, nextlatd, nextlongd, boatMph);
                  
         System.out.println("DONE");
         Graphics2D g = surface.createGraphics();
         starttime = date2InputText+"T"+time2InputText+"Z";
         System.out.println(starttime);
         
         try {
           endtime = starttime;
           SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
           Date d = df.parse(endtime); 
           Calendar cal = Calendar.getInstance();
           cal.setTime(d);
           cal.add(Calendar.MINUTE, timeIncrementint);
           endtime = df.format(cal.getTime());
         } catch (ParseException parseException) { //Code should be added to handle invalid date format}
         }
         //System.out.println(endtime);
         dateAndTimeOutPut.setText(endtime);
         landArray = model.landarray();
         probpositions = model.probpositionarray(starttime, endtime, objecttype, probpositions, landArray, totalElapsedTime, (timeIncrementint*60));
         g.setColor(Color.red);
         totalElapsedTime = (timeIncrementint*60);
         System.out.println("DONE");
         
         for(int i=0; i<1000; i++)
         {
           if( probpositions[i][2]<= totalElapsedTime)
           {
             randarray = model.waveeffectarray(objecttype, probpositions[i][0], probpositions[i][1],  probpositions[i][2] , totalElapsedTime);
             for(int k=0; k<100; k++)
             {  
               if(model.landcheck(randarray[k][0], randarray[k][1], landArray) == false)
                 g.draw(new Line2D.Double(((((randarray[k][1])-260)-xmin)*ratio),((-((randarray[k][0])-60))-ymin)*ratio,((((randarray[k][1])-260)-xmin)*ratio),((-((randarray[k][0])-60))-ymin)*ratio));
               randarrayHolder[i][k][0] =  randarray[k][0];
               randarrayHolder[i][k][1] =  randarray[k][1];
             }; 
           }
         } 
         g.dispose();
         view.repaint();
     }
     }});
   
   skipforward.addActionListener(new ActionListener()////////////////
    { public void actionPerformed(ActionEvent e)
     {   
       if(rows == 1||rows == 0)
       {
         starttime = endtime;
          try {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date d = df.parse(endtime); 
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, timeIncrementint);
        endtime = df.format(cal.getTime());
      } catch (ParseException parseException) { 
        //Code should be added to handle invalid date format
      } 
      dateAndTimeOutPut.setText(endtime);

      zoom();//ZOOM
           
      newlatandlong = model.singleDrift(newlatandlong[0], newlatandlong[1], starttime, endtime, objecttype, landArray);
      totalElapsedTime = totalElapsedTime + (timeIncrementint*60);
      randarray = model.waveeffectarray(objecttype, newlatandlong[0], newlatandlong[1],  0 , totalElapsedTime);
      
       Graphics2D g = surface.createGraphics();
       g.setColor(Color.red);
       //for(int k=0; k<100; k++)
      //       {  
       //  System.out.println(randarray[k][0]+"  "+ randarray[k][1]);
      // }
          for(int k=0; k<1000; k++)
          {
            if(model.landcheck(randarray[k][0], randarray[k][1], landArray) == false)
              g.draw(new Line2D.Double(((((randarray[k][1])-260)-xmin)*ratio), (((-((randarray[k][0])-60))-ymin)*ratio ), ((((randarray[k][1])-260)-xmin)*ratio), (((-((randarray[k][0])-60))-ymin)*ratio )));
               temprandarray[k][0] = randarray[k][0];
               temprandarray[k][1] = randarray[k][1];     
          }
         
               g.dispose();
      view.repaint();
       }
      
      
      if(rows == 2)
       {
      starttime = endtime;
      
      try {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Date d = df.parse(endtime); 
        Calendar cal = Calendar.getInstance();
        cal.setTime(d);
        cal.add(Calendar.MINUTE, timeIncrementint);
        endtime = df.format(cal.getTime());
      } catch (ParseException parseException) { 
        //Code should be added to handle invalid date format
      } 
      dateAndTimeOutPut.setText(endtime);
      
      zoom();//ZOOM
      //mapRedraw(ratio, unit);
      
      Graphics2D g = surface.createGraphics();
      probpositions = model.probpositionarray(starttime,  endtime,  objecttype, probpositions, landArray, totalElapsedTime, (timeIncrementint*60));
      totalElapsedTime = totalElapsedTime + (timeIncrementint*60);
      
      g.setColor(Color.red);
      for(int i=0; i<1000; i++)
        {
        if( probpositions[i][2]<= totalElapsedTime)
        {
          randarray = model.waveeffectarray(objecttype, probpositions[i][0], probpositions[i][1],probpositions[i][2], totalElapsedTime); 
          for(int k=0; k<100; k++)
          {
            if(model.landcheck(randarray[k][0], randarray[k][1], landArray) == false)
              g.draw(new Line2D.Double(((((randarray[k][1])-260)-xmin)*ratio), (((-((randarray[k][0])-60))-ymin)*ratio ), ((((randarray[k][1])-260)-xmin)*ratio), (((-((randarray[k][0])-60))-ymin)*ratio )));
            randarrayHolder[i][k][0] =  randarray[k][0];
            randarrayHolder[i][k][1] =  randarray[k][1];      
          }
        }
      }
      
      g.dispose();
      view.repaint();
      }
    
    
    
    
    
    
    
    
    
    
    
    
    
    }});
   
   c1.addActionListener(new ActionListener() {//COMBO BOX FOR object Type
     public void actionPerformed(ActionEvent event) 
     {
       JComboBox c1 = (JComboBox)event.getSource();
       c1type = String.valueOf(c1.getSelectedItem());  
       
       switch(c1type)
       {
         case "Small Unpowered(Rafts Dinghys)": objecttype = "Raft";
         break;
         case "Medium Sailboats(sail-propelled)": objecttype = "Boat";
         break;
         case "Float Buoy": objecttype = "Buoy";
         break;
         default:  objecttype = "PIW";
       }
     }});
   
   date2Input.addActionListener(new ActionListener() {//COMBO BOX FOR object Type
     public void actionPerformed(ActionEvent event) 
     {
       JComboBox date2Input = (JComboBox)event.getSource();
       date2InputText = String.valueOf(date2Input.getSelectedItem());  
     }});
   
   timeIncrement.addActionListener(new ActionListener() {//COMBO BOX FOR object Type
     public void actionPerformed(ActionEvent event) 
     {
       JComboBox timeIncrement = (JComboBox)event.getSource();
       timeIncrementText = String.valueOf(timeIncrement.getSelectedItem()); 
       switch(timeIncrementText)
       {
         case"30min": timeIncrementint = 30;
         break;
         case "1hour": timeIncrementint = 60;
         break;
         case "2hour": timeIncrementint = 120;
         break;
         case "4hour": timeIncrementint = 240;
         break;
         case "6hour": timeIncrementint = 360;
         break;
         case "16hour": timeIncrementint = 960;
         break;
         case "24hour": timeIncrementint = 1440;
         break;
         default: timeIncrementint = 15;
       }
     }});
   
   resetInfo.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e)
     {
       dateAndTimeOutPut.setText("");
       date2Input.setSelectedItem("");
       boatMphInput.setText("");
       boatMphInputText = "";
       boatMph= 0;
       date2InputText="";
       time2Input.setValue(null);
       latitude2Input.setText("");
       longitude2Input.setText("");
       j.setModel(new DefaultTableModel(4,4));
       c1type = "";
       objecttype="";
       c1.setSelectedItem(" ");
       date2Input.setSelectedItem(" ");
       simrun = false;

       TableColumn dateColumn = j.getColumnModel().getColumn(0);
       dateColumn.setCellEditor(new DefaultCellEditor(datecol));//puts a dropdown in the date column  
       j.getColumnModel().getColumn(1).setCellEditor(dce);
       j.getColumnModel().getColumn(2).setCellEditor(new CellEditor(new LatVerifier()));
       j.getColumnModel().getColumn(3).setCellEditor(new CellEditor(new LongVerifier()));
       numbercord =0;
       //
       xmax = 50; xmin = 0; ymax = 50; ymin = 0;
       ratio = 600/(xmax-xmin);
       unit = ((xmax - xmin)/5);
       mapRedraw(ratio, unit);
       //
     }});
   
   resetMap.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e)
     {    
       xmax = 50; xmin = 0; ymax = 50; ymin = 0;
       ratio = 600/(xmax-xmin);
       unit = ((xmax - xmin)/5);
       mapRedraw(ratio, unit);
     }});
      
   ocean.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e)
     { 
       if (ocean.isSelected())
       {
         oceanArrows=true;
        drawOceanArrows();
       }
       else
       {
         oceanArrows=false;
         undrawOceanArrows();
       }
     }});
  
   wind.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e)
     {     
        if (wind.isSelected())
       {
         windArrows=true;
         drawWindArrows();
       }
       else
       {
         windArrows=false;
         undrawWindArrows();
       }
       
     }});
   
   path.addActionListener(new ActionListener(){
     public void actionPerformed(ActionEvent e)
     { 
       if (path.isSelected())
       {   
         intendedPath=true;     
         String holder;
         double doubleholder;
         int count = 0;
         for(int i = 0 ; i<2; i++)//row
         {
           for(int k = 2 ; k<4; k++)//column
           {
             holder = (String)j.getValueAt(i, k);
             if(holder != null)
             {
              doubleholder = Double.parseDouble(holder); 
              cord[count] = doubleholder;
              count++;
             }
           }
         }
         numbercord = count;
         drawPath(cord, count);
       }
       else
       {intendedPath=false;
         undrawPath(cord,  numbercord);}
     } });
   

   
   
   
   
   f.add(p);
   f.setDefaultCloseOperation(EXIT_ON_CLOSE);//exit properly
   f.setVisible(true);//last
  }
  
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////functions
  public void drawPath(double [] cord, int amount) {
    Graphics2D g = surface.createGraphics();
    g.setColor(Color.green);
    double lat1, long1, lat2, long2;//example keylargo: lat = 25.08, long = -80.45    
    lat1 = ((-(cord[0]-60))-ymin)*ratio;
    long1 = ((cord[1]+100)-xmin)*ratio;
    for (int i=2; i<(amount-1); i++)
    {
      lat2 = ((-(cord[i]-60))-ymin)*ratio;
      i++;
      long2 = ((cord[i]+100)-xmin)*ratio;
      g.draw(new Line2D.Double(long1, lat1, long2, lat2));
      lat1 = lat2;
      long1 = long2;   
    } 
    
    g.dispose();
    view.repaint();
  }
  public void undrawPath(double [] cord,  int amount) {
   
    mapRedraw(ratio, unit);
    if(simrun == true)
    refreshSim();
  }
  
  public void drawOceanArrows()
  {
    Graphics2D g = surface.createGraphics();
    g.setColor(Color.blue);
    try{
    oceanarray = model.oceandataarray(1, 16, 0);
    }catch(Exception e){ System.out.println("oceanproblem");}
    
    double oceanlat = 60.25;
    for(int i = 0; i<101; i++)
    { 
      double oceanlong = 260.25;
      for(int j = 0; j<101; j++)
      {
      // System.out.println(oceanarray[i][j][1]+"   "+oceanarray[i][j][2]+" "+oceanlat +" "+ (oceanlong+(0.5*j) ) );
      double holdlong1 =(((oceanlong+(0.5*j))-260)-xmin)*ratio;
      double holdlat1 = ((-(oceanlat-60))-ymin)*ratio;
      double holdlong2 =((((oceanlong+(oceanarray[i][j][1]))+(0.5*j))-260)-xmin)*ratio;//u effects east to west 
      double holdlat2 = ((-((oceanlat+(oceanarray[i][j][2]))-60))-ymin)*ratio;//v effects north to south
      AffineTransform tx = new AffineTransform();
      Path2D path = new Path2D.Double();
      
      if(oceanarray[i][j][1] != 0 ||oceanarray[i][j][2] != 0 )
      {
        double mag = Math.sqrt(Math.pow((oceanarray[i][j][1]), 2.0) + Math.pow((oceanarray[i][j][2]), 2.0)) ;
        mag=mag+(ratio/30);
        path.moveTo( 0, mag);
        path.lineTo(-mag, -mag);
        path.lineTo(mag, -mag);
        path.closePath();
        g.draw(new Line2D.Double(holdlong1, holdlat1, holdlong2, holdlat2));
        tx.setToIdentity();
        double angle = Math.atan2(holdlat2-holdlat1, holdlong2-holdlong1);
        tx.translate(holdlong2, holdlat2);
        tx.rotate((angle-Math.PI/2d));  
        Graphics2D h = surface.createGraphics();
        h.setColor(Color.blue);
        h.setTransform(tx);   
        h.fill(path);
        h.dispose();
      }
      }
    oceanlat = oceanlat -0.5;
    }
     g.dispose();
    view.repaint();  
  }
  
  public void undrawOceanArrows()
  {
    mapRedraw(ratio, unit);
      if(simrun ==true)
    refreshSim();
  }
  
  
  
  public void drawWindArrows()
  {
    Graphics2D g = surface.createGraphics();
    g.setColor(PURPLE);
    try{
      if (endtime != null)
      {
      String tempdate = endtime.substring(0,10); //get date
      String tempmonth = endtime.substring(5,7);//get month
      String tempday = endtime.substring(8,10);  //get day
      String temphour = endtime.substring(11,13); // get hour

      int inttempmonth = Integer. parseInt(tempmonth); //convert month to int
      int inttempday = Integer. parseInt(tempday); //convert day to int
      int inttemphour = Integer. parseInt(temphour); //convert hour to int
      
      System.out.println(inttempmonth+" "+ inttempday+" "+ inttemphour);
      if(inttemphour<6)
      {inttemphour =0;}
      else if(inttemphour<12)
      {inttemphour = 6;}
      else if(inttemphour<18)
      {inttemphour = 12;}
      else if(inttemphour<24)
      {inttemphour = 18;}
      else if(inttemphour==24)
      {inttemphour = 0;}
      
      windarray = model.winddataarray(inttempmonth, inttempday, inttemphour);

      }
     else
    windarray = model.winddataarray(1, 12, 0);
      
    }catch(Exception e){ System.out.println("oceanproblem");}
    
    double windlat = 60.125;
    for(int i = 0; i<201; i+= 2)
    { 
      double windlong = 260.125;
      for(int j = 0; j<201; j += 2)
      {     
      double holdlong1 =(((windlong+(0.5*j))-260)-xmin)*ratio;
      double holdlat1 = ((-(windlat-60))-ymin)*ratio;
      double holdlong2 =((((windlong+((windarray[i][j][0])/15))+(0.5*j))-260)-xmin)*ratio;//u effects east to west 
      double holdlat2 = ((-((windlat+((windarray[i][j][1])/15))-60))-ymin)*ratio;//v effects north to south
      AffineTransform tx = new AffineTransform();
      Path2D path = new Path2D.Double();
      
      if(windarray[i][j][0] != 0 || windarray[i][j][1] != 0 )
      {
        double mag = (Math.sqrt(Math.pow((windarray[i][j][0]), 2.0) + Math.pow((windarray[i][j][1]), 2.0)))/10 ;
        mag = mag+(ratio/30);
        path.moveTo( 0, mag);
        path.lineTo(-mag, -mag);
        path.lineTo(mag, -mag);
        path.closePath();
        g.draw(new Line2D.Double(holdlong1, holdlat1, holdlong2, holdlat2));
        tx.setToIdentity();
        double angle = Math.atan2(holdlat2-holdlat1, holdlong2-holdlong1);
        tx.translate(holdlong2, holdlat2);
        tx.rotate((angle-Math.PI/2d));  
        Graphics2D h = surface.createGraphics();
        h.setColor(PURPLE);
        h.setTransform(tx);   
        h.fill(path);
        h.dispose();
      }
      }
    windlat = windlat -0.5;
    }
     g.dispose();
    view.repaint();  
  }
  
  
  public void undrawWindArrows()
  {
    mapRedraw(ratio, unit);
     if(simrun == true)
    refreshSim();
  }
  
  
  public void refreshSim()
  {
    Graphics2D g = surface.createGraphics();   
    g.setColor(Color.red);
   
    if(rows == 2)
    {
    for(int i=0; i<1000; i++)
    {
      if( probpositions[i][2]<= totalElapsedTime)
      {
        for(int k=0; k<100; k++)
        { 
          if(model.landcheck(randarrayHolder[i][k][0], randarrayHolder[i][k][1], landArray) == false)
            g.draw(new Line2D.Double(((((randarrayHolder[i][k][1])-260)-xmin)*ratio), (((-((randarrayHolder[i][k][0])-60))-ymin)*ratio ),((((randarrayHolder[i][k][1])-260)-xmin)*ratio), (((-((randarrayHolder[i][k][0])-60))-ymin)*ratio )));
        } 
      }
    }
    }
    if(rows ==0 || rows==1)
    {
       for(int k=0; k<1000; k++)
             {  
               if(model.landcheck(randarray[k][0], randarray[k][1], landArray) == false)
                 g.draw(new Line2D.Double(((((randarray[k][1])-260)-xmin)*ratio),((-((randarray[k][0])-60))-ymin)*ratio,((((randarray[k][1])-260)-xmin)*ratio),((-((randarray[k][0])-60))-ymin)*ratio));
               temprandarray[k][0] = randarray[k][0];
               temprandarray[k][1] = randarray[k][1]; 
             };
    }
    g.dispose();
    view.repaint();
  }
  
  public void tablerowcheck()
  {
    rows=0;
    String hold1, hold2;
    for(int i = 0 ; i<2; i++)//row
    {
      hold1 = (String)j.getValueAt(i, 2);
      hold2 = (String)j.getValueAt(i, 3);   
      if(hold1 != null && hold2 != null)
      {  rows++; }  
    }
  }
  
  
  
  public void zoom()
  { 
        double latzoom, longzoom;
    double stlatd, stlongd, nextlatd, nextlongd, changeinx, changeiny, diff;
String hold1, hold2;
    boolean xdom = true;
    
    if(rows == 1 )
    {
      hold1 = (String)j.getValueAt(0, 2);
      hold2 = (String)j.getValueAt(0, 3); 
      if(hold1 != null && hold2 != null)//they did not put in a intended path but there may be a location
      {
        latzoom = -((Double.parseDouble((String)j.getValueAt(0, 2)))-60);
        longzoom = (Double.parseDouble((String)j.getValueAt(0, 3)))+100;
        
        xmin = longzoom-5;
        xmax = longzoom+5;
        ymin = latzoom-5;
        ymax = latzoom+5;
      }
    }
    if(rows == 0 )
    {
      //get lat and long from the last known position
      latitude2InputText = latitude2Input.getText();               
      longitude2InputText = longitude2Input.getText();
      xmin = ((Double.parseDouble(longitude2InputText))+100)-5;
      xmax = ((Double.parseDouble(longitude2InputText))+100)+5;
      ymin = (-((Double.parseDouble(latitude2InputText))-60))-5;
      ymax = (-((Double.parseDouble(latitude2InputText))-60))+5;      
    }
    
    System.out.println("rows: "+rows);
    if(rows == 2)
    { 
      stlatd = -((Double.parseDouble((String)j.getValueAt(0, 2)))-60);
      stlongd = (Double.parseDouble((String)j.getValueAt(0, 3)))+100;
      nextlatd = -((Double.parseDouble((String)j.getValueAt(1, 2)))-60);
      nextlongd = (Double.parseDouble((String)j.getValueAt(1, 3)))+100; 
      
      if(stlatd>nextlatd)
      {ymax = stlatd; ymin = nextlatd;}
      else
      {ymax = nextlatd; ymin = stlatd;}
      if(stlongd>nextlongd)
      {xmax = stlongd; xmin = nextlongd;}
      else
      {xmax = nextlongd; xmin = stlongd;}
      System.out.println(xmax+" "+xmin+" "+ymin+" "+ymax);       
    }
    
    changeinx= xmax-xmin;
    changeiny= ymax-ymin;
    //change in x>y
    if(changeinx>changeiny)
    {
      xdom = true;
      diff = changeinx-changeiny;
      
      ymin = ymin - (diff/2);
      ymax = ymax + (diff/2);
      if(ymax > 50)
      {
        ymin = ymin - (ymax -50);
        ymax = 50;
      }
      if( ymin < 0)
      {
        ymax = ymax + (-(ymin));
        ymin = 0;
      }
    }
    if(changeinx<changeiny)
    {
      xdom = false;
      diff = changeiny-changeinx;
      xmin = xmin - (diff/2);
      xmax = xmax + (diff/2);
      if(xmax > 50)
      {
        xmin = xmin - (xmax -50);
        xmax = 50;
      }
      if( xmin < 0)
      {
        xmax = xmax + (-(xmin));
        xmin = 0;
      }        
    }       
    xmin = Math.floor(xmin);
    xmax = Math.ceil(xmax);
    ymin = Math.floor(ymin);
    ymax = Math.ceil(ymax);
    
    if(xdom == true)
    {
      unit = ((xmax - xmin)/4);
      xmin = xmin - unit;
      xmax = xmax + unit;
      ymin = ymin - unit;
      ymax = ymax + unit;
      unit = ((xmax - xmin)/5);
      ratio = 600/(xmax-xmin);
    }else{
  
      unit = ((ymax - ymin)/4);
      xmin = xmin - unit;
      xmax = xmax + unit;
      ymin = ymin - unit;
      ymax = ymax + unit;
      unit = ((xmax - xmin)/5);
      ratio = 600/(xmax-xmin);
    }
    
    mapRedraw(ratio, unit); 
  }


  public void mapRedraw(double ratio, double unit) {/////////////////////////////////////////////////////////////
    Graphics2D g = surface.createGraphics();
    
    g.fillRect(0,0,600,600);
    
    g.setColor(Color.black);
    g.drawRect (0, 0, 599, 599); 
    g.setColor(DARK_GREEN);
    int count=0;//testing 1785
    int hold=0, step =0;
    String x , y , x2= "", y2= "";
    boolean firsthit = true;
    double resultx , resulty, resultx2, resulty2;
    
    String[] var = {};
    BufferedReader in;
    try{
      in = new BufferedReader(new FileReader("IntermediateMap.txt"));
      String s; 
      int amount ;
      resultx=-1;
      resulty=-1;
      amount= 63723;
      boolean first = true;
      boolean continuous = false;
      s = in.readLine();
      var = s.split("\t");
      x = var[0];
      y = var[1];
      resultx = Double.parseDouble(x); 
      resulty = Double.parseDouble(y);
      resultx = (resultx-xmin)*ratio;
      resulty = (resulty-ymin)*ratio;
      amount--;
      
      while(count<amount)
      {
        count++;
        s = in.readLine();
        var = s.split("\t");
        x2 = var[0];
        y2 = var[1];
        resultx2 = Double.parseDouble(x2); 
        resulty2 = Double.parseDouble(y2);
        
        
        //resultx2 will only be greater than 50 when it is a flag for a new land mass
        if(resultx2 > 50)
        {
          count++;
          s = in.readLine();
          var = s.split("\t");
          x = var[0];
          y = var[1];
          resultx = Double.parseDouble(x); 
          resulty = Double.parseDouble(y);
          resultx = (resultx-xmin)*ratio;
          resulty = (resulty-ymin)*ratio;
          count++;
          s = in.readLine();
          var = s.split("\t");
          x2 = var[0];
          y2 = var[1];
          resultx2 = Double.parseDouble(x2); 
          resulty2 = Double.parseDouble(y2);
        }
        
        
        //This is for lines when a continuous coast line is segmented by the window causing a line             
        //if both the first and second is in, but they create a FAKE line
        //set the first to the second and get another second one
        //then continue
             
        if (((resultx2 <= xmax) && (resultx2 >= xmin))&&((resulty2 <= ymax)&&(resulty2 >= ymin)))
        { 
          if(count != hold+1)
          {
            resultx2 = (resultx2-xmin)*ratio;
            resulty2 = (resulty2-ymin)*ratio;
            resultx = resultx2;
            resulty = resulty2;
            count++;
            s = in.readLine();
            var = s.split("\t");
            x2 = var[0];
            y2 = var[1];
            resultx2 = Double.parseDouble(x2); 
            resulty2 = Double.parseDouble(y2);
            hold = count;                
          }
        }
                   
        if (((resultx2 <= xmax) && (resultx2 >= xmin))&&((resulty2 <= ymax)&&(resulty2 >= ymin)))
        { 
          if(first==true)
          {
            
            resultx2 = (resultx2-xmin)*ratio;
            resulty2 = (resulty2-ymin)*ratio;
            resultx = resultx2;
            resulty = resulty2;
            first=false;
          }
          else
          {                
            resultx2 = (resultx2-xmin)*ratio;
            resulty2 = (resulty2-ymin)*ratio;
            g.draw(new Line2D.Double(resultx, resulty, resultx2, resulty2));//make a line on the map
            hold = count;
            resultx = resultx2;
            resulty = resulty2;
            
          }
        }

      }
      first=true;
      g.setColor(Color.black);
      for(int i=1; i<5; i++)
      {
        spacing = (unit*i)*ratio;
        g.draw(new Line2D.Double(spacing, 0, spacing, 600));
        String str = String.valueOf(-(((unit)*i)-(60-ymin)));
        str = " "+ str;
        g.drawString(str, 0, (int)spacing);
        g.draw(new Line2D.Double(0, spacing, 600, spacing));
        String str2 = String.valueOf(((unit)*i)-(100-xmin));
        str2 = " "+ str2;
        g.drawString(str2, (int)spacing, 10); 
      }
      
      if(intendedPath==true)
      {
        String holder;
        double doubleholder;
        int countintended = 0;
        for(int i = 0 ; i<2; i++)//row
        {
          for(int k = 2 ; k<4; k++)//column
          {
            holder = (String)j.getValueAt(i, k);
            if(holder != null)
            {
              doubleholder = Double.parseDouble(holder); 
              cord[countintended] = doubleholder;
              countintended++;
            }
          }
        }
        numbercord = countintended;
        drawPath(cord, countintended);       
      }    
      
      
      if(oceanArrows==true)
      {drawOceanArrows();}
      
      
      if(windArrows==true)
      {drawWindArrows();}
      
      
      view.repaint();
    } catch (IOException e) {e.printStackTrace();}
  }
  

  private class CellEditor extends DefaultCellEditor {
    InputVerifier verifier = null;
    public CellEditor(InputVerifier verifier) {
      super(new JTextField());
      this.verifier = verifier;
    }
    @Override
    public boolean stopCellEditing() {
      return verifier.verify(editorComponent) && super.stopCellEditing();
    }
  }

  private class LatVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
      boolean verified = false;
      String text = ((JTextField) input).getText();
      try {     
        double port = Double.valueOf(text);
        if (((10 <= port) && (port <= 60))) {
          input.setBackground(Color.WHITE);
          verified = true;
        } else {
          input.setBackground(Color.RED);
        }
      } catch (NumberFormatException e) {
        input.setBackground(Color.RED);
      }
      return verified;
    }
  }
  private class LongVerifier extends InputVerifier {
    @Override
    public boolean verify(JComponent input) {
      boolean verified = false;
      String text = ((JTextField) input).getText();
      try {
        double port = Double.valueOf(text);
        if ((-100<= port) && (port <= -50)) {
          input.setBackground(Color.WHITE);
          verified = true;
        } else {
          input.setBackground(Color.RED);
        }
      } catch (NumberFormatException e) {
        input.setBackground(Color.RED);
      }
      return verified;
    }
  }
}