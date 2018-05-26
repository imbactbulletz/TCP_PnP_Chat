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


        System.out.println("Unesite 1 za Registraciju, 2 za loginUser ili 0 za izlaz.");
        scanner = new Scanner(System.in);

        String input = scanner.nextLine();

        if(input.equals("1")){
            registerUser();
        }

        if(input.equals("2")){
            loginUser();
        }

        if(input.equals("0")){
            System.exit(0);
        }

        while(true);

    }

    private static void registerUser() throws IOException {
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
            serverResponse = socket_in.readLine();

            // proveravamo da li je server potvrdio da korisnik sa tim korisnickim imenom ne postoji
            if(serverResponse.equals(ProtocolMessages.USERNAME_AVAILABLE)){
                confirmation = true; // ukoliko korisnik ne postoji, postavljamo dozvolu na true kako se vise ne bismo vrteli u ovoj petlji
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

            serverResponse = socket_in.readLine(); // ceka na odgovor servera kako bi potvrdio validnost korisnickog imena

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

            serverResponse = socket_in.readLine(); // cekaj odgovor od servera da proveri validnost lozinke

            if(serverResponse.equals(ProtocolMessages.LOGIN_SUCCESFUL)){ // ukoliko je lozinka ispravna obavestavamo korisnika i izlazimo iz petlje
                passwordConfirmation = true;
                System.out.println("Uspesno ste ulogovani!");
            }
            else{ // ukoliko lozinka nije ispravna obavestavamo korisnika i ostajemo u petlji
                System.out.println("Netacna lozinka.");
            }
        }
    }

}
