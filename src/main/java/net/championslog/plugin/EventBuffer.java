package net.championslog.plugin;

import net.championslog.plugin.events.LogEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Singleton
public class EventBuffer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBuffer.class);

    private static final int EVENTS_PER_TICK = 5;
    private static final int TICK_INTERVAL_SECONDS = 10;

    private final Queue<LogEvent> eventQueue = new ConcurrentLinkedQueue<>();

    private final ChampionsLogClient client;
    private final ScheduledExecutorService executorService;

    private ScheduledFuture<?> processQueueFuture;

    @Inject
    public EventBuffer(ChampionsLogClient client, ScheduledExecutorService executorService) {
        this.client = client;
        this.executorService = executorService;
    }

    public void start() {
        processQueueFuture = executorService.scheduleWithFixedDelay(this::processQueue, TICK_INTERVAL_SECONDS, TICK_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void shutdown() {
        processQueueFuture.cancel(false);
    }

    public void queue(LogEvent event) {
        eventQueue.add(event);
    }

    private void processQueue() {
        LogEvent event;
        var events = 0;

        while ((event = eventQueue.poll()) != null && events++ < EVENTS_PER_TICK) {
            try {
                client.submitLogEvent(event);
            } catch (Exception e) {
                LOGGER.error("Failed to submit event", e);
            }
        }
    }
}
