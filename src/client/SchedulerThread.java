package client;

import model.ProtocolMessages;

import java.io.IOException;

public class SchedulerThread implements Runnable{



    @Override
    public void run() {
        while(true){
            try {

                String receivedInput = Client.socket_in.readLine();

                if(receivedInput.startsWith(ProtocolMessages.BROADCAST_MESSAGE) || receivedInput.startsWith(ProtocolMessages.PRIVATE_MESSAGE) || receivedInput.startsWith(ProtocolMessages.ERROR_MESSAGE)){
                    Client.receivedMessagesQueue.add(receivedInput);
                }
                else
                    Client.receivedMiscellaneousQueue.add(receivedInput);

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }
}
