package org.leodreamer.dumpeverything;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import org.leodreamer.dumpeverything.loggers.BaseDump;
import org.leodreamer.dumpeverything.loggers.BlockDump;
import org.leodreamer.dumpeverything.loggers.FluidDump;
import org.leodreamer.dumpeverything.loggers.ItemDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DumpCommand {

    public static final int ALL = 0;
    public static final int BLOCK = 1;
    public static final int ITEM = 2;
    public static final int FLUID = 3;
    public static final String FOLDER = "dumps";
    public static final String FILENAME = "vocabulary.json";
    private static final Logger logger = LoggerFactory.getLogger(DumpCommand.class);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dump")
                .executes(context -> dump(context.getSource(), ALL))
                .then(Commands.literal("items")
                        .executes(context -> dump(context.getSource(), ITEM)))
                .then(Commands.literal("blocks")
                        .executes(context -> dump(context.getSource(), BLOCK)))
                .then(Commands.literal("fluid")
                        .executes(context -> dump(context.getSource(), FLUID))));
    }

    private static int dump(CommandSourceStack stack, int type) {
        File file = new File(FOLDER);
        if (!file.exists())
            file.mkdirs();
        String filePath = FOLDER + File.separator + FILENAME;
        stack.sendSystemMessage(Component.translatable("commands.dump.start"));
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(getJSONString(stack, type));
            Component component = Component.translatable("commands.dump.success.link")
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, filePath)));
            stack.sendSuccess(() -> Component.translatable("commands.dump.success")
                    .append(Component.literal(" "))
                    .append(component), true);
            return 1;
        } catch (IOException e) {
            stack.sendFailure(Component.translatable("commands.dump.failure"));
            logger.error("Exception: ", e);
            return 0;
        }
    }

    private static String getJSONString(CommandSourceStack stack, int type) {
        Map<String, Map<String, List<String>>> res = new LinkedHashMap<>();
        // Everything in Minecraft
        List<BaseDump> dumps = new ArrayList<>();

        if (type == ALL || type == BLOCK) // Block
            dumps.add(new BlockDump());
        if (type == ALL || type == ITEM)
            dumps.add(new ItemDump());
        if (type == ALL || type == FLUID)
            dumps.add(new FluidDump());


        for (BaseDump dump : dumps) {
            res.put(dump.getTypeName(), dump.getIdentifierMap());
            res.put(dump.getTypeName() + "Tags", dump.getTagMap());
        }

        // remove the repeated items in blocks
        if (type == ALL && Config.keepDistinct) {
            Map<String, List<String>> blockMap = res.get("Block");
            Map<String, List<String>> itemMap = res.get("Item");
            for (String namespace : blockMap.keySet()) {
                List<String> blockList = blockMap.get(namespace);
                List<String> itemList = itemMap.get(namespace);
                if (itemList != null && blockList != null)
                    itemList.removeAll(blockList);
            }
        }

        // write it as a JSON
        StringBuilder sb = new StringBuilder("{");
        for (String typeKey : res.keySet()) {
            sb.append("\"").append(typeKey).append("\": {");
            Map<String, List<String>> map = res.get(typeKey);
            for (String namespace : map.keySet()) {
                sb.append("\"").append(namespace).append("\":");
                List<String> list = map.get(namespace);
                sb.append(list.stream().map(name -> "\"" + name + "\"").toList()).append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append("},");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.append("}").toString();
    }
}
