package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TillOperator implements IFarmOperator {
    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, BlockPos keyPos) {
        IBlockState thisState = cursor.getWorld().getBlockState(cursor.getPos());

        if(thisState == Blocks.DIRT.getDefaultState() || thisState == Blocks.GRASS.getDefaultState())
        {
            cursor.getWorld().setBlockState(cursor.getPos(), Blocks.FARMLAND.getDefaultState());
        }
        return cursor.copy();
    }
}
