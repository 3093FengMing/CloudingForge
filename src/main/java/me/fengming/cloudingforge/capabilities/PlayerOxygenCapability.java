package me.fengming.cloudingforge.capabilities;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public class PlayerOxygenCapability implements INBTSerializable<CompoundTag> {
    private int oxygen;
    public PlayerOxygenCapability(int oxygen) {
        this.oxygen = oxygen;
    }
    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("oxygen", oxygen);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        oxygen = nbt.getInt("oxygen");
    }

    public int getOxygen() {
        return oxygen;
    }

    public void addOxygen(int oxygen) {
        int i = this.oxygen + oxygen;
        this.oxygen = (i < 0 ? 0 : (Math.min(i, 120)));
    }
}
