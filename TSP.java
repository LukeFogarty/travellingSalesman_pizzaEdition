//these are used for the graphics windows and events
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//Luke Fogarty - 20253079

public class TSP extends JPanel  {

   private Timer timer;                             //for updating the graphics.
   static TSP travellingSalesman;
   static int mouseX = 0;        //This is the mouse's x position on the screen. We use this to calculate if the mouse is over a house, to display the address information
   static int mouseY = 0;        //This is the mouse's y position on the screen.
   static  JFrame frame = new JFrame("The Travelling Salesman Problem: Pizza Delivery Edition"); // declare the frame here so our mouse position is correct
   static int width = 1200;        //This is the screen's width
   static int height = 600;        //This is the screen's height

   static House[] houses = new House[101];          //a house class array that stores all information on the input addresses
   static Driver driver;          //this is the driver. It moves around once a route has been planned and holds our speed variable
   static AngryClock clock= new AngryClock();       //visual clock for the angry minutes
   static double[][] farLatLon = new double[2][2];  // This gets the furthest lats and lons so we can display the loctions centred
   static double[][] distanceMatrix= new double[101][101];    //this will give us the distance between every house quickly, according to order number
   static double travelDistance = 0;
   static int[] xg = new int[30]; //polygon x coordinates drawing a green landmass for effect. Not important
   static int[] yg = new int[30]; //polygon y coordinates drawing a green landmass for effect. not important
   static int[] xw = new int[30]; //polygon x coordinates drawing a blue river for effect. Not important
   static int[] yw = new int[30]; //polygon y coordinates drawing a blue river for effect. not important

   static int orders = 0;         //this is the number of orders, and how we keep track of our current route
   static String error = "";      //this is our error message, it's length determines if there was an error or not. It then displays its error message
   static Boolean start = false;  //this lets us know the computation was successful, and begins the animation
   static int drawnLines = -1;    //this keeps track of the lines drawn on the map
   static double distanceToGo=18; //for cycling through the orders and remembering the length of the houses. We don't start at zero so we can create a delay before a line is drawn
   static Boolean drawn = false;  //for drawing random shape of green on background

   static ArrayList<House> route = new ArrayList<House>();      //this saves our route
   static ArrayList<House> unvisited = new ArrayList<House>();  //this tracks what house is left to be visited
   static double angryMinutes = 0;                              //time spent angrily waiting for pizza by our potential customers
   static double travelTime =0;                                 //time taken on the journey

   public TSP() {

      setBackground(new Color(110, 165, 95));//set the colour of the grass

      ActionListener playFrame = new ActionListener() {
         public void actionPerformed(ActionEvent evt) {
           mouseX = ((int)MouseInfo.getPointerInfo().getLocation().getX()-frame.getX())-5;
           mouseY = ((int)MouseInfo.getPointerInfo().getLocation().getY()-frame.getY())-30;
           if (drawnLines<orders) drawMapLines();
           repaint();// refresh the graphics.
         }
      };
      timer = new Timer( 30, playFrame );  // runs every 30 milliseconds.
   }

