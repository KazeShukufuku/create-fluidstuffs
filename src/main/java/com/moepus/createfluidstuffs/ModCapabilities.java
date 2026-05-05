package com.moepus.createfluidstuffs;

import com.moepus.createfluidstuffs.blocks.AllBlockEntityTypes;
import com.moepus.createfluidstuffs.items.AllItems;
import com.moepus.createfluidstuffs.items.BucketItem;
import com.moepus.createfluidstuffs.items.JarFluidHandler;
import com.moepus.createfluidstuffs.items.JarItem;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

@EventBusSubscriber(modid = CreateFluidStuffs.ID, bus = EventBusSubscriber.Bus.MOD)
public class ModCapabilities {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                AllBlockEntityTypes.MULTI_FLUID_TANK.get(),
                (be, side) -> be.getFluidHandler(side));

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new JarFluidHandler(stack, JarItem.capacity),
                AllItems.JAR.get());

        event.registerItem(
                Capabilities.FluidHandler.ITEM,
                (stack, ctx) -> new JarFluidHandler(stack, BucketItem.capacity),
                AllItems.Bucket.get());
    }
}
