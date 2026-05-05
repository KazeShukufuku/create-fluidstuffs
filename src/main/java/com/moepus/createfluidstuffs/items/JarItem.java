package com.moepus.createfluidstuffs.items;

import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import java.util.List;

public class JarItem extends Item {
    public static final int capacity = 10;

    public JarItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        IFluidHandlerItem handler = stack.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler == null) {
            super.appendHoverText(stack, context, tooltip, flagIn);
            return;
        }
        FluidStack fluid = handler.getFluidInTank(0);
        if (fluid.isEmpty()) {
            tooltip.add(Component.translatable("createfluidstuffs.tooltips.empty").withStyle(ChatFormatting.GRAY));
        } else {
            CreateLang.fluidName(fluid).style(ChatFormatting.GOLD).addTo(tooltip);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Level level = pContext.getLevel();
        BlockPos pos = pContext.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return InteractionResult.PASS;
        IFluidHandler blockFluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, be, pContext.getClickedFace());
        if (blockFluidHandler == null) return InteractionResult.PASS;
        // 客户端只检查是否可以交互，返回SUCCESS阻止方块的creative handler接管；服务端做实际的流体转移
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack jar = pContext.getItemInHand();
        IFluidHandlerItem jarFluidHandler = jar.getCapability(Capabilities.FluidHandler.ITEM);
        if (jarFluidHandler == null)
            return InteractionResult.PASS;

        FluidStack jarFluid = jarFluidHandler.getFluidInTank(0);
        if (jarFluid.isEmpty()) {
            // 罐子是空的：从方块抽取流体
            FluidStack canDrain = blockFluidHandler.drain(capacity, IFluidHandler.FluidAction.SIMULATE);
            if (canDrain.isEmpty())
                return InteractionResult.PASS;

            int fillable = jarFluidHandler.fill(canDrain.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (fillable == 0)
                return InteractionResult.PASS;

            FluidStack toDrain = canDrain.copy();
            toDrain.setAmount(fillable);
            FluidStack drained = blockFluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            jarFluidHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        } else {
            // 罐子有流体：填入方块
            int canFillAmount = blockFluidHandler.fill(jarFluid, IFluidHandler.FluidAction.SIMULATE);
            if (canFillAmount == 0)
                return InteractionResult.PASS;

            FluidStack toDrain = jarFluid.copy();
            toDrain.setAmount(canFillAmount);
            FluidStack drained = jarFluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            blockFluidHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }

        pContext.getPlayer().setItemInHand(pContext.getHand(), jarFluidHandler.getContainer());
        // 直接向客户端发送slot更新包，绕过containerMenu系统，在创造模式下也有效
        if (pContext.getPlayer() instanceof net.minecraft.server.level.ServerPlayer sp) {
            int slotIndex = pContext.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                ? sp.getInventory().selected
                : 40; // offhand slot index in Inventory
            sp.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                -2, 0, slotIndex, jarFluidHandler.getContainer().copy()));
        }
        return InteractionResult.SUCCESS;
    }
}
