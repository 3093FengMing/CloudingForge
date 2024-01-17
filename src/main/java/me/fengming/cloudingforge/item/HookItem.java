package me.fengming.cloudingforge.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HookItem extends Item {

    private Entity owner;

    public HookItem() {
        super(new Settings().maxCount(1));
    }

    public void setOwnerEntity(ItemStack itemStack, Entity ownerEntity) {
        this.owner = ownerEntity;
        NbtCompound nbt = new NbtCompound();
        nbt.putUuid("Owner", ownerEntity.getUuid());
        itemStack.setSubNbt("Data", nbt);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!user.getWorld().isClient) {
            if (user.fishHook == null) {
                itemStack.setCount(0);
            } else {
                owner.setVelocity(owner.getVelocity().subtract(user.getVelocity()).add(0.04, 0, 0.04));
            }
        }
        return super.use(world, user, hand);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (!context.getWorld().isClient) {
            PlayerEntity user = context.getPlayer();
            if (user != null && user.fishHook != null) {
                owner.setVelocity(owner.getVelocity().subtract(user.getVelocity()).add(0.04, 0, 0.04));
            } else {
                context.getStack().setCount(0);
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        if (!user.getWorld().isClient) {
            if (user.fishHook != null) {
                owner.setVelocity(owner.getVelocity().subtract(user.getVelocity()).subtract(0.04, 0, 0.04));
                user.fishHook.updateHookedEntityId(entity);
                if (entity instanceof PlayerEntity p) {
                    p.getInventory().insertStack(stack.copy());
                }
            }
            stack.setCount(0);
        }

        return super.useOnEntity(stack, user, entity, hand);
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        NbtCompound nbt = stack.getSubNbt("Data");
        if (world == null || nbt == null) return;
        PlayerEntity p = world.getPlayerByUuid(nbt.getUuid("Owner"));
        if (p == null) return;
        tooltip.add(Text.literal("钩子的主人：").append(p.getDisplayName()));
    }
}
