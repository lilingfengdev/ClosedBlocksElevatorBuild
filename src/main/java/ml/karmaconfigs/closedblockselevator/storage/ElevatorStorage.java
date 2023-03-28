package ml.karmaconfigs.closedblockselevator.storage;

import ml.karmaconfigs.api.common.collection.triple.TriCollection;
import ml.karmaconfigs.api.common.collection.triple.TriCollector;
import ml.karmaconfigs.api.common.karma.file.KarmaMain;
import ml.karmaconfigs.api.common.karma.file.element.multi.KarmaMap;
import ml.karmaconfigs.api.common.karma.file.element.types.Element;
import ml.karmaconfigs.api.common.karma.file.element.types.ElementPrimitive;
import ml.karmaconfigs.closedblockselevator.Main;
import ml.karmaconfigs.closedblockselevator.storage.custom.IAdder;
import ml.karmaconfigs.closedblockselevator.storage.elevator.Elevator;
import ml.karmaconfigs.closedblockselevator.storage.elevator.ElevatorLine;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public final class ElevatorStorage {

    private final static KarmaMain storage = new KarmaMain(plugin, "elevator.kf", "data");
    public static boolean itemsAdder = false;

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
            KarmaMap map = (KarmaMap) storage.get(key).getAsMap();
            if (map.containsKey(String.valueOf(y))) {
                Element<?> material_element = map.get(String.valueOf(y));

                if (material_element.isPrimitive()) {
                    ElementPrimitive primitive = material_element.getAsPrimitive();
                    if (primitive.isNumber()) {
                        int material_ordinal = material_element.getAsInteger();
                        Material material = Material.values()[material_ordinal];
                        return new Elevator(block.getLocation(), material, 0);
                    }
                    if (primitive.isString()) {
                        String material_name = material_element.getAsString();
                        ItemStack item = IAdder.getItem(material_name);
                        if (item != null) {
                            Material material = item.getType();
                            return new Elevator(block.getLocation(), material, material_name, 0);
                        }
                    }
                }
            }
        }

        return null;
    }

    public static boolean addElevator(final Block block, final ItemStack item) {
        String world = block.getWorld().getName();
        Config config = new Config();

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        String key = world + "_" + x + "_" + z;
        KarmaMap map = new KarmaMap();
        if (storage.isSet(key)) {
            Element<?> element = storage.get(key);
            if (element.isMap()) {
                map = (KarmaMap) element.getAsMap();
            }
        }
        if (Config.usesItemAdder()) {
            map.put(String.valueOf(y), IAdder.getNameSpace(item));
        } else {
            map.put(String.valueOf(y), config.elevatorItem().ordinal());
        }

        storage.set(key, map);
        return storage.save();
    }

    public static boolean iaHideElevator(final Elevator elevator, final Block target, final ItemStack iaItem) {
        Config config = new Config();

        if (iaItem != null && config.canDisguise(iaItem)) {
            Block block = elevator.getBlock();
            String world = block.getWorld().getName();

            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            String key = world + "_" + x + "_" + z;
            if (storage.isSet(key)) {
                Element<?> element = storage.get(key);
                if (element.isMap()) {
                    KarmaMap map = (KarmaMap) element.getAsMap();
                    String name_space = IAdder.getNameSpace(iaItem);
                    if (name_space != null) {
                        map.put(String.valueOf(y), name_space);

                        IAdder.tryBlock(name_space, target.getLocation());

                        storage.set(key, map);
                        return storage.save();
                    }
                }
            }
        }

        return false;
    }

    public static boolean hideElevator(final Elevator elevator, final Material camouflage) {
        Config config = new Config();

        if (config.canDisguise(new ItemStack(camouflage))) {
            Block block = elevator.getBlock();
            String world = block.getWorld().getName();

            int x = block.getX();
            int y = block.getY();
            int z = block.getZ();

            String key = world + "_" + x + "_" + z;
            if (storage.isSet(key)) {
                Element<?> element = storage.get(key);
                if (element.isMap()) {
                    KarmaMap map = (KarmaMap) element.getAsMap();
                    map.put(String.valueOf(y), camouflage.ordinal());

                    storage.set(key, map);
                    return storage.save();
                }
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
            Element<?> map = storage.get(key);
            if (map.isMap()) {
                return map.getAsMap().containsKey(String.valueOf(y));
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
            Element<?> element = storage.get(key);
            if (element.isMap()) {
                KarmaMap map = (KarmaMap) element.getAsMap();
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
            Element<?> el_map = storage.get(key);
            if (el_map.isMap()) {
                KarmaMap map = (KarmaMap) el_map.getAsMap();
                Set<String> keys = new HashSet<>();
                map.forEachKey(keys::add);

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
                        Element<?> element = map.get(String.valueOf(new_y));
                        if (element.isPrimitive() && element.getAsPrimitive().isNumber()) {
                            int material_ordinal = element.getAsInteger();
                            Material material = Material.values()[material_ordinal];
                            if (material == null || material.equals(Material.AIR)) {
                                material = Material.QUARTZ_BLOCK;
                                hideElevator(new Elevator(destination.getLocation(), material, (current_index - 1)), material);
                            }

                            if (!destination.getType().equals(material)) {
                                destination.setType(material);
                            }

                            return new Elevator(destination.getLocation(), material, (current_index - 1));
                        } else {
                            if (element.isPrimitive() && element.getAsPrimitive().isString() && Main.hasItemAdder()) {
                                String ia_id = element.getAsString();
                                ItemStack item = IAdder.getItem(ia_id);
                                if (item != null) {
                                    IAdder.tryBlock(ia_id, destination.getLocation());
                                    return new Elevator(destination.getLocation(), destination.getType(), (current_index - 1));
                                }
                            }
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
            Element<?> map_element = storage.get(key);
            if (map_element.isMap()) {
                KarmaMap map = (KarmaMap) map_element.getAsMap();
                Set<String> keys = new HashSet<>();
                map.forEachKey(keys::add);

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
                        Element<?> element = map.get(String.valueOf(new_y));
                        if (element.isPrimitive() && element.getAsPrimitive().isNumber()) {
                            int material_ordinal = element.getAsInteger();
                            Material material = Material.values()[material_ordinal];
                            if (material == null || material.equals(Material.AIR)) {
                                material = Material.QUARTZ_BLOCK;
                                hideElevator(new Elevator(destination.getLocation(), material, (current_index + 1)), material);
                            }

                            if (!destination.getType().equals(material)) {
                                destination.setType(material);
                            }

                            return new Elevator(destination.getLocation(), material, (current_index + 1));
                        } else {
                            if (element.isPrimitive() && element.getAsPrimitive().isString() && Main.hasItemAdder()) {
                                String ia_id = element.getAsString();
                                ItemStack item = IAdder.getItem(ia_id);
                                if (item != null) {
                                    IAdder.tryBlock(ia_id, destination.getLocation());
                                    return new Elevator(destination.getLocation(), destination.getType(), (current_index + 1));
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public static CompletableFuture<ElevatorLine[]> loadElevators(final World world) {
        return CompletableFuture.supplyAsync(new Supplier<ElevatorLine[]>() {
            @Override
            public ElevatorLine[] get() {
                List<ElevatorLine> worldElevators = new ArrayList<>();
                String name = world.getName();

                for (String key : storage.getKeys()) {
                    if (key.startsWith("main." + name)) {
                        String[] keyData = key.split("_");
                        String xRaw = keyData[1];
                        String zRaw = keyData[2];

                        try {
                            int x = Integer.parseInt(xRaw);
                            int z = Integer.parseInt(zRaw);

                            List<Elevator> fetched = new ArrayList<>();
                            KarmaMap map = (KarmaMap) storage.get(key).getAsMap();
                            List<Integer> y_axis = new ArrayList<>();
                            map.forEachKey((yRaw) -> {
                                try {
                                    y_axis.add(Integer.parseInt(yRaw));
                                } catch (Throwable ignored) {}
                            });
                            Collections.sort(y_axis);

                            int lvl = 0;
                            for (int y : y_axis) {
                                Block block = world.getBlockAt(x, y, z);

                                Element<?> element = map.get(String.valueOf(y));
                                if (element.isPrimitive()) {
                                    ElementPrimitive primitive = element.getAsPrimitive();
                                    if (primitive.isNumber()) {
                                        int material_ordinal = element.getAsInteger();
                                        try {
                                            Material material = Material.values()[material_ordinal];
                                            fetched.add(new Elevator(block.getLocation(), material, lvl));
                                        } catch (Throwable ignored) {}
                                    } else {
                                        if (primitive.isString() && Main.hasItemAdder()) {
                                            String ia_id = element.getAsString();
                                            ItemStack item = IAdder.getItem(ia_id);
                                            if (item != null) {
                                                IAdder.tryBlock(ia_id, block.getLocation());
                                                fetched.add(new Elevator(block.getLocation(), block.getType(), lvl));
                                            }
                                        }
                                    }
                                }
                            }

                            worldElevators.add(new ElevatorLine(fetched.toArray(new Elevator[0])));
                        } catch (Throwable ignored) {}
                    }
                }

                return worldElevators.toArray(new ElevatorLine[0]);
            }
        });
    }
}
