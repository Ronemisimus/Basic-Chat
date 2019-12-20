/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.util.Date;

/**
 *
 * @author ronen
 */
public class Message implements Comparable<Message>{

    private long timestamp;
    private String Data;
    private ClientHandler sender;
    private static Message exit = new Message(0, "exit", null);

    public Message(long millisecods, String Data, ClientHandler sender) {
        this.timestamp = millisecods;
        this.Data = Data;
        this.sender = sender;
    }
    
    @Override
    public int compareTo(Message o) {
        return (int)(o.getTimestamp() - timestamp);
    }

    public String getData() {
        return Data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public ClientHandler getSender() {
        return sender;
    }
    
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Message){
            Message msg = (Message)obj;
            return msg.getData().equals(getData());
        }
        return false;
    }
    
    public static Message getExit(){
        return exit;
    }
    
}
