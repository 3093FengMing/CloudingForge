package me.fengming.cloudingforge.item;

import me.fengming.cloudingforge.Utils;
import me.fengming.cloudingforge.capabilities.PlayerOxygenCapability;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;

public class AirBottleItem extends Item {
    public AirBottleItem() {
        super(new Properties().stacksTo(64));
    }

    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 8;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity) {
        if (!pLevel.isClientSide) {
            if (pLivingEntity instanceof Player p) {
                PlayerOxygenCapability cap = Utils.getOxygenCapability(p);
                cap.addOxygen(-20);
                p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
                pStack.shrink(1);
            }
        }
        return pStack;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        return ItemUtils.startUsingInstantly(pLevel, pPlayer, pUsedHand);
    }
}
