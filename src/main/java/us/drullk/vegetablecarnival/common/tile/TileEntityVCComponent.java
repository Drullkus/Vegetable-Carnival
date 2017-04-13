package us.drullk.vegetablecarnival.common.tile;

import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public class TileEntityVCComponent extends TileEntity {
    private TileEntityVCMachine master;

    @Nullable
    public TileEntityVCMachine getMaster()
    {
        return master;
    }

    void setMaster(TileEntityVCMachine te)
    {
        this.master = te;
    }
}
