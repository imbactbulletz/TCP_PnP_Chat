package server;

import model.User;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    public static final int port = 2018;
    public static HashMap<User,ServerThread> connections; // mapa koja sadrzi korisnike i njihove odgovarajuce thread-ove

    public static void main(String[] args) throws IOException {
        System.out.println("Server je pokrenut.");
        connections = new HashMap<User,ServerThread>();

        ServerSocket serverSocket = new ServerSocket(port);

            while(true){
            Socket socket = serverSocket.accept();
            System.out.println("["+ socket.getInetAddress()+"] se konektovao.");
            (new Thread(new ServerThread(socket))).start();
            }


    }
}
