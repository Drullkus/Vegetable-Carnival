package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class HeightOperator implements IFarmOperator
{
    private final int offsetData;

    public HeightOperator(int offset) {
        offsetData = offset;
    }

    @Override
    public FarmCursor doOperation(final FarmCursor cursor, final TileEntityVCMachine machine, final BlockPos keyPos) {
        return cursor.moveCursor(cursor.getFacing(), offsetData);
    }
}