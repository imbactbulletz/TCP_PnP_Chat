package client;

import model.ProtocolMessages;

import java.io.IOException;

public class SchedulerThread implements Runnable{



    @Override
    public void run() {
        while(true){
            try {
                String receivedInput = Client.socket_in.readLine();

                // ukoliko je brodcast/private/error poruka dodaje je u red za primljene poruke
                if(receivedInput.startsWith(ProtocolMessages.BROADCAST_MESSAGE) || receivedInput.startsWith(ProtocolMessages.PRIVATE_MESSAGE) || receivedInput.startsWith(ProtocolMessages.ERROR_MESSAGE)){
                    Client.receivedMessagesQueue.add(receivedInput);
                }
                else
                    // sve ostalo dodaje u drugi red
                    Client.receivedMiscellaneousQueue.add(receivedInput);

            } catch (IOException e) {

            }


        }
    }
}