   public void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      if (driver== null) {
       driver = new Driver();
       timer.start();

       for (int g = 1; g<30;g++){
         xg[g]=(width/3)+((width/44)*g)+(int)(Math.random()*(-40)+Math.random()*(80));
         yg[g]=((height/29)*g)+(int)(Math.random()*(-40)+Math.random()*(80));
          //rather than make anothe loop, I'm drawing the river's points here
         int xrandom = (int)(Math.random()*-50);
         int yrandom = (int)(Math.random()*100);
         xw[g]= (width-(width/29)*g)+xrandom;
         yw[g]= ((height/29)*g)+yrandom;
       }
       //place in bottom and top left corners, and half way across the top
       xg[0] = width; yg[0] =0;
       xg[1] = width/3; yg[1] = 0;
       xg[29] = width; yg[29] = height;
       xw[0] = width; yw[0] =0;
       xw[29] = 0; yw[29] = height;
     }
  //set up the land mass
  Graphics2D lines = (Graphics2D) graphics;
  lines.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// this makes the text more legible by allowing anti-aliasinh
  //draw a messy green bit, to break up the background a little
  lines.setColor(new Color(115, 175, 100));
    lines.fillPolygon(xg, yg, 30);
    //draw a random river, to break up the background a little
    Stroke riverLine = new BasicStroke(3.0f);
    lines.setStroke(riverLine);
    lines.setColor(new Color(105, 115, 185));
    for (int i = 1; i<xw.length; i++){
      lines.drawLine(xw[i-1],yw[i-1],xw[i],yw[i]);
    }
  //only start drawing when the route is computed. This is mainly for error checking
  if (start == true){
     final float dash1[] = {10.0f};//https://docs.oracle.com/javase/tutorial/2d/geometry/strokeandfill.html
     Stroke routeLine = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
     lines.setStroke(routeLine);
     lines.setColor(new Color(250, 85, 85));
     //drawing each line
     for (int i = 0; i<drawnLines; i++){
       int thisX = route.get(i).x;
       int thisY = route.get(i).y;
       int nextX = route.get(i+1).x;
       int nextY = route.get(i+1).y;
       lines.drawLine(thisX,thisY,nextX,nextY);
     }
     //draw the moving line
     if(drawnLines>=0 && drawnLines<orders-1 && distanceToGo>=0){
       int setX = route.get(drawnLines).x;
       int setY = route.get(drawnLines).y;
       //this might look horrible and maybe a bit complicated, but it makes sure that the line being drawn cannot exceed the distance between each house. This keeps the lines neat.
       int movingX = (int)((route.get(drawnLines+1).x-setX)*Math.max(0,Math.min(1,((distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]-distanceToGo)/distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]))));
       int movingY = (int)((route.get(drawnLines+1).y-setY)*Math.max(0,Math.min(1,((distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]-distanceToGo)/distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]))));
       lines.drawLine(setX,setY,setX+movingX,setY+movingY);
     }

    //draw the houses. The furtherst south are drawn last, appearing over obejects behind them. This goes for the info bubble too.
    //this is because we sorted them using a bubblesort earlier
    for (int i = 0; i<orders; i++){
      houses[i].draw((Graphics2D)graphics);
    }
    //Next, draw the driver over the houseAddress
    driver.draw((Graphics2D)graphics);
    clock.draw((Graphics2D)graphics);
    //reset the basic stroke
    lines.setStroke(new BasicStroke(1.0f));
   }
}
/*------------------------------coordinates class-------------------------------*/
   public static class Coordinates {
     double  lat = 0.0;
     double  lon = 0.0;
     //constructor
     Coordinates(double newLat, double newLon){
       lat = newLat;
       lon = newLon;
     }
   }

