/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import javax.swing.JOptionPane;

/**
 *
 * @author ronen
 */
public class Main {
    
    private static List<ClientHandler> clients = new ArrayList<>();
    private static Queue<Message> MessageQue = new PriorityQueue<Message>();
    private static Integer port = null;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Thread closer = new Thread(){
            @Override
            public void run() {
                JOptionPane.showMessageDialog(null, "server open! click ok to close! " + port);
                System.exit(0);
            }
            
        };
        ServerSocket server;
        SenderThread st = new SenderThread();
        try{
            server= new ServerSocket(0);
            port = server.getLocalPort();
            closer.start();
            System.out.println("working on port " + server.getLocalPort());
            Socket client;
            client = server.accept();
            System.out.print("a client is recived: " + client.getRemoteSocketAddress());
            ClientHandler t = new ClientHandler(client);
            clients.add(t);
            t.start();
            st.start();
            while(true){
                client = server.accept();
                System.out.println("a client is recived");
                t = new ClientHandler(client);
                clients.add(t);
                t.start();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    public static void remove(ClientHandler ch){
        clients.remove(ch);
    }
    
    public static List<ClientHandler> getClients(){
        return clients;
    }
    
    public static synchronized void add(Message msg){
        MessageQue.add(msg);
    }
    
    public static synchronized Message remove(){
        return MessageQue.poll();
    }
    
}
