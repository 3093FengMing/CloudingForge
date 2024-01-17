package me.fengming.cloudingforge.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HookItem extends Item {

    private Entity owner;

    public HookItem() {
        super(new Properties().stacksTo(1));
    }

    public void setOwnerEntity(ItemStack itemStack, Entity ownerEntity) {
        this.owner = ownerEntity;
        CompoundTag nbt = new CompoundTag();
        nbt.putUUID("Owner", ownerEntity.getUUID());
        itemStack.setTag(nbt);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemStack = pPlayer.getItemInHand(pUsedHand);
        if (!pLevel.isClientSide) {
            if (pPlayer.fishing == null) {
                itemStack.setCount(0);
            } else {
                owner.setDeltaMovement(owner.getDeltaMovement().subtract(pPlayer.getDeltaMovement()).add(0.04, 0, 0.04));
            }
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        if (!pContext.getLevel().isClientSide) {
            Player user = pContext.getPlayer();
            if (user != null && user.fishing != null) {
                owner.setDeltaMovement(owner.getDeltaMovement().subtract(user.getDeltaMovement()).add(0.04, 0, 0.04));
            }
        }

        return super.useOn(pContext);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack pStack, Player pPlayer, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (pPlayer.level().isClientSide) {
            if (pPlayer.fishing != null) {
                owner.setDeltaMovement(owner.getDeltaMovement().subtract(pPlayer.getDeltaMovement()).add(0.04, 0, 0.04));
                pPlayer.fishing.setHookedEntity(pInteractionTarget);
                if (pInteractionTarget instanceof Player p) {
                    p.getInventory().add(pStack.copy());
                }
            }
            pStack.setCount(0);
        }
        return super.interactLivingEntity(pStack, pPlayer, pInteractionTarget, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltipComponents, TooltipFlag pIsAdvanced) {
        CompoundTag nbt = pStack.getTag();
        if (pLevel == null || nbt == null) return;
        Player p = pLevel.getPlayerByUUID(nbt.getUUID("Owner"));
        if (p != null) pTooltipComponents.add(Component.literal("钩子的主人：").append(p.getDisplayName()));
    }

}
