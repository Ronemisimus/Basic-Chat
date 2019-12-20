/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.util.List;

/**
 *
 * @author ronen
 */
public class SenderThread extends Thread{

    public SenderThread() {
        super();
    }

    @Override
    public void run() {
        Message msg=null;
        List<ClientHandler> clients=null;
        System.out.println("starting to send messages to clients...");
        while(true){
            msg = Main.remove();
            if(msg != null){
                System.out.println("sending message:" + msg.getData());
                while(msg!=null){
                    clients = Main.getClients();
                    for(ClientHandler ch:clients){
                        if(!msg.getSender().equals(ch))
                            ch.sendMessage(msg);
                    }
                    msg= Main.remove();
                }
            }
        }
    }
    
    
    
}
