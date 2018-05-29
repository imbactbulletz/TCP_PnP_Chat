package client;

import model.ProtocolMessages;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Client {
    private static final int port = 2018;

    private static Socket socket;
    public static PrintWriter socket_out;
    public static BufferedReader socket_in;

    private static Scanner scanner;

    private static Thread schedulerThread;
    private static Thread messageReaderThread;

    public static BlockingQueue<String> receivedMessagesQueue; // primljene javne, privatne i poruke o greskama
    public static BlockingQueue<String> receivedMiscellaneousQueue; // sve osim gore navedenog

    public static void main(String[] args) throws Exception {
        receivedMessagesQueue = new ArrayBlockingQueue<String>(1024,true);
        receivedMiscellaneousQueue = new ArrayBlockingQueue<String>(1024,true);

        InetAddress address = InetAddress.getByName("127.0.0.1");

        socket = new Socket(address,port);

        socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        socket_out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

        schedulerThread = new Thread(new SchedulerThread());
        schedulerThread.start();

        messageReaderThread = new Thread(new MessageReaderThread());
        messageReaderThread.start();

        System.out.println("Unesite 1 za Registraciju, 2 za login ili 0 za izlaz.");
        scanner = new Scanner(System.in);

        String input = scanner.nextLine();

        if(input.equals("1")){
            registerUser();
            enterLobby();
        }

        if(input.equals("2")){
            loginUser();
            enterLobby();
        }

        if(input.equals("0")){
            System.exit(0);
        }




    }

    private static void registerUser() throws IOException, InterruptedException {
        boolean confirmation = false; // potvrda od servera da ne postoji vec korisnik sa istim korisnickim imenom
        String serverResponse; // predstavlja odgovor od servera
        String username;



        while (!confirmation) {
            System.out.println("Unesite korisicko ime:");
             username = scanner.nextLine();

            // saljemo zahtev serveru za registraciju
            socket_out.println(ProtocolMessages.REGISTRATION_REQUEST);


            // saljemo korisnicko ime serveru
            socket_out.println(username);

            // citamo odgovor od servera
            serverResponse = receivedMiscellaneousQueue.take();

            // proveravamo da li je server potvrdio da korisnik sa tim korisnickim imenom ne postoji
            if(serverResponse.equals(ProtocolMessages.USERNAME_AVAILABLE)){
                confirmation = true; // ukoliko korisnik ne postoji, postavljamo dozvolu na true kako se vise ne bismo vrteli u ovoj petlji
            }
            else{ // zauzeto korisnicko ime
                System.out.println("Korisnik sa tim korisnickim imenom vec postoji. Unesite neko drugo korisnicko ime.");
            }
        }

        System.out.println("Unesite lozinku:");
        String password = scanner.nextLine();

        // saljemo lozinku serveru
        socket_out.println(password);
    }

    private static void loginUser() throws Exception{
        boolean usernameConfirmation = false; // potvrda od servera da je korisnicko ime ispravno
        boolean passwordConfirmation = false; // potvrda od servera da je lozinka ispravna

        String serverResponse;
        String username;

        while(!usernameConfirmation){
            System.out.println("Unesite korisnicko ime:");
            username = scanner.nextLine();

            socket_out.println(ProtocolMessages.LOGIN_REQUEST); // salje serveru zahtev za login
            socket_out.println(username); // salje korisnicko ime

            serverResponse = receivedMiscellaneousQueue.take();; // ceka na odgovor servera kako bi potvrdio validnost korisnickog imena

            if(serverResponse.equals(ProtocolMessages.LOGIN_SUCCESFUL)){ // ukoliko je korisnicko ime tacno, izlazimo iz petlje
                usernameConfirmation = true;
            }
            else{
                System.out.println("Takvo korisnicko ime ne postoji."); // ukoliko nije tacno, ispisuje se poruka i ostajemo u petlji
            }
        }

        // ovaj deo koda se izvrsava nakon sto je korisnik uneo tacan username

        while(!passwordConfirmation){
            System.out.println("Unesite lozinku:");
            String password = scanner.nextLine(); // ocekuje lozinku

            socket_out.println(password); // salje lozinku serveru

            serverResponse = receivedMiscellaneousQueue.take(); // cekaj odgovor od servera da proveri validnost lozinke

            if(serverResponse.equals(ProtocolMessages.LOGIN_SUCCESFUL)){ // ukoliko je lozinka ispravna obavestavamo korisnika i izlazimo iz petlje
                passwordConfirmation = true;
                System.out.println("Uspesno ste ulogovani!");
            }
            else{ // ukoliko lozinka nije ispravna obavestavamo korisnika i ostajemo u petlji
                System.out.println("Netacna lozinka.");
            }
        }
    }

    private static void enterLobby() throws Exception {
        System.out.println("Dobrodosli u javni chat! Za slanje privatnih poruka unesite /w <imePrimaoca>, za prikaz online korisnika unesite \"who\", a za odjavu \"logout\".");

        String input = scanner.nextLine();

        while(!input.equals("logout")){
            if(input.equals("who")){
                socket_out.println(ProtocolMessages.LIST_CONTACTS_REQUEST);

                String response = receivedMiscellaneousQueue.take();

                String[] users = response.split(";");

                System.out.println("Online korisnici:");
                for(int i=0; i<users.length; i++){
                    System.out.println(users[i]);
                }

            }

            if(input.contains("/w ")){
                input = input.substring(3);
                socket_out.println(ProtocolMessages.PRIVATE_MESSAGE + " " + input);
            }else if(!input.equals("who")){
                socket_out.println(ProtocolMessages.BROADCAST_MESSAGE  + " " + input);
            }
            input = scanner.nextLine();
        }

        // logout
        socket_out.println(ProtocolMessages.CLIENT_CONNECTION_CLOSURE);
        socket_out.close();
        socket_in.close();
        socket.close();

        System.exit(0);
    }

}
