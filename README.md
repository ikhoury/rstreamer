# Rstreamer: Redis work queue processor
[![CircleCI](https://circleci.com/gh/ikhoury/rstreamer/tree/master.svg?style=svg)](https://circleci.com/gh/ikhoury/rstreamer/tree/master)

This library can be used to implement applications that need to process messages from redis queues.

## Setup
Maven is used to build the project artefact. Run `mvn install` to have the artefact available in your local repostiory.

### Dependencies
#### Jedis
The library uses https://github.com/xetorthio/jedis as its redis java driver in `JedisBatchPoller`.
Add the missing jedis dependency to your project's pom:

```
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>${jedis.version}</version>
</dependency>
```
The library is built wih version `3.1.0`.

#### Resilience4j
The library depends on https://github.com/resilience4j/resilience4j for reliably handling redis connection failures.
Add the missing resilience4j dependencies to your project's pom if you plan on using the `Resilience4jReliableBatchPoller` decorator:
```
<dependency>
	<groupId>io.github.resilience4j</groupId>
	<artifactId>resilience4j-circuitbreaker</artifactId>
	<version>${resilience4j.version}</version>
</dependency>
<dependency>
	<groupId>io.github.resilience4j</groupId>
	<artifactId>resilience4j-retry</artifactId>
	<version>${resilience4j.version}</version>
</dependency>
```
The library is built with version `1.1.0`. 

## Concepts
A `WorkSubscription` describes a group of interested message handlers that want to process items from a redis queue.
The subscription is activated using the `SubscriptionManager`. It runs background threads that poll for tasks and process them using workers.
RStreamer is more efficient than regular polling using jedis because of the following:
- Several tasks can be fetched in one batch which is far more network efficient and allows workers to process this batch in one run.
- Each `WorkSubscription` has one thread dedicated to fetching tasks from redis.
- Workers process tasks asynchronously on a dedicated thread pool for each subscription.
- Tasks can be processed concurrently. The concurrency level is controlled using `Leases` which effectively back-pressure the polling thread, making it wait until capacity is available before fetching more tasks from the queue.

### Subscription Manager
All subscriptions must be added to the `SubscriptionManager` prior to activation.
The manager depends on a `RedisBatchPoller`, the application driver for polling tasks from redis.
Its current implementation is `JedisBatchPoller`, which uses the jedis driver to communicate with redis.
To ensure graceful shutdown, call `deactivateSubscriptions()` before exiting your application to stop polling from redis and finish processing outstanding tasks.

## Configuration
### BatchPollerConfig
A `RedisBatchPoller` is configured using a `BatchPollerConfig`.
```
BatchPollerConfig batchPollerConfig = BatchPollerConfigBuilder.defaultBatchPollerConfig()
                .withHost("myhost")
                .withPort(6379)
                .build();
```
The default config assumes you are connecting to the default redis port on localhost.

### ReliableBatchPollerConfig
A `ReliableBatchPoller` is configured using a `ReliableBatchPollerConfig`.
```
ReliableBatchPollerConfig reliableBatchPollerConfig = defaultReliableBatchPollerConfig()
        .withRetryAttempts(3)
        .withFailureRateThreshold(70)
        .build();
```
A sample of the operations are evaluated. If the sample's failure rate is above the set threshold,
the circuit breaker will open for a while and stop the caller from making requests to an unresponsive server.

### SubscriptionManagerConfig
`SubscriptionManager` is configured using `SubscriptionManagerConfig`.
This config holds a `LeaseConfig` and a `PollingConfig`.
```
SubscriptionManagerConfig config = SubscriptionManageConfigBuilder.defaultSubscriptionManagerConfig()
                .with(
                        LeaseConfigBuilder.defaultLeaseConfig()
                                .withAvailableLeases(5)
                )
                .with(
                        PollingConfigBuilder.defaultPollingConfig()
                                .withBatchSize(50)
                                .withBatchSizeThreshold(20)
                )
                .build();
```
#### LeaseConfig
The number of available leases controls the concurrency level for each subscription.
The larger the number, the more tasks (or group of tasks) can be processed in parallel before blocking the polling thread.
#### PollingConfig
It is more efficient to single poll than to batch poll a queue with very few items. Single polling uses redis's blocking operation and hence can wait on the server side until an item is inserted. On the other hand, batch polling will continuously try to fetch a list of items. Therefore, a `batchSizeThreshold` parameter is used to specify the minimum number of items that must be fetched in a batch in order to continue batch polling in the next round.

## Sample snippets
### Worker 
```
public class SampleWorker implements Worker {

    @Override
    public void processSingleItem(String item) {
        System.out.println("Got item: " + item);
    }
}
```
### Batch Worker
```
public class SampleBatchWorker implements BatchWorker {

    @Override
    public void processMultipleItems(List<String> items) {
        System.out.println("Got " + items.size() + " items");
    }

    @Override
    public void processSingleItem(String item) {
        System.out.println("Got item: " + item);
    }
}
```
### Work Subscription
```
List<Worker> workers = new ArrayList();
workers.add(new SampleWorker());
workers.add(new SampleBatchWorker());
WorkSubscription subscription = new WorkSubscription("my:task:queue", workers);
```

### Batch Poller
```
BatchPollerConfig batchPollerConfig = defaultBatchPollerConfig()
        .withHost("java-0")
        .build();

RedisBatchPoller batchPoller = new JedisBatchPoller(batchPollerConfig, subscriptionCount);
```

### Reliable Batch Poller
```
ReliableBatchPollerConfig reliableBatchPollerConfig = defaultReliableBatchPollerConfig()
        .withRetryAttempts(3)
        .withFailureRateThreshold(70)
        .build();

RedisBatchPoller reliableBatchPoller = new Resilience4jReliableBatchPoller(batchPoller, reliableBatchPollerConfig, subscriptionCount);
```

### Subscription Manager
```
SubscriptionManager subscriptionManager = new SubscriptionManager(createSubscriptionManagerConfig(), createRedisPoller());

subscriptionManager.addSubscription(subscription);
subscriptionManager.activateSubscriptions();

Runtime.getRuntime().addShutdownHook(new Thread(subscriptionManager::deactivateSubscriptions));
```
