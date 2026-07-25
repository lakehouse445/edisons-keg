package org.fuzedaze.edisonskeg.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.fuzedaze.edisonskeg.alcohol.CrateContents;
import org.fuzedaze.edisonskeg.registry.ModBlockEntities;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * Holds a crate's {@link CrateContents} and keeps clients in step, since the GeckoLib
 * renderer picks its model from what is inside. No animations are played.
 */
public class DrinkCrateBlockEntity extends BlockEntity implements GeoBlockEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private CrateContents contents = CrateContents.EMPTY;

    public DrinkCrateBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRINK_CRATE.get(), pos, state);
    }

    public CrateContents getContents() {
        return this.contents;
    }

    /** Server-side only; saves and pushes the change out to everyone watching. */
    public void setContents(CrateContents contents) {
        this.contents = contents;
        setChanged();

        if (this.level != null && !this.level.isClientSide)
            this.level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    // ------------------------------------------------------------------ persistence

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        this.contents.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.contents = CrateContents.load(tag);
    }

    // ------------------------------------------------------------------ client sync

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        // Never let this come out empty, or the packet carrying it is dropped and clients
        // keep showing whatever the crate held before it was emptied.
        this.contents.saveForSync(tag);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // ------------------------------------------------------------------ geckolib

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", state -> PlayState.STOP));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
