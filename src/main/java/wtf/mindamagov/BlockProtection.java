package wtf.mindamagov;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.HashSet;
import java.util.Set;

public class BlockProtection {

    private final MdCore plugin;

    private boolean placeEnabled, placeSendMsg;
    private String placeMsg;
    private final Set<String> placeWL = new HashSet<>();
    private final Set<String> placeBL = new HashSet<>();
    private final Set<Material> placeAllowed = new HashSet<>();

    private boolean breakEnabled, breakSendMsg;
    private String breakMsg;
    private final Set<String> breakWL = new HashSet<>();
    private final Set<String> breakBL = new HashSet<>();
    private final Set<Material> breakAllowed = new HashSet<>();

    public BlockProtection(MdCore plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        placeAllowed.clear(); breakAllowed.clear();
        placeWL.clear(); placeBL.clear(); breakWL.clear(); breakBL.clear();

        placeEnabled = plugin.getConfig().getBoolean("block-place.enabled", true);
        placeSendMsg = plugin.getConfig().getBoolean("block-place.send-message", true);
        placeMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("block-place.message", "&f» &fВы не можете &x&f&f&3&3&0&0ставить&f данный &x&f&f&3&3&0&0блок!"));
        placeWL.addAll(plugin.getConfig().getStringList("block-place.whitelist-worlds"));
        placeBL.addAll(plugin.getConfig().getStringList("block-place.blacklist-worlds"));
        loadMaterials("block-place.blocks-whitelist", placeAllowed);

        breakEnabled = plugin.getConfig().getBoolean("block-break.enabled", true);
        breakSendMsg = plugin.getConfig().getBoolean("block-break.send-message", true);
        breakMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("block-break.message", "&f» &fВы не можете &x&f&f&3&3&0&0сломать&f данный &x&f&f&3&3&0&0блок!"));
        breakWL.addAll(plugin.getConfig().getStringList("block-break.whitelist-worlds"));
        breakBL.addAll(plugin.getConfig().getStringList("block-break.blacklist-worlds"));
        loadMaterials("block-break.blocks-whitelist", breakAllowed);
    }

    private void loadMaterials(String path, Set<Material> target) {
        for (String name : plugin.getConfig().getStringList(path)) {
            Material m = Material.matchMaterial(name.trim().toUpperCase());
            if (m != null && m.isBlock()) target.add(m);
            else if (m != null) plugin.getLogger().warning("Не блок: " + name);
            else plugin.getLogger().warning("Неизвестный материал: " + name);
        }
    }

    public boolean allowPlace(BlockPlaceEvent e) {
        if (!placeEnabled) return true;
        Player p = e.getPlayer();
        World w = e.getBlock().getWorld();
        String wn = w.getName();
        Material mat = e.getBlockPlaced().getType();

        if (placeBL.contains(wn)) {
            if (placeSendMsg) p.sendMessage(placeMsg);
            return false;
        }
        if (!placeWL.isEmpty() && !placeWL.contains(wn)) return true;
        if (placeAllowed.contains(mat)) return true;

        if (placeSendMsg) p.sendMessage(placeMsg);
        return false;
    }

    public boolean allowBreak(BlockBreakEvent e) {
        if (!breakEnabled) return true;
        Player p = e.getPlayer();
        World w = e.getBlock().getWorld();
        String wn = w.getName();
        Material mat = e.getBlock().getType();

        if (breakBL.contains(wn)) {
            if (breakSendMsg) p.sendMessage(breakMsg);
            return false;
        }
        if (!breakWL.isEmpty() && !breakWL.contains(wn)) return true;
        if (breakAllowed.contains(mat)) return true;

        if (breakSendMsg) p.sendMessage(breakMsg);
        return false;
    }
}