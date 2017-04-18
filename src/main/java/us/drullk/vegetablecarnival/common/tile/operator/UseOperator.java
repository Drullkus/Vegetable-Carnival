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

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class UseOperator implements IFarmOperator
{
    @Override
    public FarmCursor doOperation(final FarmCursor cursor, final TileEntityVCMachine machine, final BlockPos keyPos)
    {
        //System.out.println("operating " + cursor.getPos() + " for machine at " + machine.getPos());

        TileEntity te = cursor.getWorld().getTileEntity(keyPos.down());

        if(te != null && te instanceof IInventory)
        {
            IInventory inventoryTE = (IInventory) te;

            for(int i = 0; i < inventoryTE.getSizeInventory(); i++)
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

                        if(!thisState.getBlock().onBlockActivated(
                                thisWorld,
                                thisPos,
                                thisState,
                                vegetableMan,
                                vegetableMan.getActiveHand(),
                                stack,
                                EnumFacing.UP,
                                0.5f, 0.5f, 0.5f))
                        {
                            stack.getItem().onItemUse(
                                    stack,
                                    vegetableMan,
                                    thisWorld,
                                    thisPos,
                                    vegetableMan.getActiveHand(),
                                    EnumFacing.UP,
                                    0.5f, 0.5f, 0.5f);
                        }

                        inventoryTE.setInventorySlotContents(i, stack);

                        /*if()
                        {
                            cursor.getWorld().spawnEntityInWorld(new EntityItem(cursor.getWorld(), 0.5, 0.5, 0.5, stack));
                        }*/

                        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1);
                    }
                }
            }
        }

        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1);
    }
}