package com.redhat;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TransactionConsumer {

    @Inject
    @Channel("txn-whitelist")
    Emitter<Transaction> emitter;

    private static final Logger LOGGER = Logger.getLogger("MovieConsumer");

    @Incoming("txn-from-kafka")
    public void receive(Transaction transaction) {
        LOGGER.infof("Received movie:", transaction.getId(), transaction.getAmount());
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
