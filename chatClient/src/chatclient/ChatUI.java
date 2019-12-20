/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatclient;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicBorders;

/**
 *
 * @author ronen
 */
public class ChatUI extends JFrame{
    
    
    private String data = null;
    private JPanel messageboard;
    private JButton cls, reconnect;
    private JTextField input;
    private Socket mysocket=null;
    private JLabel text;
    private int message_counter = 0;
    private final int Max_Msg = 10;
    private RSA rsa=null;
    
    public ChatUI() throws HeadlessException {
        super("chat");
        System.out.println("initiallizing UI");
        String name = JOptionPane.showInputDialog("please insert you're chat name", null);
        String ip = JOptionPane.showInputDialog("enter ip of server");
        int port = Integer.parseInt(JOptionPane.showInputDialog("enter port num:"));
        initiallize(name, ip, port);
        pack();
        System.out.println("done");
        System.out.println("getting user name");
        System.out.println("done");
        startThreads(name, ip, port);
    }
    
    private void initiallize(String name, String ip, int port) {
        //<editor-fold desc="rsa initiallization">
        while(rsa == null){
            try{
            rsa = new RSA();
            }
            catch(RSA.BadKeyCalculationException e){
                
            }
        }
        //</editor-fold>
        setTitle(getTitle() + " - " + name);
        setSize(850, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new FlowLayout(FlowLayout.CENTER));
        messageboard = new JPanel(new FlowLayout(FlowLayout.LEFT));
        messageboard.setPreferredSize(new Dimension(770, 550));
        text = new JLabel();
        messageboard.setBorder(new BasicBorders.FieldBorder(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK));
        messageboard.add(text);
        input = new JTextField("insert message and click enter", 60);
        cls = new JButton("clear");
        reconnect = new JButton("reconnect");
        input.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    System.out.println("setting message");
                    setData(name + ": " +input.getText());
                    if(message_counter < Max_Msg){
                        text.setText(
                                    convertToMultiline(
                                            convertBack(text.getText()) + "\n\n" +
                                                        name + ": " +input.getText()
                                                   )
                                    );
                        message_counter++;
                    }
                    else{
                        text.setText(
                                convertToMultiline(
                                        "\n\n" +
                                        name + ": " +input.getText()
                                                )
                                    );
                        message_counter = 1;
                    }
                    System.out.println("message set to " + data);
                    input.setText("");
                }
            }
            
        });
        cls.addActionListener((ActionEvent e) -> {
            text.setText("");
        });
        reconnect.addActionListener((ActionEvent e)->{
            if(!mysocket.isConnected()){
                try{
                    mysocket.connect(new InetSocketAddress(ip, port));
                }catch(IOException ex){
                    ex.printStackTrace();
                    System.exit(1);
                }
            }
        });
        add(messageboard);
        add(input);
        add(cls);
        add(reconnect);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                System.out.println("trying to close socket");
                super.windowClosed(e); //To change body of generated methods, choose Tools | Templates.
                try {
                    mysocket.close();
                    System.out.println("success");
                } catch (IOException ex) {
                    Logger.getLogger(ChatUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
        });
        setVisible(true);
    }
    
    /**
     * defines the protocol on input and output
     * @param name - user name in chat
     * @param ip - the ip of the server
     */
    private void startThreads(String name, String ip, int port) {
        try{
            System.out.println("initiallzing socket with ip: " + ip );
            mysocket= new Socket(ip, port);
            System.out.println("now connected to " + mysocket.getRemoteSocketAddress());
            final ObjectOutputStream dos = new ObjectOutputStream(mysocket.getOutputStream());
            final ObjectInputStream dis = new ObjectInputStream(mysocket.getInputStream());
            /**
             * output protocol in sender
             * input protocol in getter
             */
            Thread sender = new Thread(){
                @Override
                public void run() {
                    // send the client name to server
                    try{
                        // send public key
                        dos.writeObject(rsa.getPublic_key());
                        dos.writeObject(rsa.getN());
                        
                        // send incripted name
                        dos.writeObject(rsa.encript(name, Key.private_key));
                    }catch(IOException e){
                        System.out.println("fail to send name!");
                        System.exit(0);
                    }
                    String msg = null;
                    while(true){
                        // gets the data to send once avaliable
                        msg = getData();
                        if(msg != null){
                            try{
                                // send the msg encripted
                                dos.writeObject(rsa.encript(msg, Key.private_key));
                            }catch(IOException e){
                                System.out.println("failed to sent msg: " + msg);
                            }
                            setData(null);
                        }
                    }
                }

            };
            Thread getter = new Thread(){
                @Override
                public void run() {
                    while(true){
                        try{
                            // get the message it self
                            String msg = rsa.Decript((byte[])dis.readObject(), Key.private_key);
                            if(message_counter < Max_Msg){
                                text.setText(
                                        convertToMultiline(
                                                convertBack(text.getText()) + "\n\n" +
                                                            msg
                                                       )
                                            );
                                message_counter++;
                            }
                            else{
                                text.setText(
                                        convertToMultiline("\n\n" +
                                                            msg
                                                            )
                                            );
                                message_counter = 1;
                            }
                        }
                        catch(IOException | ClassNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            };
            sender.start();
            getter.start();
        }catch(IOException e){
            if(message_counter < Max_Msg){
                text.setText(convertToMultiline(
                                            convertBack(text.getText()) +
                                                    "\n\n" +
                                                    "you disconnected"
                                            )
                            );
                message_counter ++;
            }
            else{
                text.setText(convertToMultiline("you disconnected"
                                            )
                            );
                message_counter = 1;
            }
             e.printStackTrace();
        }
        catch(HeadlessException e){
            if(message_counter < Max_Msg){
                text.setText(convertToMultiline(
                                            convertBack(text.getText()) +
                                                    "\n\n" +
                                                    "you disconnected"
                                            )
                            );
                message_counter ++;
            }
            else{
                text.setText(convertToMultiline("you disconnected"
                                            )
                            );
                message_counter = 1;
            }
            e.printStackTrace();
        }    
    }
    
    public static String convertToMultiline(String orig){
        return "<html>" + orig.replaceAll("\n", "<br>");
    }
    
    public static String convertBack(String orig){
        return orig.replaceAll("<br>", "\n").replaceAll("<html>", "");
    }

    public synchronized String getData() {
        return data;
    }

    public synchronized void setData(String data) {
        this.data = data;
    }
    
}
