package ml.karmaconfigs.closedblockselevator.command;

import ml.karmaconfigs.api.common.utils.string.StringUtils;
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
            if (sender.hasPermission("elevator.add")) {
                switch (args.length) {
                    case 1: {
                        String player_name = args[0];
                        Player player = plugin.getServer().getPlayer(player_name);

                        if (player != null) {
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
                                sender.sendMessage(StringUtils.toColor(messages.elevatorGive(1, player_name)));
                                player.sendMessage(StringUtils.toColor(messages.elevatorReceive(1)));

                                ItemStack elevator = new ItemStack(config.elevatorItem(), 1);
                                ItemMeta dropMeta = elevator.getItemMeta();
                                if (dropMeta != null) {
                                    dropMeta.setDisplayName(config.elevatorItemName());
                                    dropMeta.setLore(config.elevatorItemLore());
                                    elevator.setItemMeta(dropMeta);
                                }

                                player_inventory.addItem(elevator);
                            } else {
                                sender.sendMessage(StringUtils.toColor(messages.notEnoughSpace(1, 0, player_name)));
                            }
                        } else {
                            try {
                                int amount = Integer.parseInt(player_name);

                                player = (Player) sender;
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
                                    sender.sendMessage(StringUtils.toColor(messages.notEnoughSpace(amount, available, player_name)));
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

                                sender.sendMessage(StringUtils.toColor(messages.elevatorGive(amount, player_name)));
                            } catch (Throwable ex) {
                                sender.sendMessage(StringUtils.toColor(messages.notOnline(player_name)));
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
                                    sender.sendMessage(StringUtils.toColor(messages.notEnoughSpace(amount, available, player_name)));
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

                                sender.sendMessage(StringUtils.toColor(messages.elevatorGive(amount, player_name)));
                                player.sendMessage(StringUtils.toColor(messages.elevatorReceive(amount)));
                            } else {
                                sender.sendMessage(StringUtils.toColor(messages.notOnline(player_name)));
                            }
                        } catch (Throwable ex) {
                            sender.sendMessage(StringUtils.toColor(messages.permission())); //Don't know what to do, so we send permission error
                        }
                    }
                    break;
                    default:
                        sender.sendMessage(StringUtils.toColor("&cPlease provide a player name and (optional) amount of elevators to give"));
                        break;
                }
            } else {
                sender.sendMessage(StringUtils.toColor(messages.permission()));
            }
        } else {
            switch (args.length) {
                case 1: {
                    String player_name = args[0];
                    Player player = plugin.getServer().getPlayer(player_name);

                    if (player != null) {
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
                            player.sendMessage(StringUtils.toColor(messages.elevatorReceive(1)));

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
                            player.sendMessage(StringUtils.toColor(messages.elevatorReceive(amount)));
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
