package ml.karmaconfigs.closedblockselevator.storage;

import dev.lone.itemsadder.api.CustomStack;
import ml.karmaconfigs.api.common.karma.file.KarmaMain;
import ml.karmaconfigs.api.common.karma.file.element.KarmaPrimitive;
import ml.karmaconfigs.api.common.karma.file.element.types.Element;
import ml.karmaconfigs.api.common.karma.file.element.types.ElementPrimitive;
import ml.karmaconfigs.api.common.karma.file.yaml.KarmaYamlManager;
import ml.karmaconfigs.api.common.karma.file.yaml.YamlReloader;
import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.closedblockselevator.Main;
import ml.karmaconfigs.closedblockselevator.storage.custom.IAdder;
import ml.karmaconfigs.closedblockselevator.storage.custom.ModelConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.MetadataValue;

import java.nio.file.Path;
import java.util.*;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.*;

public final class Config {

    private final static Path config = plugin.getDataPath().resolve("config.yml");
    private final static KarmaYamlManager yaml = new KarmaYamlManager(config);
    private static ShapedRecipe recipe;

    private static boolean advised = false;

    public static void reload() {
        YamlReloader reloader = yaml.getReloader();
        if (reloader != null) {
            reloader.reload();
        }
    }


    public boolean canCamouflage(final Player player, final Block elevator) {
        if (yaml.getBoolean("CamouflageOnlyOwner", false)) {
            return OwnerStorage.isOwner(player, elevator);
        }

        return true;
    }

    public boolean showBossBar() {
        return yaml.getBoolean("ShowBossBar", true);
    }

    public boolean showParticles() {
        return yaml.getBoolean("ShowParticles", true);
    }

    public boolean canBreak(final Player player, final Block elevator) {
        if (yaml.getBoolean("BreakOnlyOwner", true)) {
            return OwnerStorage.isOwner(player, elevator);
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

    public int elevatorModel() {
        return yaml.getInt("ItemModelData", -1);
    }

    public ModelConfiguration modelConfiguration() {
        boolean prompt = yaml.getBoolean("ModelData.Prompt", false);
        boolean force = yaml.getBoolean("ModelData.Force", false);
        boolean advise = yaml.getBoolean("ModelData.Advise", false);
        String url = yaml.getString("ModelData.Download.URL");
        String hash = yaml.getString("ModelData.Download.HASH");

        return new ModelConfiguration(prompt, force, advise, url, hash);
    }

    /**
     * Get the elevator item type
     *
     * @return the elevator item type
     */
    public Material elevatorItem() {
        String name = yaml.getString("ItemMaterial", "QUARTZ_BLOCK").toUpperCase().replace(" ", "_");
        Material tmp = Material.getMaterial(name);
        if (tmp == null) {
            tmp = Material.QUARTZ_BLOCK;
        } else {
            if (!tmp.isBlock())
                tmp = Material.QUARTZ_BLOCK;
        }

        return tmp;
    }

    public String elevatorMaterialName() {
        return yaml.getString("ItemMaterial", "QUARTZ_BLOCK").toUpperCase().replace(" ", "_");
    }

    public String literalElevatorMaterialName() {
        return yaml.getString("ItemMaterial", "QUARTZ_BLOCK");
    }

    public double offset() {
        return yaml.getDouble("PlaceOffset", 0d);
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

    public boolean canDisguise(final ItemStack hand) {
        List<String> filter = yaml.getStringList("DisguiseBlocks");
        String name = hand.getType().name();
        if (Main.hasItemAdder()) {
            if (IAdder.isCustomItem(hand)) {
                name = IAdder.getNameSpace(hand);
                if (name == null) name = hand.getType().name();
            }
        }

        boolean whitelist = yaml.getString("DisguiseMode", "BLACKLIST").equalsIgnoreCase("whitelist");
        return (whitelist ? filter.stream().anyMatch(name::equalsIgnoreCase) : filter.stream().noneMatch(name::equalsIgnoreCase));
    }

    public static boolean usesItemAdder() {
        if (Main.hasItemAdder()) {
            return IAdder.getItem() != null;
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    public ShapedRecipe getRecipe() {
        if (recipe != null) return recipe;
        List<String> rawRecipe = yaml.getStringList("ItemRecipe",
                "*;*;*",
                "*;.;*",
                "*,.,*");

        ItemStack elevator;
        if (usesItemAdder()) {
            elevator = IAdder.getItem();
        } else {
            int model = elevatorModel();

            elevator = new ItemStack(elevatorItem(), 1);
            ItemMeta recipeMeta = elevator.getItemMeta();
            if (recipeMeta != null) {
                recipeMeta.setDisplayName(elevatorItemName());
                recipeMeta.setLore(elevatorItemLore());

                if (!usesItemAdder()) {
                    if (model != -1) {
                        if (!advised) {
                            advised = true;
                            plugin.console().send("You are using a custom model data for an elevator. (If you aren't, please check the material type for the elevator)", Level.WARNING);
                        }

                        recipeMeta.setCustomModelData(model);
                    }
                }

                elevator.setItemMeta(recipeMeta);
            }
        }

        ShapedRecipe shaped;
        try {
            shaped = new ShapedRecipe(new org.bukkit.NamespacedKey(plugin, "elevator"), elevator);
        } catch (Throwable ex) {
            try {
                shaped = new ShapedRecipe(new org.bukkit.NamespacedKey("closedblockselevator", "elevator"), elevator);
            } catch (Throwable exc) {
                shaped = new ShapedRecipe(elevator);
            }
        }

        Set<String> chars = new HashSet<>();

        String[] raw = new String[3];
        if (rawRecipe.size() >= 3) {
            for (int i = 0; i < 3; i++) {
                String recipe = rawRecipe.get(i);
                if (recipe.contains(";") && recipe.length() == 5) {
                    String[] data = recipe.split(";");

                    String first = data[0];
                    String second = data[1];
                    String third = data[2];

                    raw[i] = first + second + third;
                    chars.add(first);
                    chars.add(second);
                    chars.add(third);
                } else {
                    if (i == 1) {
                        raw[i] = "`´`";

                        chars.add("´");
                    } else {
                        raw[i] = "```";
                    }

                    chars.add("`");
                    try {
                        Material.valueOf("WOOL");
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

        shaped.shape(raw);
        for (String character : chars) {
            char ch = character.charAt(0);

            KarmaMain mn = new KarmaMain(plugin, "item_values.kf").internal(plugin.getResource("item_values.kf"));
            if (!mn.exists())
                mn.exportDefaults();

            String value = "WOOL";
            Element<?> element = mn.get(character, new KarmaPrimitive("WOOL"));
            if (element.isPrimitive()) {
                ElementPrimitive primitive = element.getAsPrimitive();
                if (primitive.isString()) {
                    value = primitive.asString();
                }
            }

            try {
                Material.valueOf(value);
            } catch (Throwable ex) {
                Material tmp = Material.getMaterial((!value.toLowerCase().startsWith("legacy") ? "LEGACY_" : "") + value, true);
                if (tmp != null) {
                    value = tmp.name();
                } else {
                    value = null;
                }
            }

            if (value == null) {
                try {
                    value = "WOOL";
                } catch (Throwable ex) {
                    value = "WHITE_WOOL";
                }
            }

            shaped.setIngredient(ch, Material.valueOf(value.toUpperCase()));
        }
        recipe = shaped;
        return recipe;
    }
}
