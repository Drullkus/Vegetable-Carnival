package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SkipOperator implements IFarmOperator {
    private final int skip;

    public SkipOperator(int toSkip)
    {
        skip = toSkip;
    }

    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, final BlockPos keyPos) {
        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, skip, cursor.getFacing());
    }
}
