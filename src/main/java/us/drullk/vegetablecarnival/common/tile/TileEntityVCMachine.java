package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.VegetableCarnival;
import us.drullk.vegetablecarnival.common.block.BlockVCCable;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileEntityVCMachine extends TileEntity implements ITickable {

    public TileEntityVCMachine() {

    }

    private final int operationsPerTick = 2;

    private final int radiusX = 10;
    private final int radiusY = 10;

    private int operatingPos = 0;

    private int totalX;
    private int totalY;

    private int farmMachineRadiusX = 0;
    private int farmMachineRadiusZ = 0;

    private boolean valid = false;

    private final int maxMachineSize = 3;

    public boolean isFarmValidated()
    {
        return valid;
    }

    @Override
    public void update()
    {
        //System.out.println("world remoteness is " + !world.isRemote);

        if(!world.isRemote)
        {
            if(!this.valid)
            {
                //this.validateFarm();
            }
            else
            {
                //System.out.println("OPERATING");

                this.totalX = getDiameterFromRadiusPlusCenter(this.radiusX);
                this.totalY = getDiameterFromRadiusPlusCenter(this.radiusY);

                for (int i = 0; i < this.operationsPerTick; i++)
                {
                    if(this.operatingPos >= this.totalX * this.totalY)
                    {
                        this.operatingPos = 0;
                    }

                    int operatingPosX = (this.operatingPos%this.totalX)-this.radiusX;
                    int operatingPosY = ((this.operatingPos-(this.operatingPos%this.totalY))/this.totalY)-this.radiusY;

                    // begin zoning

                    //checkZoning(operatingPosX, operatingPosY, 0);

                    // end zoning

                    this.doOperation(operatingPosX, operatingPosY);
                }
            }
        }
    }

    public void validateFarm() {
        this.invalidateFarm();

        this.valid = false;
        farmMachineRadiusX = 0;
        farmMachineRadiusZ = 0;

        IBlockState conduitXOff = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.X)
                .withProperty(BlockVCCable.VALIDATION, false);
        IBlockState conduitZOff = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.Z)
                .withProperty(BlockVCCable.VALIDATION, false);

        IBlockState conduitXOn = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.X)
                .withProperty(BlockVCCable.VALIDATION, true);
        IBlockState conduitZOn = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.Z)
                .withProperty(BlockVCCable.VALIDATION, true);

        // X -West +East

        for (int i = 1; i < this.maxMachineSize + 1; i++)
        {
            // generate positions

            BlockPos posMin = new BlockPos(this.getPos().getX()-i, this.getPos().getY(), this.getPos().getZ());
            BlockPos posMax = new BlockPos(this.getPos().getX()+i, this.getPos().getY(), this.getPos().getZ());

            // get IBlockStates

            IBlockState blockStateMin = this.getWorld().getBlockState(posMin);
            IBlockState blockStateMax = this.getWorld().getBlockState(posMax);

            // get TEs

            TileEntity tileMin = this.getWorld().getTileEntity(posMin);
            TileEntity tileMax = this.getWorld().getTileEntity(posMax);

            // Check Validity

            if(blockStateMin.equals(conduitXOff) && blockStateMax.equals(conduitXOff) &&
                    tileMin != null && tileMax != null &&
                    tileMin instanceof TileEntityVCComponent &&
                    tileMax instanceof TileEntityVCComponent &&
                    ((TileEntityVCComponent) tileMin).getMaster() == null &&
                    ((TileEntityVCComponent) tileMax).getMaster() == null)
            {
                this.farmMachineRadiusX++;
            }
            else
            {
                break;
            }
        }

        //System.out.println("X: " + farmMachineRadiusX);

        if(farmMachineRadiusX == 0)
        {
            return;
        }

        // Z -North +South

        for (int i = 1; i < this.maxMachineSize + 1; i++)
        {
            // generate positions

            BlockPos posMin = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()-i);
            BlockPos posMax = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()+i);

            // get IBlockStates

            IBlockState blockStateMin = this.getWorld().getBlockState(posMin);
            IBlockState blockStateMax = this.getWorld().getBlockState(posMax);

            // get TEs

            TileEntity tileMin = this.getWorld().getTileEntity(posMin);
            TileEntity tileMax = this.getWorld().getTileEntity(posMax);

            // Check Validity

            if(blockStateMin.equals(conduitZOff) && blockStateMax.equals(conduitZOff) &&
                    tileMin != null && tileMax != null &&
                    tileMin instanceof TileEntityVCComponent &&
                    tileMax instanceof TileEntityVCComponent &&
                    ((TileEntityVCComponent) tileMin).getMaster() == null &&
                    ((TileEntityVCComponent) tileMax).getMaster() == null)
            {
                this.farmMachineRadiusZ++;
            }
            else
            {
                break;
            }
        }

        //System.out.println("Z: " + farmMachineRadiusZ);

        if(farmMachineRadiusZ == 0)
        {
            farmMachineRadiusX = 0;

            return;
        }

        if(farmMachineRadiusX > 0 && farmMachineRadiusZ > 0)
        {
            for(int i = 1; i <= farmMachineRadiusX; i++)
            {
                // generate positions

                BlockPos posMin = new BlockPos(this.getPos().getX()-i, this.getPos().getY(), this.getPos().getZ());
                BlockPos posMax = new BlockPos(this.getPos().getX()+i, this.getPos().getY(), this.getPos().getZ());

                this.getWorld().setBlockState(posMin, conduitXOn);
                this.getWorld().setBlockState(posMax, conduitXOn);

                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMin)).setMasterPos(this.getPos());
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMax)).setMasterPos(this.getPos());
            }

            for(int i = 1; i <= farmMachineRadiusZ; i++)
            {
                // generate positions

                BlockPos posMin = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()-i);
                BlockPos posMax = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()+i);

                this.getWorld().setBlockState(posMin, conduitZOn);
                this.getWorld().setBlockState(posMax, conduitZOn);

                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMin)).setMasterPos(this.getPos());
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMax)).setMasterPos(this.getPos());
            }

            this.valid = true;
        }

        //System.out.println(valid);
    }

    public void invalidateFarm() {
        IBlockState conduitXOn = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.X)
                .withProperty(BlockVCCable.VALIDATION, true);
        IBlockState conduitZOn = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.Z)
                .withProperty(BlockVCCable.VALIDATION, true);

        IBlockState conduitXOff = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.X)
                .withProperty(BlockVCCable.VALIDATION, false);
        IBlockState conduitZOff = VegetableCarnival.farmCable.getDefaultState()
                .withProperty(BlockVCCable.AXIS, EnumFacing.Axis.Z)
                .withProperty(BlockVCCable.VALIDATION, false);

        for(int i = 1; i <= farmMachineRadiusX; i++)
        {
            // generate positions

            BlockPos posMin = new BlockPos(this.getPos().getX()-i, this.getPos().getY(), this.getPos().getZ());
            BlockPos posMax = new BlockPos(this.getPos().getX()+i, this.getPos().getY(), this.getPos().getZ());

            if(this.getWorld().getBlockState(posMin) == conduitXOn)
            {
                this.getWorld().setBlockState(posMin, conduitXOff);
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMin)).setMasterPos(null);
            }

            if(this.getWorld().getBlockState(posMax) == conduitXOn)
            {
                this.getWorld().setBlockState(posMax, conduitXOff);
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMax)).setMasterPos(null);
            }
        }

        for(int i = 1; i <= farmMachineRadiusZ; i++)
        {
            // generate positions

            BlockPos posMin = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()-i);
            BlockPos posMax = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()+i);

            if(this.getWorld().getBlockState(posMin) == conduitZOn)
            {
                this.getWorld().setBlockState(posMin, conduitZOff);
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMin)).setMasterPos(null);
            }

            if(this.getWorld().getBlockState(posMax) == conduitZOn)
            {
                this.getWorld().setBlockState(posMax, conduitZOff);
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posMax)).setMasterPos(null);
            }
        }

        farmMachineRadiusX = 0;
        farmMachineRadiusZ = 0;
        valid = false;
    }


    private void doOperation(int posX, int posY)
    {
        //begin operation

        //check pos
        if ( posX < -this.farmMachineRadiusX || posY < -this.farmMachineRadiusZ ||
                posX > this.farmMachineRadiusX || posY > this.farmMachineRadiusZ)
        {
            //do operation
            if (this.world.getBlockState(new BlockPos(
                    this.getPos().getX()+posX,
                    this.getPos().getY(),
                    this.getPos().getZ()+posY))
                    == Blocks.STONEBRICK.getDefaultState())
            {
                this.world.setBlockState(new BlockPos(
                        this.getPos().getX()+posX,
                        this.getPos().getY()+1,
                        this.getPos().getZ()+posY),
                        Blocks.DIRT.getDefaultState());
            }
        }

        this.operatingPos++;
    }

    private int getDiameterFromRadiusPlusCenter(int radius)
    {
        return (radius*2)+1;
    }

    /*private void checkZoning(int posX, int posY, int height)
    {
        if (!(posX >= farmOperationZoneXMin && posY >= farmOperationZoneYMin &&
                posX <= farmOperationZoneXMax && posY <= farmOperationZoneYMax))
        {
            int tempXmin = -1;
            int tempYmin = -1;
            int tempXmax = -1;
            int tempYmax = -1;

            if (this.world.getBlockState(new BlockPos(
                    this.getPos().getX()+posX-radiusX-1,
                    this.getPos().getY()+height,
                    this.getPos().getZ()+posY-radiusY-1))
                    == Blocks.REDSTONE_BLOCK.getDefaultState())
            {
                    this.world.setBlockState((new BlockPos(
                            this.getPos().getX()+posX-radiusX-1,
                            this.getPos().getY()+10,
                            this.getPos().getZ()+posY-radiusY-1)), Blocks.LAPIS_BLOCK.getDefaultState());

                tempXmin = posX;
                tempYmin = posY;

                for (int xs = posX+1; xs < totalX; xs++)
                {
                    if (this.world.getBlockState(new BlockPos(
                            this.getPos().getX()+xs-radiusX-1,
                            this.getPos().getY()+height,
                            this.getPos().getZ()+posY-radiusY-1))
                            == Blocks.REDSTONE_BLOCK.getDefaultState())
                    {
                        tempXmax = xs;
                            this.world.setBlockState((new BlockPos(
                                    this.getPos().getX()+xs-radiusX-1,
                                    this.getPos().getY()+10,
                                    this.getPos().getZ()+posY-radiusY-1)), Blocks.BRICK_BLOCK.getDefaultState());
                        break;
                    }
                    else
                    {
                        tempXmax = -1;
                    }
                }

                if(tempXmax >= 0)
                {
                    for (int ys = posY+1; ys < totalY; ys++)
                    {
                        if (this.world.getBlockState(new BlockPos(
                                this.getPos().getX()+posX-radiusX-1,
                                this.getPos().getY()+height,
                                this.getPos().getZ()+ys-radiusY-1))
                                == Blocks.REDSTONE_BLOCK.getDefaultState())
                        {
                                this.world.setBlockState((new BlockPos(
                                        this.getPos().getX()+posX-radiusX-1,
                                        this.getPos().getY()+10,
                                        this.getPos().getZ()+ys-radiusY-1)), Blocks.CLAY.getDefaultState());

                            if (this.world.getBlockState(new BlockPos(
                                    this.getPos().getX()+farmOperationZoneXMax-radiusX-1,
                                    this.getPos().getY()+height,
                                    this.getPos().getZ()+ys-radiusY-1))
                                    == Blocks.REDSTONE_BLOCK.getDefaultState())
                            {
                                tempYmax = ys;

                                farmOperationZoneXMin = tempXmin;
                                farmOperationZoneXMax = tempYmax;
                                farmOperationZoneYMin = tempYmin;
                                farmOperationZoneYMax = tempYmax;

                                this.world.setBlockState((new BlockPos(
                                        this.getPos().getX()+farmOperationZoneXMax-radiusX-1,
                                        this.getPos().getY()+10,
                                        this.getPos().getZ()+ys-radiusY-1)), Blocks.MAGMA.getDefaultState());
                                break;
                            }
                        }
                        else
                        {
                            tempYmax = -1;
                        }
                    }
                }
                else
                {
                    farmOperationZoneXMin = -1;
                    farmOperationZoneXMax = -1;
                    farmOperationZoneYMin = -1;
                    farmOperationZoneYMax = -1;
                }
            }
        }
    }*/

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        valid = compound.getBoolean("operational");

        operatingPos = compound.getInteger("operatingpos");

        farmMachineRadiusX = compound.getInteger("radiusX");
        farmMachineRadiusZ = compound.getInteger("radiusZ");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setBoolean("operational", valid);

        compound.setInteger("operatingpos", operatingPos);

        compound.setInteger("radiusX", farmMachineRadiusX);
        compound.setInteger("radiusZ", farmMachineRadiusZ);

        return compound;
    }
}
