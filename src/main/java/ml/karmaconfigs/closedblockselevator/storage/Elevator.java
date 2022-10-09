package ml.karmaconfigs.closedblockselevator.storage;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public final class Elevator {

    private final Location location;
    private final Material type;

    public Elevator(final Location loc, final Material ty) {
        location = loc;
        type = ty;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public Material getCamouflage() {
        return (type != null ? type : location.getBlock().getType());
    }
}
