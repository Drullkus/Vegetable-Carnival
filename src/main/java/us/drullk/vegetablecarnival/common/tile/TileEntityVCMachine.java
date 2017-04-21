package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
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
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;

import javax.annotation.ParametersAreNonnullByDefault;

import static us.drullk.vegetablecarnival.common.util.VCConfig.maximumRadius;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileEntityVCMachine extends TileEntity implements ITickable {
    //private static final String FAKE_PLAYER_NAME = "[VEGETABLE_MAN]";
    //private static final UUID FAKE_PLAYER_UUID = new UUID(0x98207c63a54c4f1dL, 0x965f71e7a5f7f1aaL);

    //private final FakePlayer vegetableCarnivalFakePlayer = FakePlayerFactory.get((WorldServer) this.getWorld(), new GameProfile(FAKE_PLAYER_UUID, FAKE_PLAYER_NAME));

    private int operationsPerTick = 0;

    private int radiusX = 10;
    private int radiusY = 10;

    private int operatingPos = 0;

    private int farmMachineRadiusX = 0;
    private int farmMachineRadiusY = 0;

    private boolean assembled = false;

    private final int maxMachineSize = maximumRadius;

    public boolean isFarmValidated()
    {
        return assembled;
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
            if(this.assembled)
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

    public void assembleFarm() {
        dissassembleFarm();

        EnumFacing.Axis[] interceptingAxes = new EnumFacing.Axis[2]; // Axes that intercept Controller's Axis
        EnumFacing[] interceptingFaces = new EnumFacing[4]; // Faces on above Axes intercepting Controller's Axis
        IBlockState conduitOff = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, false);
        IBlockState conduitOn = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, true);

        int j = 0;
        for (EnumFacing.Axis axisCheck : EnumFacing.Axis.values()) // Assign intercepting Axes
            if (axisCheck != getWorld().getBlockState(this.getPos()).getValue(BlockVCMachine.FACING).getAxis()) {
                interceptingAxes[j & 1] = axisCheck;
                j++;
            }

        for (int i = 0; i < interceptingFaces.length; i++) // Assign Faces on Axes intercepting Controller's Axis
            interceptingFaces[i] = EnumFacing.getFacingFromAxis( EnumFacing.AxisDirection.values()[(i+1) & 1], interceptingAxes[(i / 2) & 1] );

        BlockPos masterPos = this.getPos(); // Less getPos() getPos() repetition
        int[] scannedRadii = new int[4];
        for (int direction = 0; direction < scannedRadii.length; direction++) {
            for (int length = 1; length <= this.maxMachineSize; length++) {
                BlockPos posCheck = masterPos.offset(interceptingFaces[direction], length);
                IBlockState stateCheck = this.getWorld().getBlockState(posCheck);
                TileEntity tileCheck = this.getWorld().getTileEntity(posCheck);

                if(stateCheck == conduitOff.withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()) &&
                        tileCheck != null && tileCheck instanceof TileEntityVCComponent && ((TileEntityVCComponent) tileCheck).getMaster() == null)
                    scannedRadii[direction]++;
                else
                    break;
            }

            if (scannedRadii[direction] == 0)
                return;
        }

        for(int i = 0; i < 2; i++)
            if (scannedRadii[i*2] != scannedRadii[(i*2)+1]) {
                int result = Math.min(scannedRadii[i*2], scannedRadii[(i*2)+1]);
                scannedRadii[i*2] = result;
                scannedRadii[(i*2)+1] = result;
            }

        for(int direction = 0; direction < scannedRadii.length; direction++)
            for(int length = 1; length <= scannedRadii[direction]; length++) {
                BlockPos posGet = masterPos.offset(interceptingFaces[direction], length);
                this.getWorld().setBlockState(posGet, conduitOn.withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()));
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posGet)).setMasterPos(masterPos);
            }

        farmMachineRadiusX = scannedRadii[0];
        farmMachineRadiusY = scannedRadii[2];
        this.assembled = true;
        operationsPerTick = Math.min(farmMachineRadiusX, farmMachineRadiusY);
        this.radiusX = (farmMachineRadiusX * 2) + (farmMachineRadiusX^2);
        this.radiusY = (farmMachineRadiusY * 2) + (farmMachineRadiusY^2);
    }

    public void dissassembleFarm() {
        EnumFacing.Axis[] interceptingAxes = new EnumFacing.Axis[2]; // Axes that intercept Controller's Axis
        EnumFacing[] interceptingFaces = new EnumFacing[4]; // Faces on above Axes intercepting Controller's Axis
        IBlockState conduitOff = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, false);
        IBlockState conduitOn = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, true);

        int j = 0;
        for (EnumFacing.Axis axisCheck : EnumFacing.Axis.values()) // Assign intercepting Axes
            if (axisCheck != getWorld().getBlockState(this.getPos()).getValue(BlockVCMachine.FACING).getAxis()) {
                interceptingAxes[j & 1] = axisCheck;
                j++;
            }

        for (int i = 0; i < interceptingFaces.length; i++) // Assign Faces on Axes intercepting Controller's Axis
            interceptingFaces[i] = EnumFacing.getFacingFromAxis( EnumFacing.AxisDirection.values()[(i+1) & 1], interceptingAxes[(i / 2) & 1] );

        int[] scannedRadii = new int[]{farmMachineRadiusX, farmMachineRadiusX, farmMachineRadiusY, farmMachineRadiusY};
        for (int direction = 0; direction < scannedRadii.length; direction++)
            for(int length = 1; length <= scannedRadii[direction]; length++) {
                BlockPos posGet = this.getPos().offset(interceptingFaces[direction], length);

                if(this.getWorld().getBlockState(posGet) == conduitOn.withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis())) {
                    this.getWorld().setBlockState(posGet, conduitOff.withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()));
                    ((TileEntityVCComponent) this.getWorld().getTileEntity(posGet)).setMasterPos(null);
                }
            }

        farmMachineRadiusX = 0;
        farmMachineRadiusY = 0;
        this.assembled = false;
        operationsPerTick = 0;
        this.radiusX = 0;
        this.radiusY = 0;
    }

    private void doOperation(int posX, int posZ)
    {
        //begin operation

        //check pos
        /* TODO refactor to direction sensitive
        if ( this.getPos().getY() < 254 && ( posX < -this.farmMachineRadiusX || posZ < -this.farmMachineRadiusY ||
                posX > this.farmMachineRadiusX || posZ > this.farmMachineRadiusY))
        {
            FarmCursor farmCursor = new FarmCursor(new BlockPos(this.pos.getX() + posX, this.pos.getY(), this.pos.getZ() + posZ), this.getWorld(), null, 0);

            int limit = 20;

            for(int i = 0; i < limit && this.getPos().getY() - i >= 0 && farmCursor.getOrder() == IFarmOperator.orders.CONTINUE; i++)
            {
                BlockPos keyPos = new BlockPos(this.getPos().getX() + posX, this.getPos().getY() - i, this.getPos().getZ() + posZ);

                IBlockState keyState = this.getWorld().getBlockState(keyPos);

                IFarmOperator operator = VegetableCarnival.getOperation(keyState);

                if(operator != null)
                {
                    farmCursor = operator.doOperation(farmCursor, this, keyPos);

                    i += farmCursor.getBlocksToSkip();
                    limit += farmCursor.getBlocksToSkip();
                }
            }
        }*/

        this.operatingPos++;
    }

    private int getDiameterFromRadiusPlusCenter(int radius)
    {
        return (radius*2)+1;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        assembled = compound.getBoolean("operational");

        operatingPos = compound.getInteger("operatingpos");

        operationsPerTick = compound.getInteger("operatingcount");

        farmMachineRadiusX = compound.getInteger("radiusX");
        farmMachineRadiusY = compound.getInteger("radiusY");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        compound.setBoolean("operational", assembled);

        compound.setInteger("operatingpos", operatingPos);

        compound.setInteger("operatingcount", operationsPerTick);

        compound.setInteger("radiusX", farmMachineRadiusX);
        compound.setInteger("radiusY", farmMachineRadiusY);

        return compound;
    }
}
