package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
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
public class BreakOperator implements IFarmOperator {
    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, BlockPos keyPos) {
        TileEntity te = cursor.getWorld().getTileEntity(keyPos.offset(cursor.getFacing(), -1));

        FakePlayer vegetableMan = machine.getFakePlayer();

        Common.unpack(vegetableMan, te, cursor);

        // ------------------

        World thisWorld = cursor.getWorld();
        BlockPos thisPos = cursor.getPos();
        IBlockState thisState = thisWorld.getBlockState(thisPos);

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

        if(isHarvestable && isRemovedByPlayer) {
            thisState.getBlock().harvestBlock(thisWorld, vegetableMan, thisPos, thisState, thisWorld.getTileEntity(thisPos), duplicateStack);
        }

        // ------------------

        Common.repack(vegetableMan, te, cursor);

        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1, cursor.getFacing());
    }
}