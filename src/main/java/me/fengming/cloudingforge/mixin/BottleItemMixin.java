package me.fengming.cloudingforge.mixin;

import me.fengming.cloudingforge.CloudingForge;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BottleItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(BottleItem.class)
public abstract class BottleItemMixin extends Item {
    public BottleItemMixin(Properties pProperties) {
        super(pProperties);
    }

    @Shadow protected abstract ItemStack turnBottleIntoItem(ItemStack pBottleStack, Player pPlayer, ItemStack pFilledBottleStack);

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void injected_use(Level pLevel, Player pPlayer, InteractionHand pHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        List<AreaEffectCloud> list = pLevel.getEntitiesOfClass(AreaEffectCloud.class, pPlayer.getBoundingBox().inflate(2.0D), (p_289499_) -> p_289499_ != null && p_289499_.isAlive() && p_289499_.getOwner() instanceof EnderDragon);
        ItemStack itemstack = pPlayer.getItemInHand(pHand);
        if (!list.isEmpty()) {
            AreaEffectCloud areaeffectcloud = list.get(0);
            areaeffectcloud.setRadius(areaeffectcloud.getRadius() - 0.5F);
            pLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL_DRAGONBREATH, SoundSource.NEUTRAL, 1.0F, 1.0F);
            pLevel.gameEvent(pPlayer, GameEvent.FLUID_PICKUP, pPlayer.position());
            if (pPlayer instanceof ServerPlayer) {
                ServerPlayer serverplayer = (ServerPlayer)pPlayer;
                CriteriaTriggers.PLAYER_INTERACTED_WITH_ENTITY.trigger(serverplayer, itemstack, areaeffectcloud);
            }

            cir.setReturnValue(InteractionResultHolder.sidedSuccess(this.turnBottleIntoItem(itemstack, pPlayer, new ItemStack(Items.DRAGON_BREATH)), pLevel.isClientSide()));
        } else {
            BlockHitResult blockhitresult = getPlayerPOVHitResult(pLevel, pPlayer, ClipContext.Fluid.SOURCE_ONLY);
            if (blockhitresult.getType() == HitResult.Type.MISS) {
                if (pPlayer.getY() > 192) {
                    itemstack.shrink(1);
                    pPlayer.getInventory().add(new ItemStack(CloudingForge.AIR_BOTTLE_ITEM.get()));
                } else {
                    itemstack.shrink(1);
                    pPlayer.getInventory().add(new ItemStack(CloudingForge.POISONOUS_BOTTLE_ITEM.get()));
                }
                cir.setReturnValue(InteractionResultHolder.pass(itemstack));
            } else {
                if (blockhitresult.getType() == HitResult.Type.BLOCK) {
                    BlockPos blockpos = blockhitresult.getBlockPos();
                    if (!pLevel.mayInteract(pPlayer, blockpos)) {
                        if (pPlayer.getY() > 192) {
                            itemstack.shrink(1);
                            pPlayer.getInventory().add(new ItemStack(CloudingForge.AIR_BOTTLE_ITEM.get()));
                        } else {
                            itemstack.shrink(1);
                            pPlayer.getInventory().add(new ItemStack(CloudingForge.POISONOUS_BOTTLE_ITEM.get()));
                        }
                        cir.setReturnValue(InteractionResultHolder.pass(itemstack));
                        return;
                    }

                    if (pLevel.getFluidState(blockpos).is(FluidTags.WATER)) {
                        pLevel.playSound(pPlayer, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.BOTTLE_FILL, SoundSource.NEUTRAL, 1.0F, 1.0F);
                        pLevel.gameEvent(pPlayer, GameEvent.FLUID_PICKUP, blockpos);
                        cir.setReturnValue(InteractionResultHolder.sidedSuccess(this.turnBottleIntoItem(itemstack, pPlayer, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)), pLevel.isClientSide()));
                        return;
                    }
                }

                cir.setReturnValue(InteractionResultHolder.pass(itemstack));
            }
        }
    }
}
