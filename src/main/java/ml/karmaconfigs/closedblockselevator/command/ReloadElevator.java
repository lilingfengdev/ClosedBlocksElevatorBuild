package ml.karmaconfigs.closedblockselevator.command;

import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.closedblockselevator.Client;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.LAST_LINE_MATCHER;
import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class ReloadElevator implements CommandExecutor {

    /**
     * Executes the given command, returning its success.
     * <br>
     * If false is returned, then the "usage" plugin.yml entry for this command
     * (if defined) will be sent to the player.
     *
     * @param sender  Source of the command
     * @param command Command which was executed
     * @param label   Alias of the command which was used
     * @param args    Passed command arguments
     * @return true if a valid command, otherwise false
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Messages messages = new Messages();
        Config config = new Config();

        if (sender instanceof Player) {
            Player player = (Player) sender;
            Client client = new Client(player);

            if (player.hasPermission("elevator.reload")) {
                Config.reload();
                Messages.reload();

                client.send("&aReloaded elevator plugin");
            } else {
                client.send(messages.permission());
            }
        } else {
            Config.reload();
            Messages.reload();

            plugin.console().send("&aReloaded elevator plugin");
        }

        return false;
    }
}
