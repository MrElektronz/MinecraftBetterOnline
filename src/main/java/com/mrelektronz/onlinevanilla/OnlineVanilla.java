package com.mrelektronz.onlinevanilla;

import io.papermc.paper.event.player.PlayerNameEntityEvent;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public final class OnlineVanilla extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        startChunkLoadingTask();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        p.setInvulnerable(false);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        player.sendMessage(ChatColor.GRAY + "You died at: " + ChatColor.RED + player.getLocation().getBlockX() + ", " + player.getLocation().getBlockY() + ", " + player.getLocation().getBlockZ());
        if(new Random().nextInt(50) == 0) {
            player.getWorld().dropItemNaturally(player.getLocation().add(0,0.5,0), getPlayerHead(player));
        }
    }

    @EventHandler
    public void onVillagerNameChange(PlayerNameEntityEvent e) {
        if (e.getEntity() instanceof Villager) {
            Chunk c = e.getEntity().getWorld().getChunkAt(e.getEntity().getLocation());
            c.setForceLoaded(true);
        }
    }

    @EventHandler
    public void onUndying(EntityDamageEvent e) {
        if (!e.isCancelled() && e.getEntity() instanceof Player) {
            Player p = (Player) e.getEntity();
            int itemOfUndyingSlot = getTotemOfUndyingSlot(p);
            if (e.getCause() == EntityDamageEvent.DamageCause.VOID &&
                    itemOfUndyingSlot != -1 && p.getHealth() < 5d) {
                e.setCancelled(true);
                p.getInventory().setItem(itemOfUndyingSlot, null);
                Location playerSpawn = p.getRespawnLocation() != null ? p.getRespawnLocation() :
                        p.getWorld().getSpawnLocation();
                p.setInvulnerable(true);
                p.teleport(playerSpawn);
                p.sendMessage(ChatColor.RED + "You could have survived, but you didn't deserve it! >:(");
                Bukkit.getScheduler().runTaskLater(this, () -> p.setInvulnerable(false), 20L);
            }
        }
    }

    private int getTotemOfUndyingSlot(Player p) {
        if (p.getInventory().getItemInMainHand().getType() == Material.TOTEM_OF_UNDYING) {
            return p.getInventory().getHeldItemSlot();
        }
        if (p.getInventory().getItemInOffHand().getType() == Material.TOTEM_OF_UNDYING) {
            return 40;
        }
        return -1;
    }

    private void startChunkLoadingTask() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            Bukkit.getWorld("world").getLivingEntities().forEach((ent) -> {
                if (ent instanceof Villager && ent.customName() != null) {
                    ent.getWorld().getChunkAt(ent.getLocation()).setForceLoaded(true);
                    Bukkit.getConsoleSender().sendMessage("Loaded chunk at: " + ent.getLocation());
                }
            });
        }, 20 * 10L);
    }

    private ItemStack getPlayerHead(Player p) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta m = (SkullMeta) skull.getItemMeta();
        m.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
        skull.setItemMeta(m);
        return skull;
    }
}
