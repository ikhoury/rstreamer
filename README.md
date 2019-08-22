# Rstreamer: Redis work queue processor
[![CircleCI](https://circleci.com/gh/ikhoury/rstreamer/tree/master.svg?style=svg)](https://circleci.com/gh/ikhoury/rstreamer/tree/master)

This library can be used to implement applications that need to process messages from redis queues.
The library uses https://github.com/xetorthio/jedis as its redis java driver in `JedisBatchPoller`.

## Design
The concept of a `WorkSubscription` is used to describe a group of interested message handlers that want to process items from a redis queue.
The subscription is activated using the `SubscriptionManager` which runs background threads that poll for tasks and process them using workers.
RStreamer is more efficient than regular polling using jedis:
- Tasks can be fetched in batches from a queue, which is much more efficient over the network and allows workers to process this batch in one run.
- Each `WorkSubscription` will have only one thread polling for tasks regardless of the number of workers.
- Workers will process tasks asynchronously on a dedicated thread pool for each subscription, freeing the polling thread.
- Concurrent task processing is controlled using `Leases` that effectively back-pressure the polling thread. 
A configurable number of running processes is allowed. The polling thread waits until capacity is available before fetching more tasks from the queue.
### Subscription Manager
All work subscriptions must be added to the `SubscriptionManager` prior to `activation`.
The manager depends on a `RedisBatchPoller`, the application driver for polling tasks from redis.
Its current implementation is `JedisBatchPoller`, which uses the jedis driver to communicate with redis.
To ensure graceful shutdown, call `deactivateSubscriptions()` before exiting your application to stop polling from redis and finish processing of outstanding tasks.

## Configuration
### JedisConfig
`JedisBatchPoller` is configured using `JedisConfig`.
```
JedisConfig jedisConfig = JedisConfigBuilder.defaultJedisConfig()
                .withHost("myhost")
                .withPort(6379)
                .withPollTimeoutInSeconds(3)
                .withSubscriptionCount(10)
                .build();
```
The default config assumes you are connecting to the default redis port on localhost.
The subscription count must be at least equal to the number of subscriptions added to
`SubscriptionManager` in order to have enough resources to poll tasks from all queues concurrently.  

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
The number of available leases controls the concurrency level for each subscription.
The larger the number, the more tasks (or group of tasks) can be processed in parallel before blocking the polling thread.
When a queue has only a few items, it can be more efficient to single poll for items than to batch poll,
especially because single polling can block on the server side until items are available, while batch polling will continuously attempt to fetch items.
Therefore, the batch size threshold sets a minimum number of items that must be fetched in a batch in order to continue batch polling in the next round.

### Sample code
#### Worker 
```
public class SampleWorker implements Worker {

    @Override
    public void processSingleItem(String item) {
        System.out.println("Got item: " + item);
    }
}
```
#### Batch Worker
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
#### Work Subscription
```
List<Worker> workers = new ArrayList();
workers.add(new SampleWorker());
workers.add(new SampleBatchWorker());
WorkSubscription subscription = new WorkSubscription("my:task:queue", workers);
```
#### Subscription Manager
```
SubscriptionManager subscriptionManager = new SubscriptionManager(createSubscriptionManagerConfig(), createRedisPoller());

subscriptionManager.addSubscription(subscription);
subscriptionManager.activateSubscriptions();

Runtime.getRuntime().addShutdownHook(new Thread(subscriptionManager::deactivateSubscriptions));
```