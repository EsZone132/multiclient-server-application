package client;

import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.event.*;

public class Client {

    //general class static variables to be accessible in Thread
    static Socket clientSocket;
    static JTextArea mainTextArea;
    static JComboBox<String> recipient;
    static JButton sendButton;
    static JTextArea sendTextArea;
    static JTextField nameTextField;

    static String clientname;

    public static void main(String[] args) throws Exception {

        //Create the GUI frame and components
        JFrame frame = new JFrame ("Chatting Client");
        frame.setLayout(null);
        frame.setBounds(100, 100, 500, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel nameLabel = new JLabel("Client Name");
        nameLabel.setBounds(20, 20, 80, 30);
        frame.getContentPane().add(nameLabel);

        nameTextField = new JTextField(); //takes name of client (used for private communication mainly)
        nameTextField.setBounds(110, 20, 150, 30);
        frame.getContentPane().add(nameTextField);

        JButton connectButton = new JButton("Connect"); //used to connect and later disconnect to server
        connectButton.setBounds(290, 18, 100, 30);
        frame.getContentPane().add(connectButton);

        mainTextArea = new JTextArea(); //contains server and client messages (general and private)
        mainTextArea.setBounds(20, 60, 440, 300);
        mainTextArea.setEditable(false);
        frame.getContentPane().add(mainTextArea);

        //text area scroll to be able to contain all messages in conversation
        JScrollPane mainTextAreaScroll = new JScrollPane(mainTextArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainTextAreaScroll.setBounds(20, 60, 440, 300);
        frame.getContentPane().add(mainTextAreaScroll);

        JLabel sendLabel = new JLabel("Send to"); //client(s) to send message to
        sendLabel.setBounds(20, 380, 100, 30);
        frame.getContentPane().add(sendLabel);
        sendLabel.setVisible(false);

        sendTextArea = new JTextArea(); //message to be sent
        sendTextArea.setBounds(20, 430, 300, 70);
        frame.getContentPane().add(sendTextArea);
        sendTextArea.setVisible(false);

        sendButton = new JButton("Send"); //button to send message
        sendButton.setBounds(340, 460, 80, 30);
        frame.getContentPane().add(sendButton);
        sendButton.setVisible(false);

        //Action listener when connect/disconnect button is pressed
        connectButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {

                    if (connectButton.getText().equals("Connect")) { //if pressed to Connect

                        if (nameTextField.getText().length() > 0) { //check if field contains name
                            clientname = nameTextField.getText();

                            //create a new socket to connect with the server application
                            clientSocket = new Socket ("localhost", 6969);

                            //send name
                            DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                            outToServer.writeBytes("-AddUser;" + clientname + "\n"); //send name to server for validation

                            //receive the reply (rejected or accepted)
                            BufferedReader inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
                            String receivedcheck = inFromServer.readLine(); //this check has no prefix it is the only packet starting with Valid/Invalid
                            String []strings = receivedcheck.split(";");

                            if (strings[0].equals("Invalid")) { //server found name in use, no client created and gui stays same

                                mainTextArea.setText("Connection rejected: The name " + clientname + " is used by another client");

                            } else if (strings[0].equals("Valid")) { //name is unique, client is created

                                StartThread(); //this Thread checks for input messages from server

                                String[] choices = {"All Users"}; //default recipient list can send message to all users
                                recipient = new JComboBox<String>(choices); //create drop down menu
                                recipient.setBounds(110, 380, 150, 30);
                                recipient.setVisible(true);
                                frame.add(recipient);

                                //need to add existing users to recipient list
                                for (int i = 1; i < strings.length; i++) {
                                    recipient.addItem(strings[i]);
                                }

                                //make the GUI components visible, so the client can send messages
                                sendButton.setVisible(true);
                                sendLabel.setVisible(true);
                                sendTextArea.setVisible(true);

                                mainTextArea.setText("You are Connected"); //clears all previous messages (new session)

                                //change the Connect button text to Disconnect
                                connectButton.setText("Disconnect");
                                nameTextField.setEnabled(false);
                            }
                        }

                    } else { //if pressed to Disconnect

                        //create an output stream and send a RemUser message to disconnect from the server
                        DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                        outToServer.writeBytes("-RemUser" + "\n");

                        //close the client's socket
                        clientSocket.close();

                        //make the GUI components for sending message invisible
                        sendButton.setVisible(false);
                        sendLabel.setVisible(false);
                        sendTextArea.setVisible(false);
                        recipient.setVisible(false);

                        mainTextArea.setText(mainTextArea.getText() + "\n" + "You Disconnected");

                        //change the Disconnect button text to Connect
                        connectButton.setText("Connect");
                        nameTextField.setEnabled(true);

                    }

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }});

        //Action listener when send button is pressed
        sendButton.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e)
            {
                try {
                    //create an output stream
                    DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());

                    if (!sendTextArea.getText().equals("")) { //make sure there is a message in text area

                        //send message with intended recipient before
                        String sendingSentence = "-Message;" + recipient.getSelectedItem() + ";" + sendTextArea.getText() + "\n"; //using ; as separators
                        outToServer.writeBytes(sendingSentence);
                        //System.out.println("Sending:" + sendingSentence); //used for debugging

                        //update text area for sender
                        if (recipient.getSelectedItem().equals("All Users")) { //syntax depends on global or one to one message
                            mainTextArea.setText(mainTextArea.getText() + "\n" + "You: " + sendTextArea.getText()); //append message to existing text
                        } else {
                            mainTextArea.setText(mainTextArea.getText() + "\n" + "You to " + recipient.getSelectedItem() + ": " + sendTextArea.getText());
                        }
                        sendTextArea.setText(""); //clear message text area to be able to right next message right away

                    }

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }
            }});

        //Disconnect on window close
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {

                try {

                    //create an output stream and send a RemUser message to disconnect from the server
                    DataOutputStream outToServer = new DataOutputStream (clientSocket.getOutputStream());
                    outToServer.writeBytes("-RemUser" + "\n");

                    //close the client's socket
                    clientSocket.close();

                    System.exit(0); //exit code

                } catch (Exception ex) {
                    System.out.println(ex.toString());
                }

            }
        });

        frame.setVisible(true);

    }

    //Thread to always read messages from the server and print them in the textArea
    private static void StartThread() {

        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                //create a buffer reader and connect it to the socket's input stream
                BufferedReader inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));

                String receivedSentence;

                //always read received messages and append them to the textArea
                while (true) {

                    receivedSentence = inFromServer.readLine();
                    //System.out.println(receivedSentence); //used for debugging

                    if (receivedSentence.startsWith("-Message")) { //received a message from another user

                        String []strings = receivedSentence.split(";");
                        mainTextArea.setText(mainTextArea.getText() + "\n" + strings[1]); //second index is the message with sender information

                    } else if (receivedSentence.startsWith("-AddUser")) { //new user to add to recipient list

                        String []strings = receivedSentence.split(";");
                        mainTextArea.setText(mainTextArea.getText() + "\n" + strings[1] + " is Connected"); //second index is the name of the user

                        //add user to drop down menu for sending message
                        recipient.addItem(strings[1]);
                        recipient.setEnabled(true);

                        //enable button and text area in case they were previously disabled if there was only one client connected
                        sendButton.setEnabled(true);
                        sendTextArea.setEnabled(true);
                        if (sendTextArea.getText().equals("No Other Users Connected")) {
                            sendTextArea.setText(""); //message erased that specifies that only one client is connected
                        }

                    } else if (receivedSentence.startsWith("-RemUser")) { //user removed from recipient list

                        String []strings = receivedSentence.split(";");
                        mainTextArea.setText(mainTextArea.getText() + "\n" + strings[1] +  " Disconnected"); //second index is the name of the user

                        for (int i = 0; i < recipient.getItemCount(); i++) {
                            if (recipient.getItemAt(i).equals(strings[1])) { //find and remove user from drop down menu
                                recipient.removeItemAt(i);
                                break;
                            }
                        }

                    } else if (receivedSentence.startsWith("-OnlyUser")) { //user is the only one connected to server
                        recipient.setEnabled(false); //disable send message related features
                        sendButton.setEnabled(false);
                        sendTextArea.setText("No Other Users Connected"); //set message in text area to tell user why they cannot send a message
                        sendTextArea.setEnabled(false);
                    }
                }

            }
            catch(Exception ex) {

            }

        }}).start();

    }

}
