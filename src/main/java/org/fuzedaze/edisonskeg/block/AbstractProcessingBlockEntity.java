package org.fuzedaze.edisonskeg.block;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import org.fuzedaze.edisonskeg.recipe.ProcessingRecipe;

/**
 * Shared behaviour for every furnace-like machine: an inventory split into input and
 * output slots, a progress bar, and a tick loop that finds a matching
 * {@link ProcessingRecipe}, counts down, then swaps ingredients for the result.
 *
 * <p>A new machine subclasses this and supplies its {@link RecipeType}; the recipes
 * themselves are data. Nothing here is specific to brewing, so the same base serves a
 * fermenter, a still, or anything else that consumes ingredients over time.
 *
 * <p>Progress resets if the recipe stops matching — pull an ingredient out mid-way and the
 * machine starts over rather than finishing something it no longer has the parts for.
 */
public abstract class AbstractProcessingBlockEntity extends BlockEntity {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_PROGRESS = "Progress";

    protected final ItemStackHandler inventory;
    private final int inputSlots;
    private final int outputSlots;
    private final RecipeWrapper recipeView;
    private final LazyOptional<IItemHandler> itemHandler;

    private int progress;
    private int processingTime;

    protected AbstractProcessingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                            int inputSlots, int outputSlots) {
        super(type, pos, state);
        this.inputSlots = inputSlots;
        this.outputSlots = outputSlots;
        this.inventory = new ItemStackHandler(inputSlots + outputSlots) {
            @Override
            protected void onContentsChanged(int slot) {
                setChanged();
            }
        };
        this.recipeView = new RecipeWrapper(this.inventory);
        this.itemHandler = LazyOptional.of(() -> this.inventory);
    }

    /** Which family of recipes this machine runs. */
    protected abstract RecipeType<ProcessingRecipe> getRecipeType();

    public ItemStackHandler getInventory() {
        return this.inventory;
    }

    /** Progress towards the current recipe, 0 to 1, for progress bars. */
    public float getProgressFraction() {
        return this.processingTime <= 0 ? 0.0F : (float) this.progress / this.processingTime;
    }

    // ------------------------------------------------------------------ ticking

    /** Hook this up from the block's ticker; runs on the server only. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractProcessingBlockEntity machine) {
        machine.tick(level);
    }

    private void tick(Level level) {
        ProcessingRecipe recipe = level.getRecipeManager()
                .getRecipeFor(getRecipeType(), this.recipeView, level)
                .orElse(null);

        if (recipe == null || !resultFits(recipe, level)) {
            resetProgress();
            return;
        }

        this.processingTime = recipe.processingTime();
        this.progress++;

        if (this.progress >= this.processingTime) {
            complete(recipe, level);
            resetProgress();
        }
        setChanged();
    }

    private void resetProgress() {
        if (this.progress == 0)
            return;

        this.progress = 0;
        setChanged();
    }

    private void complete(ProcessingRecipe recipe, Level level) {
        recipe.consumeFrom(this.inventory);
        insertResult(recipe.assemble(this.recipeView, level.registryAccess()), false);
    }

    private boolean resultFits(ProcessingRecipe recipe, Level level) {
        return insertResult(recipe.assemble(this.recipeView, level.registryAccess()), true).isEmpty();
    }

    /** Pushes a stack into the output slots, returning whatever would not fit. */
    private ItemStack insertResult(ItemStack result, boolean simulate) {
        ItemStack remaining = result;
        for (int slot = this.inputSlots; slot < this.inputSlots + this.outputSlots && !remaining.isEmpty(); slot++)
            remaining = this.inventory.insertItem(slot, remaining, simulate);
        return remaining;
    }

    // ------------------------------------------------------------------ capabilities

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER)
            return this.itemHandler.cast();
        return super.getCapability(capability, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        this.itemHandler.invalidate();
    }

    // ------------------------------------------------------------------ persistence

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put(TAG_INVENTORY, this.inventory.serializeNBT());
        tag.putInt(TAG_PROGRESS, this.progress);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound(TAG_INVENTORY));
        this.progress = tag.getInt(TAG_PROGRESS);
    }
}
