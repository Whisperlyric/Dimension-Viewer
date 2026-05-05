package dev.stick_stack.dimensionviewer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.stick_stack.dimensionviewer.platform.Services;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public class CustomCommands {

    public static void RegisterCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("dimensionviewer")
                .then(Commands.literal("get")
                        .executes(CustomCommands::getDimensionId)
                        .then(Commands.literal("color")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            ServerLevel world = DimensionArgument.getDimension(ctx, "dimension");
                                            String dim = world.dimension().identifier().toString();
                                            String value = Services.CONFIG.GetCustomColor(dim);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Color for %s is '%s'".formatted(dim, value)), false);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("alias")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            ServerLevel world = DimensionArgument.getDimension(ctx, "dimension");
                                            String dim = world.dimension().identifier().toString();
                                            String value = Services.CONFIG.GetAlias(dim);
                                            ctx.getSource().sendSuccess(() -> Component.literal("Alias for %s is '%s'".formatted(dim, value)), false);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("set")
                        .then(Commands.literal("color")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("custom_color", StringArgumentType.word())
                                                .executes(ctx -> {
                                                    ServerLevel world = DimensionArgument.getDimension(ctx, "dimension");
                                                    String dim = world.dimension().identifier().toString();
                                                    String color = StringArgumentType.getString(ctx, "custom_color").toUpperCase(Locale.ROOT);

                                                    Services.CONFIG.SetColor(dim, color);
                                                    refreshDisplayNames(ctx);

                                                    ctx.getSource().sendSuccess(() -> Component.literal("Set color of dimension %s to '%s'".formatted(dim, color)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("alias")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .then(Commands.argument("custom_alias", StringArgumentType.greedyString())
                                                .executes(ctx -> {
                                                    ServerLevel world = DimensionArgument.getDimension(ctx, "dimension");
                                                    String dim = world.dimension().identifier().toString();
                                                    String alias = StringArgumentType.getString(ctx, "custom_alias");

                                                    Services.CONFIG.SetAlias(dim, alias);
                                                    refreshDisplayNames(ctx);

                                                    ctx.getSource().sendSuccess(() -> Component.literal("Set alias of dimension %s to '%s'".formatted(dim, alias)), true);
                                                    return 1;
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("format")
                                .then(Commands.argument("custom_format", StringArgumentType.greedyString())
                                        .executes(ctx -> {
                                            String format = StringArgumentType.getString(ctx, "custom_format");

                                            Services.CONFIG.SetFormat(format);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Set list format to '%s'".formatted(format)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("position")
                                .then(Commands.argument("custom_position", StringArgumentType.word())
                                        .executes(ctx -> {
                                            String position = StringArgumentType.getString(ctx, "custom_position");

                                            Services.CONFIG.SetPosition(position);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Set dimension position to '%s'".formatted(position)), true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("remove")
                        .then(Commands.literal("color")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            ServerLevel world = DimensionArgument.getDimension(ctx, "dimension");
                                            String dim = world.dimension().identifier().toString();

                                            Services.CONFIG.RemoveColor(dim);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Removed color for dimension %s".formatted(dim)), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("alias")
                                .then(Commands.argument("dimension", DimensionArgument.dimension())
                                        .executes(ctx -> {
                                            ServerLevel world = DimensionArgument.getDimension(ctx, "dimension");
                                            String dim = world.dimension().identifier().toString();

                                            Services.CONFIG.RemoveAlias(dim);
                                            refreshDisplayNames(ctx);

                                            ctx.getSource().sendSuccess(() -> Component.literal("Removed alias for dimension %s".formatted(dim)), true);
                                            return 1;
                                        })
                                )
                        )
                )
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            Services.CONFIG.Reload();
                            refreshDisplayNames(ctx);

                            ctx.getSource().sendSuccess(() -> Component.literal("Reloaded Dimension Viewer config"), true);
                            return 1;
                        })
                )
        );
    }

    private static void refreshDisplayNames(CommandContext<CommandSourceStack> ctx) {
        FabricUtils.refreshDisplayNames(ctx.getSource().getServer().getPlayerList());
    }

    private static int getDimensionId(CommandContext<CommandSourceStack> context) {
        try {
            var player = context.getSource().getPlayerOrException();
            var dim = player.level().dimension().identifier();
            context.getSource().sendSuccess(() -> Component.literal(dim + ": " + CommonUtils.dimensionToString(dim)), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to find player!"));
            return -1;
        }
    }

}
