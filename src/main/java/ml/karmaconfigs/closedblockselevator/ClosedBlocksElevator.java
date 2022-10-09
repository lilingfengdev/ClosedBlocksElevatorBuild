package ml.karmaconfigs.closedblockselevator;

import ml.karmaconfigs.api.bukkit.KarmaPlugin;
import ml.karmaconfigs.api.common.karma.APISource;

public final class ClosedBlocksElevator {

    public static KarmaPlugin plugin = (KarmaPlugin) APISource.loadProvider("ClosedBlocksElevator");

    public static String LAST_LINE_MATCHER = "---------------";
}
