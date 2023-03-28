package ml.karmaconfigs.closedblockselevator.util;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
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
                playParticleAt(p, running, new Particle.DustOptions(from, 1f), inst);
                //world.spawnParticle(Particle.REDSTONE, inst, 0, dustOptions);

                newLocation.add(0, formula, 0);
            }, i);
        }

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            running.set(false);
        }, 30);
    }

    private static void playParticleAt(final Player player, final AtomicBoolean repeat, final Particle.DustOptions options, final Location location) {
        player.spawnParticle(Particle.REDSTONE, location.clone(), 0, options);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (repeat.get()) {
                playParticleAt(player, repeat, options, location.clone());
            }
        }, 10);
    }
}
