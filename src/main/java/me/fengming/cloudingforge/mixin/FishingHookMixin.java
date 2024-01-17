package me.fengming.cloudingforge.mixin;

import me.fengming.cloudingforge.CloudingForge;
import me.fengming.cloudingforge.item.HookItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(FishingHook.class)
public abstract class FishingHookMixin {
    @Shadow @Nullable private Entity hookedIn;

    @Shadow @Nullable public abstract Player getPlayerOwner();

    @Redirect(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/projectile/FishingHook;life:I", opcode = Opcodes.GETFIELD))
    private int redirected_life(FishingHook instance) {
        return 0;
    }

    @Inject(method = "onHitEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;setHookedEntity(Lnet/minecraft/world/entity/Entity;)V", shift = At.Shift.AFTER))
    private void injected_onHitEntity_givePlayerHook(EntityHitResult pResult, CallbackInfo ci) {
        if (this.hookedIn instanceof Player p) {
            HookItem hook = (HookItem) CloudingForge.HOOK_ITEM.get();
            ItemStack itemStack = new ItemStack(hook);
            hook.setOwnerEntity(itemStack, this.getPlayerOwner());
            p.getInventory().add(itemStack);
        }
    }

}
