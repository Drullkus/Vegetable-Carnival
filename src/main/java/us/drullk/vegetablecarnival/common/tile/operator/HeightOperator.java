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
    private final int[] offsetData;

    public HeightOperator(int[] offsets)
    {
        offsetData = offsets.length == 3 ? offsets : new int[]{ 0, 0, 0 };
    }

    @Override
    public FarmCursor doOperation(final FarmCursor cursor, final TileEntityVCMachine machine, final BlockPos keyPos)
    {
        System.out.println("shifting " + offsetData[1] + " for cursor at " + cursor.getPos());// + " for machine at " + machine.getPos());
        return cursor.moveCursor(offsetData);
    }
}