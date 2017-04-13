package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import us.drullk.vegetablecarnival.VegetableCarnival;

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
    private int farmMachineRadiusY = 0;

    private boolean valid = false;

    private final int maxMachineSize = 3;

    @Override
    public void update() {
        if (world.isRemote)
        {
            if(!this.valid)
            {
                this.validateFarm();
            }
            else
            {
                System.out.println("OPERATING");

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

    private void validateFarm()
    {
        System.out.println("Attempting to Validate");

        BlockPos[] xMin = new BlockPos[this.maxMachineSize];
        BlockPos[] xMax = new BlockPos[this.maxMachineSize];
        BlockPos[] yMin = new BlockPos[this.maxMachineSize];
        BlockPos[] yMax = new BlockPos[this.maxMachineSize];

        for(int i = 1; i <= this.maxMachineSize; i++)
        {
            xMin[i-1] = new BlockPos(this.getPos().getX()-i, this.getPos().getY(), this.getPos().getZ());
            xMax[i-1] = new BlockPos(this.getPos().getX()+i, this.getPos().getY(), this.getPos().getZ());

            TileEntity xMinTE = null;
            TileEntity xMaxTE = null;

            if(this.world.getBlockState(xMin[i-1]).getBlock() == VegetableCarnival.farmCable &&
                    this.world.getBlockState(xMax[i-1]).getBlock() == VegetableCarnival.farmCable)
            {
                xMinTE = this.world.getTileEntity(xMin[i-1]);
                xMaxTE = this.world.getTileEntity(xMax[i-1]);
            }

            if(xMinTE != null && xMaxTE != null &&
                    this.world.getTileEntity(xMin[i-1]) instanceof TileEntityVCComponent &&
                    this.world.getTileEntity(xMax[i-1]) instanceof TileEntityVCComponent)
            {
                this.farmMachineRadiusX++;
            }
            else
            {
                break;
            }
        }

        for(int i = 1; i <= this.maxMachineSize; i++)
        {
            yMin[i-1] = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()-i);
            yMax[i-1] = new BlockPos(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ()+i);

            TileEntity yMinTE = null;
            TileEntity yMaxTE = null;

            if(this.world.getBlockState(yMin[i-1]).getBlock() == VegetableCarnival.farmCable &&
                    this.world.getBlockState(yMax[i-1]).getBlock() == VegetableCarnival.farmCable)
            {
                yMinTE = this.world.getTileEntity(yMin[i-1]);
                yMaxTE = this.world.getTileEntity(yMax[i-1]);
            }

            if(yMinTE != null && yMaxTE != null &&
                    this.world.getTileEntity(yMin[i-1]) instanceof TileEntityVCComponent &&
                    this.world.getTileEntity(yMax[i-1]) instanceof TileEntityVCComponent)
            {
                this.farmMachineRadiusY++;
            }
            else
            {
                break;
            }
        }

        this.valid = this.farmMachineRadiusX >= 1 && this.farmMachineRadiusY >= 1;

        //System.out.println("Validation " + this.valid);

        if (this.valid)
        {
            //System.out.println(this.pos);

            for (int i = 1; i <= this.farmMachineRadiusX; i++)
            {
                //System.out.println(xMin[i-1]);
                ((TileEntityVCComponent) this.world.getTileEntity(xMin[i-1])).setMaster(this);
                ((TileEntityVCComponent) this.world.getTileEntity(xMax[i-1])).setMaster(this);
            }

            for (int i = 1; i <= this.farmMachineRadiusY; i++)
            {
                ((TileEntityVCComponent) this.world.getTileEntity(yMin[i-1])).setMaster(this);
                ((TileEntityVCComponent) this.world.getTileEntity(yMax[i-1])).setMaster(this);
            }
        }
        else
        {
            this.farmMachineRadiusX = 0;
            this.farmMachineRadiusY = 0;
        }
    }

    public void invalidateDependents()
    {
        this.valid = false;

        for (int i = 1; i <= this.farmMachineRadiusX; i++)
        {
            ((TileEntityVCComponent) this.world.getTileEntity(new BlockPos(
                    this.getPos().getX()+i,
                    this.getPos().getY(),
                    this.getPos().getZ()))).setMaster(null);
            ((TileEntityVCComponent) this.world.getTileEntity(new BlockPos(
                    this.getPos().getX()-i,
                    this.getPos().getY(),
                    this.getPos().getZ()))).setMaster(null);
        }

        for (int i = 1; i <= this.farmMachineRadiusY; i++)
        {
            ((TileEntityVCComponent) this.world.getTileEntity(new BlockPos(
                    this.getPos().getX(),
                    this.getPos().getY(),
                    this.getPos().getZ()+i))).setMaster(null);
            ((TileEntityVCComponent) this.world.getTileEntity(new BlockPos(
                    this.getPos().getX(),
                    this.getPos().getY(),
                    this.getPos().getZ()-i))).setMaster(null);
        }

        this.farmMachineRadiusX = 0;
        this.farmMachineRadiusY = 0;

        //System.out.println("invalidated");
    }

    private void doOperation(int posX, int posY)
    {
        //begin operation

        //check pos
        if ( posX < -this.farmMachineRadiusX || posY < -this.farmMachineRadiusY ||
                posX > this.farmMachineRadiusX || posY > this.farmMachineRadiusY )
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

    /*@Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        return compound;
    }*/
}
