package server;

import java.awt.Color;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class Server {

    //Array of type server.ClientServiceThread, for all connected clients
    public static ArrayList<ClientServiceThread> Clients = new ArrayList<ClientServiceThread>();

    public static void main(String[] args) throws Exception {

        //Create the GUI frame and components
        JFrame frame = new JFrame ("Chatting Server");
        frame.setLayout(null);
        frame.setBounds(100, 100, 300, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JLabel connectionStatusLabel = new JLabel("No Clients Connected");
        connectionStatusLabel.setBounds(80, 30, 200, 30);
        connectionStatusLabel.setForeground(Color.red);
        frame.getContentPane().add(connectionStatusLabel);

        //create the welcoming server's socket (should not be closed)
        ServerSocket welcomeSocket = new ServerSocket(6969);

        //thread to always listen for new connections from clients
        new Thread (new Runnable(){ @Override
        public void run() {

            Socket connectionSocket;
            DataOutputStream outToClient;

            while (!welcomeSocket.isClosed()) { //make sure socket is open

                try {

                    //when a new client connect, accept this connection and assign it to a new connection socket
                    connectionSocket = welcomeSocket.accept();

                    //receive the connection request with name
                    BufferedReader inFromClient = new BufferedReader (new InputStreamReader(connectionSocket.getInputStream()));
                    String receivedcheck = inFromClient.readLine();

                    String check = "Valid"; //by default name is valid unless found otherwise

                    if (receivedcheck.startsWith("-AddUser")) {
                        String []strings = receivedcheck.split(";");

                        String clientname = strings[1];
                        String users = "";

                        for (int i = 0; i < Clients.size(); i++) { //check if name is not already in use by another client
                            users = users + ";" + Clients.get(i).clientname; //store list of all existing users in a string
                            if (Clients.get(i).clientname.equals(clientname)) {
                                check = "Invalid"; //request invalid if already used
                                break;
                            }
                        }

                        if (check.equals("Valid")) {

                            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                            outToClient.writeBytes(check + users + "\n"); //if valid send list of existing users to client to add to drop down menu

                            for (int i = 0; i < Clients.size(); i++) {
                                outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-AddUser;" + clientname + "\n"); //send user name to all other clients to add new user to their drop down menu
                            }

                            //add the new client to the client's array
                            Clients.add(new ClientServiceThread(clientname, connectionSocket, Clients));

                            //start the new client's thread
                            Clients.get(Clients.size() - 1).start();

                        } else if (check.equals("Invalid")) {
                            outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                            outToClient.writeBytes(check + "\n"); //if invalid send back invalid message
                        }

                    }

                }
                catch (Exception ex) {

                }

            }

        }}).start();



        //thread to always get the count of connected clients and update the label and send to clients if they are alone online
        new Thread (new Runnable(){ @Override
        public void run() {

            try {

                DataOutputStream outToClient;

                while (true) {

                    if (Clients.size() > 0) //if there are one or more clients print their number
                    {
                        if (Clients.size() == 1) {

                            connectionStatusLabel.setText("1 Client Connected");

                            //tell client if not one else is connected
                            outToClient = new DataOutputStream(Clients.get(0).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-OnlyUser" + "\n");

                        } else {

                            connectionStatusLabel.setText(Clients.size() + " Clients Connected");

                        }

                        connectionStatusLabel.setForeground(Color.blue);
                    }
                    else { //if there are no clients connected, print "No Clients Connected"

                        connectionStatusLabel.setText("No Clients Connected");
                        connectionStatusLabel.setForeground(Color.red);

                    }

                    Thread.sleep(1000); //to avoid Thread looping back with non delay

                }

            } catch (Exception ex) {

            }

        }}).start();

        frame.setVisible(true);

    }

}

