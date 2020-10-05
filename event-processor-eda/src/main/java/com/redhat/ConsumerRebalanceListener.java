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
@Named("txn-from-kafka.rebalancer")
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
                    //return consumer.committed(topicPartition)
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
	
//    public OffsetAndMetadata findHighestMirroredOffset(List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets, TopicPartition partition) {
//        OffsetAndMetadata foundOffset = null;
//
//        for (Map<TopicPartition, OffsetAndMetadata> offsets : mirroredOffsets)  {
//            if (offsets.containsKey(partition)) {
//                if (foundOffset == null)    {
//                    foundOffset = offsets.get(partition);
//                } else  {
//                    OffsetAndMetadata newOffset = offsets.get(partition);
//                    if (foundOffset.offset() < newOffset.offset())   {
//                        foundOffset = newOffset;
//                    }
//                }
//            }
//        }
//
//        return foundOffset;
//    }
//    
//    public List<Map<TopicPartition, OffsetAndMetadata>> getMirroredOffsets() {
//        Set<String> clusters = null;
//        try {
//            clusters = RemoteClusterUtils.upstreamClusters(props);
//        } catch (InterruptedException e) {
//            LOGGER.error("Failed to get remote cluster", e);
//            return Collections.emptyList();
//        } catch (TimeoutException e) {
//        	LOGGER.error("Failed to get remote cluster", e);
//            return Collections.emptyList();
//        }
//
//        List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets = new ArrayList<>();
//
//        for (String cluster : clusters) {
//            try {
//                mirroredOffsets.add(RemoteClusterUtils.translateOffsets(props, cluster, props.get(ConsumerConfig.GROUP_ID_CONFIG).toString(), Duration.ofMinutes(1)));
//            } catch (InterruptedException e) {
//            	LOGGER.error("Failed to translate offsets", e);
//                e.printStackTrace();
//            } catch (TimeoutException e) {
//            	LOGGER.error("Failed to translate offsets", e);
//            }
//        }
//
//        return mirroredOffsets;
//    }
	
}