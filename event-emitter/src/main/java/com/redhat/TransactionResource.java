package com.redhat;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/transaction")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TransactionResource {

    private static final Logger LOGGER = Logger.getLogger("TransactionResource");

    @Inject @Channel("txn") Emitter<Transaction> emitter;


    @POST
    public Response enqueueMovie(Transaction transaction) {
        System.out.println("came here");
        LOGGER.infof("Sending transactions %s to Kafka", transaction.getId());
        emitter.send(transaction);
        return Response.accepted().build();
    }

}
