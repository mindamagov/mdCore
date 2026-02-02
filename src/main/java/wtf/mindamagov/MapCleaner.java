package wtf.mindamagov;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class MapCleaner implements Listener {

    private final MdCore plugin;

    private boolean enabled;
    private long delayTicks;
    private boolean sendMsg;
    private String removeMsg;
    private final Set<Material> tracked = new HashSet<>();
    private final Set<String> wlWorlds = new HashSet<>();

    public MapCleaner(MdCore plugin) {
        this.plugin = plugin;
        reload();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void reload() {
        tracked.clear();
        wlWorlds.clear();

        enabled = plugin.getConfig().getBoolean("map-cleaner.enabled", true);
        delayTicks = plugin.getConfig().getLong("map-cleaner.time-to-clear", 10) * 20L;
        sendMsg = plugin.getConfig().getBoolean("map-cleaner.send-message-on-remove", false);
        removeMsg = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("map-cleaner.remove-message", "&7[MapCleaner] Ваш временный блок исчез."));

        wlWorlds.addAll(plugin.getConfig().getStringList("map-cleaner.whitelist-worlds"));

        for (String s : plugin.getConfig().getStringList("map-cleaner.blocks")) {
            Material m = Material.matchMaterial(s.trim().toUpperCase());
            if (m != null && m.isBlock()) tracked.add(m);
            else if (m != null) plugin.getLogger().warning("[MapCleaner] Не блок: " + s);
            else plugin.getLogger().warning("[MapCleaner] Неизвестный: " + s);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!enabled || e.isCancelled()) return;

        Block b = e.getBlockPlaced();
        Material type = b.getType();
        if (!tracked.contains(type)) return;

        World w = b.getWorld();
        if (!wlWorlds.isEmpty() && !wlWorlds.contains(w.getName())) return;

        Player p = e.getPlayer();
        Location loc = b.getLocation();

        new BukkitRunnable() {
            @Override
            public void run() {
                Block current = loc.getBlock();
                if (current.getType() == type) {
                    current.setType(Material.AIR);
                    if (sendMsg) p.sendMessage(removeMsg);
                }
            }
        }.runTaskLater(plugin, delayTicks);
    }
}