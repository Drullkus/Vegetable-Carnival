package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
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
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;
import us.drullk.vegetablecarnival.common.util.Common;

import javax.annotation.ParametersAreNonnullByDefault;

import static us.drullk.vegetablecarnival.common.util.Common.isCoordInsideNoZone;
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

    private int farmMachineRadiusPrimary = 0;
    private int farmMachineRadiusSecondary = 0;

    //private EnumFacing.Axis axisPrimary;
    //private EnumFacing.Axis axisSecondary;

    private EnumFacing thisFacing;

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

    private int debug = 0;

    @Override
    public void update()
    {
        if(this.getWorld().getBlockState(this.pos).getBlock() == VegetableCarnival.autoFarmOperator && this.assembled && !world.isRemote){
            int totalX = getDiameterFromRadiusPlusCenter(this.radiusX);
            int totalY = getDiameterFromRadiusPlusCenter(this.radiusY);

            for (int i = 0; i < this.operationsPerTick; i++) {
                if (this.operatingPos >= totalX * totalY) this.operatingPos = 0;

                //int operatingPosX = (this.operatingPos % totalX) - this.radiusX;
                //int operatingPosY = ((this.operatingPos - (this.operatingPos % totalY)) / totalY) - this.radiusY;

                int operatingPosX = this.operatingPos % totalX;
                int operatingPosY = this.operatingPos / totalY;

                if (debug < 10)
                {
                    System.out.print(operatingPos + ": {" + operatingPosX + ", " + operatingPosY + "}, ");
                }
                else
                {
                    System.out.println("{" + operatingPosX + ", " + operatingPosY + "}, ");
                    debug = 0;
                }

                this.doOperation(operatingPosX, operatingPosY);
            }
        }
    }

    public void assembleFarm() {
        thisFacing = getWorld().getBlockState(this.getPos()).getValue(BlockVCMachine.FACING);

        dissassembleFarm();

        EnumFacing.Axis[] interceptingAxes = Common.getInterceptingAxes(thisFacing.getAxis()); // Axes that intercept Controller's Axis
        EnumFacing[] interceptingFaces = Common.getInterceptingFaces(interceptingAxes); // Faces on above Axes intercepting Controller's Axis
        IBlockState conduitOff = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, false);
        IBlockState conduitOn = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, true);

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

        //this.axisPrimary = interceptingAxes[0];
        //this.axisSecondary = interceptingAxes[1];
        this.farmMachineRadiusPrimary = scannedRadii[0];
        this.farmMachineRadiusSecondary = scannedRadii[2];
        this.radiusX = (farmMachineRadiusPrimary*2) + (farmMachineRadiusPrimary^2);
        this.radiusY = (farmMachineRadiusSecondary*2) + (farmMachineRadiusSecondary^2);
        this.assembled = true;
        this.operationsPerTick = Math.min(farmMachineRadiusPrimary, farmMachineRadiusSecondary);
    }

    public void dissassembleFarm() {
        IBlockState thisState = this.getWorld().getBlockState(this.getPos());
        if(thisState.getMaterial() != Material.AIR && thisState.getBlock() == VegetableCarnival.autoFarmOperator)
            thisFacing = thisState.getValue(BlockVCMachine.FACING);

        EnumFacing.Axis[] interceptingAxes = Common.getInterceptingAxes(thisFacing.getAxis()); // Axes that intercept Controller's Axis
        EnumFacing[] interceptingFaces = Common.getInterceptingFaces(interceptingAxes); // Faces on above Axes intercepting Controller's Axis
        IBlockState conduitOff = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, false);
        IBlockState conduitOn = VegetableCarnival.farmCable.getDefaultState().withProperty(BlockVCCable.VALIDATION, true);

        int[] scannedRadii = new int[]{farmMachineRadiusPrimary, farmMachineRadiusPrimary, farmMachineRadiusSecondary, farmMachineRadiusSecondary};
        for (int direction = 0; direction < scannedRadii.length; direction++)
            for(int length = 1; length <= scannedRadii[direction]; length++) {
                BlockPos posGet = this.getPos().offset(interceptingFaces[direction], length);

                if(this.getWorld().getBlockState(posGet) == conduitOn.withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis())) {
                    this.getWorld().setBlockState(posGet, conduitOff.withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()));
                    ((TileEntityVCComponent) this.getWorld().getTileEntity(posGet)).setMasterPos(null);
                }
            }

        //this.axisPrimary = null;
        //this.axisSecondary = null;
        this.farmMachineRadiusPrimary = 0;
        this.farmMachineRadiusSecondary = 0;
        this.radiusX = 0;
        this.radiusY = 0;
        this.assembled = false;
        this.operationsPerTick = 0;
    }

    private void doOperation(int posPrimaryOffset, int posSecondaryOffset)
    {
        BlockPos masterPos = this.getPos();
        EnumFacing.Axis thisAxis = this.getWorld().getBlockState(masterPos).getValue(BlockVCMachine.FACING).getAxis();
        EnumFacing.Axis[] interceptingAxes = Common.getInterceptingAxes(thisAxis);

        if(isCoordInsideNoZone(posPrimaryOffset, posSecondaryOffset, this.farmMachineRadiusPrimary, this.farmMachineRadiusSecondary)) {
            if(thisAxis == EnumFacing.Axis.Y ||
                (thisAxis == EnumFacing.Axis.X &&
                masterPos.getY()+farmMachineRadiusPrimary >= 0 &&
                masterPos.getY()+farmMachineRadiusPrimary < 256) ||
                (thisAxis == EnumFacing.Axis.Z &&
                masterPos.getY()+farmMachineRadiusSecondary >= 0 &&
                masterPos.getY()+farmMachineRadiusSecondary < 256)
            ) {
                this.getWorld().setBlockState(
                        this.getPos()
                                .offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, interceptingAxes[0]), posPrimaryOffset)
                                .offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, interceptingAxes[1]), posSecondaryOffset),
                        Blocks.DIRT.getDefaultState());
            } else
                VegetableCarnival.logger.info("TE at " + masterPos + " on Axis " + thisAxis + " tried to operate out of Y bounds at ? " + (masterPos.getY()+farmMachineRadiusPrimary) + " or " + (masterPos.getY()+farmMachineRadiusSecondary));
        }
        /* TODO refactor to direction sensitive
        if ( this.getPos().getY() < 254 && ( posX < -this.farmMachineRadiusPrimary || posSecondaryOffset < -this.farmMachineRadiusSecondary ||
                posX > this.farmMachineRadiusPrimary || posSecondaryOffset > this.farmMachineRadiusSecondary))
        {
            FarmCursor farmCursor = new FarmCursor(new BlockPos(this.pos.getX() + posX, this.pos.getY(), this.pos.getZ() + posSecondaryOffset), this.getWorld(), null, 0);

            int limit = 20;

            for(int i = 0; i < limit && this.getPos().getY() - i >= 0 && farmCursor.getOrder() == IFarmOperator.orders.CONTINUE; i++)
            {
                BlockPos keyPos = new BlockPos(this.getPos().getX() + posX, this.getPos().getY() - i, this.getPos().getZ() + posSecondaryOffset);

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

    private static int getDiameterFromRadiusPlusCenter(int radius)
    {
        return (radius*2)+1;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        //thisFacing = EnumFacing.getFront(compound.getInteger("rotation"));

        assembled = compound.getBoolean("operational");

        operatingPos = compound.getInteger("operatingpos");

        operationsPerTick = compound.getInteger("operatingcount");

        farmMachineRadiusPrimary = compound.getInteger("radiusX");
        farmMachineRadiusSecondary = compound.getInteger("radiusY");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        //compound.setInteger("rotation", thisFacing.ordinal());

        compound.setBoolean("operational", assembled);

        compound.setInteger("operatingpos", operatingPos);

        compound.setInteger("operatingcount", operationsPerTick);

        compound.setInteger("radiusX", farmMachineRadiusPrimary);
        compound.setInteger("radiusY", farmMachineRadiusSecondary);

        return compound;
    }
}
