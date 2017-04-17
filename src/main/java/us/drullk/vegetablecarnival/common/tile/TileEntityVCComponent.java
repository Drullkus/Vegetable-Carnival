package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
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
        if(masterPos == null)
        {
            return null;
        }

        TileEntity te = this.getWorld().getTileEntity(masterPos);

        if(te != null && te instanceof TileEntityVCMachine)
        {
            return (TileEntityVCMachine) te;
        }
        else
        {
            masterPos = null;
            return null;
        }
    }

    void setMasterPos(@Nullable BlockPos posOfNewMaster)
    {
        this.masterPos = posOfNewMaster;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        int[] posArray = compound.getIntArray("masterpos");

        if (posArray.length == 3)
        {
            masterPos = new BlockPos(posArray[0], posArray[1], posArray[2]);
        }
        else
        {
            masterPos = null;
        }
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

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() == newState.getBlock();
    }
}
