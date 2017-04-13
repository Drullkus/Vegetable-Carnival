package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.common.block.BlockVCCable;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileEntityVCComponent extends TileEntity {
    private TileEntityVCMachine master = null;

    @Nullable
    public TileEntityVCMachine getMaster()
    {
        return master;
    }

    void setMaster(@Nullable TileEntityVCMachine te)
    {
        this.master = te;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        int[] posArray = compound.getIntArray("masterpos");

        if (posArray.length == 3)
        {
            TileEntity te = world.getTileEntity(new BlockPos(posArray[0], posArray[1], posArray[2]));

            if(te instanceof TileEntityVCMachine)
            {
                master = ((TileEntityVCMachine) te);

                world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockVCCable.VALIDATION, true));

                return;
            }
        }

        world.setBlockState(pos, world.getBlockState(pos).withProperty(BlockVCCable.VALIDATION, false));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (master != null)
        {
            int[] posArray = { master.getPos().getX(), master.getPos().getY(), master.getPos().getZ() };

            compound.setIntArray("masterpos", posArray);
        }

        return compound;
    }
}
