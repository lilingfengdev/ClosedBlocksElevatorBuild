package ml.karmaconfigs.closedblockselevator;

import ml.karmaconfigs.api.bukkit.KarmaPlugin;
import ml.karmaconfigs.api.bukkit.reflection.BossMessage;
import ml.karmaconfigs.api.bukkit.server.BukkitServer;
import ml.karmaconfigs.api.bukkit.server.Version;
import ml.karmaconfigs.api.common.ResourceDownloader;
import ml.karmaconfigs.api.common.data.path.PathUtilities;
import ml.karmaconfigs.api.common.karma.file.yaml.FileCopy;
import ml.karmaconfigs.api.common.karma.loader.BruteLoader;
import ml.karmaconfigs.api.common.timer.SchedulerUnit;
import ml.karmaconfigs.api.common.timer.SourceScheduler;
import ml.karmaconfigs.api.common.timer.scheduler.SimpleScheduler;
import ml.karmaconfigs.api.common.utils.enums.Level;
import ml.karmaconfigs.api.common.utils.url.HttpUtil;
import ml.karmaconfigs.api.common.utils.url.URLUtils;
import ml.karmaconfigs.api.common.version.checker.VersionUpdater;
import ml.karmaconfigs.api.common.version.updater.VersionCheckType;
import ml.karmaconfigs.closedblockselevator.command.AddElevator;
import ml.karmaconfigs.closedblockselevator.command.ReloadElevator;
import ml.karmaconfigs.closedblockselevator.command.TestUnit;
import ml.karmaconfigs.closedblockselevator.command.TextureElevator;
import ml.karmaconfigs.closedblockselevator.listener.*;
import ml.karmaconfigs.closedblockselevator.listener.ia.ItemsAdderListener;
import ml.karmaconfigs.closedblockselevator.storage.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ShapedRecipe;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import static ml.karmaconfigs.closedblockselevator.ClosedBlocksElevator.plugin;

public final class Main extends KarmaPlugin {

    private int knows_is_outdated = 1;

    private ShapedRecipe shaped = null;
    private static boolean iAdder = false;

    private static boolean licensed = false;


    @Override
    @SuppressWarnings("deprecation")
    public void enable() {
        FileCopy config_copy = new FileCopy(this, "config.yml");
        FileCopy messag_copy = new FileCopy(this, "messages.yml");

        try {
            config_copy.copy(getDataPath().resolve("config.yml"));
            messag_copy.copy(getDataPath().resolve("messages.yml"));

            getServer().getPluginManager().registerEvents(new BlockListener(), this);
            getServer().getPluginManager().registerEvents(new ActionListener(), this);
            getServer().getPluginManager().registerEvents(new ExplodeListener17(), this);
            getServer().getPluginManager().registerEvents(new ModelListener(), this);
            getServer().getPluginManager().registerEvent(PlayerJoinEvent.class, new Listener(){}, EventPriority.HIGHEST, (handler, event) -> {
                PlayerJoinEvent pje = (PlayerJoinEvent) event;
                Player player = pje.getPlayer();

                Client client = new Client(player);
                client.addScheduler();

                BossMessage empty = new BossMessage(this, "", 1);
                empty.scheduleBar(new Player[]{player}); //We do this so the boss bar is ready to be instantly shown to players
            }, this, true);
            if (BukkitServer.isOver(Version.v1_8)) {
                getServer().getPluginManager().registerEvents(new NewExplodeListener(), this);
            } else {
                console().send("You are using a very old minecraft version, elevators may not work as expected", Level.WARNING);
            }

            getCommand("give-elevator").setExecutor(new AddElevator());
            getCommand("reload-elevator").setExecutor(new ReloadElevator());
            getCommand("elevator-pack").setExecutor(new TextureElevator());

            PluginCommand test = getCommand("elevator-test");
            if (test != null) {
                console().send("Enabled test unit command, make sure you don't have this in a production server!", Level.GRAVE);
                test.setExecutor(new TestUnit());
            }

            console().send("Elevators has been initialize successfully", Level.OK);

            VersionUpdater updater = VersionUpdater.createNewBuilder(this)
                    .withVersionType(VersionCheckType.NUMBER)
                    .build();

            SimpleScheduler scheduler = new SourceScheduler(this, 30, SchedulerUnit.SECOND, true).multiThreading(true)
                    .restartAction(() -> {
                        updater.fetch(true).whenComplete((latest_version, error) -> {
                            if (error != null) {
                                logger().scheduleLog(Level.GRAVE, error);
                                logger().scheduleLog(Level.INFO, "Failed to check for updates, are we on the internet?");
                            } else {
                                if (!latest_version.isUpdated()) {
                                    if (--knows_is_outdated == 0) {
                                        knows_is_outdated = 3;
                                        console().send("You are in an outdated version of Elevator! ({0}). Latest is {1}", Level.GRAVE, latest_version.getCurrent(), latest_version.getLatest());
                                        console().send("You can update now from where you purchased the plugin:", Level.INFO);
                                        for (String update : latest_version.getUpdateURLs()) {
                                            console().send("- {0}", Level.INFO, update);
                                        }

                                        console().send("&eChangelog: ");
                                        for (String str : latest_version.getChangelog()) {
                                            console().send(str);
                                        }
                                    }
                                }
                            }
                        });
                    });

            scheduler.start();

            try {
                console().send("Trying to detect ItemsAdder", Level.INFO);
                Class.forName("dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent");
                console().send("Detected ItemsAdder successfully", Level.OK);

                getServer().getPluginManager().registerEvents(new ItemsAdderListener(), this);
                iAdder = true;
            } catch (Throwable ex) {
                console().send("Failed to detect ItemsAdder. Plugin will work anyway", Level.WARNING);
                Config config = new Config();
                shaped = config.getRecipe();
                getServer().addRecipe(shaped);
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        try {
            getServer().removeRecipe(shaped.getKey());
        } catch (Throwable ignored) {}

        console().send("Elevators stopped", Level.WARNING);
    }

    @Override
    public String updateURL() {
        return URLUtils.getOrBackup(
                "https://karmaconfigs.github.io/updates/ClosedBlocksElevator/latest.kup",
                "https://karmadev.es/cbe/latest.kup",
                "https://karmaconfigs.ml/cbe/latest.kup",
                "https://karmarepo.ml/cbe/latest.kup",
                "https://backup.karmadev.es/cbe/latest.kup",
                "https://backup.karmaconfigs.ml/cbe/latest.kup",
                "https://backup.karmarepo.ml/cbe/latest.kup"
        ).toString();
    }

    public static boolean hasItemAdder() {
        return iAdder;
    }
}
