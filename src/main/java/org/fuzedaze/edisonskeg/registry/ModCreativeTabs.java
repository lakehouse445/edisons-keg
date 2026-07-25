package org.fuzedaze.edisonskeg.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.fuzedaze.edisonskeg.EdisonsKeg;
import org.fuzedaze.edisonskeg.alcohol.AlcoholType;
import org.fuzedaze.edisonskeg.alcohol.AlcoholTypes;
import org.fuzedaze.edisonskeg.alcohol.CrateContents;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EdisonsKeg.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.edisonskeg.main"))
                    .icon(() -> new ItemStack(ModItems.DRINK_CRATE.get()))
                    .displayItems((parameters, output) -> {
                        for (AlcoholType type : AlcoholTypes.all())
                            output.accept(new ItemStack(type.drinkItem()));

                        // The empty crate, then one full crate per drink for convenience.
                        // Partly-filled crates are a state you reach by drinking, not an entry here.
                        output.accept(new ItemStack(ModItems.DRINK_CRATE.get()));
                        for (AlcoholType type : AlcoholTypes.all())
                            output.accept(CrateContents.full(type).toStack(ModItems.DRINK_CRATE.get()));
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
