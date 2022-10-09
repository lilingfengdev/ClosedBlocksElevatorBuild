package ml.karmaconfigs.closedblockselevator;

import ml.karmaconfigs.api.bukkit.KarmaPlugin;
import ml.karmaconfigs.api.bukkit.server.BukkitServer;
import ml.karmaconfigs.api.bukkit.server.Version;
import ml.karmaconfigs.api.common.karmafile.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.closedblockselevator.command.AddElevator;
import ml.karmaconfigs.closedblockselevator.command.ReloadElevator;
import ml.karmaconfigs.closedblockselevator.listener.ActionListener;
import ml.karmaconfigs.closedblockselevator.listener.BlockListener;
import ml.karmaconfigs.closedblockselevator.listener.ExplodeListener17;
import ml.karmaconfigs.closedblockselevator.listener.NewExplodeListener;

public final class Main extends KarmaPlugin {

    @Override
    public void enable() {
        FileCopy config_copy = new FileCopy(this, "config.yml");
        FileCopy messag_copy = new FileCopy(this, "messages.yml");

        try {
            config_copy.copy(getDataPath().resolve("config.yml"));
            messag_copy.copy(getDataPath().resolve("messages.yml"));

            getServer().getPluginManager().registerEvents(new BlockListener(), this);
            getServer().getPluginManager().registerEvents(new ActionListener(), this);
            getServer().getPluginManager().registerEvents(new ExplodeListener17(), this);
            if (BukkitServer.isOver(Version.v1_7_10)) {
                getServer().getPluginManager().registerEvents(new NewExplodeListener(), this);
            } else {
                console().send("You are using a very old minecraft version, elevators may not work as expected", Level.WARNING);
            }

            getCommand("give-elevator").setExecutor(new AddElevator());
            getCommand("reload-elevator").setExecutor(new ReloadElevator());
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public String updateURL() {
        return "https://karmaconfigs.github.io/updates/ClosedBlocksElevator/latest.kupdter";
    }
}
