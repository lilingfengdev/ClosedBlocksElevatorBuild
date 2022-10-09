package ml.karmaconfigs.closedblockselevator.storage;

import ml.karmaconfigs.api.common.karmafile.karmayaml.KarmaYamlManager;
import ml.karmaconfigs.api.common.karmafile.karmayaml.YamlReloader;
import ml.karmaconfigs.api.common.utils.string.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.nio.file.Path;
import java.util.List;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.LAST_LINE_MATCHER;
import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public final class Config {

    private final static Path config = plugin.getDataPath().resolve("config.yml");
    private final static KarmaYamlManager yaml = new KarmaYamlManager(config);

    public static void reload() {
        YamlReloader reloader = yaml.getReloader();
        if (reloader != null) {
            reloader.reload();
        }
    }


    public boolean canCamouflage(final Player player, final Block elevator) {
        if (yaml.getBoolean("CamouflageOnlyOwner", false)) {
            if (elevator.hasMetadata("elevator_owner")) {
                MetadataValue value = elevator.getMetadata("elevator_owner").get(0);
                return value.asString().equals(player.getUniqueId().toString());
            }

            return false;
        }

        return true;
    }

    public boolean canBreak(final Player player, final Block elevator) {
        if (yaml.getBoolean("BreakOnlyOwner", true)) {
            if (elevator.hasMetadata("elevator_owner")) {
                MetadataValue value = elevator.getMetadata("elevator_owner").get(0);
                return value.asString().equals(player.getUniqueId().toString());
            }

            return false;
        }

        return true;
    }

    public int getMaxRange() {
        return Math.max(yaml.getInt("MaxRange", 64), 16);
    }

    public boolean preventExplosions() {
        return yaml.getBoolean("PreventExplode", true);
    }

    public boolean preventBurn() {
        return yaml.getBoolean("PreventBurn", true);
    }

    public boolean preventGrief() {
        return yaml.getBoolean("PreventGrief", true);
    }

    public Material elevatorItem() {
        Material material = Material.getMaterial(yaml.getString("ItemMaterial", "QUARTZ_BLOCK").toUpperCase().replace(" ", "_"));
        if (material == null || !material.isBlock())
            material = Material.QUARTZ_BLOCK;

        return material;
    }

    public String elevatorItemName() {
        return StringUtils.toColor(yaml.getString("ItemName", "&fElevator")); //Color ready
    }

    public List<String> elevatorItemLore() {
        List<String> lore = yaml.getStringList("ItemLore",
                "&7Put this elevator anywhere",
                "&7under another elevator or",
                "&7over another elevator to",
                "&7allow you go through them",
                "&7by jumping or crouching");
        lore.add(" ");
        lore.add("&c" + LAST_LINE_MATCHER); //Item watermark
        lore.add(0, "&c" + LAST_LINE_MATCHER);

        return StringUtils.toColor(lore); //Color ready
    }
}
