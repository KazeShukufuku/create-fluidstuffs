package com.moepus.createfluidstuffs;

import com.moepus.createfluidstuffs.items.BucketItem;
import com.moepus.createfluidstuffs.items.JarItem;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

@EventBusSubscriber(modid = CreateFluidStuffs.ID, bus = EventBusSubscriber.Bus.GAME)
public class FluidInteractionEvents {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getItemStack().getItem() instanceof BucketItem) &&
            !(event.getItemStack().getItem() instanceof JarItem))
            return;

        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        BlockEntity be = level.getBlockEntity(event.getPos());
        if (be == null) return;

        Direction face = event.getFace() != null ? event.getFace() : Direction.UP;
        IFluidHandler blockFluidHandler = level.getCapability(
            Capabilities.FluidHandler.BLOCK, event.getPos(), state, be, face);
        if (blockFluidHandler == null) return;

        // 阻止任何方块的 useItemOn()（包括 Create 等 mod 在创造模式下的处理逻辑）
        // 让物品自己的 useOn() 完全接管流体转移，对所有 mod 的流体容器均有效
        event.setUseBlock(TriState.FALSE);
    }
}
