package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SetBlockOperator implements IFarmOperator {
    private final IBlockState blockstate;

    public SetBlockOperator(IBlockState state) {
        blockstate = state;
    }

    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, BlockPos keyPos) {
        cursor.getWorld().setBlockState(cursor.getPos(), blockstate);
        return cursor.copy();
    }
}
