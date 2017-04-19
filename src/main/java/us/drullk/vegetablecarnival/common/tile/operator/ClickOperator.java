package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;
import us.drullk.vegetablecarnival.common.util.Common;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClickOperator implements IFarmOperator {
    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, BlockPos keyPos) {
        TileEntity te = cursor.getWorld().getTileEntity(keyPos.down());

        if(te != null && te instanceof IInventory)
        {
            IInventory inventoryTE = (IInventory) te;

            World thisWorld = cursor.getWorld();
            BlockPos thisPos = cursor.getPos();
            IBlockState thisState = thisWorld.getBlockState(thisPos);
            FakePlayer vegetableMan = machine.getFakePlayer();

            Common.unpack(vegetableMan, inventoryTE);

            //thisState.getBlock().onBlockClicked(thisWorld, thisPos, vegetableMan);

            Common.repack(vegetableMan, inventoryTE, cursor);

            /*for(int i = 0; i < inventoryTE.getSizeInventory(); i++)
            {
                ItemStack stackCheck = inventoryTE.getStackInSlot(i);

                if (stackCheck != null)
                {
                    ItemStack stack = inventoryTE.removeStackFromSlot(i);

                    if(stack != null)
                    {
                        World thisWorld = cursor.getWorld();
                        BlockPos thisPos = cursor.getPos();
                        IBlockState thisState = thisWorld.getBlockState(thisPos);
                        FakePlayer vegetableMan = machine.getFakePlayer();

                        thisState.getBlock().onBlockClicked(thisWorld, thisPos, vegetableMan);

                        inventoryTE.setInventorySlotContents(i, stack);

                        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1);
                    }
                }
            }*/
        }



        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1);
    }
}