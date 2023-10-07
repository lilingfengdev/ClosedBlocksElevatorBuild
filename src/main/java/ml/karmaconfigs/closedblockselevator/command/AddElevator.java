package ml.karmaconfigs.closedblockselevator.command;

import ml.karmaconfigs.api.common.string.StringUtils;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.closedblockselevator.Client;
import ml.karmaconfigs.closedblockselevator.Main;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import ml.karmaconfigs.closedblockselevator.storage.Messages;
import ml.karmaconfigs.closedblockselevator.storage.custom.IAdder;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.LAST_LINE_MATCHER;
import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public class AddElevator implements CommandExecutor {

    private final Messages messages = new Messages();
    private final Config config = new Config();

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


        if (sender instanceof Player) {
            Player issuer = (Player) sender;
            Client client = new Client(issuer);

            if (issuer.hasPermission("elevator.add")) {
                switch (args.length) {
                    case 1: {
                        String player_name = args[0];
                        Player player = plugin.getServer().getPlayer(player_name);

                        if (player != null) {
                            Client target = new Client(player);

                            Inventory player_inventory = player.getInventory();
                            int available = countSlots(player);

                            if (available >= 1) {
                                client.send(messages.elevatorGive(1, player_name));
                                target.send(messages.elevatorReceive(1));

                                grantElevator(player, Collections.singletonList(1));
                            } else {
                                client.send(messages.notEnoughSpace(1, 0, player_name));
                            }
                        } else {
                            try {
                                int amount = Integer.parseInt(player_name);
                                player_name = issuer.getName();
                                player = issuer;

                                Inventory player_inventory = issuer.getInventory();
                                int available = countSlots(player);

                                if (available <= amount) {
                                    client.send(messages.notEnoughSpace(amount, available, player_name));
                                    amount = available;
                                }

                                List<Integer> stacks = new ArrayList<>();
                                int current_stack = 0;
                                for (int i = 0; i < amount; i++) {
                                    current_stack++;
                                    if (current_stack == 64) {
                                        stacks.add(current_stack);
                                        current_stack = 0;
                                    } else {
                                        if (i == amount - 1) {
                                            stacks.add(amount);
                                        }
                                    }
                                }

                                grantElevator(player, stacks);

                                client.send(messages.elevatorGive(amount, player_name));
                            } catch (Throwable ex) {
                                client.send(messages.notOnline(player_name));
                            }
                        }
                    }
                    break;
                    case 2: {
                        String player_name = args[0];
                        String amount_txt = args[1];

                        try {
                            int amount = Math.abs(Integer.parseInt(amount_txt));

                            Player player = plugin.getServer().getPlayer(player_name);
                            if (player != null) {
                                Client target = new Client(player);

                                Inventory player_inventory = player.getInventory();
                                int available = countSlots(player);

                                if (available <= amount) {
                                    client.send(messages.notEnoughSpace(amount, available, player_name));
                                    amount = available;
                                }

                                List<Integer> stacks = new ArrayList<>();
                                int current_stack = 0;
                                for (int i = 0; i < amount; i++) {
                                    current_stack++;
                                    if (current_stack == 64) {
                                        stacks.add(current_stack);
                                        current_stack = 0;
                                    } else {
                                        if (i == amount - 1) {
                                            stacks.add(amount);
                                        }
                                    }
                                }

                                grantElevator(player, stacks);

                                client.send(messages.elevatorGive(amount, player_name));
                                target.send(messages.elevatorReceive(amount));
                            } else {
                                client.send(messages.notOnline(player_name));
                            }
                        } catch (Throwable ex) {
                            client.send("&cA plugin error occurred");
                        }
                    }
                    break;
                    default:
                        client.send("&cPlease provide a player name and (optional) amount of elevators to give");
                        break;
                }
            } else {
                client.send(messages.permission());
            }
        } else {
            switch (args.length) {
                case 1: {
                    String player_name = args[0];
                    Player player = plugin.getServer().getPlayer(player_name);

                    if (player != null) {
                        Client target = new Client(player);

                        Inventory player_inventory = player.getInventory();
                        int available = countSlots(player);
                        if (available >= 1) {
                            plugin.console().send(messages.elevatorGive(1, player_name));
                            target.send(messages.elevatorReceive(1));

                            grantElevator(player, Collections.singletonList(1));
                        } else {
                            plugin.console().send(messages.notEnoughSpace(1, 0, player_name));
                        }
                    } else {
                        plugin.console().send(messages.notOnline(player_name));
                    }
                }
                    break;
                case 2: {
                    String player_name = args[0];
                    String amount_txt = args[1];

                    try {
                        int amount = Math.abs(Integer.parseInt(amount_txt));

                        Player player = plugin.getServer().getPlayer(player_name);

                        if (player != null) {
                            Client target = new Client(player);
                            int available = countSlots(player);

                            if (available <= amount) {
                                plugin.console().send(messages.notEnoughSpace(amount, available, player_name));
                                amount = available;
                            }

                            List<Integer> stacks = new ArrayList<>();
                            int current_stack = 0;
                            for (int i = 0; i < amount; i++) {
                                current_stack++;
                                if (current_stack == 64) {
                                    stacks.add(current_stack);
                                    current_stack = 0;
                                } else {
                                    if (i == amount - 1) {
                                        stacks.add(amount);
                                    }
                                }
                            }

                            grantElevator(player, stacks);

                            plugin.console().send(messages.elevatorGive(amount, player_name));
                            target.send(messages.elevatorReceive(amount));
                        } else {
                            plugin.console().send(messages.notOnline(player_name));
                        }
                    } catch (Throwable ex) {
                        plugin.console().send(messages.permission()); //Don't know what to do, so we send permission error
                    }
                }
                    break;
                default:
                    plugin.console().send("&cPlease provide a player name and (optional) amount of elevators to give");
                    break;
            }
        }

        return false;
    }

    private int countSlots(final Player player) {
        Inventory player_inventory = player.getInventory();
        int available = 0;
        for (int slot = 0; slot < player_inventory.getSize(); slot++) {
            ItemStack hand = player_inventory.getItem(slot);
            if (hand != null && !hand.getType().equals(Material.AIR)) {
                ItemMeta meta = hand.getItemMeta();
                if (meta != null) {
                    if (meta.hasLore()) {
                        List<String> lore = meta.getLore();
                        if (lore != null && !lore.isEmpty()) {
                            String last_line = StringUtils.stripColor(lore.get(lore.size() - 1));
                            if (last_line.equals(LAST_LINE_MATCHER)) {
                                available = available + (hand.getMaxStackSize() - hand.getAmount());
                            }
                        }
                    }
                }
            } else {
                available = available + 64;
            }
        }

        return available;
    }

    private void grantElevator(final Player player, final List<Integer> stacks) {
        PlayerInventory inventory = player.getInventory();

        for (int stack : stacks) {
            ItemStack elevator = null;
            if (Config.usesItemAdder()) {
                elevator = IAdder.getItem();
                elevator.setAmount(stack);
            }

            if (elevator == null) {
                elevator = new ItemStack(config.elevatorItem(), stack);
            }

            ItemMeta dropMeta = elevator.getItemMeta();
            if (dropMeta != null) {
                dropMeta.setDisplayName(config.elevatorItemName());
                dropMeta.setLore(config.elevatorItemLore());
                elevator.setItemMeta(dropMeta);
            }

            inventory.addItem(elevator);
        }
    }
}
