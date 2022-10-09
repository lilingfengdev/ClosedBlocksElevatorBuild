package ml.karmaconfigs.closedblockselevator;

import ml.karmaconfigs.api.bukkit.KarmaPlugin;
import ml.karmaconfigs.api.bukkit.server.BukkitServer;
import ml.karmaconfigs.api.bukkit.server.Version;
import ml.karmaconfigs.api.common.karmafile.karmayaml.FileCopy;
import ml.karmaconfigs.api.common.timer.SchedulerUnit;
import ml.karmaconfigs.api.common.timer.SourceScheduler;
import ml.karmaconfigs.api.common.timer.scheduler.SimpleScheduler;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.api.common.version.VersionUpdater;
import ml.karmaconfigs.api.common.version.util.VersionCheckType;
import ml.karmaconfigs.closedblockselevator.command.AddElevator;
import ml.karmaconfigs.closedblockselevator.command.ReloadElevator;
import ml.karmaconfigs.closedblockselevator.listener.ActionListener;
import ml.karmaconfigs.closedblockselevator.listener.BlockListener;
import ml.karmaconfigs.closedblockselevator.listener.ExplodeListener17;
import ml.karmaconfigs.closedblockselevator.listener.NewExplodeListener;

public final class Main extends KarmaPlugin {

    private int knows_is_outdated = 1;

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

            console().send("Elevators has been initialize successfully", Level.OK);

            VersionUpdater updater = VersionUpdater.createNewBuilder(this)
                    .withVersionType(VersionCheckType.NUMBER)
                    .build();

            SimpleScheduler scheduler = new SourceScheduler(this, 5, SchedulerUnit.MINUTE, true).multiThreading(true)
                    .restartAction(() -> updater.fetch(true).whenComplete((latest_version, error) -> {
                        if (error != null) {
                            logger().scheduleLog(Level.GRAVE, error);
                            logger().scheduleLog(Level.INFO, "Failed to check for updates, are we on the internet?");
                        } else {
                            if (!latest_version.isUpdated()) {
                                if (--knows_is_outdated == 0) {
                                    knows_is_outdated = 3;
                                    console().send("You are in an outdated version of Elevator! ({0}). Latest is {1}", Level.GRAVE, latest_version.getCurrent(), latest_version.getLatest());
                                    console().send("You can update now from {0}", Level.INFO, latest_version.getUpdateURL());

                                    console().send("&eChangelog: ");
                                    for (String str : latest_version.getChangelog()) {
                                        console().send(str);
                                    }
                                }
                            }
                        }
                    }));

            scheduler.start();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        console().send("Elevators stopped", Level.WARNING);
    }

    @Override
    public String updateURL() {
        return "https://karmaconfigs.github.io/updates/ClosedBlocksElevator/latest.kupdter";
    }
}
