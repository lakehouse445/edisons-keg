package org.fuzedaze.edisonskeg.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;

/**
 * Reads {@link ProcessingRecipe}s from JSON and syncs them to clients. One instance per
 * machine, each bound to that machine's {@link RecipeType}, so every machine gets its own
 * recipe folder without any bespoke code.
 *
 * <pre>{@code
 * {
 *   "type": "edisonskeg:fermenting",
 *   "ingredients": [ { "item": "minecraft:wheat", "count": 3 } ],
 *   "result": { "item": "edisonskeg:beer" },
 *   "time": 400
 * }
 * }</pre>
 */
public class ProcessingRecipeSerializer implements RecipeSerializer<ProcessingRecipe> {
    /** Default ticks a recipe takes when it doesn't say. */
    public static final int DEFAULT_TIME = 200;

    private final Supplier<RecipeType<?>> type;

    /** Takes a supplier because the type and serializer are registered side by side. */
    public ProcessingRecipeSerializer(Supplier<RecipeType<?>> type) {
        this.type = type;
    }

    @Override
    public ProcessingRecipe fromJson(ResourceLocation id, JsonObject json) {
        List<CountedIngredient> inputs = new ArrayList<>();
        for (JsonElement element : GsonHelper.getAsJsonArray(json, "ingredients"))
            inputs.add(CountedIngredient.fromJson(element));

        if (inputs.isEmpty())
            throw new JsonParseException("Recipe " + id + " has no ingredients");

        ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
        int time = GsonHelper.getAsInt(json, "time", DEFAULT_TIME);
        return new ProcessingRecipe(id, this.type.get(), this, inputs, result, time);
    }

    @Override
    public ProcessingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        int inputCount = buffer.readVarInt();
        List<CountedIngredient> inputs = new ArrayList<>(inputCount);
        for (int i = 0; i < inputCount; i++)
            inputs.add(CountedIngredient.fromNetwork(buffer));

        ItemStack result = buffer.readItem();
        int time = buffer.readVarInt();
        return new ProcessingRecipe(id, this.type.get(), this, inputs, result, time);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ProcessingRecipe recipe) {
        buffer.writeVarInt(recipe.inputs().size());
        for (CountedIngredient input : recipe.inputs())
            input.toNetwork(buffer);

        buffer.writeItem(recipe.getResultItem(null));
        buffer.writeVarInt(recipe.processingTime());
    }
}
