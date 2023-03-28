package ml.karmaconfigs.closedblockselevator.storage;

import ml.karmaconfigs.api.common.karma.file.yaml.KarmaYamlManager;
import ml.karmaconfigs.api.common.karma.file.yaml.YamlReloader;
import ml.karmaconfigs.api.common.string.ListTransformation;
import ml.karmaconfigs.api.common.string.StringUtils;

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

    public String elevatorUp(final int level) {
        return yaml.getString("ElevatorBossUp", "&bLevel: &a{level}").replace("{level}", String.valueOf(level));
    }

    public String elevatorDown(final int level) {
        return yaml.getString("ElevatorBossDown", "&bLevel: &c{level}").replace("{level}", String.valueOf(level));
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

    public String requestTextures() {
        return yaml.getString("CustomModelData.RequestTextures", "&cThis server uses custom textures for some blocks/items, download the resource pack when required to ensure a complete experience in the server");
    }

    public String deniedTextures() {
        return StringUtils.listToString(yaml.getStringList("CustomModelData.DeclinedTextures",
                "&7ServerName",
                "",
                "&cPlease accept the resource pack",
                "&cto play in this server. The resource",
                "&cpack contains custom items and blocks",
                "&cwhich our resource pack provides and",
                "&cyou need to ensure a full experience."), ListTransformation.NEW_LINES);
    }

    public String noTextures() {
        return yaml.getString("CustomModelData.NoTextures", "&cYou are viewing a simple version of the elevator. This servers provides a resource pack to make it look better, install the resource pack using /elevator-pack command");
    }
}
