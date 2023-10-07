package ml.karmaconfigs.closedblockselevator;

import ml.karmaconfigs.api.bukkit.server.BukkitServer;
import ml.karmaconfigs.api.bukkit.server.Version;
import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.OwnerStorage;
import ml.karmaconfigs.closedblockselevator.storage.elevator.Elevator;
import ml.karmaconfigs.closedblockselevator.storage.ElevatorStorage;
import ml.karmaconfigs.closedblockselevator.storage.elevator.ElevatorLine;
import ml.karmaconfigs.closedblockselevator.util.ParticleUtil;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public final class Client {

    private final Player player;
    private final Set<UUID> textured = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final static Map<UUID, BukkitTask> elevator_scheduler = new ConcurrentHashMap<>();

    public Client(final Player p) {
        player = p;
    }

    public void send(final String message) {
        player.sendMessage(StringUtils.toColor(message));
    }

    public void acceptTextures() {
        textured.add(player.getUniqueId());
    }

    public void unTexturize() {
        textured.remove(player.getUniqueId());
    }

    public boolean hasTextures() {
        return textured.contains(player.getUniqueId());
    }

    public void addScheduler() {
        UUID id = player.getUniqueId();

        if (!elevator_scheduler.containsKey(player.getUniqueId())) {
            Random random = new Random();
            int min = 60;
            int max = 75;
            int randomNumber = random.nextInt(max - min + 1) + min; //Different time so it won't be that laggy

            Config config = new Config();
            AtomicBoolean printed_error = new AtomicBoolean(false);
            BukkitTask scheduler = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
                Player instance = plugin.getServer().getPlayer(id);
                if (instance != null && instance.isOnline() && config.showParticles()) {
                    World world = instance.getWorld();
                    Location location = instance.getLocation();

                    plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> ElevatorStorage.loadElevators(world).whenCompleteAsync((elevators, error) -> {
                        if (error == null) {
                            for (ElevatorLine elevator : elevators) {
                                Elevator e;
                                Elevator pre = null;
                                while ((e = elevator.up()) != null) {
                                    Block block = e.getBlock();
                                    if (OwnerStorage.canSee(player, block) || player.hasPermission("elevator.particle")) {
                                        Elevator next = elevator.up();
                                        Elevator previus = pre;
                                        if (next != null) {
                                            elevator.down();
                                        }

                                        Location blockLocation = block.getLocation().clone();
                                        blockLocation.add(0.5, 1, 0.5);

                                        if (blockLocation.distance(location) <= 10) {
                                            plugin.getServer().getScheduler().runTask(plugin, () -> ParticleUtil.showArrowDirections(instance, blockLocation, next != null, previus != null));
                                        }

                                        pre = e;
                                    }
                                }
                            }
                        } else {
                            if (!printed_error.get()) {
                                plugin.logger().scheduleLog(Level.GRAVE, error);
                                printed_error.set(true);
                            }
                        }
                    }));
                }
            }, 0, randomNumber);

            elevator_scheduler.put(id, scheduler);
        }
    }

    //TODO: Create elevators GUI and elevator/owner storage to list them in the GUI
    public List<Elevator> getElevators() {
        return null;
    }

    public void placeElevator(final Block block) {
        //TODO: Assign to GUI storage
    }
}
