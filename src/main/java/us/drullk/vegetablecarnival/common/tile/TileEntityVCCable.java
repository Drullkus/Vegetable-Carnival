package us.drullk.vegetablecarnival.common.tile;

import cofh.api.energy.IEnergyReceiver;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TileEntityVCCable extends TileEntityVCComponent implements IEnergyReceiver {
    public TileEntityVCCable() {
        super();
    }

    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        return (capability == CapabilityEnergy.ENERGY && getMaster() != null) || super.hasCapability(capability, facing);
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY && getMaster() != null ? CapabilityEnergy.ENERGY.cast(new PoweredOperationHandler(getMaster())) : super.getCapability(capability, facing);
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        if( getMaster() == null || (!getMaster().isFarmValidated()) ) {
            return 0;
        } else {
            int energyReceived = maxReceive == 40 ? 40 : 0;
            if(!simulate && energyReceived == 40) getMaster().update();
            return energyReceived;
        }
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return 40;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return getMaster() != null;
    }
}
