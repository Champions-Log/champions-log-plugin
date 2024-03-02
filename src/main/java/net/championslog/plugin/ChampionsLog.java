package net.championslog.plugin;

import net.championslog.plugin.events.DisplayNameEvent;
import net.championslog.plugin.events.LogEvent;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.PlayerChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;

import static net.runelite.api.InventoryID.BANK;
import static net.runelite.api.InventoryID.EQUIPMENT;
import static net.runelite.api.InventoryID.INVENTORY;

@PluginDescriptor(name = "Champions Log")
public class ChampionsLog extends Plugin {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChampionsLog.class);

    private static final String CONFIG_GROUP = "championsLog";
    private static final int CONFIG_CHECK_INTERVAL_MINS = 10;

    @Inject
    private Client client;

    @Inject
    private ConfigManager configManager;

    @Inject
    private ChampionsLogClient championsLogClient;

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(
            runnable -> {
                var thread = new Thread(runnable);
                thread.setDaemon(true);
                thread.setName("ChampionsLog");
                thread.setUncaughtExceptionHandler(this::exceptionHandler);
                return thread;
            }
    );
    private EventBuffer eventBuffer;
    private ScheduledFuture<?> checkConfigFuture;
    private RemoteConfig remoteConfig;

    private String currentDisplayName;

    @Override
    protected void startUp() {
        if (eventBuffer == null) {
            eventBuffer = new EventBuffer(championsLogClient, executorService);
        }
        currentDisplayName = null;

        loadRemoteConfig();
        eventBuffer.start();
        checkConfigFuture = executorService.scheduleWithFixedDelay(this::checkRemoteConfig, CONFIG_CHECK_INTERVAL_MINS, CONFIG_CHECK_INTERVAL_MINS, TimeUnit.MINUTES);
    }

    @Override
    protected void shutDown() {
        eventBuffer.shutdown();
        checkConfigFuture.cancel(false);
    }

    private void checkRemoteConfig() {
        executorService.execute(() -> {
            try {
                LOGGER.debug("Checking for a new remote config version...");
                var remoteVersion = championsLogClient.fetchRemoteConfigVersion();

                if (remoteConfig == null || remoteVersion > remoteConfig.version()) {
                    LOGGER.debug("New remote config version available, local: {}, remote: {}", (remoteConfig != null ? remoteConfig.version() : -1), remoteVersion);
                    loadRemoteConfig();
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to check remote config: {}", e.getMessage());
            }
        });
    }

    private void loadRemoteConfig() {
        executorService.execute(() -> {
            try {
                LOGGER.debug("Loading remote config...");
                remoteConfig = championsLogClient.fetchRemoteConfig();
                LOGGER.info("Loaded a new remote config, version: {}", remoteConfig.version());
            } catch (IOException e) {
                LOGGER.warn("Failed to fetch remote config: {}", e.getMessage());
            }
        });
    }

    private void upsertDisplayName(long characterId, String displayName) {
        LOGGER.debug("Upserting player '{}' display name to '{}'", characterId, displayName);
        executorService.execute(() -> {
            try {
                var event = new DisplayNameEvent(characterId, displayName);
                championsLogClient.submitNameEvent(event);
            } catch (Exception e) {
                LOGGER.error("Failed to submit event", e);
            }
        });
    }

    @Subscribe
    public void onPlayerChanged(PlayerChanged event) {
        var player = event.getPlayer();

        if (client.getLocalPlayer() != player) {
            return;
        }
        var displayName = player.getName();

        if (displayName == null || displayName.isBlank()) {
            return;
        }

        if (Objects.equals(displayName, currentDisplayName)) {
            return;
        }
        upsertDisplayName(client.getAccountHash(), displayName);
        currentDisplayName = displayName;
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() != GameState.LOGIN_SCREEN) {
            return;
        }
        currentDisplayName = null;
    }

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged event) {
        // skip commonly modified inventories that'll never be tracked
        if (event.getContainerId() == BANK.getId() || event.getContainerId() == INVENTORY.getId() || event.getContainerId() == EQUIPMENT.getId()) {
            return;
        }

        if (remoteConfig == null) {
            return;
        }
        var inventory = event.getItemContainer();

        for (var trigger : remoteConfig.inventoryTriggers()) {

            if (trigger.inventoryId() != event.getContainerId()) {
                continue;
            }
            var crc = new CRC32();

            for (var item : inventory.getItems()) {
                crc.update(item.getId());
                crc.update(item.getQuantity());
            }
            var inventoryId = inventory.getId();
            var previousCrc = getInventoryCrc(inventoryId);
            var currentCrc = crc.getValue();

            if (previousCrc == currentCrc) {
                LOGGER.debug("Ignoring inventory {} changed event, contents match previous, prev: {} curr: {}", inventoryId, previousCrc, currentCrc);
                return;
            }
            saveInventoryCrc(inventoryId, currentCrc);

            var logEvent = new LogEvent(client.getAccountHash(),
                    trigger.activity(),
                    inventory.getItems(),
                    Map.of()
            );
            eventBuffer.queue(logEvent);
        }
    }

    private long getInventoryCrc(int inventoryId) {
        Long value = configManager.getConfiguration(CONFIG_GROUP, "inventory-" + inventoryId, Long.class);
        if (value == null) {
            return -1;
        }
        return value;
    }

    private void saveInventoryCrc(int inventoryId, long crc) {
        configManager.setConfiguration(CONFIG_GROUP, "inventory-" + inventoryId, crc);
    }

    private void exceptionHandler(Thread thread, Throwable throwable) {
        LOGGER.error("Failed to submit events", throwable);
    }
}
