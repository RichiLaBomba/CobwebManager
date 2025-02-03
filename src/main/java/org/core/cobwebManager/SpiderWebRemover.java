package org.core.cobwebManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;

public class SpiderWebRemover extends JavaPlugin implements Listener {

    private File configFile;
    private FileConfiguration config;
    private int removalTime;
    private final DecimalFormat decimalFormat = new DecimalFormat("0.0");
    private final Set<Location> cobwebLocations = new HashSet<>();
    private final Set<ArmorStand> holograms = new HashSet<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        createConfig();
        removalTime = config.getInt("removal-time", 30);

        // Verifica se il comando è stato caricato correttamente
        if (getCommand("swb") != null) {
            getCommand("swb").setExecutor(this); // Imposta l'esecutore del comando
        } else {
            getLogger().warning("Il comando 'swb' non è stato registrato correttamente.");
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getPlayer().getGameMode().toString().equals("SURVIVAL") &&
                event.getBlock().getType() == Material.COBWEB) {

            Location blockLocation = event.getBlock().getLocation();
            cobwebLocations.add(blockLocation);

            Location hologramLocation = blockLocation.add(0.5, 0.8, 0.5);
            ArmorStand hologram = (ArmorStand) hologramLocation.getWorld().spawnEntity(hologramLocation, EntityType.ARMOR_STAND);
            hologram.setCustomNameVisible(true);
            hologram.setInvisible(true);
            hologram.setGravity(false);
            hologram.setMarker(true);
            hologram.setCustomName("");
            holograms.add(hologram);

            new BukkitRunnable() {
                double timeLeft = removalTime;

                @Override
                public void run() {
                    if (blockLocation.getBlock().getType() == Material.COBWEB) {
                        if (timeLeft <= 0) {
                            blockLocation.getBlock().setType(Material.AIR);
                            cobwebLocations.remove(blockLocation);
                            holograms.remove(hologram);
                            hologram.remove();
                            cancel();
                        } else {
                            hologram.setCustomName(ChatColor.BOLD + "" + ChatColor.RED + decimalFormat.format(timeLeft) + "s");
                            timeLeft -= 0.1;
                        }
                    } else {
                        holograms.remove(hologram);
                        hologram.remove();
                        cancel();
                    }
                }
            }.runTaskTimer(this, 0L, 2L);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("swb") && args.length > 0 && args[0].equalsIgnoreCase("clear")) {
            for (Location loc : cobwebLocations) {
                if (loc.getBlock().getType() == Material.COBWEB) {
                    loc.getBlock().setType(Material.AIR);
                }
            }
            cobwebLocations.clear();

            for (ArmorStand hologram : holograms) {
                hologram.remove();
            }
            holograms.clear();

            sender.sendMessage(ChatColor.GREEN + "Tutte le ragnatele e gli ologrammi sono stati rimossi!");
            return true;
        }
        return false;
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