/*--------------------------------house class-----------------------------------*/
   public static class House {
     int id = 0;
     int waiting = 0;
     Coordinates coord = new Coordinates(0,0);
     String address = "";
     //
     Boolean ifVisited = false;
     //visual variables
     double popUp = 0.0;
     int easeIn = 5+(int)(Math.random()*25);
     double infoBubble = 0.0;
     Color roofColour = new Color(100+(int)(Math.random()*150), 100+(int)(Math.random()*150), 100+(int)(Math.random()*150));
     Color doorColour = new Color(80+(int)(Math.random()*50), 80+(int)(Math.random()*50), 80+(int)(Math.random()*50));
     //set the postions on the screen so they can be refered to easily
     int x = 0;
     int y = 0;

     //constructor
     House(int idNew, String houseAddress, int initialWait, double newLat, double newLon){
       id = idNew;
       waiting = initialWait;
       coord = new Coordinates(newLat, newLon);
       address = houseAddress;
     }

     void draw(Graphics2D g) {  // Draws the house at its lat and lon.
       g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// this makes the text more legible by allowing anti-aliasing
       //this set up for X and Y ensures that all addresses will always be visible. It may distort the elongate the shape,
       //but once the numbers are correct I don't think that's bad!
        x = 420+(int)(700*((coord.lon-farLatLon[1][0])/(farLatLon[1][1]-farLatLon[1][0])));
        y = 500-(int)(450*((coord.lat-farLatLon[0][0])/(farLatLon[0][1]-farLatLon[0][0])));

       //easeIn and popUp make the house markers pop up nice and randomly around the map when first created.
       if (easeIn>0) easeIn--;
       if (easeIn<=0 && popUp<1) popUp += 0.2;

       //draw the house, or apache pizza if the address is right
       if (address.contains("Apache Pizza") && waiting == -1){
         g.setColor(new Color(230, 230, 230));
  		   g.fillRect(x-(int)(5*popUp), y-(int)(3*popUp), (int)(10*popUp), (int)(6*popUp));
         g.setColor(new Color(150, 190, 230));
  		   g.fillRect(x-(int)(4*popUp), y-(int)(2*popUp), (int)(6*popUp), (int)(4*popUp));
         g.setColor(new Color(160, 100, 90));
  		   g.fillRect(x+(int)(1*popUp), y-(int)(2*popUp), (int)(3*popUp), (int)(6*popUp));
         g.setColor(new Color(230, 100, 90));
         int[] xr = {x-(int)(6*popUp),x-(int)(3*popUp),x+(int)(3*popUp),x+(int)(7*popUp)};
         int[] yr = {y-(int)(3*popUp),y-(int)(8*popUp),y-(int)(8*popUp),y-(int)(3*popUp)};
         g.fillPolygon(xr, yr, 4);
       }else{
		   g.setColor(new Color(230, 230, 230));
		   g.fillRect(x-(int)(4*popUp), y-(int)(3*popUp), (int)(8*popUp), (int)(6*popUp));
       g.setColor(doorColour);
		   g.fillRect(x-(int)(1*popUp), y-(int)(2*popUp), (int)(3*popUp), (int)(6*popUp));
       g.setColor(roofColour);
       int[] xr = {x,x-(int)(6*popUp),x+(int)(6*popUp)};
       int[] yr = {y-(int)(8*popUp),y-(int)(3*popUp),y-(int)(3*popUp)};
       g.fillPolygon(xr, yr, 3);
        }
       //draw a bubble with the address inside when hovering near the house
       int addressLength = g.getFontMetrics().stringWidth(address);
       g.setColor(new Color(255, 255, 255));
       g.fillRoundRect( x-(int)((addressLength*infoBubble)/2),y-30,(int)((addressLength+10)*infoBubble),18,15,15);
       //this creates a triangle that points to the marker. xs and ys are the X and Y coordinates of each point in the polygon
       int[] xs = {x,x-(int)(5*infoBubble),x+(int)(5*infoBubble)};
       int[] ys = {y-5,y-15,y-15};
       g.fillPolygon(xs, ys, 3);

       int printLetters = Math.max(0,Math.min(address.length()-1,(int)(address.length()*infoBubble)));
       g.setColor(new Color(0, 0, 0));
       g.drawString(address.substring(0, printLetters), x-(int)((addressLength*infoBubble)/2)+5, y-17);

       //this expands the bubble when the mouse is in the correct position
      if (mouseX-x <= 5 && mouseX-x >= -5 && mouseY-y<= 5 && mouseY-y>= -5){
            if (infoBubble<1) infoBubble+=0.2;
            if (infoBubble>1) infoBubble=1;
            if (popUp < 1.4) popUp +=0.2;
        } else if (infoBubble>0){infoBubble-=0.3; if (infoBubble<0) {infoBubble = 0;} if (popUp > 1) popUp -=0.2;}
	   }
   }
   /*--------------------------------driver class-----------------------------------*/
      public static class Driver {
        //set the postions on the screen so they can be refered to easily
        int x = 500;
        int y = 200;
        int facing = 1; //this will flip the driver depending on his direction
        int facingAssist = 0; //because the origin of drawn graphics is in its top left corner, we need a little assitance to make everythinf line up correctly when filpped
        double speed = 600.0/3600.0;
        void draw(Graphics2D g) {  // Draws the house at its lat and lon.
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// this makes the text more legible by allowing anti-aliasing
          if (route.size()>0 && distanceToGo>=0){
            //this set up for X and Y is the same as the line being drawn.
            x = route.get(drawnLines).x+(int)((route.get(drawnLines+1).x-route.get(drawnLines).x)*Math.max(0,Math.min(1,((distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]-distanceToGo)/distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]))));
            y = route.get(drawnLines).y+(int)((route.get(drawnLines+1).y-route.get(drawnLines).y)*Math.max(0,Math.min(1,((distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]-distanceToGo)/distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id]))));
          }
          if (drawnLines == route.size() && distanceToGo>=0){
            //this set up for X and Y is the same as the line being drawn.
            x = route.get(drawnLines).x+(int)((route.get(route.size()-1).x-route.get(drawnLines).x)*Math.max(0,Math.min(1,((distanceMatrix[route.get(drawnLines).id][route.get(route.size()-1).id]-distanceToGo)/distanceMatrix[route.get(drawnLines).id][route.get(route.size()-1).id]))));
            y = route.get(drawnLines).y+(int)((route.get(route.size()-1).y-route.get(drawnLines).y)*Math.max(0,Math.min(1,((distanceMatrix[route.get(drawnLines).id][route.get(route.size()-1).id]-distanceToGo)/distanceMatrix[route.get(drawnLines).id][route.get(route.size()-1).id]))));
          }
          //change facing direction
          if (route.size()>0){ if (x>route.get(drawnLines).x) {facing=-1; facingAssist=1;} else {facing = 1; facingAssist=0;}}
          //drawing the bike and driver. The bike is first, then wheels then Driver
          //wheels
          g.setColor(new Color(90, 90, 120));
          g.fillOval(x-2*(1-facingAssist)-11*facing, y-2, 7,7);
          g.fillOval(x+2*facing, y-2, 7,7);
          //bike frame
          g.setColor(new Color(240, 210, 100));
          int[] xr = {x+(facingAssist*5)-9*facing, x+(facingAssist*5)-3*facing, x+(facingAssist*5)-3*facing, x+(facingAssist*5)+2*facing, x+(facingAssist*5)+3*facing, x+(facingAssist*5)+8*facing, x+(facingAssist*5)+7*facing};
          int[] yr = {y+1, y-8, y-2, y-3, y-7, y-7, y};
          g.fillPolygon(xr, yr, 7);

          //pizza box, that depletes as orders are made. Its fairly pointless considering how fast the scooter moves about, but still!
          g.setColor(new Color(230, 230, 230));
          int pizzasLeft = (orders-drawnLines)/4;
   		    g.fillRect(x+4*facing, y-5-pizzasLeft, 6, pizzasLeft);
          //Driver
          g.setColor(new Color(125, 60, 60));//body
   		    g.fillOval(x-1*facing, y-12, 5,9);
          g.setColor(new Color(60, 60, 60));//legs
   		    g.fillOval(x+(facingAssist)-3*facing, y-8, 4, 7);
          g.setColor(new Color(60, 60, 60));//head
   		    g.fillOval(x-(facingAssist)-3*facing, y-16, 7,6);
          g.setColor(new Color(100, 100, 120));//helmet shine
   		    g.fillOval(x+(facingAssist*2)-3*(facing), y-15, 4,3);
        }
      }
