package ml.karmaconfigs.closedblockselevator.util;

import com.github.fierioziy.particlenativeapi.api.ParticleNativeAPI;
import com.github.fierioziy.particlenativeapi.api.packet.ParticlePacket;
import ml.karmaconfigs.api.bukkit.server.BukkitServer;
import ml.karmaconfigs.api.bukkit.server.Version;
import ml.karmaconfigs.closedblockselevator.Main;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.atomic.AtomicBoolean;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;


public class ParticleUtil {

    public static void showArrowDirections(final Player fo, Location location, boolean hasElevatorUp, boolean hasElevatorDown) {
        double x1 = 0;

        Location playerLocation = fo.getLocation();
        if (hasElevatorUp) {
            // Spawn particles for up arrow
            Location upLocation = location.clone().add((hasElevatorDown ? -0.25: 0), 0, 0);
            spawnArrowParticle(fo, upLocation, new Vector(0, -1, 0), Color.LIME, +0.1);
            x1 = 0.25;
        }

        if (hasElevatorDown) {
            // Spawn particles for down arrow
            Location downLocation = location.clone().add(x1, 3, 0);
            spawnArrowParticle(fo, downLocation, new Vector(0, 1, 0), Color.fromBGR(127, 127, 255), -0.1);
        }
    }

    private static void spawnArrowParticle(Player p, Location location, Vector direction, Color from, final double formula) {
        Location newLocation = location.clone();
        AtomicBoolean running = new AtomicBoolean(true);
        for (int i = 0; i < 30; i++) { //3 blocks
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                Location inst = newLocation.clone();
                playParticleAt(p, running, from, inst);
                //world.spawnParticle(Particle.REDSTONE, inst, 0, dustOptions);

                newLocation.add(0, formula, 0);
            }, i);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            running.set(false);
        }, 30);
    }

    private static void playParticleAt(final Player player, final AtomicBoolean repeat, final Color color, final Location location) {
        ParticleNativeAPI pna = Main.particleAPI;
        if (pna == null || BukkitServer.isOver(Version.v1_12)) {
            player.spawnParticle(org.bukkit.Particle.REDSTONE, location.clone(), 0, new org.bukkit.Particle.DustOptions(color, 1f));
        } else {
            ParticlePacket packet = pna.LIST_1_8.REDSTONE.packetColored(true, location.clone(), color);
            packet.sendInRadiusTo(Bukkit.getServer().getOnlinePlayers(), 10);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (repeat.get()) {
                playParticleAt(player, repeat, color, location.clone());
            }
        }, 10);
    }
}
