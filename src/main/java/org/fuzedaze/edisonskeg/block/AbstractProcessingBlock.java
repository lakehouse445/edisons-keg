package org.fuzedaze.edisonskeg.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

/**
 * Shared behaviour for every furnace-like machine block: it ticks its
 * {@link AbstractProcessingBlockEntity} on the server and spills the contents when broken.
 *
 * <p>Subclasses supply their own block entity type and, if they want one, a menu. Render
 * shape defaults back to {@code MODEL} so machines can use ordinary block models —
 * override it if a machine is drawn by GeckoLib instead.
 */
public abstract class AbstractProcessingBlock extends BaseEntityBlock {

    protected AbstractProcessingBlock(Properties properties) {
        super(properties);
    }

    /** The block entity type to tick; used to match the ticker to this block. */
    protected abstract BlockEntityType<? extends AbstractProcessingBlockEntity> getBlockEntityType();

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        // Processing is server-side only; clients just see the results.
        return level.isClientSide ? null
                : createTickerHelper(type, getBlockEntityType(), AbstractProcessingBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof AbstractProcessingBlockEntity machine) {
            dropContents(level, pos, machine.getInventory());

            // A latched batch owns items the inventory no longer holds: refund a
            // mid-ferment batch's ingredients, or drop the drink if the time was up.
            for (ItemStack stack : machine.dumpBatch())
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }

        super.onRemove(state, level, pos, newState, moved);
    }

    private static void dropContents(Level level, BlockPos pos, ItemStackHandler inventory) {
        for (int slot = 0; slot < inventory.getSlots(); slot++)
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(slot));
    }
}
