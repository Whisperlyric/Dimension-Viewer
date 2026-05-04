package dev.stick_stack.dimensionviewer.mixin;

import net.minecraft.text.Text;
import net.minecraft.text.MutableText;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayer extends LivingEntity {

    @Shadow public abstract Text getDisplayName();

    MixinPlayer(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    protected void onGetDisplayName(CallbackInfoReturnable<MutableText> cir) {
    }

}
