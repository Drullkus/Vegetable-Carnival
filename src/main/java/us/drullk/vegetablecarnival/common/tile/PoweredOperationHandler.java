package us.drullk.vegetablecarnival.common.tile;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraftforge.energy.EnergyStorage;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
class PoweredOperationHandler extends EnergyStorage {
    private TileEntityVCMachine master;

    PoweredOperationHandler(TileEntityVCMachine master) {
        super(40, 40, 0);

        this.master = master;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if( master == null || (!master.isFarmValidated()) || !this.canReceive() ) {
            return 0;
        } else {
            int energyReceived = maxReceive == 40 ? 40 : 0;
            if(!simulate && energyReceived == 40) master.update();
            return energyReceived;
        }
    }
}
