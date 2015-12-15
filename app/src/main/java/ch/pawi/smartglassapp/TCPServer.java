package ch.pawi.smartglassapp;

import foodfinder.hslu.ch.foodfinderapp.entity.Product;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class TCPServer implements Runnable {

    private ServerSocket tcpServer;
    private Socket input = null;
    private final int port;
    private boolean send = false;

    public TCPServer(int port) {
        this.port = port;

        try {
            tcpServer = new ServerSocket(this.port); //Erstelle den Server
        } catch (IOException ex) {
            //Ein Fehler beim Erstellen des Servers ist eingetreten!
            System.err.println(ex);
        }
    }

    public void connect() {
        try {
            input = tcpServer.accept(); //Warte auf den Client zum connecten
            //System.out.println("ClientAdresse: "+input.getInetAddress());
        } catch (IOException e) {
            System.err.println("Nicht verbunden!");
        }
    }

    @Override
    public void run() {
        try {
            if (input == null) {
                System.out.println("Waiting for Client to connect...");
                connect();
                System.out.println("connected!");
                receive();
            }
        }catch(Exception ex){
            System.out.println("Exception ist aufgetreten: "+ex);
        }
    }

    public Product receive(){

        Product prd = null;

        try{
            ObjectInputStream inFromServer = new ObjectInputStream(input.getInputStream());
            prd = (Product) inFromServer.readObject();
            System.out.println("Empfangenes Produkt: "+prd.getName());

        }catch(IOException ex){
            System.out.println("Fehler beim empfangen des Objekts: "+ex);
        }catch(ClassNotFoundException ex){
            System.out.println("Fehler Klasse nicht gefunden: "+ex);
        }
        return prd;
    }

}
