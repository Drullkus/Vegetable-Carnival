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
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.block.BlockVCCable;
import us.drullk.vegetablecarnival.common.block.BlockVCComponent;
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;
import us.drullk.vegetablecarnival.common.util.Common;

import javax.annotation.ParametersAreNonnullByDefault;

import static us.drullk.vegetablecarnival.common.util.Common.getDiameterFromRadiusPlusCenter;
import static us.drullk.vegetablecarnival.common.util.Common.isCoordOutOfNoZone;
import static us.drullk.vegetablecarnival.common.util.VCConfig.maximumRadius;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileEntityVCMachine extends TileEntity {
    //private static final String FAKE_PLAYER_NAME = "[VEGETABLE_MAN]";
    //private static final UUID FAKE_PLAYER_UUID = new UUID(0x98207c63a54c4f1dL, 0x965f71e7a5f7f1aaL);

    //private final FakePlayer vegetableCarnivalFakePlayer = FakePlayerFactory.get((WorldServer) this.getWorld(), new GameProfile(FAKE_PLAYER_UUID, FAKE_PLAYER_NAME));

    private boolean isNew = true;

    private int operationsPerTick = 0;

    private int radiusX;
    private int radiusY;

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

    public void update()
    {
        if (isNew){
            assembleFarm();
        }

        if(this.getWorld().getBlockState(this.pos).getBlock() == VegetableCarnival.autoFarmOperator && this.assembled && !world.isRemote)
            for (int i = 0; i < this.operationsPerTick; i++) {
                if (this.operatingPos >= getDiameterFromRadiusPlusCenter(this.radiusX) * getDiameterFromRadiusPlusCenter(this.radiusY))
                    this.operatingPos = 0;

                this.doOperation((this.operatingPos%getDiameterFromRadiusPlusCenter(this.radiusX))-radiusX, (this.operatingPos/getDiameterFromRadiusPlusCenter(this.radiusX))-radiusY);
            }
    }

    public void assembleFarm() {
        IBlockState thisState = getWorld().getBlockState(this.getPos());
        thisFacing = thisState.getValue(BlockVCMachine.FACING);

        //dissassembleFarm(thisState);

        EnumFacing.Axis[] interceptingAxes = Common.getInterceptingAxes(thisFacing.getAxis()); // Axes that intercept Controller's Axis
        EnumFacing[] interceptingFaces = Common.getInterceptingFaces(interceptingAxes); // Faces on above Axes intercepting Controller's Axis

        BlockPos masterPos = this.getPos(); // Less getPos() getPos() repetition
        int[] scannedRadii = new int[4];
        for (int direction = 0; direction < scannedRadii.length; direction++) {
            for (int length = 1; length <= this.maxMachineSize; length++) {
                BlockPos posCheck = masterPos.offset(interceptingFaces[direction], length);
                IBlockState stateCheck = this.getWorld().getBlockState(posCheck);

                if(stateCheck.getBlock() instanceof BlockVCComponent && stateCheck == stateCheck.withProperty(BlockVCCable.VALIDATION, false).withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()))
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
                IBlockState state = this.getWorld().getBlockState(posGet);
                this.getWorld().setBlockState(posGet, state.withProperty(BlockVCCable.VALIDATION, true).withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()));
                ((TileEntityVCComponent) this.getWorld().getTileEntity(posGet)).setMasterPos(masterPos);
            }

        //this.axisPrimary = interceptingAxes[0];
        //this.axisSecondary = interceptingAxes[1];
        this.farmMachineRadiusPrimary = scannedRadii[0];
        this.farmMachineRadiusSecondary = scannedRadii[2];
        this.radiusX = (farmMachineRadiusPrimary*2) + (farmMachineRadiusPrimary*farmMachineRadiusPrimary);
        this.radiusY = (farmMachineRadiusSecondary*2) + (farmMachineRadiusSecondary*farmMachineRadiusSecondary);
        this.assembled = true;
        this.operationsPerTick = Math.min(farmMachineRadiusPrimary, farmMachineRadiusSecondary);
    }

    public void dissassembleFarm(IBlockState state) {
        if (!isFarmValidated()) return;

        if(state.getMaterial() != Material.AIR && state.getBlock() == VegetableCarnival.autoFarmOperator)
            thisFacing = state.getValue(BlockVCMachine.FACING);

        EnumFacing.Axis[] interceptingAxes = Common.getInterceptingAxes(thisFacing.getAxis()); // Axes that intercept Controller's Axis
        EnumFacing[] interceptingFaces = Common.getInterceptingFaces(interceptingAxes); // Faces on above Axes intercepting Controller's Axis

        int[] scannedRadii = new int[]{farmMachineRadiusPrimary, farmMachineRadiusPrimary, farmMachineRadiusSecondary, farmMachineRadiusSecondary};
        for (int direction = 0; direction < scannedRadii.length; direction++)
            for(int length = 1; length <= scannedRadii[direction]; length++) {
                BlockPos posGet = this.getPos().offset(interceptingFaces[direction], length);
                IBlockState thisState = this.getWorld().getBlockState(posGet);

                if(thisState.getBlock() instanceof BlockVCComponent && thisState == thisState.withProperty(BlockVCCable.VALIDATION, true).withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()))
                    this.getWorld().setBlockState(posGet, thisState.withProperty(BlockVCCable.VALIDATION, false).withProperty(BlockVCCable.AXIS, interceptingFaces[direction].getAxis()));
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
        EnumFacing thisFacing = this.getWorld().getBlockState(masterPos).getValue(BlockVCMachine.FACING);
        EnumFacing.Axis thisAxis = thisFacing.getAxis();
        EnumFacing.Axis[] interceptingAxes = Common.getInterceptingAxes(thisAxis);

        if(isCoordOutOfNoZone(posPrimaryOffset, posSecondaryOffset, this.farmMachineRadiusPrimary, this.farmMachineRadiusSecondary)) {
            if(thisAxis == EnumFacing.Axis.Y ||
                (thisAxis == EnumFacing.Axis.X &&
                masterPos.getY()+posPrimaryOffset >= 0 &&
                masterPos.getY()+posPrimaryOffset < 256) ||
                (thisAxis == EnumFacing.Axis.Z &&
                masterPos.getY()+posSecondaryOffset >= 0 &&
                masterPos.getY()+posSecondaryOffset < 256)
            ) {
                BlockPos cursorPos = this.getPos().offset(thisFacing)
                        .offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, interceptingAxes[0]), posPrimaryOffset)
                        .offset(EnumFacing.getFacingFromAxis(EnumFacing.AxisDirection.POSITIVE, interceptingAxes[1]), posSecondaryOffset);
                BlockPos thisPos;

                if (getWorld().getBlockState(getPos().offset(thisFacing.getOpposite())) == Blocks.GLOWSTONE.getDefaultState())
                    thisPos = this.getPos().offset(thisFacing.getOpposite(), 2);
                else
                    thisPos = cursorPos;

                if (thisPos.getY() < 0 || thisPos.getY() > 255 ) {
                    System.out.println("SAFEGUARD FAILURE! TE at " + masterPos + " on Axis " + thisAxis + " tried to operate out of Y bounds at " + thisPos);
                    this.operatingPos++;
                    return;
                }

                FarmCursor farmCursor = new FarmCursor(cursorPos, this.getWorld(), null, 0, thisFacing);
                BlockPos keyPos = null;

                int limit = 20;
                for(int i = 0; (keyPos == null || thisPos.offset(thisFacing, 0-i-farmCursor.getBlocksToSkip()).getY() >= 0) && i < limit && farmCursor.getOrder() == IFarmOperator.orders.CONTINUE; i++) {
                    keyPos = thisPos.offset(thisFacing, 0-i-farmCursor.getBlocksToSkip());
                    IBlockState keyState = this.getWorld().getBlockState(keyPos);
                    IFarmOperator operator = VegetableCarnival.getOperation(keyState);

                    if(operator != null) {
                        farmCursor = operator.doOperation(farmCursor, this, keyPos);

                        i += farmCursor.getBlocksToSkip();
                        limit += farmCursor.getBlocksToSkip();
                    }
                }
            }
        }

        this.operatingPos++;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);

        //thisFacing = EnumFacing.getFront(compound.getInteger("rotation"));

        assembled = compound.getBoolean("operational");

        operatingPos = compound.getInteger("operatingpos");

        operationsPerTick = compound.getInteger("operatingcount");

        radiusX = compound.getInteger("radiusX");
        radiusY = compound.getInteger("radiusY");

        farmMachineRadiusPrimary = compound.getInteger("machineRadiusX");
        farmMachineRadiusSecondary = compound.getInteger("machineRadiusY");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);

        compound.setBoolean("isNew", isNew);

        //compound.setInteger("rotation", thisFacing.ordinal());

        compound.setBoolean("operational", assembled);

        compound.setInteger("operatingpos", operatingPos);

        compound.setInteger("operatingcount", operationsPerTick);

        compound.setInteger("machineRadiusX", farmMachineRadiusPrimary);
        compound.setInteger("machineRadiusY", farmMachineRadiusSecondary);

        return compound;
    }
}
