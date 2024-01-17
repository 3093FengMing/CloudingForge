package me.fengming.cloudingforge.block;

import me.fengming.cloudingforge.Utils;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class CloudBlock extends AbstractGlassBlock {
    public CloudBlock() {
        super(BlockBehaviour.Properties.of().strength(-1.0F).sound(SoundType.WOOL).noOcclusion().isViewBlocking(Utils.never()));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }


}
