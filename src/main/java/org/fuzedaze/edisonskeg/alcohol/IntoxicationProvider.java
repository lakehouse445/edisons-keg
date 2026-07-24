package org.fuzedaze.edisonskeg.alcohol;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.fuzedaze.edisonskeg.EdisonsKeg;

public class IntoxicationProvider implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<Intoxication> INTOXICATION = CapabilityManager.get(new CapabilityToken<>() {
    });
    public static final ResourceLocation ID = new ResourceLocation(EdisonsKeg.MODID, "intoxication");

    private final Intoxication intoxication = new Intoxication();
    private final LazyOptional<Intoxication> optional = LazyOptional.of(() -> this.intoxication);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return cap == INTOXICATION ? this.optional.cast() : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return this.intoxication.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        this.intoxication.deserializeNBT(tag);
    }
}
