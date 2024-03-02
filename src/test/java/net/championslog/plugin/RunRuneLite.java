package net.championslog.plugin;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunRuneLite {

    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(ChampionsLog.class);
        RuneLite.main(args);
    }
}