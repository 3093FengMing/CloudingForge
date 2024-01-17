package me.fengming.cloudingforge;

import me.fengming.cloudingforge.capabilities.PlayerOxygenCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Utils {
    public static BlockBehaviour.StatePredicate never() {
        return (_1, _2, _3) -> false;
    }

    public static PlayerOxygenCapability getOxygenCapability(Player p) {
        return p.getCapability(CloudingForge.OXYGEN_CAPABILITY).orElse(new PlayerOxygenCapability(0));
    }
}
