package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
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
public class HarvestOperator implements IFarmOperator
{
    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, BlockPos keyPos)
    {
        IBlockState thisState = cursor.getWorld().getBlockState(cursor.getPos());
        Block thisBlock = thisState.getBlock();

        if(thisBlock instanceof BlockCrops)
        {
            BlockCrops crop = (BlockCrops) thisBlock;
            if(crop.isMaxAge(thisState))
            {
                TileEntity te = cursor.getWorld().getTileEntity(keyPos.down());

                if(te != null && te instanceof IInventory)
                {
                    IInventory inventoryTE = (IInventory) te;
                    FakePlayer vegetableMan = machine.getFakePlayer();

                    Common.unpack(vegetableMan, inventoryTE, cursor);

                    // ------------------

                    World thisWorld = cursor.getWorld();
                    BlockPos thisPos = cursor.getPos();

                    ItemStack stack = vegetableMan.getHeldItemMainhand();
                    ItemStack duplicateStack = stack == null ? null : stack.copy();

                    boolean isHarvestable = thisState.getBlock().canHarvestBlock(thisWorld, thisPos, vegetableMan);

                    if(!Common.isStackNull(stack))
                    {
                        stack.onBlockDestroyed(thisWorld, thisState, thisPos, vegetableMan);
                    }

                    boolean isRemovedByPlayer = thisState.getBlock().removedByPlayer(thisState, thisWorld, thisPos, vegetableMan, isHarvestable);
                    if (isRemovedByPlayer)
                    {
                        thisState.getBlock().onBlockDestroyedByPlayer(thisWorld, thisPos, thisState);
                    }

                    if(isHarvestable && isRemovedByPlayer)
                    {
                        thisState.getBlock().harvestBlock(thisWorld, vegetableMan, thisPos, thisState, thisWorld.getTileEntity(thisPos), duplicateStack);
                    }

                    // ------------------

                    Common.repack(vegetableMan, inventoryTE, cursor);
                }
            }
        }

        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1, cursor.getFacing());
    }
}
