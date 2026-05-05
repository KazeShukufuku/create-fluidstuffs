package com.moepus.createfluidstuffs;

import com.moepus.createfluidstuffs.blocks.AllBlockEntityTypes;
import com.moepus.createfluidstuffs.blocks.AllBlocks;
import com.moepus.createfluidstuffs.items.AllItems;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import net.minecraft.network.chat.Component;

import static net.createmod.catnip.lang.LangBuilder.resolveBuilders;


// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(CreateFluidStuffs.ID)
public class CreateFluidStuffs {

    // Define mod id in a common place for everything to reference
    public static final String ID = "createfluidstuffs";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final ResourceKey<CreativeModeTab> BASE_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(ID, "base"));
    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(ID);

    public CreateFluidStuffs(IEventBus modEventBus) {
        IEventBus forgeEventBus = NeoForge.EVENT_BUS;

        REGISTRATE.defaultCreativeTab(BASE_TAB_KEY);
        REGISTRATE.registerEventListeners(modEventBus);
        AllItems.register();
        AllBlocks.register();
        AllBlockEntityTypes.register();
        AllCreativeModeTabs.register(modEventBus);
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static MutableComponent translateDirect(String key, Object... args) {
        return Component.translatable(ID + "." + key, resolveBuilders(args));
    }
}
