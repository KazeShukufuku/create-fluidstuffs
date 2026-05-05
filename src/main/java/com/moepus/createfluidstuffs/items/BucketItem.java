package com.moepus.createfluidstuffs.items;

import com.simibubi.create.foundation.utility.CreateLang;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

public class BucketItem extends Item {
    public static final int capacity = 2000;

    public BucketItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    @OnlyIn(value = Dist.CLIENT)
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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
            tooltip.add(Component.literal(Integer.toString(fluid.getAmount()) + "mB").withStyle(ChatFormatting.WHITE));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        net.minecraft.world.level.Level level = pContext.getLevel();
        net.minecraft.core.BlockPos pos = pContext.getClickedPos();
        net.minecraft.world.level.block.state.BlockState state = level.getBlockState(pos);
        net.minecraft.world.level.block.entity.BlockEntity be = level.getBlockEntity(pos);
        if (be == null) return InteractionResult.PASS;
        IFluidHandler blockFluidHandler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, be, pContext.getClickedFace());
        if (blockFluidHandler == null) return InteractionResult.PASS;
        // 客户端只检查是否可以交互，返回SUCCESS阻止方块的creative handler接管；服务端做实际的流体转移
        if (level.isClientSide) return InteractionResult.SUCCESS;

        ItemStack bucket = pContext.getItemInHand();
        IFluidHandlerItem bucketFluidHandler = bucket.getCapability(Capabilities.FluidHandler.ITEM);
        if (bucketFluidHandler == null)
            return InteractionResult.PASS;

        FluidStack bucketFluid = bucketFluidHandler.getFluidInTank(0);
        if (bucketFluid.isEmpty()) {
            // 桶是空的：从方块抽取流体
            FluidStack canDrain = blockFluidHandler.drain(capacity, IFluidHandler.FluidAction.SIMULATE);
            if (canDrain.isEmpty())
                return InteractionResult.PASS;

            // 先模拟确认桶能接受该流体
            int fillable = bucketFluidHandler.fill(canDrain.copy(), IFluidHandler.FluidAction.SIMULATE);
            if (fillable == 0)
                return InteractionResult.PASS;

            FluidStack toDrain = canDrain.copy();
            toDrain.setAmount(fillable);
            FluidStack drained = blockFluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            bucketFluidHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        } else {
            // 桶有流体：填入方块
            int canFillAmount = blockFluidHandler.fill(bucketFluid, IFluidHandler.FluidAction.SIMULATE);
            if(canFillAmount == 0)
                return InteractionResult.PASS;

            FluidStack toDrain = bucketFluid.copy();
            toDrain.setAmount(canFillAmount);
            FluidStack drained = bucketFluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE);
            blockFluidHandler.fill(drained, IFluidHandler.FluidAction.EXECUTE);
        }

        pContext.getPlayer().setItemInHand(pContext.getHand(), bucketFluidHandler.getContainer());
        // 直接向客户端发送slot更新包，绕过containerMenu系统，在创造模式下也有效
        if (pContext.getPlayer() instanceof net.minecraft.server.level.ServerPlayer sp) {
            int slotIndex = pContext.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                ? sp.getInventory().selected
                : 40; // offhand slot index in Inventory
            sp.connection.send(new net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket(
                -2, 0, slotIndex, bucketFluidHandler.getContainer().copy()));
        }
        return InteractionResult.SUCCESS;
    }
}
