package client;

import model.ProtocolMessages;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final int port = 2018;

    private static PrintWriter socket_out;
    private static BufferedReader socket_in;

    private static Scanner scanner;

    public static void main(String[] args) throws Exception {
        InetAddress address = InetAddress.getByName("127.0.0.1");

        Socket socket = new Socket(address,port);

        socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socket_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);


        System.out.println("Unesite 1 za Registraciju, 2 za login ili 0 za izlaz.");
        scanner = new Scanner(System.in);

        String input = scanner.nextLine();

        if(input.equals("1")){
            registracija();
        }

        if(input.equals("2")){


        }

        if(input.equals("0")){
            System.exit(0);
        }

        while(true);

    }

    private static void registracija() throws IOException {
        boolean potvrda = false; // potvrda od servera da ne postoji vec korisnik sa istim korisnickim imenom
        String serverResponse; // predstavlja odgovor od servera
        String username;



        while (!potvrda) {
            System.out.println("Unesite korisicko ime:");
             username = scanner.nextLine();

            // saljemo zahtev serveru za registraciju
            socket_out.println(ProtocolMessages.REGISTRATION_REQUEST);


            // saljemo korisnicko ime serveru
            socket_out.println(username);

            // citamo odgovor od servera
            serverResponse = socket_in.readLine();

            // proveravamo da li je server potvrdio da korisnik sa tim korisnickim imenom ne postoji
            if(serverResponse.equals(ProtocolMessages.USERNAME_AVAILABLE)){
                potvrda = true; // ukoliko korisnik ne postoji, postavljamo dozvolu na true kako se vise ne bismo vrteli u ovoj petlji
            }
            else{ // zauzeto korisnicko ime
                System.out.println("Korisnik sa tim korisnickim imenom vec pstoji. Unesite neko drugo korisnicko ime.");
            }
        }

        System.out.println("Unesite lozinku:");
        String password = scanner.nextLine();

        // saljemo lozinku serveru
        socket_out.println(password);
    }
}
