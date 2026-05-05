/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package com.moepus.createfluidstuffs.items;

import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.fluids.*;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class JarFluidHandler implements IFluidHandlerItem
{
    public static final String FLUID_NBT_KEY = "Fluid";
    public static final String UUID_NBT_KEY = "UUID";

    @NotNull
    protected ItemStack container;
    protected int capacity;

    /**
     * @param container  The container itemStack, data is stored on it directly as NBT.
     * @param capacity   The maximum capacity of this fluid tank.
     */
    public JarFluidHandler(@NotNull ItemStack container, int capacity)
    {
        this.container = container;
        this.capacity = capacity;
    }

    @NotNull
    @Override
    public ItemStack getContainer()
    {
        return container;
    }

    @NotNull
    public FluidStack getFluid()
    {
        CustomData customData = container.get(DataComponents.CUSTOM_DATA);
        if (customData == null || !customData.getUnsafe().contains(FLUID_NBT_KEY))
        {
            return FluidStack.EMPTY;
        }
        return FluidStack.CODEC.parse(NbtOps.INSTANCE, customData.getUnsafe().getCompound(FLUID_NBT_KEY))
                .result().orElse(FluidStack.EMPTY);
    }

    protected void setFluid(FluidStack fluid)
    {
        Tag fluidTag = FluidStack.CODEC.encodeStart(NbtOps.INSTANCE, fluid).result().orElse(new CompoundTag());
        CompoundTag tag = container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.put(FLUID_NBT_KEY, fluidTag);
        tag.putUUID(UUID_NBT_KEY, UUID.randomUUID());
        container.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    @Override
    public int getTanks() {

        return 1;
    }

    @NotNull
    @Override
    public FluidStack getFluidInTank(int tank) {

        return getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {

        return capacity;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {

        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction doFill)
    {
        if (resource.isEmpty() || !canFillFluidType(resource))
        {
            return 0;
        }

        FluidStack contained = getFluid();
        if (contained.isEmpty())
        {
            int fillAmount = Math.min(resource.getAmount(), capacity);
            if (doFill.execute())
            {
                FluidStack filled = resource.copy();
                filled.setAmount(fillAmount);
                setFluid(filled);
            }
            return fillAmount;
        }
        return 0;
    }

    @NotNull
    @Override
    public FluidStack drain(FluidStack resource, FluidAction action)
    {
        if (resource.isEmpty() || !FluidStack.isSameFluid(resource, getFluid()))
        {
            return FluidStack.EMPTY;
        }
        return drain(resource.getAmount(), action);
    }

    @NotNull
    @Override
    public FluidStack drain(int maxDrain, FluidAction action)
    {
        if (maxDrain <= 0)
        {
            return FluidStack.EMPTY;
        }

        FluidStack contained = getFluid();
        if (contained.isEmpty() || !canDrainFluidType(contained))
        {
            return FluidStack.EMPTY;
        }

        int drainAmount = Math.min(maxDrain, contained.getAmount());
        FluidStack drained = contained.copy();
        drained.setAmount(drainAmount);

        if (action.execute())
        {
            int remaining = contained.getAmount() - drainAmount;
            if (remaining <= 0)
            {
                setContainerToEmpty();
            }
            else
            {
                FluidStack newContained = contained.copy();
                newContained.setAmount(remaining);
                setFluid(newContained);
            }
        }

        return drained;
    }

    public boolean canFillFluidType(FluidStack fluid)
    {
        return true;
    }

    public boolean canDrainFluidType(FluidStack fluid)
    {
        return true;
    }

    /**
     * Override this method for special handling.
     * Can be used to swap out or destroy the container.
     */
    protected void setContainerToEmpty()
    {
        CompoundTag tag = container.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        tag.remove(FLUID_NBT_KEY);
        tag.remove(UUID_NBT_KEY);
        if (tag.isEmpty()) {
            container.remove(DataComponents.CUSTOM_DATA);
        } else {
            container.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        }
    }
}
