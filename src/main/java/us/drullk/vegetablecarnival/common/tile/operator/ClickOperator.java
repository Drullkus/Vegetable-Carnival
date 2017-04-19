package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import us.drullk.vegetablecarnival.api.FarmCursor;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;
import us.drullk.vegetablecarnival.common.util.Common;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class ClickOperator implements IFarmOperator {
    @Override
    public FarmCursor doOperation(FarmCursor cursor, TileEntityVCMachine machine, BlockPos keyPos) {
        TileEntity te = cursor.getWorld().getTileEntity(keyPos.down());

        if(te != null && te instanceof IInventory)
        {
            IInventory inventoryTE = (IInventory) te;

            World thisWorld = cursor.getWorld();
            BlockPos thisPos = cursor.getPos();
            IBlockState thisState = thisWorld.getBlockState(thisPos);
            FakePlayer vegetableMan = machine.getFakePlayer();

            Common.unpack(vegetableMan, inventoryTE);

            //thisState.getBlock().onBlockClicked(thisWorld, thisPos, vegetableMan);

            Common.repack(vegetableMan, inventoryTE, cursor);
        }



        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1);
    }
}