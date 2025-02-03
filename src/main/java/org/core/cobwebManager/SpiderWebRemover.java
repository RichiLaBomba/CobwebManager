package org.core.cobwebManager;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

public class SpiderWebRemover extends JavaPlugin implements Listener {

    private File configFile;
    private FileConfiguration config;
    private int removalTime;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        createConfig();
        removalTime = config.getInt("removal-time", 30);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode().toString().equals("SURVIVAL") &&
                event.getBlock().getType() == Material.COBWEB) {

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (event.getBlock().getType() == Material.COBWEB) {
                        event.getBlock().setType(Material.AIR);
                    }
                }
            }.runTaskLater(this, removalTime * 20L);
        }
    }

    private void createConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        if (!config.contains("removal-time")) {
            config.set("removal-time", 30);
            saveConfigFile();
        }
    }

    private void saveConfigFile() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
