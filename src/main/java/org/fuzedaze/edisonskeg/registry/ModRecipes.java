package org.fuzedaze.edisonskeg.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.fuzedaze.edisonskeg.EdisonsKeg;
import org.fuzedaze.edisonskeg.recipe.ProcessingRecipe;
import org.fuzedaze.edisonskeg.recipe.ProcessingRecipeSerializer;

/**
 * Recipe families for the furnace-like machines.
 *
 * <p>Each machine gets a "kind" — a {@link RecipeType} plus a matching serializer — from a
 * single {@link #registerKind(String)} call. Adding a machine is then:
 *
 * <pre>{@code
 * public static final ProcessingKind FERMENTING = registerKind("fermenting");
 * }</pre>
 *
 * with its recipes living in {@code data/edisonskeg/recipes/} under
 * {@code "type": "edisonskeg:fermenting"}. No new Java classes required.
 */
public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, EdisonsKeg.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, EdisonsKeg.MODID);

    /** A machine's recipe family: the type recipes are looked up by, and its serializer. */
    public record ProcessingKind(RegistryObject<RecipeType<ProcessingRecipe>> typeHolder,
                                 RegistryObject<RecipeSerializer<ProcessingRecipe>> serializerHolder) {
        public RecipeType<ProcessingRecipe> type() {
            return this.typeHolder.get();
        }
    }

    /** Registers a recipe type and serializer under the same name. */
    public static ProcessingKind registerKind(String name) {
        ResourceLocation id = new ResourceLocation(EdisonsKeg.MODID, name);
        RegistryObject<RecipeType<ProcessingRecipe>> type =
                TYPES.register(name, () -> RecipeType.simple(id));
        RegistryObject<RecipeSerializer<ProcessingRecipe>> serializer =
                SERIALIZERS.register(name, () -> new ProcessingRecipeSerializer(type::get));
        return new ProcessingKind(type, serializer);
    }

    private ModRecipes() {
    }
}