/*--------------------------------clock class-----------------------------------*/
      public static class AngryClock {
        //set the postions on the screen so they can be refered to easily
        int x = width-85;
        int y = height-105;
        int count = 0; //this count up out angry minutes
        double angle = 0;
        int jumpIn = 200;
        int handX = 0;
        int handY = 0;
        double squash = 0;
        boolean reverse = false;

        void squashAndStretch(){
          x = width-85;
          y = height-105+jumpIn;

          //small bit of animation
          if (jumpIn>0) jumpIn-=5;
          if (jumpIn<0) jumpIn =0;

          if (!reverse) squash+=0.3;
          if (reverse) squash-=0.3;

          if (squash > 3) reverse = true;
          if (squash < 0) reverse = false;
          if (count<(int)(angryMinutes/60)-1)  angle+=0.4;
          //spin the hand
          if ( angle>360) angle = 0;
          handX = (int)((x+18-(x+30))* Math.cos(angle) - (y+18-(y+30)) * Math.sin(angle));
          handY = (int)((x+18-(x+30))* Math.sin(angle) + (y+18-(y+30)) * Math.cos(angle));
          //raise the angry minute counter
          count = (int)((angryMinutes/(orders-drawnLines))/60);
        }

        void draw(Graphics2D g) {
          g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);// this makes the text more legible by allowing anti-aliasing
          squashAndStretch();

          g.setColor(new Color(230, 100, 50));//clock
          g.fillOval(x, y+(int)(squash/2), 60,60-(int)squash);
          g.setColor(new Color(
          230, 230, 200));//face
          g.fillOval(x+10, y+10+(int)(squash/2), 40,40-(int)squash);
          Stroke clockLine = new BasicStroke(10.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
          g.setColor(new Color(210, 180, 175));//hand
          g.setStroke(clockLine);
          g.drawLine(x+30,y+30,x+30+handX,y+30+handY);
          g.setColor(new Color(60, 60, 120));//eyebrows
          g.drawLine(x+5,y-(int)squash,x+30,y+10+(int)squash);
          g.drawLine(x+30,y+10+(int)(squash*1.5),x+55,y-(int)squash);
          Font font1 = new Font("SansSerif", Font.BOLD, 18);
          g.setFont(font1);
          int stringLength = g.getFontMetrics().stringWidth(count+"");
          g.setColor(new Color(230, 30, 30));
          g.drawString(count+"", x+30-(int)((stringLength)/2), y+37);

        }
      }
   /*------------------------draw lines for the map-----------------------------*/
      public void drawMapLines() {
        if (distanceToGo <=0 && drawnLines<orders-1){
          drawnLines++;
          if (drawnLines<orders-2) distanceToGo = distanceMatrix[route.get(drawnLines).id][route.get(drawnLines+1).id];
          if (drawnLines==orders-2) distanceToGo = distanceMatrix[route.get(route.size()-2).id][route.get(route.size()-1).id];
        }else{
          distanceToGo -= 0.3;
        }
      }

