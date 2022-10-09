package ml.karmaconfigs.closedblockselevator.listener;

import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.ElevatorStorage;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class NewExplodeListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBlockExplode(BlockExplodeEvent e) {
        Config config = new Config();
        List<Block> toRemove = new ArrayList<>();

        for (Block block : e.blockList()) {
            if (ElevatorStorage.isElevator(block)) {
                if (config.preventExplosions()) {
                    toRemove.add(block);
                } else {
                    block.getDrops().clear();

                    ItemStack drop = new ItemStack(config.elevatorItem(), 1);
                    ItemMeta meta = drop.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(config.elevatorItemName());
                        meta.setLore(config.elevatorItemLore());
                        drop.setItemMeta(meta);
                    }

                    block.setType(Material.AIR);
                    block.getWorld().dropItemNaturally(block.getLocation(), drop);

                    block.removeMetadata("elevator_owner", plugin);
                }
            }
        }

        e.blockList().removeAll(toRemove);
    }
}
