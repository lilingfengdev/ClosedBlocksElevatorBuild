package ml.karmaconfigs.closedblockselevator.listener;

import ml.karmaconfigs.api.common.utils.string.StringUtils;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.Elevator;
import ml.karmaconfigs.closedblockselevator.storage.ElevatorStorage;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.lang.reflect.Field;
import java.util.List;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.LAST_LINE_MATCHER;
import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class BlockListener implements Listener {

    private static Sound enderman_sound = null;
    private static Sound block_break = null;

    static {
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

        boolean handled = false;
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && clicked != null) { //Block place
            ItemStack hand = e.getItem();
            if (hand != null) {
                ItemMeta meta = hand.getItemMeta();
                if (meta != null) {
                    if (meta.hasLore()) {
                        List<String> lore = meta.getLore();
                        if (lore != null && !lore.isEmpty()) {
                            String last_line = StringUtils.stripColor(lore.get(lore.size() - 1));
                            if (last_line.equals(LAST_LINE_MATCHER)) {
                                Block where = clicked.getRelative(e.getBlockFace());

                                if (ElevatorStorage.addElevator(where)) {
                                    where.setMetadata("elevator_owner", new FixedMetadataValue(plugin, player.getUniqueId().toString()));
                                    player.sendMessage(StringUtils.toColor(messages.elevatorPlaced()));
                                } else {
                                    player.sendMessage(StringUtils.toColor(messages.elevatorFailed()));
                                    e.setCancelled(true);
                                }

                                handled = true;
                            }
                        }
                    }
                }

                if (!handled && ElevatorStorage.isElevator(clicked)) {
                    Material type = hand.getType();
                    if (!type.equals(Material.AIR)) {
                        if (!player.isSneaking()) {
                            Elevator elevator = ElevatorStorage.loadElevator(clicked);
                            assert elevator != null;

                            if (!elevator.getCamouflage().equals(type)) {
                                if (type.isBlock() && !player.isSneaking()) {
                                    if (config.canCamouflage(player, clicked)) {
                                        if (ElevatorStorage.hideElevator(elevator, type)) {
                                            clicked.setType(type);

                                            if (enderman_sound != null) {
                                                player.playSound(player.getLocation(), enderman_sound, 2f, 0.5f);
                                            }

                                            player.sendMessage(StringUtils.toColor(messages.elevatorHidden()));
                                        } else {
                                            player.sendMessage(StringUtils.toColor(messages.elevatorHideError()));
                                        }
                                    }
                                }
                            }

                            e.setCancelled(true);
                        }
                    }
                }
            }
        } else {
            if (e.getAction().equals(Action.LEFT_CLICK_BLOCK) && clicked != null && ElevatorStorage.isElevator(clicked)) {
                ItemStack hand = e.getItem();

                if (player.isSneaking() && (hand == null || hand.getType().equals(Material.AIR))) {
                    if (config.canBreak(player, clicked)) {
                        if (ElevatorStorage.destroyElevator(clicked)) {
                            clicked.getDrops().clear(); //We must make it drop an actual elevator, and not whatever it is
                            ItemStack drop = new ItemStack(config.elevatorItem(), 1);
                            ItemMeta dropMeta = drop.getItemMeta();
                            if (dropMeta != null) {
                                dropMeta.setDisplayName(config.elevatorItemName());
                                dropMeta.setLore(config.elevatorItemLore());
                                drop.setItemMeta(dropMeta);
                            }

                            clicked.setType(Material.AIR);
                            player.getWorld().dropItemNaturally(clicked.getLocation(), drop);
                            player.sendMessage(StringUtils.toColor(messages.elevatorDestroyed()));

                            clicked.removeMetadata("elevator_owner", plugin);
                            if (block_break != null) {
                                player.playSound(player.getLocation(), block_break, 2f, 1f);
                            }
                        } else {
                            player.sendMessage(StringUtils.toColor(messages.elevatorDestroyFail()));
                        }
                    }

                    e.setCancelled(true);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();

        Messages messages = new Messages();
        Config config = new Config();

        if (ElevatorStorage.isElevator(block)) {
            Elevator elevator = ElevatorStorage.loadElevator(block);
            assert elevator != null;

            if (config.canBreak(player, block)) {
                if (ElevatorStorage.destroyElevator(block)) {
                    block.getDrops().clear(); //We must make it drop an actual elevator, and not whatever it is

                    ItemStack drop = new ItemStack(config.elevatorItem(), 1);
                    ItemMeta meta = drop.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(config.elevatorItemName());
                        meta.setLore(config.elevatorItemLore());
                        drop.setItemMeta(meta);
                    }

                    block.setType(Material.AIR);
                    player.getWorld().dropItemNaturally(block.getLocation(), drop);
                    try {
                        e.setDropItems(false); //Apparently, this method does not exist in 1.7
                    } catch (Throwable ignored) {}

                    player.sendMessage(StringUtils.toColor(messages.elevatorDestroyed()));
                    block.removeMetadata("elevator_owner", plugin);
                } else {
                    e.setCancelled(true);
                    player.sendMessage(StringUtils.toColor(messages.elevatorDestroyFail()));
                }
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBurn(BlockBurnEvent e) {
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
}
