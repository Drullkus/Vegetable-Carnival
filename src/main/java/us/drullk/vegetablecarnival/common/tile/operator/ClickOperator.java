package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
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
        TileEntity te = cursor.getWorld().getTileEntity(keyPos.offset(cursor.getFacing(), -1));

        if(te != null && te instanceof IInventory)
        {
            IInventory inventoryTE = (IInventory) te;

            World thisWorld = cursor.getWorld();
            BlockPos thisPos = cursor.getPos();
            FakePlayer vegetableMan = machine.getFakePlayer();

            Common.unpack(vegetableMan, inventoryTE);

            PlayerInteractEvent.LeftClickBlock leftClickEvent = ForgeHooks.onLeftClickBlock(vegetableMan, thisPos, cursor.getFacing(), ForgeHooks.rayTraceEyeHitVec(vegetableMan, 1.0D));

            IBlockState thisState = thisWorld.getBlockState(thisPos);

            if(leftClickEvent.isCanceled())
            {
                thisWorld.notifyBlockUpdate(thisPos, thisState, thisState, 3);
            }
            else
            {
                if(!thisState.getBlock().isAir(thisState, thisWorld, thisPos))
                {
                    if(leftClickEvent.getUseBlock() != Event.Result.DENY)
                    {
                        thisState.getBlock().onBlockClicked(thisWorld, thisPos, vegetableMan);

                        thisWorld.extinguishFire(null, thisPos, cursor.getFacing());
                    }
                    else
                    {
                        thisWorld.notifyBlockUpdate(thisPos, thisState, thisState, 3);
                    }
                }

                thisState = thisWorld.getBlockState(thisPos);

                if(leftClickEvent.getUseItem() == Event.Result.DENY)
                {
                    thisWorld.notifyBlockUpdate(thisPos, thisState, thisState, 3);
                }
            }

            Common.repack(vegetableMan, inventoryTE, cursor);
        }

        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1, cursor.getFacing());
    }
}