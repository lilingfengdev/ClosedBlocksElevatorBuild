package ml.karmaconfigs.closedblockselevator.listener;

import ml.karmaconfigs.closedblockselevator.storage.Elevator;
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
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class ActionListener implements Listener {

    private Sound up_sound = null;
    private Sound down_sound = null;

    public ActionListener() {
        Sound[] sounds = Sound.values();

        for (Sound sound : sounds) {
            if (sound.name().endsWith("TOAST_IN")) {
                up_sound = sound;
            }
            if (sound.name().endsWith("TOAST_OUT")) {
                down_sound = sound;
            }

            if (up_sound != null && down_sound != null) {
                break;
            }
        }

        if (up_sound == null || down_sound == null) {
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

                if (up_sound != null && down_sound != null) {
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSneak(PlayerToggleSneakEvent e) {
        Player player = e.getPlayer();
        if (e.isSneaking()) {
            Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
            if (block.getType().equals(Material.AIR)) {
                block = block.getRelative(BlockFace.UP);
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

                    if (isSafe(target.getBlock())) {
                        player.teleport(target);
                        if (down_sound != null) {
                            player.playSound(player.getLocation(), down_sound, 2f, 2f);
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

            if (to != null) {
                if (from.getX() == to.getX() && from.getZ() == to.getZ()) {
                    if (from.getY() < to.getY()) {
                        Block from_block = from.getBlock().getRelative(BlockFace.DOWN);
                        if (from_block.getType().equals(Material.AIR)) {
                            from_block = from.getBlock().getRelative(BlockFace.UP);
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

                                if (isSafe(target.getBlock())) {
                                    e.setFrom(up.getBlock().getLocation());
                                    e.setTo(target);
                                    player.teleport(target);
                                    if (up_sound != null) {
                                        player.playSound(player.getLocation(), up_sound, 2f, 2f);
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
