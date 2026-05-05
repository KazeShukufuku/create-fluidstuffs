package com.moepus.createfluidstuffs;

import com.moepus.createfluidstuffs.items.JarModel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = CreateFluidStuffs.ID)
public class ClientSetup
{
    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event)
    {
        event.register(CreateFluidStuffs.asResource("jar_model"), JarModel.Loader.INSTANCE);
    }
}
