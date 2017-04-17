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
    private BlockPos masterPos = null;

    @Nullable
    public TileEntityVCMachine getMaster()
    {
        return world.getTileEntity(this.masterPos) instanceof TileEntityVCMachine ? (TileEntityVCMachine) this.world.getTileEntity(this.masterPos) : null;
    }

    void setMaster(@Nullable BlockPos posOfMaster)
    {
        this.masterPos = posOfMaster;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        int[] posArray = compound.getIntArray("masterpos");

        if (posArray.length == 3)
        {
            this.masterPos = new BlockPos(posArray[0], posArray[1], posArray[2]);

            TileEntity te = this.world.getTileEntity(this.masterPos);

            if(te instanceof TileEntityVCMachine)
            {
                this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).withProperty(BlockVCCable.VALIDATION, true));

                return;
            }
        }

        this.masterPos = null;
        this.world.setBlockState(this.pos, this.world.getBlockState(this.pos).withProperty(BlockVCCable.VALIDATION, false));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (masterPos != null)
        {
            int[] posArray = { masterPos.getX(), masterPos.getY(), masterPos.getZ() };

            compound.setIntArray("masterpos", posArray);
        }

        return compound;
    }
}
