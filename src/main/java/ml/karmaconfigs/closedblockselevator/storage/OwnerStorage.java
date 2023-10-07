package ml.karmaconfigs.closedblockselevator.storage;

import com.google.gson.JsonArray;
import ml.karmaconfigs.api.common.karma.file.KarmaMain;
import ml.karmaconfigs.api.common.karma.file.element.KarmaPrimitive;
import ml.karmaconfigs.api.common.karma.file.element.multi.KarmaArray;
import ml.karmaconfigs.api.common.karma.file.element.types.Element;
import ml.karmaconfigs.api.common.karma.file.element.types.ElementPrimitive;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public final class OwnerStorage {

    private final static KarmaMain storage = new KarmaMain(plugin, "owners.kf", "data");
    private final static KarmaMain viewStorage = new KarmaMain(plugin, "visibility.kf", "data");
    public static boolean itemsAdder = false;

    static {
        if (!storage.exists())
            storage.create();

        if (!viewStorage.exists())
            viewStorage.create();
    }

    public static void assign(final Player player, final Block block) {
        World world = player.getWorld();
        KarmaArray array;
        if (storage.isSet(player.getUniqueId().toString())) {
            array = (KarmaArray) storage.get(player.getUniqueId().toString()).getAsArray();
        } else {
            array = new KarmaArray();
        }

        String serial = world.getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
        array.add(serial);

        storage.set(player.getUniqueId().toString(), array);
        storage.save();

        setVisibility(block, false); //Hide particles by default
    }

    public static void setVisibility(final Block block, final boolean status) {
        World world = block.getWorld();
        viewStorage.setRaw(world.getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ(), status);
        viewStorage.save();
    }

    public static boolean getVisibility(final Block block) {
        World world = block.getWorld();
        Element<?> element = viewStorage.get(world.getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        if (element.isPrimitive()) {
            ElementPrimitive primitive = element.getAsPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
        }

        return false;
    }

    public static boolean isOwner(final Player player, final Block block) {
        World world = player.getWorld();
        KarmaArray array;
        if (storage.isSet(player.getUniqueId().toString())) {
            array = (KarmaArray) storage.get(player.getUniqueId().toString()).getAsArray();
        } else {
            array = new KarmaArray();
        }
        String serial = world.getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
        return array.contains(new KarmaPrimitive(serial));
    }

    public static boolean canSee(final Player player, final Block block) {
        boolean isOwner = isOwner(player, block);

        World world = block.getWorld();
        Element<?> element = viewStorage.get(world.getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ());
        if (element.isPrimitive()) {
            ElementPrimitive primitive = element.getAsPrimitive();
            if (primitive.isBoolean()) {
                return primitive.getAsBoolean() || isOwner;
            }
        }

        return isOwner;
    }

    public static void remove(final Block block) {
        World world = block.getWorld();
        String serial = world.getName() + "_" + block.getX() + "_" + block.getY() + "_" + block.getZ();
        for (String key : storage.getKeys()) {
            KarmaArray array = (KarmaArray) storage.get(key).getAsArray();
            if (array.contains(new KarmaPrimitive(serial))) {
                array.remove(serial);
                storage.set(key, array);
                storage.save();

                break;
            }
        }

        viewStorage.unset(serial);
        viewStorage.save();
    }
}
