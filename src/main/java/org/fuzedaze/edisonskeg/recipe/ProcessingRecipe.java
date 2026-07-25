package org.fuzedaze.edisonskeg.recipe;

import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandlerModifiable;

/**
 * One "put these ingredients in, wait, get this out" recipe — the shape every furnace-like
 * machine in this mod uses.
 *
 * <p>The class is shared by every machine; what separates a fermenter recipe from a still
 * recipe is only the {@link RecipeType} it was registered under. That means adding a
 * machine needs no new recipe or serializer class — see
 * {@link org.fuzedaze.edisonskeg.registry.ModRecipes#registerKind(String)}.
 */
public class ProcessingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final RecipeType<?> type;
    private final RecipeSerializer<?> serializer;
    private final List<CountedIngredient> inputs;
    private final ItemStack result;
    private final int processingTime;

    public ProcessingRecipe(ResourceLocation id, RecipeType<?> type, RecipeSerializer<?> serializer,
                            List<CountedIngredient> inputs, ItemStack result, int processingTime) {
        this.id = id;
        this.type = type;
        this.serializer = serializer;
        this.inputs = List.copyOf(inputs);
        this.result = result;
        this.processingTime = processingTime;
    }

    public List<CountedIngredient> inputs() {
        return this.inputs;
    }

    /** Ticks this recipe takes to finish. */
    public int processingTime() {
        return this.processingTime;
    }

    // ------------------------------------------------------------------ matching

    @Override
    public boolean matches(Container container, Level level) {
        boolean[] claimed = new boolean[container.getContainerSize()];

        for (CountedIngredient input : this.inputs) {
            if (!claimSlotFor(input, container, claimed))
                return false;
        }
        return true;
    }

    /** Finds an unclaimed slot satisfying this ingredient, marking it used if found. */
    private static boolean claimSlotFor(CountedIngredient input, Container container, boolean[] claimed) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (claimed[slot] || !input.test(container.getItem(slot)))
                continue;

            claimed[slot] = true;
            return true;
        }
        return false;
    }

    /**
     * Removes this recipe's ingredients from the given inventory. Assumes
     * {@link #matches} already passed for it.
     */
    public void consumeFrom(IItemHandlerModifiable inventory) {
        for (CountedIngredient input : this.inputs) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!input.test(stack))
                    continue;

                stack.shrink(input.count());
                inventory.setStackInSlot(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
                break;
            }
        }
    }

    // ------------------------------------------------------------------ recipe contract

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return this.result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return this.result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (CountedIngredient input : this.inputs)
            ingredients.add(input.ingredient());
        return ingredients;
    }

    @Override
    public ResourceLocation getId() {
        return this.id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return this.serializer;
    }

    @Override
    public RecipeType<?> getType() {
        return this.type;
    }
}
