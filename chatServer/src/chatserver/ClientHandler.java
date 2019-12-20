/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatserver;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;

/**
 *
 * @author ronen
 */
public class ClientHandler extends Thread {
    
    private Socket client;
    private ObjectOutputStream dos;
    private ObjectInputStream dis;
    private RSA pub_criptor;
    
    public ClientHandler(Socket client) {
        this.client = client;
    }
    
    @Override
    public void run() {
        System.out.println("starting comunication with " + client.getRemoteSocketAddress());
        
        try {
            dis = new ObjectInputStream(client.getInputStream());
            dos = new ObjectOutputStream(client.getOutputStream());
            pub_criptor = new RSA(BigInteger.ZERO, (BigInteger)dis.readObject(), (BigInteger)dis.readObject());
            
            String name  = pub_criptor.Decript((byte[])dis.readObject(), Key.public_key);
            setName(name);
            Message msg = null;
            while (!Message.getExit().equals(msg)) {
                long time = System.currentTimeMillis();
                String data = pub_criptor.Decript((byte[])dis.readObject(), Key.public_key);
                msg = new Message(time, data, this);
                Main.add(msg);
            } 
        }catch(EOFException | ClassNotFoundException e){
            Main.add(new Message(System.currentTimeMillis(), getName() + " disconnected", this));
        }catch (IOException e) {
            Main.add(new Message(System.currentTimeMillis(), getName() + " disconnected", this));
        }finally {
            try {
                dis.close();
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Main.remove(this);
        }
    }
    
    public void sendMessage(Message m) {
        try{
            dos.writeObject(pub_criptor.encript(m.getData(), Key.public_key));
        }catch(IOException | NullPointerException e){
            System.out.println("failed to send message " + m.getData());
        }
    }
    
}
