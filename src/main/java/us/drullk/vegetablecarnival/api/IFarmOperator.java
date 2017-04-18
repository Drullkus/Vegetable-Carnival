package us.drullk.vegetablecarnival.api;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IFarmOperator {
    FarmCursor doOperation(final FarmCursor cursor, final TileEntityVCMachine machine, final BlockPos keyPos);

    enum orders
    {
        CONTINUE,
        STOP
    }
}
