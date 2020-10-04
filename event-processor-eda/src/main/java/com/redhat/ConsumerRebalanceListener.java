@ApplicationScoped
@Named("txn-from-kafka.rebalancer")
public class KafkaRebalancedConsumerRebalanceListener implements KafkaRebalancedConsumerRebalanceListener {

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets = getMirroredOffsets();

        for (TopicPartition partition : partitions) {
            OffsetAndMetadata mirroredOffset = findHighestMirroredOffset(mirroredOffsets, partition);
            OffsetAndMetadata localOffset = consumer.committed(Collections.singleton(partition)).get(partition);

            if (mirroredOffset != null) {
                if (localOffset != null)    {
                    if (mirroredOffset.offset() > localOffset.offset()) {
                        LOG.warn("Seeking to {} in {} (higher than local offset {})", mirroredOffset.offset(), partition, localOffset.offset());
                        consumer.seek(partition, mirroredOffset);
                    } else {
                        LOG.warn("Keeping local offset {} in {} (higher than mirrored offset {})", localOffset.offset(), partition, mirroredOffset.offset());
                    }
                } else {
                    LOG.warn("Seeking to {} in {} (local offset does not exist)", mirroredOffset.offset(), partition);
                    consumer.seek(partition, mirroredOffset);
                }
            } else {
                LOG.warn("Mirrored offset does not exist for partition {}", partition);
            }
        }
    }

    public OffsetAndMetadata findHighestMirroredOffset(List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets, TopicPartition partition) {
        OffsetAndMetadata foundOffset = null;

        for (Map<TopicPartition, OffsetAndMetadata> offsets : mirroredOffsets)  {
            if (offsets.containsKey(partition)) {
                if (foundOffset == null)    {
                    foundOffset = offsets.get(partition);
                } else  {
                    OffsetAndMetadata newOffset = offsets.get(partition);
                    if (foundOffset.offset() < newOffset.offset())   {
                        foundOffset = newOffset;
                    }
                }
            }
        }

        return foundOffset;
    }

    public List<Map<TopicPartition, OffsetAndMetadata>> getMirroredOffsets() {
        Set<String> clusters = null;
        try {
            clusters = RemoteClusterUtils.upstreamClusters(props);
        } catch (InterruptedException e) {
            LOG.error("Failed to get remote cluster", e);
            return Collections.emptyList();
        } catch (TimeoutException e) {
            LOG.error("Failed to get remote cluster", e);
            return Collections.emptyList();
        }

        List<Map<TopicPartition, OffsetAndMetadata>> mirroredOffsets = new ArrayList<>();

        for (String cluster : clusters) {
            try {
                mirroredOffsets.add(RemoteClusterUtils.translateOffsets(props, cluster, props.get(ConsumerConfig.GROUP_ID_CONFIG).toString(), Duration.ofMinutes(1)));
            } catch (InterruptedException e) {
                LOG.error("Failed to translate offsets", e);
                e.printStackTrace();
            } catch (TimeoutException e) {
                LOG.error("Failed to translate offsets", e);
            }
        }

        return mirroredOffsets;
    }

}    