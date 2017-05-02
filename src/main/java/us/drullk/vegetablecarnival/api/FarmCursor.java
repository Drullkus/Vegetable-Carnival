package us.drullk.vegetablecarnival.api;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class FarmCursor {
    private final FarmCursor previousCursor;
    private final BlockPos pos;
    private final World world;
    private IFarmOperator.orders nextOrder = IFarmOperator.orders.CONTINUE;
    private int blocksToSkip = 0;
    private EnumFacing facing;

    public FarmCursor(BlockPos initialPos, World world, @Nullable FarmCursor cursor, int skip, EnumFacing facing)
    {
        this.pos = initialPos;
        this.world = world;
        this.previousCursor = cursor;
        this.blocksToSkip = skip >= 0 ? skip : 0;
        this.facing = facing;
    }

    public BlockPos getPos()
    {
        return pos;
    }

    public World getWorld()
    {
        return world;
    }

    public FarmCursor getStop()
    {
        FarmCursor newCursor = this.copy();
        newCursor.nextOrder = IFarmOperator.orders.STOP;

        return newCursor;
    }

    public IFarmOperator.orders getOrder()
    {
        return nextOrder;
    }

    public int getBlocksToSkip() {
        return blocksToSkip;
    }

    public EnumFacing getFacing() {
        return facing;
    }

    public FarmCursor moveCursor(EnumFacing enumFacing, int offset)
    {
        return new FarmCursor(this.getPos().offset(enumFacing, offset), world, this, 0, facing);
    }

    /*public FarmCursor moveCursor(int[] offsets)
    {
        if (offsets.length == 3)
        {
            return new FarmCursor(new BlockPos(pos.getX() + offsets[0], pos.getY() + offsets[1], pos.getZ() + offsets[2]), world, this, 0, facing);
        }

        return this;
    }*/

    public FarmCursor copy()
    {
        return new FarmCursor(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), world, this, 0, facing);
    }
}
