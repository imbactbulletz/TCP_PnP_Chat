package server;

import model.ProtocolMessages;
import model.User;

import java.io.*;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;

public class ServerThread implements Runnable {
    private Socket socket;

    private PrintWriter socket_out;
    private BufferedReader socket_in;


    public ServerThread(Socket socket){
        try{
            this.socket = socket;

            socket_in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            socket_out = new PrintWriter(new BufferedWriter( new OutputStreamWriter(socket.getOutputStream())),true);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        try {

            String input = socket_in.readLine();

            // vrti se sve dok klijent ne zatrazi da se zavrsi konekcija
            while(!input.equals(ProtocolMessages.CLIENT_CONNECTION_CLOSURE)){

                // ukoliko je klijent trazio registraciju
                if(input.equals(ProtocolMessages.REGISTRATION_REQUEST)){
                    registerUser();
                }

                // ukoliko je klijent trazio da se loginuje
                if(input.equals(ProtocolMessages.LOGIN_REQUEST)){
                    loginUser();
                }

                // ukoliko je klijent trazio da se izlistaju svi kontakti
                if(input.equals(ProtocolMessages.LIST_CONTACTS_REQUEST)){
                    String data = ""; // sadrzi podatke koji se salju korisniku
                    Iterator it = Server.connections.entrySet().iterator();

                    // iterira kroz hashmapu i dodaje sve korisnike osim korisnika koji je pozvao "who" komandu
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();

                        User user = (User)pair.getKey();

                        if(pair.getValue() != null && pair.getValue() != this){
                            data += user.getUsername();
                            data += ";";
                        }
                    }

                    socket_out.println(data);
                }


                // ukoliko je klijent poslao broadcast poruku
                if(input.startsWith(ProtocolMessages.BROADCAST_MESSAGE)){
                    String sender = null;
                    Iterator it = Server.connections.entrySet().iterator();

                    // posto je string u formatu "BROADCAST_MESSAGE <content>" secemo BROADCAST_MESSAGE deo
                    int i = input.indexOf(' ');
                    input = input.substring(i+1);
                    input = input.trim();

                    String message = input; // <content> deo

                    // iteriramo kroz mapu i trazimo thread od kog je stigao zahtev, zatim trazimo korisnika koji odgovara tom thread-u
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();

                        User user = (User)pair.getKey();

                        if((ServerThread)pair.getValue() == this){
                            sender = user.getUsername();
                            break;
                        }
                    }




                    it = Server.connections.entrySet().iterator();

                    // iteriramo kroz mapu i saljemo svim korisnicima osim korisniku koji je poslao zahtev za slanjem
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();


                        if(pair.getValue() != null && pair.getValue() != this){
                            ((ServerThread)pair.getValue()).socket_out.println(ProtocolMessages.BROADCAST_MESSAGE + " " + "[LOBBY-" + sender + "]: " + message);
                        }
                    }
                }


                // ukoliko je klijent zatrazio da posalje privatnu poruku
                if(input.startsWith(ProtocolMessages.PRIVATE_MESSAGE)){
                    int i = input.indexOf(' ');
                    input = input.substring(i+1); // posto je poruka u formatu "PRIVATE_MESSAGE <recepient> <content> mora da se isparsira

                    i = input.indexOf(' ');
                    String recipient = input.substring(0, i+1).trim(); // <recepient> deo
                    input = input.substring(i);
                    String message = input.trim(); // <content> deo


                    String sender = null;
                    Iterator it = Server.connections.entrySet().iterator();

                    // trazi thread koji odgovara korisniku kom se salje poruka
                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();

                        User user = (User)pair.getKey();

                        if((ServerThread)pair.getValue() == this){
                            sender = user.getUsername();
                            break;
                        }
                    }

                    it = Server.connections.entrySet().iterator();


                    boolean userExists = false; // oznacava da li je primalac poruke nadjen ili ne

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();

                        User user = (User)pair.getKey();

                        // ukoliko je nadjen korisnik, salje poruku
                        if(user.getUsername().equals(recipient) && pair.getValue() != null && pair.getValue() != this){
                            ((ServerThread)pair.getValue()).socket_out.println(ProtocolMessages.PRIVATE_MESSAGE + " " + "[" + sender + " whispers]: " + message);
                            userExists = true;
                            break;
                        }

                        // ukoliko je korisnik offline
                        else if (user.getUsername().equals(recipient) && pair.getValue() == null){
                            socket_out.println(ProtocolMessages.ERROR_MESSAGE + " " + recipient + " nije online.");
                            userExists = true;
                            break;
                        }
                    }

