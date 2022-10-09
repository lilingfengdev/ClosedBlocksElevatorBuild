package ml.karmaconfigs.closedblockselevator.storage;

import ml.karmaconfigs.api.common.karma.file.KarmaMain;
import ml.karmaconfigs.api.common.karma.file.element.KarmaElement;
import ml.karmaconfigs.api.common.karma.file.element.KarmaKeyArray;
import ml.karmaconfigs.api.common.karma.file.element.KarmaObject;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Arrays;
import java.util.Set;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public final class ElevatorStorage {

    private final static KarmaMain storage = new KarmaMain(plugin, "elevator.kf", "data");

    static {
        if (!storage.exists())
            storage.create();
    }

    public static Elevator loadElevator(final Block block) {
        String world = block.getWorld().getName();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        String key = world + "_" + x + "_" + z;
        if (storage.isSet(key)) {
            KarmaKeyArray map = storage.get(key).getKeyArray();
            if (map.containsKey(String.valueOf(y))) {
                KarmaElement material_element = map.get(String.valueOf(y));

                if (material_element.isNumber()) {
                    int material_ordinal = material_element.getObjet().getNumber().intValue();
                    Material material = Material.values()[material_ordinal];
                    return new Elevator(block.getLocation(), material);
                }
            }
        }

        return null;
    }

    public static boolean addElevator(final Block block) {
        String world = block.getWorld().getName();
        Config config = new Config();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        String key = world + "_" + x + "_" + z;
        KarmaKeyArray map = new KarmaKeyArray();
        if (storage.isSet(key)) {
            KarmaElement element = storage.get(key);
            if (element.isKeyArray()) {
                map = element.getKeyArray();
            }
        }
        map.add(String.valueOf(y), new KarmaObject(config.elevatorItem().ordinal()), false);

        storage.set(key, map);
        return storage.save();
    }

    public static boolean hideElevator(final Elevator elevator, final Material camouflage) {
        Block block = elevator.getBlock();
        String world = block.getWorld().getName();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        String key = world + "_" + x + "_" + z;
        if (storage.isSet(key)) {
            KarmaElement element = storage.get(key);
            if (element.isKeyArray()) {
                KarmaKeyArray map = element.getKeyArray();
                map.add(String.valueOf(y), new KarmaObject(camouflage.ordinal()), false);

                storage.set(key, map);
                return storage.save();
            }
        }

        return false;
    }

    public static boolean isElevator(final Block clicked) {
        String world = clicked.getWorld().getName();

        int x = clicked.getX();
        int y = clicked.getY();
        int z = clicked.getZ();

        String key = world + "_" + x + "_" + z;
        if (storage.isSet(key)) {
            KarmaElement map = storage.get(key);
            if (map.isKeyArray()) {
                return map.getKeyArray().containsKey(String.valueOf(y));
            }
        }

        return false;
    }

    public static boolean destroyElevator(final Block target) {
        String world = target.getWorld().getName();

        int x = target.getX();
        int y = target.getY();
        int z = target.getZ();

        String key = world + "_" + x + "_" + z;
        if (storage.isSet(key)) {
            KarmaElement element = storage.get(key);
            if (element.isKeyArray()) {
                KarmaKeyArray map = element.getKeyArray();
                map.remove(String.valueOf(y));

                storage.set(key, map);
                return storage.save();
            }
        }

        return false;
    }

    public static Elevator getDown(final Block from) {
        String world = from.getWorld().getName();
        Config config = new Config();

        int x = from.getX();
        int y = from.getY();
        int z = from.getZ();

        String key = world + "_" + x + "_" + z;
        if (storage.isSet(key)) {
            KarmaElement map_element = storage.get(key);
            if (map_element.isKeyArray()) {
                KarmaKeyArray map = map_element.getKeyArray();
                Set<String> keys = map.getKeys();

                int[] y_cords = new int[keys.size()];

                int index = 0;
                int current_index = 0;
                for (String i : keys) {
                    int current = Integer.parseInt(i);
                    y_cords[index++] = current;
                }

                Arrays.sort(y_cords);

                index = 0;
                for (int cord : y_cords) {
                    if (cord == y) {
                        current_index = index;
                        break;
                    }

                    index++;
                }

                if (current_index > 0) {
                    int new_y = y_cords[current_index - 1];

                    int diff = Math.abs(y - new_y);
                    if (diff <= config.getMaxRange()) {
                        Block destination = from.getWorld().getBlockAt(x, new_y, z);
                        KarmaElement element = map.get(String.valueOf(new_y));
                        if (element.isNumber()) {
                            int material_ordinal = element.getObjet().getNumber().intValue();
                            Material material = Material.values()[material_ordinal];
                            if (material == null || material.equals(Material.AIR)) {
                                material = Material.QUARTZ_BLOCK;
                                hideElevator(new Elevator(destination.getLocation(), material), material);
                            }

                            if (!destination.getType().equals(material)) {
                                destination.setType(material);
                            }

                            return new Elevator(destination.getLocation(), material);
                        }
                    }
                }
            }
        }

        return null;
    }

    public static Elevator getUp(final Block from) {
        String world = from.getWorld().getName();
        Config config = new Config();

        int x = from.getX();
        int y = from.getY();
        int z = from.getZ();

        String key = world + "_" + x + "_" + z;
        if (storage.isSet(key)) {
            KarmaElement map_element = storage.get(key);
            if (map_element.isKeyArray()) {
                KarmaKeyArray map = map_element.getKeyArray();
                Set<String> keys = map.getKeys();

                int[] y_cords = new int[keys.size()];

                int index = 0;
                int current_index = 0;
                for (String i : keys) {
                    int current = Integer.parseInt(i);
                    y_cords[index++] = current;
                }

                Arrays.sort(y_cords);

                index = 0;
                for (int cord : y_cords) {
                    if (cord == y) {
                        current_index = index;
                        break;
                    }

                    index++;
                }

                if (current_index < (y_cords.length - 1)) {
                    int new_y = y_cords[current_index + 1];

                    int diff = Math.abs(new_y - y);
                    if (diff <= config.getMaxRange()) {
                        Block destination = from.getWorld().getBlockAt(x, new_y, z);
                        KarmaElement element = map.get(String.valueOf(new_y));
                        if (element.isNumber()) {
                            int material_ordinal = element.getObjet().getNumber().intValue();
                            Material material = Material.values()[material_ordinal];
                            if (material == null || material.equals(Material.AIR)) {
                                material = Material.QUARTZ_BLOCK;
                                hideElevator(new Elevator(destination.getLocation(), material), material);
                            }

                            if (!destination.getType().equals(material)) {
                                destination.setType(material);
                            }

                            return new Elevator(destination.getLocation(), material);
                        }
                    }
                }
            }
        }

        return null;
    }
}
