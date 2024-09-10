package org.leodreamer.dumpeverything.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SelectedData {
    private static final Map<String, SelectedArea> AREA_MAP = new HashMap<>();

    public static void setSelectedPos1(@NotNull Player player, BlockPos pos) {
        if (!AREA_MAP.containsKey(player.getStringUUID()))
            AREA_MAP.put(player.getStringUUID(), new SelectedArea());
        AREA_MAP.get(player.getStringUUID()).pos1 = pos;
    }

    public static void setSelectedPos2(@NotNull Player player, BlockPos pos) {
        if (!AREA_MAP.containsKey(player.getStringUUID()))
            AREA_MAP.put(player.getStringUUID(), new SelectedArea());
        AREA_MAP.get(player.getStringUUID()).pos2 = pos;
    }

    public static SelectedArea getSelectedArea(@NotNull Player player) {
        return AREA_MAP.get(player.getStringUUID());
    }

    public static final class SelectedArea {
        public BlockPos pos1;
        public BlockPos pos2;
    }
}
