/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package primenumberserver;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 *
 * @author siriya_s
 */
public class TextListener implements MessageListener {
    
    private MessageProducer replyProducer;
    private Session session;
    
    public TextListener(Session session) {
              
        this.session = session;
        try {
            replyProducer = session.createProducer(null);
        } catch (JMSException ex) {
            Logger.getLogger(TextListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    @Override
    public void onMessage(Message message) {
        TextMessage msg = null;

        try {
            if (message instanceof TextMessage) {
                msg = (TextMessage) message;
                System.out.println("Reading message: " + msg.getText());
            } else {
                System.err.println("Message is not a TextMessage");
            }
            
            // parse input
            String msgText = msg.getText();
            String[] inputs = msgText.split(",");
            int[] inputNums = new int[inputs.length];
            for(int i = 0; i < inputs.length; i++) {
               inputNums[i] = Integer.parseInt(inputs[i].trim());
            }
            int countPrime = 0;
            for (int i = inputNums[0]; i <= inputNums[1]; i++) {
            if (isPrime(i)) {
                    countPrime = countPrime + 1;
                }
            }
            String replyMsg = "The number of prime between " + inputNums[0] + " and " + inputNums[1] + " is " + countPrime;

            // set up the reply message 
            TextMessage response = session.createTextMessage(replyMsg); 
            response.setJMSCorrelationID(message.getJMSCorrelationID()); // read correlation id and set into message
            System.out.println("sending message " + response.getText());
            replyProducer.send(message.getJMSReplyTo(), response); // send to temp queue
        } catch (JMSException e) {
            System.err.println("JMSException in onMessage(): " + e.toString());
        } catch (Throwable t) {
            System.err.println("Exception in onMessage():" + t.getMessage());
        }
        
    }
    
    private boolean isPrime(int n) {
        int i;
        for (i = 2; i*i <= n; i++) {
            if ((n % i) == 0) {
                return false;
            }
        }
        return true;
    }
}
