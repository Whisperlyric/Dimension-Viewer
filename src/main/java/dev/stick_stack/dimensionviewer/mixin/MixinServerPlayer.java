package dev.stick_stack.dimensionviewer.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.stick_stack.dimensionviewer.*;
import net.minecraft.util.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.PlainTextContent;
import net.minecraft.util.Identifier;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayer extends MixinPlayer {

    protected MixinServerPlayer(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Unique
    private static Text createDimensionComponent(ServerPlayerEntity player, MutableText originalName) {
        Identifier dimension = player.getEntityWorld().getRegistryKey().getValue();
        String dimSource = CommonUtils.toTitleCase(CommonUtils.splitResourceLocation(dimension, 0));
        final PlayerListHandlerFabric handler = new PlayerListHandlerFabric();

        Style style = Style.EMPTY;
        boolean foundModdedDim = false;
        if (ConfigFabric.get().PER_DIM_COLOR) {
            for (String modDim : ConfigFabric.get().MODDED_DIMS) {
                if (modDim.split(" ")[0].equals(dimension.toString())) {
                    style = tryGetColor(modDim.split(" ")[1]);
                    foundModdedDim = true;
                    break;
                }
            }

            if (!foundModdedDim) {
                style = switch (dimension.toString()) {
                    case "minecraft:overworld" -> tryGetColor(ConfigFabric.get().OVERWORLD_COLOR);
                    case "minecraft:the_nether" -> tryGetColor(ConfigFabric.get().NETHER_COLOR);
                    case "minecraft:the_end" -> tryGetColor(ConfigFabric.get().END_COLOR);
                    default -> tryGetColor(ConfigFabric.get().DEFAULT_COLOR);
                };
            }
        } else {
            style = tryGetColor(ConfigFabric.get().DEFAULT_COLOR);
        }

        MutableText dimComponent = handler.makeDimensionComponent(player, ConfigFabric.get().LIST_FORMAT);
        dimComponent = dimComponent.fillStyle(style);

        if (ConfigFabric.get().CHAT_DIM_HOVER) {
            dimComponent = dimComponent.setStyle(dimComponent.getStyle().withHoverEvent(
                    new HoverEvent.ShowText(Text.literal(dimSource))
            ));
        }

        MutableText spacer = MutableText.of(new PlainTextContent.Literal(" "));
        if (ConfigFabric.get().DIM_POSITION == CommonUtils.DimensionPosition.PREPEND) {
            spacer = spacer.setStyle(Style.EMPTY.withColor(Formatting.WHITE));
            spacer = spacer.append(originalName);
            return dimComponent.append(spacer);
        } else {
            spacer = spacer.append(dimComponent);
            return originalName.append(spacer);
        }
    }

    @Unique
    private static Style tryGetColor(String color) {
        try {
            Formatting format = Formatting.valueOf(color);
            return Style.EMPTY.withColor(format);
        } catch (IllegalArgumentException exception) {
            for (String entry : ConfigFabric.get().CUSTOM_COLORS) {
                String[] splits = entry.split(" ");

                if (color.equals(splits[0])) {
                    if (splits[1].startsWith("#")) {
                        return Style.EMPTY.withColor(CommonUtils.hexToInt(splits[1]));
                    } else {
                        int r = Integer.parseInt(splits[1].substring(1));
                        int g = Integer.parseInt(splits[2].substring(1));
                        int b = Integer.parseInt(splits[3].substring(1));

                        return Style.EMPTY.withColor(CommonUtils.rgbToInt(r, g, b));
                    }
                }
            }
        }

        Constants.LOG.error("Invalid colour {}! Setting to default...", color);
        return Style.EMPTY;
    }

    @Override
    protected void onGetDisplayName(CallbackInfoReturnable<MutableText> cir) {
        if (!ConfigFabric.get().DIM_IN_CHAT_NAME) return;

        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        cir.setReturnValue(createDimensionComponent(self, cir.getReturnValue()).copy());
    }

    @ModifyReturnValue(method = "getPlayerListName", at = @At("RETURN"))
    private Text onGetPlayerListName(@Nullable Text component) {
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        MutableText nameComponent = getName().copy();
        return createDimensionComponent(self, nameComponent);
    }
}
