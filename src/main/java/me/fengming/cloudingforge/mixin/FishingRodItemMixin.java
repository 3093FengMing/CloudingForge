package me.fengming.cloudingforge.mixin;

import me.fengming.cloudingforge.CloudingForge;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodItem.class)
public class FishingRodItemMixin {
    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void injected_use(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemStack = pPlayer.getItemInHand(pHand);
        if (pPlayer.fishing != null) {
            if (!pLevel.isClientSide) {
                if (pPlayer.getInventory().hasAnyMatching((i) -> i.getItem().builtInRegistryHolder().is(new ResourceLocation(CloudingForge.MODID, "hook")))) {
                    cir.setReturnValue(InteractionResultHolder.pass(itemStack));
                    cir.cancel();
                    return;
                }
                int damageAmount = pPlayer.fishing.retrieve(itemStack);
                itemStack.hurtAndBreak(damageAmount, pPlayer, (p) -> p.broadcastBreakEvent(pHand));
            }

            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.NEUTRAL, 1.0F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_FINISH);
        } else {
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.NEUTRAL, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            if (!pLevel.isClientSide) {
                int lure = EnchantmentHelper.getFishingSpeedBonus(itemStack);
                int luckOfTheSea = EnchantmentHelper.getFishingLuckBonus(itemStack);
                pLevel.addFreshEntity(new FishingHook(pPlayer, pLevel, luckOfTheSea, lure));
            }

            pPlayer.awardStat(Stats.ITEM_USED.get((FishingRodItem)(Object)this));
            pPlayer.gameEvent(GameEvent.ITEM_INTERACT_START);
        }

        cir.setReturnValue(InteractionResultHolder.sidedSuccess(itemStack, pLevel.isClientSide()));
        cir.cancel();
    }
}