/*-----------------getting the data separated into houses-----------------------*/
      static void populateMapFromInput(String text){
        //reset these static variables so we can start again.
        orders = 0;
        drawnLines = 0;
        travelDistance =0;
        angryMinutes = 0;
        travelTime = 0;
        distanceToGo=18;//this gives the houses time to pop up before drawing the lines
        route.clear();
        unvisited.clear();
        error = "";

        Scanner textScanner = new Scanner(text);
        //if the text is not long enough.
        if (text.length()<5 || text == null){
          error = "Please input some addresses! The correct order is: ID, Address, Time Waiting, GPS North, GPS West.";
          text = "";
        } else while (textScanner.hasNextLine() && orders<houses.length) {
           String line = textScanner.nextLine();
           //check for commas, else split by tabs
           String[] parts = line.split("\t");
           if (line.contains(",")){
             parts = line.split(",");
           }
           //do some error checking, if there are not enough parts for the address
           if (parts.length < 5 && line.length()>0){
             orders = 0;
             error = "There is an error with the information! Please ensure the order is correct./nThe order is: ID, Address, Time Waiting, GPS North, GPS West.";
             text = "";
             break;
           }
           for (int i = 0;i<parts.length; i++){
             parts[i] = parts[i].replace(",","");
             parts[i] = parts[i].trim();
           }
           //adding the restaurant
           if (orders == 0){
             houses[orders] = new House(0, "Apache Pizza ", -1, 53.38197, -6.59274);
             //getting the furthest lat points from each other and the furthers lon points
             farLatLon[0][0] = houses[orders].coord.lat;
             farLatLon[0][1] = houses[orders].coord.lat;
             farLatLon[1][0] = houses[orders].coord.lon;
             farLatLon[1][1] = houses[orders].coord.lon;
             orders ++;
           }
           //adding our new house
           houses[orders] = new House(Integer.parseInt(parts[0])," "+parts[1]+" ",Integer.parseInt(parts[2]),Double.parseDouble(parts[3]),Double.parseDouble(parts[4]));
           //changing the furthest coordinates
           if (houses[orders].coord.lat< farLatLon[0][0]) farLatLon[0][0] = houses[orders].coord.lat;
           if (houses[orders].coord.lat> farLatLon[0][1]) farLatLon[0][1] = houses[orders].coord.lat;
           if (houses[orders].coord.lon< farLatLon[1][0]) farLatLon[1][0] = houses[orders].coord.lon;
           if (houses[orders].coord.lon> farLatLon[1][1]) farLatLon[1][1] = houses[orders].coord.lon;
           orders ++;
         }
         //get the distance matrix set up before sorting the houses into draw order
         getDistances();//this uses order ID input into a 2Darray to find the distance between houses
         //sort the houses by latitude to  help with drawing them later
         for (int i = 0; i < orders-1; i++){
           for (int j = 0; j < orders-i-1; j++){
             if (houses[j].coord.lat < houses[j+1].coord.lat){
               House temp = houses[j];
               houses[j] = houses[j+1];
               houses[j+1] = temp;
             }
           }
         }
         //add to unvisited list
         for (int i = 0; i < orders; i++){
           unvisited.add(houses[i]);
         }
         textScanner.close();
      }

