package wtf.mindamagov;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MdCore extends JavaPlugin implements Listener {

    private BlockProtection blockProtection;
    private MapCleaner mapCleaner;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        blockProtection = new BlockProtection(this);
        mapCleaner = new MapCleaner(this);

        getLogger().info("mdCore включен!");
    }

    @Override
    public void onDisable() {
        getLogger().info("mdCore выключен!");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!getConfig().getBoolean("kill-message.enabled", true)) return;

        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            String format = getConfig().getString("kill-message.format", "&c🗡 &f%killer% убил &f%victim% &c☠️");
            String msg = format.replace("%killer%", killer.getName()).replace("%victim%", victim.getName());
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', msg));
            event.setDeathMessage(null);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (!getConfig().getBoolean("anti-ads.enabled", true)) return;

        String msgLower = event.getMessage().toLowerCase().replace(" ", "");
        for (String word : getConfig().getStringList("anti-ads.blockwords")) {
            if (msgLower.contains(word.toLowerCase())) {
                event.setCancelled(true);
                String error = getConfig().getString("anti-ads.error", "&cmindamagov.lol §fВаше сообщение было заблокировано.");
                event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', error));
                return;
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!getConfig().getBoolean("join-text.enabled", true)) return;

        Player p = event.getPlayer();
        for (String line : getConfig().getStringList("join-text.lines")) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
        }
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.isCancelled()) return;
        if (!blockProtection.allowPlace(event)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) return;
        if (!blockProtection.allowBreak(event)) {
            event.setCancelled(true);
        }
    }
}