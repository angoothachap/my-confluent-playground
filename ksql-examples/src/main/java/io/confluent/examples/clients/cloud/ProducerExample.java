package io.confluent.examples.clients.cloud;

import com.github.javafaker.Faker;
import io.confluent.examples.clients.cloud.Customer;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProducerExample {

    private static final String KAFKA_ENV_PREFIX = "KAFKA_";
    private final Logger logger = LoggerFactory.getLogger(ProducerExample.class);
    private final Properties properties;
    private final String topicName;
    private final Long messageBackOff;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        ProducerExample simpleProducer = new ProducerExample();
        simpleProducer.start();
    }

    public ProducerExample() throws ExecutionException, InterruptedException {
        properties = buildProperties(defaultProps, System.getenv(), KAFKA_ENV_PREFIX);
        properties.put("bootstrap.servers", "<Paste your real cluster URL>"); //Grab the Cluster URL looks like pkc-343j3.us-west-7.aws.confluent.cloud:9092
        properties.put("sasl.mechanism", "PLAIN");
        properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"<Use your real API Key>\" password=\"<Use a real API secret>\";"); //Input real API key/secret values
        properties.put("security.protocol", "SASL_SSL");
        properties.put("ssl.endpoint.identification.algorithm", "https");
// Schema Registry specific settings
        properties.put("schema.registry.url", "<Paste your real SR URL>"); //Grab your schema registry URL
// Required if using Confluent Cloud Schema Registry
        properties.put("basic.auth.credentials.source", "USER_INFO");
        properties.put("schema.registry.basic.auth.user.info", "<Use your real SR API Key>:<Use a real SR API secret>"); //Input real schema registry API key/secret values
        topicName = System.getenv().getOrDefault("TOPIC", "sample");
        messageBackOff = Long.valueOf(System.getenv().getOrDefault("MESSAGE_BACKOFF", "100"));
        final Integer numberOfPartitions = Integer.valueOf(System.getenv().getOrDefault("NUMBER_OF_PARTITIONS", "2"));
        final Short replicationFactor = Short.valueOf(System.getenv().getOrDefault("REPLICATION_FACTOR", "3"));

        AdminClient adminClient = KafkaAdminClient.create(properties);
        createTopic(adminClient, topicName, numberOfPartitions, replicationFactor);
    }

    private void start() throws InterruptedException {
        logger.info("creating producer with props: {}", properties);

        logger.info("Sending data to `{}` topic", topicName);

        Faker faker = new Faker();

        try (Producer<Long, Customer> producer = new KafkaProducer<>(properties)) {
            long id = 0;
            while (id < 5) { // Produce only 5 records
                Customer customer = Customer.newBuilder()
                        .setCount(id)
                        .setFirstName(faker.name().firstName())
                        .setLastName(faker.name().lastName())
                        .setAddress(faker.address().streetAddress())
                        .build();

                ProducerRecord<Long, Customer> record = new ProducerRecord<>(topicName, 0, id, customer);
                logger.info("Sending Key = {}, Value = {}", record.key(), record.value());
                producer.send(record, (recordMetadata, exception) -> sendCallback(record, recordMetadata, exception));
                id++;
                TimeUnit.MILLISECONDS.sleep(messageBackOff);

            }
        }
    }

    private void sendCallback(ProducerRecord<Long, Customer> record, RecordMetadata recordMetadata, Exception e) {
        if (e == null) {
            logger.info("succeeded sending. offset: {} partition: {}", recordMetadata.offset(), recordMetadata.partition());
        } else {
            logger.error("failed sending key: {}" + record.key(), e);
        }
    }

    private static Map<String, String> defaultProps = new HashMap<>();
    static {
        defaultProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "<Paste your real cluster URL>"); //Grab the Cluster URL looks like pkc-343j3.us-west-7.aws.confluent.cloud:9092
        defaultProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.LongSerializer");
        defaultProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "io.confluent.kafka.serializers.KafkaAvroSerializer");
    }

    private Properties buildProperties(Map<String, String> baseProps, Map<String, String> envProps, String prefix) {
        Map<String, String> systemProperties = envProps.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(prefix))
                .filter(e -> !e.getValue().isEmpty())
                .collect(Collectors.toMap(
                        e -> e.getKey()
                                .replace(prefix, "")
                                .toLowerCase()
                                .replace("_", ".")
                        , e -> e.getValue())
                );

        Properties props = new Properties();
        props.putAll(baseProps);
        props.putAll(systemProperties);
        return props;
    }

    private void createTopic(AdminClient adminClient, String topicName, Integer numberOfPartitions, Short replicationFactor) throws InterruptedException, ExecutionException {
        if (!adminClient.listTopics().names().get().contains(topicName)) {
            logger.info("Creating topic {}", topicName);

            final Map<String, String> configs = replicationFactor < 3 ? Stream.of(new String[][]{
                    {TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, "1"}
            }).collect(Collectors.toMap(data -> data[0], data -> data[1]))
                    : Stream.of(new String[][]{
                    {}
            }).collect(Collectors.toMap(data -> data[0], data -> data[0]));

            final NewTopic newTopic = new NewTopic(topicName, numberOfPartitions, replicationFactor);
            newTopic.configs(configs);
            try {
                CreateTopicsResult topicsCreationResult = adminClient.createTopics(Collections.singleton(newTopic));
                topicsCreationResult.all().get();
            } catch (ExecutionException e) {
                //silent ignore if topic already exists
            }
        }
    }
}