/*-----------------set up the distance matrix for the houses--------------------*/
         static void getDistances(){
          int earthRadius = 6371;
           //reset these static variables so we can start again.
           for (int i = 0; i<orders; i++){
             for (int j = 0; j<orders; j++){
               //Haversine formula
               Double latDistance = Math.toRadians(houses[i].coord.lat-houses[j].coord.lat);
               Double lonDistance = Math.toRadians(houses[i].coord.lon-houses[j].coord.lon);
               Double angle = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(houses[i].coord.lat)) * Math.cos(Math.toRadians(houses[j].coord.lat)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
               Double distance = 2 * Math.atan2(Math.sqrt(angle), Math.sqrt(1-angle));
               distanceMatrix[i][j] = earthRadius * distance;
             }
           }
         }
/*------------------------set up the driver's route-----------------------------*/
         static void setRoute(){
           int start = 0;
           //find the restaurant in unvisisted.
           for (int i=0; i<unvisited.size(); i++){
             if (unvisited.get(i).waiting == -1){
               start = i;
             }
           }
           int current = 0;
           int next = 0;
           route.add(unvisited.get(start));
           current= route.get(0).id;
           unvisited.remove(start);
           while (unvisited.size()>0){
             double distance = 20038; //there can't be a distance greater that half the radius of the Earth
             for (int i = 0; i<unvisited.size(); i++){
               //have how long a person has been waiting affect the distance a little, to minimise angry minutes
               if (distanceMatrix[current][unvisited.get(i).id]-(unvisited.get(i).waiting)*0.012 < distance){
                 next = i;
                 distance = distanceMatrix[current][unvisited.get(i).id];
               }
             }
             route.add(unvisited.get(next));
             current = unvisited.get(next).id;
             unvisited.remove(next);
           }
           //get the  distance
           for (int d = 1; d < route.size(); d++){
             travelDistance += distanceMatrix[route.get(d-1).id][route.get(d).id];
           }
           //randomise the route a number of times, see if we can beat our best
           for (int r = 0; r < 3000000; r++){
             jitterRoute();
           }
           //do some swaps, see if we can shorten the route
           for (int i = 1; i < route.size()-1; i++){
             for (int j = 1; j < route.size()-1; j++){
               swap2Opt(i,i+1,j,j+1);
             }
           }
           timeTaken();
         }
/*---------------------randomising the route a litte----------------------------*/
          static void jitterRoute(){
            ArrayList<House> newRoute = new ArrayList<House>();
            double currentDistance = 0;
            //get a copy of our route as is
            for (int i = 0; i < route.size(); i++){
              newRoute.add(route.get(i));
            }
            int changes = 1+(int)Math.random()*999;
            //randomise it
            for (int i = 0; i < changes; i++){
              double random = Math.random()*12;
              if (random<5){//control the randomice a little
                Collections.swap(newRoute, 1+(int)(Math.random()*(newRoute.size()-1)), 1+(int)(Math.random()*(newRoute.size()-1)));
              }
            }
            //get the new distance
            for (int i = 1; i < route.size(); i++){
              currentDistance += distanceMatrix[newRoute.get(i-1).id][newRoute.get(i).id];
              if (currentDistance>travelDistance) break; //if its greater already, stop the loop
            }
            //if shorter, use the new order
            if (currentDistance<travelDistance){
              travelDistance = currentDistance;
              route.clear();
              for (int i = 0; i < newRoute.size(); i++){
                route.add(newRoute.get(i));
              }
            }
           }
