package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileEntityVCMachine extends TileEntity implements ITickable, IInventory {

    public TileEntityVCMachine() {
        operatingPos = 0;

        farmOperationZoneXMin = -1;
        farmOperationZoneXMax = -1;
        farmOperationZoneYMin = -1;
        farmOperationZoneYMax = -1;
    }

    private final int radiusX = 10;
    private final int radiusY = 10;

    private final int operationsPerTick = 2;

    private int farmOperationZoneXMin;
    private int farmOperationZoneXMax;
    private int farmOperationZoneYMin;
    private int farmOperationZoneYMax;

    private int operatingPos;

    @Override
    public void update() {
        int totalX = getDiameterFromRadiusPlusCenter(radiusX);
        int totalY = getDiameterFromRadiusPlusCenter(radiusY);

        for (int i = 0; i < operationsPerTick; i++)
        {
            if(operatingPos >= totalX * totalY)
            {
                operatingPos = 0;
            }

            int operatingPosX = operatingPos%totalX;
            int operatingPosY = (operatingPos-(operatingPos%totalY))/totalY;

            // begin zoning

            if (!(operatingPosX >= farmOperationZoneXMin && operatingPosY >= farmOperationZoneYMin &&
                    operatingPosX <= farmOperationZoneXMax && operatingPosY <= farmOperationZoneYMax))
            {
                if (this.world.getBlockState(new BlockPos(
                        this.getPos().getX()+operatingPosX-radiusX-1,
                        this.getPos().getY(),
                        this.getPos().getZ()+operatingPosY-radiusY-1))
                        == Blocks.REDSTONE_BLOCK.getDefaultState())
                {
                    this.world.setBlockState((new BlockPos(
                            this.getPos().getX()+operatingPosX-radiusX-1,
                            this.getPos().getY()+10,
                            this.getPos().getZ()+operatingPosY-radiusY-1)), Blocks.LAPIS_BLOCK.getDefaultState());

                    farmOperationZoneXMin = operatingPosX;
                    farmOperationZoneYMin = operatingPosY;

                    for (int xs = operatingPosX+1; xs < totalX; xs++)
                    {
                        if (this.world.getBlockState(new BlockPos(
                                this.getPos().getX()+xs-radiusX-1,
                                this.getPos().getY(),
                                this.getPos().getZ()+operatingPosY-radiusY-1))
                                == Blocks.REDSTONE_BLOCK.getDefaultState())
                        {
                            farmOperationZoneXMax = xs;
                            this.world.setBlockState((new BlockPos(
                                    this.getPos().getX()+xs-radiusX-1,
                                    this.getPos().getY()+10,
                                    this.getPos().getZ()+operatingPosY-radiusY-1)), Blocks.BRICK_BLOCK.getDefaultState());
                            break;
                        }
                        else
                        {
                            farmOperationZoneXMax = -1;
                        }
                    }

                    if(farmOperationZoneXMax >= 0)
                    {
                        for (int ys = operatingPosY+1; ys < totalY; ys++)
                        {
                            if (this.world.getBlockState(new BlockPos(
                                    this.getPos().getX()+operatingPosX-radiusX-1,
                                    this.getPos().getY(),
                                    this.getPos().getZ()+ys-radiusY-1))
                                    == Blocks.REDSTONE_BLOCK.getDefaultState())
                            {
                                this.world.setBlockState((new BlockPos(
                                        this.getPos().getX()+operatingPosX-radiusX-1,
                                        this.getPos().getY()+10,
                                        this.getPos().getZ()+ys-radiusY-1)), Blocks.CLAY.getDefaultState());

                                if (this.world.getBlockState(new BlockPos(
                                        this.getPos().getX()+farmOperationZoneXMax-radiusX-1,
                                        this.getPos().getY(),
                                        this.getPos().getZ()+ys-radiusY-1))
                                        == Blocks.REDSTONE_BLOCK.getDefaultState())
                                {
                                    farmOperationZoneYMax = ys;

                                    this.world.setBlockState((new BlockPos(
                                            this.getPos().getX()+farmOperationZoneXMax-radiusX-1,
                                            this.getPos().getY()+10,
                                            this.getPos().getZ()+ys-radiusY-1)), Blocks.MAGMA.getDefaultState());
                                    break;
                                }
                            }
                            else
                            {
                                farmOperationZoneYMax = -1;
                            }
                        }
                    }
                    else
                    {
                        farmOperationZoneXMin = -1;
                        farmOperationZoneYMin = -1;
                        farmOperationZoneYMax = -1;
                    }
                }
            }

            //end zoning

            //begin operation

            //check if block exists
            if (this.world.getBlockState(new BlockPos(
                    this.getPos().getX()+operatingPosX-radiusX-1,
                    this.getPos().getY()+1,
                    this.getPos().getZ()+operatingPosY-radiusY-1))
                    == Blocks.STONEBRICK.getDefaultState())
            {
                //do operation

                if(operatingPosX >= farmOperationZoneXMin && operatingPosY >= farmOperationZoneYMin &&
                        operatingPosX <= farmOperationZoneXMax && operatingPosY <= farmOperationZoneYMax)
                {
                    world.setBlockState(new BlockPos(
                                    this.getPos().getX()+operatingPosX-radiusX-1,
                                    this.getPos().getY()+5,
                                    this.getPos().getZ()+operatingPosY-radiusY-1),
                            Blocks.EMERALD_BLOCK.getDefaultState());
                }
                else
                {
                    world.setBlockState(new BlockPos(
                                    this.getPos().getX()+operatingPosX-radiusX-1,
                                    this.getPos().getY()+2,
                                    this.getPos().getZ()+operatingPosY-radiusY-1),
                            Blocks.DIRT.getDefaultState());
                }
            }

            operatingPos++;
        }
    }

    private int getDiameterFromRadiusPlusCenter(int radius)
    {
        return (radius*2)+1;
    }

    @Override
    public int getSizeInventory() {
        return 0;
    }

    @Nullable
    @Override
    public ItemStack getStackInSlot(int i) {
        return null;
    }

    @Nullable
    @Override
    public ItemStack decrStackSize(int i, int i1) {
        return null;
    }

    @Nullable
    @Override
    public ItemStack removeStackFromSlot(int i) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int i, @Nullable ItemStack itemStack) {

    }

    @Override
    public int getInventoryStackLimit() {
        return 0;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer entityPlayer) {
        return false;
    }

    @Override
    public void openInventory(EntityPlayer entityPlayer) {

    }

    @Override
    public void closeInventory(EntityPlayer entityPlayer) {

    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        return false;
    }

    @Override
    public int getField(int i) {
        return 0;
    }

    @Override
    public void setField(int i, int i1) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }

    @Override
    public String getName() {
        return "autoFarmOperatorVC";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }
}
