package us.drullk.vegetablecarnival.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;

import javax.annotation.Nullable;

public class Common {
    public static int getDiameterFromRadiusPlusCenter(int radius)
    {
        return (radius*2)+1;
    }

    public static boolean isCoordOutOfNoZone(int posX, int posY, int noX, int noY)
    {
        return posX<-noX||posY<-noY||posX>noX||posY>noY;
    }

    public static EnumFacing.Axis[] getInterceptingAxes(EnumFacing.Axis axis){
        EnumFacing.Axis[] interceptingAxes = new EnumFacing.Axis[2];
        int j = 0;
        for (EnumFacing.Axis axisCheck : EnumFacing.Axis.values()) // Assign intercepting Axes
            if (axisCheck != axis) {
                interceptingAxes[j & 1] = axisCheck;
                j++;
            }
        return interceptingAxes;
    }

    public static EnumFacing[] getInterceptingFaces(EnumFacing.Axis[] interceptingAxes) {
        EnumFacing[] interceptingFaces = new EnumFacing[4];
        for (int i = 0; i < interceptingFaces.length; i++) // Assign Faces on Axes intercepting Controller's Axis
            interceptingFaces[i] = EnumFacing.getFacingFromAxis( EnumFacing.AxisDirection.values()[(i+1) & 1], interceptingAxes[(i / 2) & 1] );
        return interceptingFaces;
    }

    public static void unpack(EntityPlayer vegetableMan, @Nullable IInventory inventoryTE) {
        if (inventoryTE == null)
            return;

        for(int i = 0; i < inventoryTE.getSizeInventory() && i < vegetableMan.inventory.getSizeInventory(); i++) {
            ItemStack stackIn = inventoryTE.removeStackFromSlot(i);

            if (!vegetableMan.inventory.addItemStackToInventory(stackIn))
                inventoryTE.setInventorySlotContents(i, stackIn);
        }
    }

    public static void repack(EntityPlayer vegetableMan, IInventory inventoryTE, FarmCursor cursor) {
        for(int i = 0; i < vegetableMan.inventory.getSizeInventory(); i++) {
            ItemStack stackOut = vegetableMan.inventory.getStackInSlot(i);

            if(!isStackNull(stackOut)) {
                stackOut = vegetableMan.inventory.removeStackFromSlot(i);

                boolean mustDeposit = !isStackNull(stackOut);

                for (int j = 0; mustDeposit && j < vegetableMan.inventory.getSizeInventory() && j < inventoryTE.getSizeInventory(); j++) {
                    if( isStackNull(inventoryTE.getStackInSlot(j)) && inventoryTE.isItemValidForSlot(j, stackOut) ) {
                        inventoryTE.setInventorySlotContents(j, stackOut);
                        mustDeposit = false;
                        break;
                    }
                }

                if(mustDeposit)
                    cursor.getWorld().spawnEntityInWorld(new EntityItem(cursor.getWorld(), 0.5, 0.5, 0.5, stackOut));
            }
        }
    }

    public static boolean isStackNull(@Nullable ItemStack stack)
    {
        return stack == null;
    }

    public static boolean isLogOrLeaves(IBlockState state, World world, BlockPos pos) {
        return state.getBlock().isWood(world, pos) || state.getBlock().isLeaves(state, world, pos);
    }
}
