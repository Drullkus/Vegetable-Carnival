package us.drullk.vegetablecarnival.common.tile;

import com.mojang.authlib.GameProfile;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import us.drullk.vegetablecarnival.VegetableCarnival;
import us.drullk.vegetablecarnival.common.block.BlockVCCable;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.UUID;

import static us.drullk.vegetablecarnival.common.util.VCConfig.maximumRadius;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileEntityVCMachine extends TileEntity implements ITickable {
    //private static final String FAKE_PLAYER_NAME = "[VEGETABLE_MAN]";
    //private static final UUID FAKE_PLAYER_UUID = new UUID(0x98207c63a54c4f1dL, 0x965f71e7a5f7f1aaL);

    //private final FakePlayer vegetableCarnivalFakePlayer = FakePlayerFactory.get((WorldServer) this.getWorld(), new GameProfile(FAKE_PLAYER_UUID, FAKE_PLAYER_NAME));

    private final int operationsPerTick = 2;

    private int radiusX = 10;
    private int radiusY = 10;

    private int operatingPos = 0;

    private int farmMachineRadiusX = 0;
    private int farmMachineRadiusZ = 0;

    private boolean valid = false;

    private final int maxMachineSize = maximumRadius;

    public boolean isFarmValidated()
    {
        return valid;
    }

    public FakePlayer getFakePlayer()
    {
        return FakePlayerFactory.getMinecraft((WorldServer) world);
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

                int totalX = getDiameterFromRadiusPlusCenter(this.radiusX);
                int totalY = getDiameterFromRadiusPlusCenter(this.radiusY);

                for (int i = 0; i < this.operationsPerTick; i++)
                {
                    if(this.operatingPos >= totalX * totalY)
                    {
                        this.operatingPos = 0;
                    }

                    int operatingPosX = (this.operatingPos% totalX)-this.radiusX;
                    int operatingPosY = ((this.operatingPos-(this.operatingPos% totalY))/ totalY)-this.radiusY;

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

        for (int i = 1; i <= this.maxMachineSize; i++)
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

        for (int i = 1; i <= this.maxMachineSize; i++)
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
            this.radiusX = (farmMachineRadiusX * 2) + (farmMachineRadiusX^2);
            this.radiusY = (farmMachineRadiusZ * 2) + (farmMachineRadiusZ^2);
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
        if ( this.getPos().getY() < 254 && ( posX < -this.farmMachineRadiusX || posY < -this.farmMachineRadiusZ ||
                posX > this.farmMachineRadiusX || posY > this.farmMachineRadiusZ ))
        {
            FarmCursor farmCursor = new FarmCursor(new BlockPos(this.pos.getX() + posX, this.pos.getY(), this.pos.getZ() + posY), this.getWorld(), null, 0);

            int limit = 20;

            for(int i = 0; i < limit && this.getPos().getY() - i >= 0 && farmCursor.getOrder() == IFarmOperator.orders.CONTINUE; i++)
            {
                //System.out.println("operating at " + new BlockPos(this.pos.getX() + posX, this.pos.getY(), this.pos.getZ() + posY));

                BlockPos keyPos = new BlockPos(this.getPos().getX() + posX, this.getPos().getY() - i, this.getPos().getZ() + posY);

                IBlockState keyState = this.getWorld().getBlockState(keyPos);

                IFarmOperator operator = VegetableCarnival.getOperation(keyState);

                if(operator != null)
                {
                    farmCursor = operator.doOperation(farmCursor, this, keyPos);

                    i += farmCursor.getBlocksToSkip();
                    limit += farmCursor.getBlocksToSkip();
                }
            }
        }
        else
        {
            //System.out.println("pos at " + new BlockPos(this.pos.getX() + posX, this.pos.getY(), this.pos.getZ() + posY) + " is not workable");
        }

        this.operatingPos++;
    }

    private int getDiameterFromRadiusPlusCenter(int radius)
    {
        return (radius*2)+1;
    }

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
