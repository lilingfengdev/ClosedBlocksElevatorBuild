package ml.karmaconfigs.closedblockselevator;

import ml.karmaconfigs.api.common.utils.string.StringUtils;
import ml.karmaconfigs.closedblockselevator.storage.Elevator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public final class Client {

    private final Player player;

    public Client(final Player p) {
        player = p;
    }

    public void send(final String message) {
        player.sendMessage(StringUtils.toColor(message));
    }

    //TODO: Create elevators GUI and elevator/owner storage to list them in the GUI

    public List<Elevator> getElevators() {
        return null;
    }

    public void placeElevator(final Block block) {
        //TODO: Assign to GUI storage
    }
}
