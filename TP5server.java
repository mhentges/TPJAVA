/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tp5server;

import java.io.BufferedReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Valerie
 */
public class TP5server {

    Connection conn;
    ServerSocket serverSocket;
    public static void main (String[] args) throws IOException
  {
      
      
    new TP5server();
    
  }
    public  TP5server() throws IOException {
        
        
        try
    {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      String url = "jdbc:mysql://localhost/TP5";
      conn = DriverManager.getConnection(url, "root", "root");
     begin(4444);
      conn.close();
    }
    catch (ClassNotFoundException ex) {System.err.println(ex.getMessage());}
    catch (IllegalAccessException ex) {System.err.println(ex.getMessage());}
    catch (InstantiationException ex) {System.err.println(ex.getMessage());}
    catch (SQLException ex)           {System.err.println(ex.getMessage());}
  
        
    }
    
    public void begin(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        while (true) {
            System.out.println("Waiting for clients to connect on port " + port + "...");
            new ProtocolThread(serverSocket.accept()).start();
            //Thread.start() calls Thread.run()
        }
        
    }
    
    
    class ProtocolThread extends Thread {

        Socket socket;
        PrintWriter out_socket;
        BufferedReader in_socket;
        ObjectOutputStream out;

        public ProtocolThread(Socket socket) {
            System.out.println("Accepting connection from " + socket.getInetAddress() + "...");
            this.socket = socket;
            try {
                out_socket = new PrintWriter(socket.getOutputStream(), true);
                in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out= new ObjectOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try{
             out_socket.println("Hello");
             //envoyer liste des pieces
             ArrayList play=new ArrayList();
            play= listPlay();
            
                try {          
                    out.writeObject(play);
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(TP5server.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    String playSelected= in_socket.readLine();
                    
                  while(!"reservation".equals(playSelected)){
                    
                    int nbOfPlaces=nbOfPlaces(playSelected);
                    try {          
                    out.writeObject(nbOfPlaces);
                    out.flush();
                    playSelected= in_socket.readLine();
                        }
                     catch (IOException ex) {
                    Logger.getLogger(TP5server.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } 
                  
                  
                    } catch (IOException ex) {
                    Logger.getLogger(TP5server.class.getName()).log(Level.SEVERE, null, ex);
                        }
                try {
                    String nbPlace=in_socket.readLine();
                    String playUpdated=in_socket.readLine();
                    changePlace(nbPlace,playUpdated);
                    
                } catch (IOException ex) {
                    Logger.getLogger(TP5server.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                try {
                    String name=in_socket.readLine();
                    String playReserved=in_socket.readLine();
                    String number=in_socket.readLine();
                    
                    keepReservation(name,playReserved,number);
                    int reservationNumber=getNumberOfReservation(name);
                    out.writeObject(reservationNumber);
                    out.flush();
                    
                } catch (IOException ex) {
                    Logger.getLogger(TP5server.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                
                    }
        
            finally {
                try {
                    System.out.println("Closing connection.");
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    
    }
    
    
    private ArrayList listPlay(){
    
    ArrayList play=new ArrayList();
    
    System.out.println("SELECT en cours");
    String query = "SELECT nom FROM piecesTheatre";
    try
    {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      while (rs.next())
      {
      
         
         play.add( rs.getString("nom"));
        System.out.println("List of play found");
        
      }
     
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
        return play;
  
    
    }
    
    private int nbOfPlaces(String play){
    
        int numberOfPlaces=0;
        System.out.println("SELECT en cours");
    String query = "SELECT nbplace FROM piecesTheatre WHERE nom=\""+play+"\"";
    try
    {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      while (rs.next())
      {
      
         String number = rs.getString("nbPlace");
         numberOfPlaces=Integer.parseInt(number);
        System.out.println("number of places for "+play+" found");
       
      }
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
     return numberOfPlaces;
    }
    
    
    private void changePlace(String number,String play){
    
        try
    {
      Statement st = conn.createStatement();
      st.executeUpdate("UPDATE piecesTheatre SET nbPlace=nbPlace-"+number+" WHERE nom='"+play+"'");
      
      System.out.println(number+" places sold for "+play+" : bdd up to date");
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
        
        
    }
    
    private void keepReservation(String name, String play, String nb){
    
        String id="";
        System.out.println("select");
        String query = "SELECT ID FROM piecesTheatre WHERE nom=\""+play+"\"";
   try
    {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      while (rs.next())
      {
      
          id = rs.getString("ID");
         
        ;
       
      }
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
   
    try
    {
      Statement st = conn.createStatement();
      st.executeUpdate("INSERT INTO Réservation(nom,PieceID,nbplace) VALUES('"+name+"',"+id+","+nb+")");
      
      System.out.println("reservation Done");
      
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
   }
   
    private int getNumberOfReservation(String name){
    
        int id2=0;
        System.out.println("SELECT ");
    String query = "SELECT ID FROM Réservation WHERE nom=\""+name+"\" ORDER BY id DESC LIMIT 1";
    try
    {
      Statement st = conn.createStatement();
      ResultSet rs = st.executeQuery(query);
      while (rs.next())
      {
      
         String id = rs.getString("ID");
         id2=Integer.parseInt(id);
        
       
      }
    }
    catch (SQLException ex)
    {
      System.err.println(ex.getMessage());
    }
     return id2;
        
    };
    
}
