package me.fengming.cloudingforge.capabilities;

import me.fengming.cloudingforge.CloudingForge;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerOxygenCapabilityProvider implements NonNullSupplier<PlayerOxygenCapability>, ICapabilitySerializable<CompoundTag> {

    private final PlayerOxygenCapability capability;
    public PlayerOxygenCapabilityProvider(int initialOxygen) {
        this.capability = new PlayerOxygenCapability(initialOxygen);
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CloudingForge.OXYGEN_CAPABILITY ? LazyOptional.of(this).cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.capability.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.capability.deserializeNBT(nbt);
    }

    @Override
    public @NotNull PlayerOxygenCapability get() {
        return this.capability;
    }
}
