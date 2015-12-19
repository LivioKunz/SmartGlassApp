package ch.pawi.smartglassapp.communication;

import foodfinder.hslu.ch.foodfinderapp.entity.Product;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPServer implements Runnable {

    private ServerSocket tcpServer;
    private Socket input = null;
    private int port;
    private boolean send = false;

    private static TCPServer instance;

    public Socket getInput() {
        return input;
    }

    private TCPServer() {
    }

    public void setPort(int port) {
        this.port = port;
    }

    public static synchronized TCPServer getInstance() {

        if (instance == null){
            instance = new TCPServer();
        }
        return instance;
    }

    public void connect() throws IOException {
        try {
            this.tcpServer = new ServerSocket(this.port);
            input = tcpServer.accept(); //Warte auf den Client zum connecten
            //System.out.println("ClientAdresse: "+input.getInetAddress());
        } catch (IOException e) {
            System.err.println("Nicht verbunden!");
        }finally{
            tcpServer.close();
        }
    }

    @Override
    public void run() {
        try {
            if (input == null) {
                System.out.println("Waiting for Client to connect...");
                connect();
                System.out.println("connected!");
            }
        }catch(Exception ex){
            System.out.println("Exception ist aufgetreten: "+ex);
        }
    }

    //Product Objekt erhalten von Smarthpone
    public Product receive(){
        Product prd = null;
        try{
            ObjectInputStream inFromServer = new ObjectInputStream(input.getInputStream());
            prd = (Product) inFromServer.readObject();

            System.out.println("Empfangenes Produkt: " + prd.getName());

        }catch(IOException ex){
            System.out.println("Fehler beim empfangen des Objekts: "+ex);
        }catch(ClassNotFoundException ex){
            System.out.println("Fehler Klasse nicht gefunden: "+ex);
        }
        return prd;
    }

    //Boolean senden: Objekt gefunden -> true
    public void send(boolean objectFound){
        try{
            ObjectOutputStream outToServer = new ObjectOutputStream(this.input.getOutputStream());
            outToServer.writeObject(objectFound);
            this.input.close();
        }catch (IOException ex){
            System.out.println("Fehler beim senden des Objekts: "+ex);
        }
    }
}
