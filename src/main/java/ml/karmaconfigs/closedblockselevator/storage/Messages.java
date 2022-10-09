package ml.karmaconfigs.closedblockselevator.storage;

import ml.karmaconfigs.api.common.karmafile.karmayaml.KarmaYamlManager;
import ml.karmaconfigs.api.common.karmafile.karmayaml.YamlReloader;

import java.nio.file.Path;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public final class Messages {

    private final static Path messages = plugin.getDataPath().resolve("messages.yml");
    private final static KarmaYamlManager yaml = new KarmaYamlManager(messages);

    public static void reload() {
        YamlReloader reloader = yaml.getReloader();
        if (reloader != null) {
            reloader.reload();
        }
    }

    public String elevatorPlaced() {
        return yaml.getString("ElevatorPlaced", "&aElevator placed successfully");
    }

    public String elevatorFailed() {
        return yaml.getString("ElevatorFailed", "&cFailed to place elevator");
    }

    public String elevatorHidden() {
        return yaml.getString("ElevatorHidden", "&aYou've successfully hide the elevator");
    }

    public String elevatorHideError() {
        return yaml.getString("ElevatorHideError", "&cFailed to hide the elevator");
    }

    public String elevatorDestroyed() {
        return yaml.getString("ElevatorDestroyed", "&aDestroyed elevator successfully");
    }

    public String elevatorDestroyFail() {
        return yaml.getString("ElevatorFailDestroy", "&cFailed to destroy elevator");
    }

    public String elevatorGive(final int amount, final String name) {
        return yaml.getString("ElevatorGive", "&aSuccessfully gave x{amount} elevators to {player}")
                .replace("{amount}", String.valueOf(amount))
                .replace("{player}", name);
    }

    public String elevatorReceive(final int amount) {
        return yaml.getString("ElevatorReceive", "&aYou've received x{amount} elevators").replace("{amount}", String.valueOf(amount));
    }

    public String notOnline(final String name) {
        return yaml.getString("NotOnline", "&cThe player {player} is not online").replace("{player}", name);
    }

    public String notEnoughSpace(final int amount, final int new_amount, final String name) {
        return yaml.getString("NotEnoughSpace", "&cThe player {player} didn't had enough space to keep x{amount} elevators. So we gave him x{new_amount} instead")
                .replace("{player}", name)
                .replace("{amount}", String.valueOf(amount))
                .replace("{new_amount}", String.valueOf(new_amount));
    }

    public String permission() {
        return yaml.getString("Permission", "&cYou do not have enough permissions to execute that command!");
    }
}
