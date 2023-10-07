package ml.karmaconfigs.closedblockselevator.listener;

import dev.lone.itemsadder.api.CustomBlock;
import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.closedblockselevator.Client;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.OwnerStorage;
import ml.karmaconfigs.closedblockselevator.storage.elevator.Elevator;
import ml.karmaconfigs.closedblockselevator.storage.ElevatorStorage;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import ml.karmaconfigs.closedblockselevator.storage.custom.IAdder;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.LAST_LINE_MATCHER;
import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class BlockListener implements Listener {

    private Sound enderman_sound = null;
    private Sound block_break = null;

    private final ConcurrentMap<UUID, Long> iterationMap = new ConcurrentHashMap<>();

    public BlockListener() {
        Sound[] sounds = Sound.values();

        for (Sound sound : sounds) {
            if (sound.name().endsWith("ENDERMAN_TELEPORT")) {
                enderman_sound = sound;
            }
            if (sound.name().endsWith("STONE_BREAK") || sound.name().endsWith("DIG_STONE")) {
                block_break = sound;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(PlayerInteractEvent e) {
        Messages messages = new Messages();
        Config config = new Config();

        Player player = e.getPlayer();
        Block clicked = e.getClickedBlock();
        Client client = new Client(player);

        boolean handled = false;
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && clicked != null) { //Block place
            ItemStack hand = e.getItem();
            if (hand != null) {
                if (!IAdder.isCustomItem(hand)) {
                    ItemMeta meta = hand.getItemMeta();
                    if (meta != null) {
                        if (meta.hasLore()) {
                            List<String> lore = meta.getLore();
                            if (lore != null && !lore.isEmpty()) {
                                String last_line = StringUtils.stripColor(lore.get(lore.size() - 1));
                                if (last_line.equals(LAST_LINE_MATCHER)) {
                                    Block where = clicked.getRelative(e.getBlockFace());

                                    if (ElevatorStorage.addElevator(where, hand)) {
                                        if (clicked.getType().isInteractable() && !player.isSneaking()) {
                                            e.setCancelled(true); //Prevent the elevator from being placed
                                            return;
                                        }

                                        OwnerStorage.assign(player, where);
                                        client.send(messages.elevatorPlaced());

                                        if (Config.usesItemAdder()) {
                                            Block placed = clicked.getRelative(e.getBlockFace());
                                            IAdder.tryBlock(placed.getLocation());
                                        }
                                    } else {
                                        client.send(messages.elevatorFailed());
                                        e.setCancelled(true);
                                    }

                                    if (!client.hasTextures() && !StringUtils.isNullOrEmpty(config.modelConfiguration().downloadURL())) {
                                        client.send(messages.noTextures());
                                    }

                                    handled = true;
                                }
                            }
                        }
                    }
                }

                if (!handled && ElevatorStorage.isElevator(clicked)) {
                    if (!IAdder.isCustomItem(hand) || !IAdder.isCustomBlock(clicked)) {
                        Material type = hand.getType();
                        if (!type.equals(Material.AIR)) {
                            if (!player.isSneaking()) {
                                Elevator elevator = ElevatorStorage.loadElevator(clicked);
                                if (elevator != null) {
                                    if (!elevator.isSameCamouflage(hand)) {
                                        boolean block = false;
                                        boolean solid = type.isSolid();
                                        if (Config.usesItemAdder() && IAdder.isCustomItem(hand)) {
                                            block = IAdder.isBlock(hand);
                                            solid = block;
                                        } else {
                                            block = type.isBlock();
                                            solid = type.isSolid();
                                        }

                                        if (block && solid) {
                                            if (config.canCamouflage(player, clicked)) {
                                                if (Config.usesItemAdder() && IAdder.isBlock(hand)) {
                                                    if (ElevatorStorage.iaHideElevator(elevator, clicked, IAdder.getItem(hand))) {
                                                        if (enderman_sound != null) {
                                                            player.playSound(player.getLocation(), enderman_sound, 2f, 0.5f);
                                                        }

                                                        client.send(messages.elevatorHidden());
                                                    } else {
                                                        client.send(messages.elevatorHideError());
                                                    }
                                                } else {
                                                    if (ElevatorStorage.hideElevator(elevator, type)) {
                                                        clicked.setType(type);

                                                        if (enderman_sound != null) {
                                                            player.playSound(player.getLocation(), enderman_sound, 2f, 0.5f);
                                                        }

                                                        client.send(messages.elevatorHidden());
                                                    } else {
                                                        client.send(messages.elevatorHideError());
                                                    }
                                                }

                                                e.setCancelled(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (player.isSneaking()) {
                    if (OwnerStorage.isOwner(player, clicked)) {
                        long lastIteration = iterationMap.getOrDefault(player.getUniqueId(), -1l);
                        long now = System.currentTimeMillis();

                        if (now >= lastIteration) {
                            OwnerStorage.setVisibility(clicked, !OwnerStorage.getVisibility(clicked));

                            if (OwnerStorage.getVisibility(clicked)) {
                                client.send(messages.elevatorVisible());
                            } else {
                                client.send(messages.elevatorInvisible());
                            }

                            now += 1000;
                            iterationMap.put(player.getUniqueId(), now);
                        }

                        e.setCancelled(true);
                        return;
                    }
                }
            }
        } else {
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) && clicked != null && ElevatorStorage.isElevator(clicked)) {
                if (!IAdder.isCustomBlock(clicked)) {
                    ItemStack hand = e.getItem();

                    if (player.isSneaking() && (hand == null || hand.getType().equals(Material.AIR))) {
                        if (config.canBreak(player, clicked)) {
                            if (ElevatorStorage.destroyElevator(clicked)) {
                                clicked.getDrops().clear(); //We must make it drop an actual elevator, and not whatever it is

                                if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                                    ItemStack drop;
                                    if (Config.usesItemAdder()) {
                                        drop = IAdder.getItem();
                                    } else {
                                        drop = new ItemStack(config.elevatorItem(), 1);
                                    }
                                    ItemMeta meta = drop.getItemMeta();
                                    if (meta != null) {
                                        meta.setDisplayName(config.elevatorItemName());
                                        meta.setLore(config.elevatorItemLore());
                                        drop.setItemMeta(meta);
                                    }

                                    player.getWorld().dropItem(clicked.getLocation(), drop);
                                }

                                clicked.setType(Material.AIR);
                                client.send(messages.elevatorDestroyed());

                                OwnerStorage.remove(clicked);
                                if (block_break != null) {
                                    player.playSound(player.getLocation(), block_break, 2f, 1f);
                                }
                            } else {
                                client.send(messages.elevatorDestroyFail());
                            }
                        }

                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();

        Block block = e.getBlock();
        Client client = new Client(player);

        Messages messages = new Messages();
        Config config = new Config();

        if (!IAdder.isCustomBlock(block)) {
            if (ElevatorStorage.isElevator(block)) {
                Elevator elevator = ElevatorStorage.loadElevator(block);
                assert elevator != null;

                if (config.canBreak(player, block)) {
                    if (ElevatorStorage.destroyElevator(block)) {
                        block.getDrops().clear(); //We must make it drop an actual elevator, and not whatever it is

                        if (player.getGameMode().equals(GameMode.SURVIVAL)) {
                            ItemStack drop;
                            if (Config.usesItemAdder()) {
                                drop = IAdder.getItem();
                            } else {
                                drop = new ItemStack(config.elevatorItem(), 1);
                            }
                            ItemMeta meta = drop.getItemMeta();
                            if (meta != null) {
                                meta.setDisplayName(config.elevatorItemName());
                                meta.setLore(config.elevatorItemLore());
                                drop.setItemMeta(meta);
                            }

                            player.getWorld().dropItem(block.getLocation(), drop);
                        }

                        block.setType(Material.AIR);
                        try {
                            e.setDropItems(false); //Apparently, this method does not exist in 1.7
                        } catch (Throwable ignored) {
                        }

                        if (Config.usesItemAdder()) {
                            CustomBlock cb = CustomBlock.byAlreadyPlaced(block);
                            if (cb != null) {
                                cb.playBreakEffect();
                                cb.remove();
                            }
                        }

                        client.send(messages.elevatorDestroyed());
                        OwnerStorage.remove(block);
                    } else {
                        client.send(messages.elevatorDestroyFail());
                    }
                }

                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBurn(BlockIgniteEvent e) {
        if (!e.isCancelled()) {
            Block block = null;
            Config config = new Config();

            try {
                Field field = e.getClass().getDeclaredField("block"); //BlockEvent should have it... ( workaround to versions under 1.15.2 not having getBlock method )
                block = (Block) field.get(e);
            } catch (Throwable ex) {
                try {
                    Field field = e.getClass().getField("ignitingBlock");
                    block = (Block) field.get(e);
                } catch (Throwable ignored) {
                }
            }

            if (block != null) {
                if (ElevatorStorage.isElevator(block)) {
                    e.setCancelled(config.preventBurn());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onGrief(EntityChangeBlockEvent e) {
        Block block = e.getBlock();
        Config config = new Config();

        if (ElevatorStorage.isElevator(block)) {
            e.setCancelled(config.preventGrief());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        for (Block block : e.getBlocks()) {
            if (ElevatorStorage.isElevator(block)) {
                e.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        try {
            for (Block block : e.getBlocks()) {
                if (ElevatorStorage.isElevator(block)) {
                    e.setCancelled(true);
                    break;
                }
            }
        } catch (Throwable ex) {
            Location retract = e.getRetractLocation();
            Block block = retract.getBlock();

            if (ElevatorStorage.isElevator(block)) {
                e.setCancelled(true);
            }
        }
    }
}