                    // ukoliko nije pronadjen uopste korisnik
                    if(!userExists){
                        socket_out.println(ProtocolMessages.ERROR_MESSAGE + " " + recipient + " ne postoji u bazi korisnika.");
                    }

                }


               input = socket_in.readLine();
            }


            // korisnik se diskonektovao, postavljamo korisnikov thread na null
            Iterator it = Server.connections.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();

                if((ServerThread)pair.getValue() == this){

                    pair.setValue(null);
                    break;

                }

            }

            socket_out.close();
            socket_in.close();
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void registerUser() throws IOException {
        String username = socket_in.readLine(); // ocekujemo korisnicko ime

        boolean userFound = false;

        // iteriramo kroz hash mapu kako bismo proverili akreditive korisnika
        Iterator it = Server.connections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            User user = (User)pair.getKey();

            if(user.getUsername().equals(username)){ // ukoliko se prosledjeno korisnicko ime
                userFound = true;                    // poklapa sa nekim korisnickim imenom iz mape
                break;                               // oznacavamo da je korisnik nadjen i izlazimo
            }
        }

        if(userFound){ // ukoliko je korisnik sa takvim korisnickim imenom pronadjen
            socket_out.println(ProtocolMessages.USERNAME_TAKEN);
        }
        else{ // ukoliko nema korisnika sa takvim korisnickim imenom
            socket_out.println(ProtocolMessages.USERNAME_AVAILABLE);

            String password = socket_in.readLine(); // ocekujemo sifru od korisnika

            Server.connections.put(new User(username,password),this); // dodajemo korisnika
        }
    }

    private void loginUser() throws IOException {
        String username = socket_in.readLine(); // ocekuje korisnicko ime
        boolean userFound = false; // sluzi za proveru ispravnosti korisnickog imena
        User tempUser = null;

        // iteriramo kroz hash mapu kako bismo proverili akreditive korisnika
        Iterator it = Server.connections.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();

            User user = (User)pair.getKey();

            if(user.getUsername().equals(username)){ // ukoliko se prosledjeno korisnicko ime
                tempUser = user;
                userFound = true;                    // poklapa sa nekim korisnickim imenom iz mape
                break;                               // oznacavamo da je korisnik nadjen i izlazimo iz petlje
            }
        }

        // ovaj deo koda se izvrsava ako je korisnicko ime u redu
        if(userFound){
            socket_out.println(ProtocolMessages.LOGIN_SUCCESFUL); // izvestava korisnika da je korisnicko ime u redu


            boolean passwordOK = false; // ukazuje da li je lozinka ispravna


            while(!passwordOK) {
                String password = socket_in.readLine(); // ocekuje lozinku od korisnika

                // provera da li se prosledjena lozinka poklapa sa lozinkom korisnika cije smo korisnicko ime vec verifikovali
                if (tempUser.getPassword().equals(password)) {
                    socket_out.println(ProtocolMessages.LOGIN_SUCCESFUL); // izvestavamo korisnika da je login bio uspesan

                    // "vezujemo" thread za korisnika
                    Server.connections.remove(tempUser);
                    Server.connections.put(tempUser, this);
                    passwordOK = true; // menjamo kako bismo izasli iz petlje
                } else { // ukoliko se lozinka ne poklapa izvestavamo korisnika da je lozinka neispravna i ostajemo u petlji
                    socket_out.println(ProtocolMessages.LOGIN_BAD);
                }
            }


        }
        else{ // neispravno korisnicko ime, izvestava korisnika i zavrsava sa loginom
            socket_out.println(ProtocolMessages.LOGIN_BAD);
        }
    }

    public PrintWriter getSocket_out() {
        return socket_out;
    }

    public BufferedReader getSocket_in() {
        return socket_in;
    }
}
