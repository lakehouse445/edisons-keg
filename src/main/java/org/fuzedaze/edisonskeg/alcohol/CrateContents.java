package org.fuzedaze.edisonskeg.alcohol;

import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * What is inside a crate: a beverage and how many bottles of it, or nothing at all.
 *
 * <p>A crate is generic — it starts empty and takes on whichever {@link AlcoholType} is
 * first put into it, then only accepts more of that same drink until it runs dry and
 * becomes an empty crate again.
 *
 * <p>The same value is stored in two places, both under the vanilla {@code BlockEntityTag}
 * key so placing and breaking carry contents across for free:
 * <ul>
 *   <li>on the block entity, for the placed crate;</li>
 *   <li>on the item stack, so a crate in your hotbar shows what it's holding.</li>
 * </ul>
 *
 * <p>Instances are canonical: any state with no drink or no bottles is exactly
 * {@link #EMPTY}, so {@code isEmpty()} is the only emptiness check needed.
 */
public record CrateContents(@Nullable AlcoholType type, int bottles) {
    /** Bottles a crate holds when full. */
    public static final int CAPACITY = 12;

    public static final CrateContents EMPTY = new CrateContents(null, 0);

    /** Vanilla's key for block entity data carried on an item stack. */
    public static final String STACK_TAG = "BlockEntityTag";
    private static final String TAG_TYPE = "Contents";
    private static final String TAG_BOTTLES = "Bottles";

    public CrateContents {
        if (type == null || bottles <= 0) {
            type = null;
            bottles = 0;
        } else {
            bottles = Math.min(bottles, CAPACITY);
        }
    }

    public static CrateContents of(AlcoholType type, int bottles) {
        return new CrateContents(type, bottles);
    }

    /** A full crate of the given drink. */
    public static CrateContents full(AlcoholType type) {
        return new CrateContents(type, CAPACITY);
    }

    // ------------------------------------------------------------------ queries

    public boolean isEmpty() {
        return this.type == null;
    }

    public boolean isFull() {
        return this.bottles >= CAPACITY;
    }

    /** True if another bottle of this drink would fit: same drink, or the crate is empty. */
    public boolean accepts(AlcoholType drink) {
        return !isFull() && (isEmpty() || this.type == drink);
    }

    /** True if the crate holds a different drink than the one offered. */
    public boolean holdsSomethingElse(AlcoholType drink) {
        return !isEmpty() && this.type != drink;
    }

    // ------------------------------------------------------------------ changes

    /** This crate plus one bottle. Only valid when {@link #accepts(AlcoholType)} is true. */
    public CrateContents plusOne(AlcoholType drink) {
        return new CrateContents(drink, isEmpty() ? 1 : this.bottles + 1);
    }

    /** This crate minus one bottle, becoming empty when the last one leaves. */
    public CrateContents minusOne() {
        return isEmpty() ? EMPTY : new CrateContents(this.type, this.bottles - 1);
    }

    // ------------------------------------------------------------------ persistence

    /** Writes into the given tag; an empty crate writes nothing at all. */
    public void save(CompoundTag tag) {
        if (isEmpty())
            return;

        tag.putString(TAG_TYPE, this.type.id());
        tag.putInt(TAG_BOTTLES, this.bottles);
    }

    /**
     * Like {@link #save}, but always writes the bottle count so the tag is never empty.
     *
     * <p>Use this for client sync only. {@code ClientboundBlockEntityDataPacket} replaces an
     * empty tag with null, and the client skips null tags — so an emptied crate sending
     * "nothing" would leave every client still rendering the last bottle it knew about.
     */
    public void saveForSync(CompoundTag tag) {
        save(tag);
        tag.putInt(TAG_BOTTLES, this.bottles);
    }

    public static CrateContents load(@Nullable CompoundTag tag) {
        if (tag == null || !tag.contains(TAG_TYPE))
            return EMPTY;

        // Resolves to empty if the drink was removed from the mod since this was saved.
        AlcoholType type = AlcoholTypes.byId(tag.getString(TAG_TYPE));
        return type == null ? EMPTY : new CrateContents(type, tag.getInt(TAG_BOTTLES));
    }

    /** Reads the contents a crate item stack is carrying. */
    public static CrateContents fromStack(ItemStack stack) {
        return load(stack.getTagElement(STACK_TAG));
    }

    /** Builds a crate item stack carrying these contents. */
    public ItemStack toStack(ItemLike crateItem) {
        ItemStack stack = new ItemStack(crateItem);
        writeToStack(stack);
        return stack;
    }

    /** Stamps these contents onto an existing crate stack. */
    public void writeToStack(ItemStack stack) {
        if (isEmpty()) {
            stack.removeTagKey(STACK_TAG);
            return;
        }

        CompoundTag tag = new CompoundTag();
        save(tag);
        stack.addTagElement(STACK_TAG, tag);
    }

    /** Convenience for the common "one crate item" case. */
    public ItemStack toStack(Item crateItem) {
        return toStack((ItemLike) crateItem);
    }
}
