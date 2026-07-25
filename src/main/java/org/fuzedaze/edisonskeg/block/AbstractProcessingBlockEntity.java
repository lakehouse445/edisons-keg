package org.fuzedaze.edisonskeg.block;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
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
 * Shared behaviour for every fermenter-like machine: an inventory split into input and
 * output slots, and a batch that runs on the <em>world clock</em> rather than a tick
 * counter.
 *
 * <p>When the inputs match a {@link ProcessingRecipe} the machine <em>latches</em> the
 * batch: it consumes the ingredients up front, remembers what will come out and when, and
 * then simply waits. Because the finish line is a game-time stamp and not an accumulated
 * counter, a batch keeps "fermenting" while the chunk is unloaded or the player is logged
 * off — walk away for the night and it is done when you come back. This is the whole point
 * of a slow mod: the waiting must never be babysitting.
 *
 * <p>Two guarantees follow from latching:
 * <ul>
 *   <li><b>Progress never resets.</b> The ingredients are already consumed, so nothing —
 *       hopper hiccups, pulled items, restarts — can un-match a running batch.</li>
 *   <li><b>A finished batch is never voided.</b> If the output slots are full when the
 *       time is up, the result is held and re-offered every tick until it fits.</li>
 * </ul>
 *
 * <p>A new machine subclasses this and supplies its {@link RecipeType}; the recipes
 * themselves are data. Nothing here is specific to brewing, so the same base serves a
 * fermenter, a still, or anything else that consumes ingredients over time.
 */
public abstract class AbstractProcessingBlockEntity extends BlockEntity {
    private static final String TAG_INVENTORY = "Inventory";
    private static final String TAG_BATCH = "Batch";
    private static final String TAG_RESULT = "Result";
    private static final String TAG_STARTED_AT = "StartedAt";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_REFUND = "Refund";

    protected final ItemStackHandler inventory;
    private final int inputSlots;
    private final int outputSlots;
    private final RecipeWrapper recipeView;
    private final LazyOptional<IItemHandler> itemHandler;

    /**
     * The batch currently fermenting (or waiting for output room). Empty means idle. The
     * result is captured at latch time, so datapack edits mid-batch cannot break or
     * change what was already sealed in.
     */
    private ItemStack pendingResult = ItemStack.EMPTY;
    /** Game time the batch was latched; meaningless while {@link #pendingResult} is empty. */
    private long startedAt;
    /** Ticks of world time the batch needs; meaningless while {@link #pendingResult} is empty. */
    private int duration;
    /** What the batch consumed, kept so breaking the machine mid-ferment refunds it. */
    private List<ItemStack> batchRefund = List.of();

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

    // ------------------------------------------------------------------ batch queries

    /** True while a batch is fermenting or waiting to be delivered. */
    public boolean hasBatch() {
        return !this.pendingResult.isEmpty();
    }

    /** True once the current batch's time is up (it may still be waiting for output room). */
    public boolean isBatchReady() {
        return hasBatch() && this.level != null && elapsed(this.level) >= this.duration;
    }

    /** Progress of the current batch, 0 to 1. A ready-but-stuck batch reports 1. */
    public float getProgressFraction() {
        if (!hasBatch() || this.level == null || this.duration <= 0)
            return hasBatch() ? 1.0F : 0.0F;
        return Math.min(1.0F, (float) elapsed(this.level) / this.duration);
    }

    /** World ticks remaining until the current batch is done; 0 when idle or ready. */
    public long getTicksRemaining() {
        if (!hasBatch() || this.level == null)
            return 0L;
        return Math.max(0L, this.duration - elapsed(this.level));
    }

    private long elapsed(Level level) {
        // Game time is monotonic in vanilla, but never trust a clock you didn't wind.
        return Math.max(0L, level.getGameTime() - this.startedAt);
    }

    // ------------------------------------------------------------------ ticking

    /** Hook this up from the block's ticker; runs on the server only. */
    public static void serverTick(Level level, BlockPos pos, BlockState state, AbstractProcessingBlockEntity machine) {
        machine.tick(level);
    }

    private void tick(Level level) {
        if (hasBatch()) {
            if (elapsed(level) >= this.duration)
                deliver();
            return;
        }

        ProcessingRecipe recipe = level.getRecipeManager()
                .getRecipeFor(getRecipeType(), this.recipeView, level)
                .orElse(null);
        if (recipe != null)
            latch(recipe, level);
    }

    /**
     * Seals a batch in: capture the result, stamp the clock, and eat the ingredients.
     * From here on the batch depends on nothing but time.
     */
    private void latch(ProcessingRecipe recipe, Level level) {
        this.pendingResult = recipe.assemble(this.recipeView, level.registryAccess());
        this.duration = recipe.processingTime();
        this.startedAt = level.getGameTime();
        this.batchRefund = recipe.consumeFrom(this.inventory);
        setChanged();
    }

    /**
     * Ends the current batch and hands back what it should leave behind: the finished
     * drink if the time was up, otherwise the ingredients that were sealed in. Called by
     * the block when it is broken, so a fermenting machine never destroys anything.
     */
    public List<ItemStack> dumpBatch() {
        if (!hasBatch())
            return List.of();

        List<ItemStack> drops = isBatchReady() ? List.of(this.pendingResult) : this.batchRefund;
        this.pendingResult = ItemStack.EMPTY;
        this.batchRefund = List.of();
        setChanged();
        return drops;
    }

    /**
     * Pushes the finished batch into the output slots. Whatever does not fit stays
     * pending and is offered again next tick — a finished batch is never thrown away.
     */
    private void deliver() {
        ItemStack remaining = insertResult(this.pendingResult);
        if (ItemStack.matches(remaining, this.pendingResult))
            return; // No room at all; try again next tick without dirtying the chunk.

        this.pendingResult = remaining;
        if (remaining.isEmpty())
            this.batchRefund = List.of();
        setChanged();
    }

    /** Pushes a stack into the output slots, returning whatever would not fit. */
    private ItemStack insertResult(ItemStack result) {
        ItemStack remaining = result;
        for (int slot = this.inputSlots; slot < this.inputSlots + this.outputSlots && !remaining.isEmpty(); slot++)
            remaining = this.inventory.insertItem(slot, remaining, false);
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

        if (!hasBatch())
            return;

        CompoundTag batch = new CompoundTag();
        batch.put(TAG_RESULT, this.pendingResult.save(new CompoundTag()));
        batch.putLong(TAG_STARTED_AT, this.startedAt);
        batch.putInt(TAG_DURATION, this.duration);

        ListTag refund = new ListTag();
        for (ItemStack stack : this.batchRefund)
            refund.add(stack.save(new CompoundTag()));
        batch.put(TAG_REFUND, refund);

        tag.put(TAG_BATCH, batch);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.inventory.deserializeNBT(tag.getCompound(TAG_INVENTORY));

        if (tag.contains(TAG_BATCH)) {
            CompoundTag batch = tag.getCompound(TAG_BATCH);
            this.pendingResult = ItemStack.of(batch.getCompound(TAG_RESULT));
            this.startedAt = batch.getLong(TAG_STARTED_AT);
            this.duration = batch.getInt(TAG_DURATION);

            List<ItemStack> refund = new ArrayList<>();
            for (Tag entry : batch.getList(TAG_REFUND, Tag.TAG_COMPOUND))
                refund.add(ItemStack.of((CompoundTag) entry));
            this.batchRefund = refund;
        } else {
            this.pendingResult = ItemStack.EMPTY;
            this.batchRefund = List.of();
        }
    }
}
