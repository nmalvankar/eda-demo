package com.redhat;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.eclipse.microprofile.reactive.messaging.Acknowledgment;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class TransactionConsumer {

    @Inject
    @Channel("txn-whitelist")
    Emitter<Transaction> emitter;

    private static final Logger LOGGER = Logger.getLogger("TransactionConsumer");

    @Incoming("txn-from-kafka")
    @Acknowledgment(Acknowledgment.Strategy.NONE)
    public void receive(Transaction transaction) {
        LOGGER.infof("Received transaction:", transaction.getId(), transaction.getAmount());
        try {
            if (transaction.getMerchantId().equals("MERCH0001") && transaction.getCountry().equals("IR")) {
                LOGGER.info("message check failed");
            } else if(transaction.getCountry().equals("US") && transaction.getMerchantId().equals("MERCH0002")){
                LOGGER.info("message check failed");
            } else {
                emitter.send(transaction);
            }
        }catch (Exception e){
            System.out.println("Lets handle it");
        }
    }

}
