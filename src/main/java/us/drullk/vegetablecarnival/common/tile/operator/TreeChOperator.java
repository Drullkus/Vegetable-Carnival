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
import us.drullk.vegetablecarnival.VegetableCarnival;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;
import us.drullk.vegetablecarnival.common.util.Common;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Stack;

import static us.drullk.vegetablecarnival.common.util.Common.*;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TreeChOperator implements IFarmOperator {
    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, BlockPos keyPos) {
        IBlockState machineState = machine.getWorld().getBlockState(machine.getPos());

        if (machineState.getBlock() != VegetableCarnival.autoFarmOperator)
            return cursor.copy();

        EnumFacing thisFacing = machineState.getValue(BlockVCMachine.FACING);
        EnumFacing[] interceptingFaces = getInterceptingFaces(getInterceptingAxes(thisFacing.getAxis()));

        Stack<BlockPos> treePosStack = new Stack<>();

        treePosStack.add(cursor.getPos());

        TileEntity te = cursor.getWorld().getTileEntity(keyPos.offset(cursor.getFacing(), -1));
        IInventory inventoryTE = te != null && te instanceof IInventory ? (IInventory) te : null;
        FakePlayer vegetableMan = machine.getFakePlayer();
        Common.unpack(vegetableMan, inventoryTE);

        ItemStack stack = vegetableMan.getHeldItemMainhand();
        ItemStack duplicateStack = stack == null ? null : stack.copy();

        World thisWorld = cursor.getWorld();

        while(!treePosStack.isEmpty()) {
            BlockPos candidate = treePosStack.pop();
            IBlockState thisState = thisWorld.getBlockState(candidate);

            if (isLogOrLeaves(thisState, thisWorld, candidate)) {
                // -------------

                boolean isHarvestable = thisState.getBlock().canHarvestBlock(thisWorld, candidate, vegetableMan);

                if (!Common.isStackNull(stack)) {
                    stack.onBlockDestroyed(thisWorld, thisState, candidate, vegetableMan);
                }

                boolean isRemovedByPlayer = thisState.getBlock().removedByPlayer(thisState, thisWorld, candidate, vegetableMan, isHarvestable);
                if (isRemovedByPlayer) {
                    thisState.getBlock().onBlockDestroyedByPlayer(thisWorld, candidate, thisState);
                }

                if (isHarvestable && isRemovedByPlayer) {
                    thisState.getBlock().harvestBlock(thisWorld, vegetableMan, candidate, thisState, thisWorld.getTileEntity(candidate), duplicateStack);
                }

                // -------------

                packDrops(vegetableMan, getDrops(thisWorld, candidate), thisWorld, candidate);

                // -------------

                treePosStack.add(candidate.offset(thisFacing));

                for (EnumFacing side : interceptingFaces) {
                    treePosStack.add(candidate.offset(side));
                }
            }
        }

        Common.repack(vegetableMan, inventoryTE, cursor);

        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1, cursor.getFacing());
    }
}
