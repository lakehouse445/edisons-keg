package org.fuzedaze.edisonskeg.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.fuzedaze.edisonskeg.EdisonsKeg;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EdisonsKeg.MODID);

    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.edisonskeg.main"))
                    .icon(() -> new ItemStack(ModItems.BEER_CRATE.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BEER.get());
                        output.accept(ModItems.BEER_CRATE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
