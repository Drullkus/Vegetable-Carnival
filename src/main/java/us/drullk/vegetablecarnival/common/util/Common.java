package us.drullk.vegetablecarnival.common.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import us.drullk.vegetablecarnival.api.FarmCursor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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

    public static void unpack(EntityPlayer vegetableMan, @Nullable TileEntity te, FarmCursor cursor) {
        movePlayerToCursor(vegetableMan, cursor);
        if (te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) return;

        IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        for(int i = 0; i < itemHandler.getSlots() && i < vegetableMan.inventory.getSizeInventory(); i++) {
            ItemStack stackCheck = itemHandler.getStackInSlot(i);

            if (!isStackNull(stackCheck)){
                ItemStack stackIn = itemHandler.extractItem(i, stackCheck.stackSize, false);

                if (!vegetableMan.inventory.addItemStackToInventory(stackIn)) {
                    itemHandler.insertItem(i, stackIn, false);
                    break;
                }
            }
        }
    }

    public static void repack(EntityPlayer vegetableMan, @Nullable TileEntity te, FarmCursor cursor) {
        movePlayerToCursor(vegetableMan, cursor);

        if (te == null || !te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
            vegetableMan.inventory.dropAllItems();
            return;
        }

        IItemHandler itemHandler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);

        for(int i = 0; i < vegetableMan.inventory.getSizeInventory(); i++) {
            ItemStack stackCheck = vegetableMan.inventory.getStackInSlot(i);

            if(!isStackNull(stackCheck)) {
                ItemStack stackOut = vegetableMan.inventory.removeStackFromSlot(i);

                boolean mustDump = !isStackNull(stackOut);

                for (int j = 0; mustDump && j < itemHandler.getSlots(); j++) {
                    stackOut = itemHandler.insertItem(j, stackOut, false);

                    if(isStackNull(stackOut)) {
                        mustDump = false;
                        break;
                    }
                }

                if(mustDump) {
                    cursor.getWorld().spawnEntityInWorld(new EntityItem(cursor.getWorld(), (double) cursor.getPos().getX(), (double) cursor.getPos().getY(), (double) cursor.getPos().getZ(), stackOut));
                }
            }
        }

        //vegetableMan.inventory.dropAllItems();
    }

    public static boolean isStackNull(@Nullable ItemStack stack) {
        return stack == null;
    }

    public static boolean isLogOrLeaves(IBlockState state, World world, BlockPos pos) {
        return state.getBlock().isWood(world, pos) || state.getBlock().isLeaves(state, world, pos);
    }

    public static List<ItemStack> getDrops(World thisWorld, BlockPos pos) {
        List<EntityItem> drops = thisWorld.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(pos));
        List<ItemStack> stacks = new ArrayList<>();

        for (EntityItem drop : drops) {
            ItemStack stack = drop.getEntityItem();
            stacks.add(stack.copy());
            drop.setDead();
        }

        return stacks;
    }

    public static void packDrops(EntityPlayer vegetableMan, List<ItemStack> stacks, World thisWorld, BlockPos thisPos) {
        for (ItemStack stack : stacks) {
            if (!vegetableMan.inventory.addItemStackToInventory(stack))
                thisWorld.spawnEntityInWorld(new EntityItem(thisWorld, (double) thisPos.getX(),(double) thisPos.getY(),(double) thisPos.getZ(), stack));
        }
    }

    private static void movePlayerToCursor(EntityPlayer vegetableMan, FarmCursor cursor) {
        boolean clip = vegetableMan.noClip;
        vegetableMan.noClip = true;
        vegetableMan.setPosition((double) cursor.getPos().getX(),(double) cursor.getPos().getY(),(double) cursor.getPos().getZ());
        //vegetableMan.moveEntity((double) cursor.getPos().getX(),(double) cursor.getPos().getY(),(double) cursor.getPos().getZ());
        vegetableMan.noClip = clip;
    }
}
