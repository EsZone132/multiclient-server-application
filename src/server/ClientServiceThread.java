package server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ClientServiceThread extends Thread {

    //the server.ClientServiceThread class extends the Thread class and has the following parameters
    public String clientname; //client name
    public Socket connectionSocket; //client connection socket
    ArrayList<ClientServiceThread> Clients; //list of all clients connected to the server

    //constructor function
    public ClientServiceThread(String clientname, Socket connectionSocket, ArrayList<ClientServiceThread> Clients) {

        this.clientname = clientname; //used to identify clients
        this.connectionSocket = connectionSocket;
        this.Clients = Clients;

    }

    //thread's run function
    public void run() {

        try {

            //create a buffer reader and connect it to the client's connection socket
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));;
            String clientSentence;
            DataOutputStream outToClient;

            //always read messages from client
            while (true) {

                clientSentence = inFromClient.readLine();

                //check the start of the message
                if (clientSentence.startsWith("-RemUser")) { //Remove Client

                    for (int i = 0; i < Clients.size(); i++) {
                        if (Clients.get(i).clientname.equals(clientname)) {
                            Clients.remove(i); //removed client from list of clients
                            i--; //to avoid skipping a compare
                        } else {
                            outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                            outToClient.writeBytes("-RemUser;" + clientname + "\n"); //send to all other users to remove client from each drop down menu
                        }
                    }


                } else if (clientSentence.startsWith("-Message")) { //Message to be transmitted to one of more clients

                    String []msg = clientSentence.split(";");
                    //System.out.println("Server: " + clientSentence); //used for debugging

                    for (int i = 0; i < Clients.size(); i++) {
                        if (!Clients.get(i).clientname.equals(clientname)) { //avoid sending message back to sender
                            if (msg[1].equals(Clients.get(i).clientname)) { //check if message for specific user
                                outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-Message;" + clientname + " (Privately): " + msg[2] + "\n"); //send private message with sender info
                            } else if (msg[1].equals("All Users")) { //check if message for all users
                                outToClient = new DataOutputStream(Clients.get(i).connectionSocket.getOutputStream());
                                outToClient.writeBytes("-Message;" + clientname + ": " + msg[2] + "\n"); //send general message with sender info
                            }
                        }
                    }

                }
            }

        } catch(Exception ex) {

        }

    }

}
