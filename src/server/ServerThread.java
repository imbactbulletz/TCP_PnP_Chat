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

            while(!input.equals(ProtocolMessages.CLIENT_CONNECTION_CLOSURE)){

                if(input.equals(ProtocolMessages.REGISTRATION_REQUEST)){
                    registerUser();
                }

                if(input.equals(ProtocolMessages.LOGIN_REQUEST)){
                    loginUser();
                }

                if(input.equals(ProtocolMessages.LIST_CONTACTS_REQUEST)){
                    String data = "";
                    Iterator it = Server.connections.entrySet().iterator();

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

                if(input.contains(ProtocolMessages.BROADCAST_MESSAGE)){
                    Iterator it = Server.connections.entrySet().iterator();

                    while (it.hasNext()) {
                        Map.Entry pair = (Map.Entry)it.next();


                        if(pair.getValue() != null && pair.getValue() != this){
                            ((ServerThread)pair.getValue()).socket_out.println(input);
                        }
                    }
                }
               input = socket_in.readLine();
            }


            // korisnik se diskonektovao, postavljamo korisnikov thread na null
            Iterator it = Server.connections.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();

                if((ServerThread)pair.getValue() == this){
                    //System.out.println("[DBG]: [B] " + ((User)pair.getKey()).getUsername() + " " + pair.getValue());
                    pair.setValue(null);

                    //System.out.println("[DBG]: [A] " + ((User)pair.getKey()).getUsername() + " " + pair.getValue());
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
