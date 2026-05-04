package dev.stick_stack.dimensionviewer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.stick_stack.dimensionviewer.platform.Services;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Locale;

public class CustomCommands {

    public static void RegisterCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("dimensionviewer")
                .then(CommandManager.literal("get")
                        .executes(CustomCommands::getDimensionId)
                        .then(CommandManager.literal("color")
                                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .executes(ctx -> {
                                            ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                            String dim = world.getRegistryKey().getValue().toString();
                                            String value = Services.CONFIG.GetCustomColor(dim);
                                            ctx.getSource().sendFeedback(() -> Text.literal("Color for %s is '%s'".formatted(dim, value)), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("alias")
                                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .executes(ctx -> {
                                            ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                            String dim = world.getRegistryKey().getValue().toString();
                                            String value = Services.CONFIG.GetAlias(dim);
                                            ctx.getSource().sendFeedback(() -> Text.literal("Alias for %s is '%s'".formatted(dim, value)), false);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("set")
                        .then(CommandManager.literal("color")
                                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .then(CommandManager.argument("custom_color", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                                    String dim = world.getRegistryKey().getValue().toString();
                                                    String color = StringArgumentType.getString(ctx, "custom_color").toUpperCase(Locale.ROOT);

                                                    Services.CONFIG.SetColor(dim, color);
                                                    refreshDisplayNames(ctx);

                                                    ctx.getSource().sendFeedback(() -> Text.literal("Set color of dimension %s to '%s'".formatted(dim, color)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(CommandManager.literal("alias")
                                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .then(CommandManager.argument("custom_alias", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                                    String dim = world.getRegistryKey().getValue().toString();
                                                    String alias = StringArgumentType.getString(ctx, "custom_alias");

                                                    Services.CONFIG.SetAlias(dim, alias);
                                                    refreshDisplayNames(ctx);

                                                    ctx.getSource().sendFeedback(() -> Text.literal("Set alias of dimension %s to '%s'".formatted(dim, alias)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(CommandManager.literal("format")
                                .then(CommandManager.argument("custom_format", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String format = StringArgumentType.getString(ctx, "custom_format");

                                            Services.CONFIG.SetFormat(format);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendFeedback(() -> Text.literal("Set list format to '%s'".formatted(format)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("position")
                                .then(CommandManager.argument("custom_position", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String position = StringArgumentType.getString(ctx, "custom_position");

                                            Services.CONFIG.SetPosition(position);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendFeedback(() -> Text.literal("Set dimension position to '%s'".formatted(position)), true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("remove")
                        .then(CommandManager.literal("color")
                                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .executes(ctx -> {
                                            ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                            String dim = world.getRegistryKey().getValue().toString();

                                            Services.CONFIG.RemoveColor(dim);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendFeedback(() -> Text.literal("Removed color for dimension %s".formatted(dim)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(CommandManager.literal("alias")
                                .then(CommandManager.argument("dimension", DimensionArgumentType.dimension())
                                        .executes(ctx -> {
                                            ServerWorld world = DimensionArgumentType.getDimensionArgument(ctx, "dimension");
                                            String dim = world.getRegistryKey().getValue().toString();

                                            Services.CONFIG.RemoveAlias(dim);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendFeedback(() -> Text.literal("Removed alias for dimension %s".formatted(dim)), true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(CommandManager.literal("reload")
                        .executes(ctx -> {
                            Services.CONFIG.Reload();
                            refreshDisplayNames(ctx);

                            ctx.getSource().sendFeedback(() -> Text.literal("Reloaded Dimension Viewer config"), true);
                            return 1;
                        })
                )
        );
    }

    private static void refreshDisplayNames(CommandContext<ServerCommandSource> ctx) {
        FabricUtils.refreshDisplayNames(ctx.getSource().getServer().getPlayerManager());
    }

    private static int getDimensionId(CommandContext<ServerCommandSource> context) {
        try {
            var player = context.getSource().getPlayerOrThrow();
            var dim = player.getEntityWorld().getRegistryKey().getValue();
            context.getSource().sendFeedback(() -> Text.literal(dim + ": " + CommonUtils.dimensionToString(dim)), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendError(Text.literal("Failed to find player!"));
            return -1;
        }
    }

}