/*----------------------swapping parts of the route-----------------------------*/
         static void swap2Opt(int swap1, int swap2, int swap3, int swap4){
           double currentDistance = 0;
           //swap our elements
           Collections.swap(route, swap1, swap4);
           Collections.swap(route, swap2, swap3);
           //get the new distance
           for (int i = 1; i < route.size(); i++){
             currentDistance += distanceMatrix[route.get(i-1).id][route.get(i).id];
             if (currentDistance>travelDistance) break;
           }
           //if shorter, use the new order
           if (currentDistance<travelDistance){
             travelDistance = currentDistance;
           }else{//swap back if not shorter
             Collections.swap(route, swap1, swap4);
             Collections.swap(route, swap2, swap3);
           }
          }
/*----------------------swapping parts of the route-----------------------------*/
        static void timeTaken(){
            for(int i=1;i<route.size();i++){
            travelTime+=(distanceMatrix[route.get(i-1).id][route.get(i).id]*10)/driver.speed;
            angryMinutes+=Math.max(0.0,route.get(i).waiting*60+travelTime-1800);
          }
        }
/*------------------------------menu settings-----------------------------------*/
   public static void addComponentsToPane(Container container) {
	   container.setLayout(null);
	     JLabel labelForInput = new JLabel("Enter the order details:");
	     JTextArea input = new JTextArea(100,5); //set input
       input.getLineWrap();
       JScrollPane inputScrollBox = new JScrollPane(input); //make input scrollable
       JLabel labelForOutput = new JLabel("Route taken by Order Number:");
       JTextArea output = new JTextArea(100,5); //set output
       output.getLineWrap();
       JScrollPane outputScrollBox = new JScrollPane(output); //make output scrollable

       JButton button = new JButton("Compute Route"); //set submit button.

       JPanel controlPanel = new JPanel(null);
       //add to the panel
       controlPanel.add(button);
       controlPanel.add(labelForInput);
       controlPanel.add(inputScrollBox);
       controlPanel.add(labelForOutput);
       controlPanel.add(outputScrollBox);

       Dimension size = new Dimension(280, 20);
       labelForInput.setBounds(10 , 5, size.width, size.height);
       size = new Dimension(290, 400);
       inputScrollBox.setPreferredSize(size);
       inputScrollBox.setBounds(5, 25, size.width, size.height);

       size = new Dimension(280, 20);
       labelForOutput.setBounds(10 , 425, size.width, size.height);
       size = new Dimension(290, 45);
       outputScrollBox.setPreferredSize(size);
       outputScrollBox.setBounds(5, 445, size.width, size.height);

       size = new Dimension(180, 20);
       button.setBounds(60, 505, size.width, size.height);
       container.add(controlPanel);
       controlPanel.setBounds(10, 10, 300, 530);
       //button controller.
       button.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
             start = false;
             populateMapFromInput(input.getText());
             if (error.length() == 0){
               //set the route and get the output to be pasted
               setRoute();
               String routeToPrint = "";
               //here we add each destionation in order of order number/id
               for (int i = 1; i<orders; i++){
                 routeToPrint+= ""+route.get(i).id;
                 if (i<orders-1){
                   routeToPrint += ",";
                 }
                 //start the animation. I say animation, I mean movement...
                 start = true;
               }
               output.setText(routeToPrint);
             }else{
              output.setText(error);
             }
           }
       });
   }

/*---------------------------creating the display-------------------------------*/
   private static void createAndShowFrame() {
       travellingSalesman = new TSP();
       frame.setBounds(0,0,width,height);

       //Add contents to the window.
       frame.setContentPane(travellingSalesman);
       addComponentsToPane(frame.getContentPane());

       frame.setSize(width, height);
       frame.setLocation(500,200);
       frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
       frame.setResizable(false);

       frame.setVisible(true);
   }

/*------------------------------main method-------------------------------------*/
      public static void main(String[] args) {
          javax.swing.SwingUtilities.invokeLater(new Runnable() {
              public void run() {
            	  createAndShowFrame();
              }
          });

      }
} //the end
