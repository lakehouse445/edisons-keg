package org.fuzedaze.edisonskeg.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * An {@link Ingredient} plus how many of it a recipe needs, which vanilla ingredients
 * cannot express on their own. Written in JSON exactly like a normal ingredient, with an
 * optional {@code count}:
 *
 * <pre>{@code
 * { "item": "minecraft:wheat", "count": 3 }
 * { "tag": "minecraft:flowers" }
 * }</pre>
 */
public record CountedIngredient(Ingredient ingredient, int count) {

    public static CountedIngredient fromJson(JsonElement json) {
        int count = 1;
        if (json.isJsonObject()) {
            JsonObject object = json.getAsJsonObject();
            if (object.has("count"))
                count = GsonHelper.getAsInt(object, "count");
        }
        return new CountedIngredient(Ingredient.fromJson(json), Math.max(1, count));
    }

    /** True if this stack satisfies the ingredient <em>and</em> has enough of it. */
    public boolean test(ItemStack stack) {
        return this.ingredient.test(stack) && stack.getCount() >= this.count;
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        this.ingredient.toNetwork(buffer);
        buffer.writeVarInt(this.count);
    }

    public static CountedIngredient fromNetwork(FriendlyByteBuf buffer) {
        return new CountedIngredient(Ingredient.fromNetwork(buffer), buffer.readVarInt());
    }
}
