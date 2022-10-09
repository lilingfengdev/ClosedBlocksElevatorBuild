package ml.karmaconfigs.closedblockselevator.command;

import ml.karmaconfigs.api.common.utils.string.StringUtils;
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

public class AddElevator implements CommandExecutor {

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
                            int available = 0;
                            for (int slot = 0; slot < player_inventory.getSize(); slot++) {
                                if (available < 1) {
                                    ItemStack hand = player_inventory.getItem(slot);
                                    if (hand != null && !hand.getType().equals(Material.AIR)) {
                                        ItemMeta meta = hand.getItemMeta();
                                        if (meta != null) {
                                            if (meta.hasLore()) {
                                                List<String> lore = meta.getLore();
                                                if (lore != null && !lore.isEmpty()) {
                                                    String last_line = lore.get(lore.size() - 1);
                                                    if (last_line.equals(StringUtils.toColor("&c&b&e"))) {
                                                        available = available + (hand.getMaxStackSize() - hand.getAmount());
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        available = available + 64;
                                        break;
                                    }
                                } else {
                                    break;
                                }
                            }

                            if (available >= 1) {
                                client.send(messages.elevatorGive(1, player_name));
                                target.send(messages.elevatorReceive(1));

                                ItemStack elevator = new ItemStack(config.elevatorItem(), 1);
                                ItemMeta dropMeta = elevator.getItemMeta();
                                if (dropMeta != null) {
                                    dropMeta.setDisplayName(config.elevatorItemName());
                                    dropMeta.setLore(config.elevatorItemLore());
                                    elevator.setItemMeta(dropMeta);
                                }

                                player_inventory.addItem(elevator);
                            } else {
                                client.send(messages.notEnoughSpace(1, 0, player_name));
                            }
                        } else {
                            try {
                                int amount = Integer.parseInt(player_name);

                                Inventory player_inventory = issuer.getInventory();
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

                                if (available <= amount) {
                                    amount = available;
                                    client.send(messages.notEnoughSpace(amount, available, player_name));
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

                                if (!stacks.isEmpty()) {
                                    for (int stack : stacks) {
                                        ItemStack elevator = new ItemStack(config.elevatorItem(), stack);
                                        ItemMeta dropMeta = elevator.getItemMeta();
                                        if (dropMeta != null) {
                                            dropMeta.setDisplayName(config.elevatorItemName());
                                            dropMeta.setLore(config.elevatorItemLore());
                                            elevator.setItemMeta(dropMeta);
                                        }

                                        player_inventory.addItem(elevator);
                                    }
                                }

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

                                if (available <= amount) {
                                    amount = available;
                                    client.send(messages.notEnoughSpace(amount, available, player_name));
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

                                if (!stacks.isEmpty()) {
                                    for (int stack : stacks) {
                                        ItemStack elevator = new ItemStack(config.elevatorItem(), stack);
                                        ItemMeta dropMeta = elevator.getItemMeta();
                                        if (dropMeta != null) {
                                            dropMeta.setDisplayName(config.elevatorItemName());
                                            dropMeta.setLore(config.elevatorItemLore());
                                            elevator.setItemMeta(dropMeta);
                                        }

                                        player_inventory.addItem(elevator);
                                    }
                                }

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
                        int available = 0;
                        for (int slot = 0; slot < player_inventory.getSize(); slot++) {
                            if (available < 1) {
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
                                    break;
                                }
                            } else {
                                break;
                            }
                        }

                        if (available >= 1) {
                            plugin.console().send(messages.elevatorGive(1, player_name));
                            target.send(messages.elevatorReceive(1));

                            ItemStack elevator = new ItemStack(config.elevatorItem(), 1);
                            ItemMeta dropMeta = elevator.getItemMeta();
                            if (dropMeta != null) {
                                dropMeta.setDisplayName(config.elevatorItemName());
                                dropMeta.setLore(config.elevatorItemLore());
                                elevator.setItemMeta(dropMeta);
                            }

                            player_inventory.addItem(elevator);
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

                            if (available <= amount) {
                                amount = available;
                                plugin.console().send(messages.notEnoughSpace(amount, available, player_name));
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

                            if (!stacks.isEmpty()) {
                                for (int stack : stacks) {
                                    ItemStack elevator = new ItemStack(config.elevatorItem(), stack);
                                    ItemMeta dropMeta = elevator.getItemMeta();
                                    if (dropMeta != null) {
                                        dropMeta.setDisplayName(config.elevatorItemName());
                                        dropMeta.setLore(config.elevatorItemLore());
                                        elevator.setItemMeta(dropMeta);
                                    }

                                    player_inventory.addItem(elevator);
                                }
                            }

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
}
