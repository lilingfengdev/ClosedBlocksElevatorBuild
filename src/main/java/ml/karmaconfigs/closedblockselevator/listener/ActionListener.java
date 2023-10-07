package ml.karmaconfigs.closedblockselevator.listener;

import ml.karmaconfigs.api.bukkit.reflection.BossMessage;
import ml.karmaconfigs.api.common.minecraft.boss.BossColor;
import ml.karmaconfigs.api.common.minecraft.boss.BossType;
import ml.karmaconfigs.api.common.minecraft.boss.ProgressiveBar;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import ml.karmaconfigs.closedblockselevator.storage.elevator.Elevator;
import ml.karmaconfigs.closedblockselevator.storage.ElevatorStorage;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class ActionListener implements Listener {

    private final List<UUID> deny_use = new ArrayList<>();
    private Sound up_sound = null;
    private Sound down_sound = null;
    private Sound pling_sound = null;

    private final Config config = new Config();
    private final Messages messages = new Messages();

    public ActionListener() {
        Sound[] sounds = Sound.values();

        for (Sound sound : sounds) {
            if (sound.name().endsWith("TOAST_IN")) {
                up_sound = sound;
            }
            if (sound.name().endsWith("TOAST_OUT")) {
                down_sound = sound;
            }
            if (sound.name().contains("ORB_PICKUP")) {
                pling_sound = sound;
            }

            if (up_sound != null && down_sound != null && pling_sound != null) {
                break;
            }
        }

        if (up_sound == null || down_sound == null || pling_sound == null) {
            for (Sound sound : sounds) {
                if (up_sound == null) {
                    if (sound.name().endsWith("GHAST_SHOOT") || sound.name().endsWith("GHAST_FIREBALL")) {
                        up_sound = sound;
                    }
                }
                if (down_sound == null) {
                    if (sound.name().endsWith("BAT_TAKEOFF")) {
                        down_sound = sound;
                    }
                }
                if (pling_sound == null) {
                    if (sound.name().endsWith("NOTE_PLING")) {
                        pling_sound = sound;
                    }
                }

                if (up_sound != null && down_sound != null && pling_sound != null) {
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (e.isSneaking() && !deny_use.contains(player.getUniqueId())) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (block.getType().equals(Material.AIR)) {
                block = block.getRelative(BlockFace.DOWN);
            }

            if (ElevatorStorage.isElevator(block)) {
                Elevator down = ElevatorStorage.getDown(block);
                if (down != null) {
                    Location location = player.getLocation();
                    double x = location.getX();
                    double z = location.getZ();

                    float yaw = location.getYaw();
                    float pitch = location.getPitch();

                    Location target = down.getBlock().getLocation();
                    target.setX(x);
                    target.setZ(z);
                    target.setY(target.getY() + 1.05);

                    target.setYaw(yaw);
                    target.setPitch(pitch);

                    if (!target.getChunk().isLoaded()) {
                        target.getChunk().load(true);
                    }

                    if (isSafe(target.getBlock())) {
                        deny_use.add(player.getUniqueId());
                        player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);

                        if (down_sound != null) {
                            player.playSound(player.getLocation(), down_sound, 2f, 2f);
                        }
                        if (pling_sound != null) {
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                player.playSound(player.getLocation(), pling_sound, 2f, 0.1f);
                            }, 5);
                        }

                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> deny_use.remove(player.getUniqueId()), 15);

                        if (config.showBossBar()) {
                            BossMessage message = new BossMessage(plugin, messages.elevatorDown(down.level()), 1);
                            message.progress(ProgressiveBar.DOWN)
                                    .color(BossColor.RED)
                                    .style(BossType.SEGMENTED_20)
                                    .scheduleBar(new Player[]{player});
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onJump(PlayerMoveEvent e) {
        if (!e.isCancelled()) {
            Player player = e.getPlayer();
            Location from = e.getFrom();
            Location to = e.getTo();

            Vector pre = player.getVelocity();
            Vector pre_direction = from.getDirection();

            if (to != null) {
                int fromX = from.getBlockX();
                int fromZ = from.getBlockZ();
                int toX = to.getBlockX();
                int toZ = to.getBlockZ();

                if (fromX == toX && fromZ == toZ) { //Check if "same" block
                    if (from.getY() < to.getY() && !player.isSneaking() && !player.isFlying()) {
                        if (!deny_use.contains(player.getUniqueId())) {
                            Block from_block = from.getBlock().getRelative(BlockFace.DOWN);
                            if (from_block.getType().equals(Material.AIR)) {
                                from_block = from.getBlock().getRelative(BlockFace.DOWN);
                            }

                            if (ElevatorStorage.isElevator(from_block)) {
                                Elevator up = ElevatorStorage.getUp(from_block);

                                if (up != null) {
                                    double x = from.getX();
                                    double z = from.getZ();

                                    float yaw = from.getYaw();
                                    float pitch = from.getPitch();

                                    Location target = up.getBlock().getLocation();
                                    target.setX(x);
                                    target.setZ(z);
                                    target.setY(target.getY() + 1.05);

                                    target.setYaw(yaw);
                                    target.setPitch(pitch);

                                    target.setDirection(pre_direction);
                                    if (!target.getChunk().isLoaded()) {
                                        target.getChunk().load(true);
                                    }

                                    if (isSafe(target.getBlock())) {
                                        deny_use.add(player.getUniqueId());
                                        player.teleport(target, PlayerTeleportEvent.TeleportCause.PLUGIN);

                                        if (up_sound != null) {
                                            player.playSound(player.getLocation(), up_sound, 2f, 2f);
                                        }
                                        if (pling_sound != null) {
                                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                                player.playSound(player.getLocation(), pling_sound, 2f, 2f);
                                            }, 5);
                                        }

                                        player.setVelocity(pre.subtract(new Vector(0, 0.25, 0)));
                                        plugin.getServer().getScheduler().runTaskLater(plugin, () -> deny_use.remove(player.getUniqueId()), 15);

                                        if (config.showBossBar()) {
                                            BossMessage message = new BossMessage(plugin, messages.elevatorUp(up.level()), 1);
                                            message.progress(ProgressiveBar.UP)
                                                    .color(BossColor.GREEN)
                                                    .style(BossType.SEGMENTED_20)
                                                    .scheduleBar(new Player[]{player});
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isSafe(final Block target_block) {
        Block bottom = target_block.getRelative(BlockFace.DOWN);
        Block roof = target_block.getRelative(BlockFace.UP);

        if (!target_block.getType().isSolid() && !roof.getType().isSolid()) {
            return true;
        }

        Slab.Type bottomSlab = null;
        Slab.Type targetSlab = null;
        Slab.Type roofSlab = null;

        if (bottom.getType().name().endsWith("SLAB")) {
            Slab data = (Slab) bottom.getBlockData();
            bottomSlab = data.getType();
        }
        if (target_block.getType().name().endsWith("SLAB")) {
            Slab data = (Slab) target_block.getBlockData();
            targetSlab = data.getType();
        }
        if (roof.getType().name().endsWith("SLAB")) {
            Slab data = (Slab) roof.getBlockData();
            roofSlab = data.getType();
        }

        if (bottomSlab != null) {
            return (targetSlab != null && roofSlab == null) || (roofSlab != null ? roofSlab.equals(Slab.Type.TOP) : (target_block.getType().isSolid() && roof.getType().isSolid()));
        } else {
            if (targetSlab != null) {
                if (roofSlab != null) {
                    switch (targetSlab) {
                        case TOP:
                        case DOUBLE:
                        default:
                            return false;
                        case BOTTOM:
                            return roofSlab.equals(Slab.Type.TOP);
                    }
                }

                return true;
            } else {
                return (!target_block.getType().isSolid() && (roofSlab != null ? roofSlab.equals(Slab.Type.TOP) : !roof.getType().isSolid()));
            }
        }
    }
}
