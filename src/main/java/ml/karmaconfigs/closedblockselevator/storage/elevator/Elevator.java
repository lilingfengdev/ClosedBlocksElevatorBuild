package ml.karmaconfigs.closedblockselevator.storage.elevator;

import ml.karmaconfigs.closedblockselevator.storage.custom.IAdder;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public final class Elevator {

    private final Location location;
    private final Material type;
    private final String iAdderName;
    private final int level;

    public Elevator(final Location loc, final Material ty, final int lvl) {
        this(loc, ty, null, lvl);
    }

    public Elevator(final Location loc, final Material ty, final String name, final int lvl) {
        location = loc;
        type = ty;
        iAdderName = name;
        level = lvl;
    }

    public Block getBlock() {
        return location.getBlock();
    }

    public int level() {
        return level;
    }

    public Material getCamouflage() {
        return (type != null ? type : location.getBlock().getType());
    }

    public String getCamouflageName() {
        return iAdderName;
    }

    public boolean isSameCamouflage(final ItemStack item) {
        if (iAdderName != null) {
            return iAdderName.equals(IAdder.getNameSpace(item));
        } else {
            return item.getType().equals(type);
        }
    }

    /**
     * Returns a string representation of the object. In general, the
     * {@code toString} method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p>
     * The {@code toString} method for class {@code Object}
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `{@code @}', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "Elevator@" + hashCode() + "{" +
                "location=" + location + "," +
                "material=" + type + "," +
                "items_adder_name=" + (iAdderName != null ? iAdderName : false) + "," +
                "level=" + level + "}";
    }
}
