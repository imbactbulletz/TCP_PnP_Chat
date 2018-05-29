package client;

import model.ProtocolMessages;

public class MessageReaderThread implements Runnable {

    @Override
    public void run() {
        while(true){
            try {
                String message = Client.receivedMessagesQueue.take();

                if(message.startsWith(ProtocolMessages.BROADCAST_MESSAGE)){
                    // brise "BROADCAST_MESSAGE" deo
                    message = message.substring(18);
                }
                if(message.startsWith(ProtocolMessages.PRIVATE_MESSAGE)) {
                    // brise "PRIVATE_MESSAGE" deo
                    message = message.substring(16);
                }
                if(message.startsWith(ProtocolMessages.ERROR_MESSAGE)){
                    // brise "ERROR_MESSAGE" deo
                    message = message.substring(14);
                }
                System.out.println(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
