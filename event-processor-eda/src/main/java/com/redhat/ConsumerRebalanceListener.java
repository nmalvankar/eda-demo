package com.redhat;

import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Named;

import org.jboss.logging.Logger;

import io.smallrye.mutiny.Uni;
import io.smallrye.reactive.messaging.kafka.KafkaConsumerRebalanceListener;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.mutiny.kafka.client.consumer.KafkaConsumer;

@ApplicationScoped
@Named("transaction.rebalancer")
public class ConsumerRebalanceListener implements KafkaConsumerRebalanceListener {
	
	private static final Logger LOGGER = Logger.getLogger("ConsumerRebalanceListener");
	
//    @Override
//    public Uni<Void> onPartitionsAssigned(KafkaConsumer<?, ?> consumer, Set<TopicPartition> topicPartitions) {
//        long now = System.currentTimeMillis();
//        long shouldStartAt = now - 600_000L; //10 minute ago
//
//        return Uni
//            .combine()
//            .all()
//            .unis(topicPartitions
//                .stream()
//                .map(topicPartition -> {
//                    LOGGER.info("Assigned " + topicPartition);
//                    return consumer.committed(topicPartition)
//                        .onItem()
//                        .invoke(o -> LOGGER.info("Seeking to " + o))
//                        .onItem()
//                        .transformToUni(o -> consumer
//                            .seek(topicPartition, o == null ? 0L : o.getOffset())
//                            .onItem()
//                            .invoke(v -> LOGGER.info("Seeked to " + o))
//                        );
//                })
//                .collect(Collectors.toList()))
//            .combinedWith(a -> null);
//    }
	
	
    @Override
    public Uni<Void> onPartitionsAssigned(KafkaConsumer<?, ?> consumer, Set<TopicPartition> topicPartitions) {
        long now = System.currentTimeMillis();
        long shouldStartAt = now - 600_000L; //10 minute ago

        return Uni
            .combine()
            .all()
            .unis(topicPartitions
                .stream()
                .map(topicPartition -> {
                    LOGGER.info("Assigned " + topicPartition);
                    return consumer.offsetsForTimes(topicPartition, shouldStartAt)
                        .onItem()
                        .invoke(o -> LOGGER.info("Seeking to " + o))
                        .onItem()
                        .transformToUni(o -> consumer
                            .seek(topicPartition, o == null ? 0L : o.getOffset())
                            .onItem()
                            .invoke(v -> LOGGER.info("Seeked to " + o))
                        );
                })
                .collect(Collectors.toList()))
            .combinedWith(a -> null);
    }

	@Override
	public Uni<Void> onPartitionsRevoked(KafkaConsumer<?, ?> arg0, Set<TopicPartition> arg1) {
		// TODO Auto-generated method stub
		return null;
	}
	
}