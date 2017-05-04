package us.drullk.vegetablecarnival.common.tile.operator;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.state.IBlockState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
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
public class UseOperator implements IFarmOperator
{
    @Override
    public FarmCursor doOperation(final FarmCursor cursor, final TileEntityVCMachine machine, final BlockPos keyPos)
    {
        TileEntity te = cursor.getWorld().getTileEntity(keyPos.offset(cursor.getFacing(), -1));

        World thisWorld = cursor.getWorld();
        BlockPos thisPos = cursor.getPos();
        IBlockState thisState = thisWorld.getBlockState(thisPos);
        FakePlayer vegetableMan = machine.getFakePlayer();

        Common.unpack(vegetableMan, te, cursor);

        ItemStack stackHeld = vegetableMan.getHeldItemMainhand();

        PlayerInteractEvent.RightClickBlock rightClickEvent = ForgeHooks.onRightClickBlock(vegetableMan, vegetableMan.getActiveHand(), stackHeld, thisPos, cursor.getFacing(), ForgeHooks.rayTraceEyeHitVec(vegetableMan, 1.0D));

        if(!rightClickEvent.isCanceled())
        {
            Item item = Common.isStackNull(stackHeld)?null:stackHeld.getItem();

            EnumActionResult useFirstResult = item == null?EnumActionResult.PASS:item.onItemUseFirst(stackHeld, vegetableMan, thisWorld, thisPos, cursor.getFacing(), 0.5f, 0.5f, 0.5f, vegetableMan.getActiveHand());

            if(useFirstResult == EnumActionResult.PASS)
            {
                boolean bypass = true;
                ItemStack[] heldItems = new ItemStack[]{vegetableMan.getHeldItemMainhand(), vegetableMan.getHeldItemOffhand()};

                for (ItemStack heldItem : heldItems) {
                    bypass = bypass && (heldItem == null || heldItem.getItem().doesSneakBypassUse(heldItem, thisWorld, thisPos, vegetableMan));
                }

                EnumActionResult actionResult = EnumActionResult.PASS;
                if(!vegetableMan.isSneaking() || bypass || rightClickEvent.getUseBlock() == Event.Result.ALLOW) {
                    if(rightClickEvent.getUseBlock() != Event.Result.DENY && thisState.getBlock().onBlockActivated(thisWorld, thisPos, thisState, vegetableMan, vegetableMan.getActiveHand(), stackHeld, cursor.getFacing(), 0.5f, 0.5f, 0.5f))
                    {
                        actionResult = EnumActionResult.SUCCESS;
                    }
                }

                if(!Common.isStackNull(stackHeld)) {
                    if(!((actionResult == EnumActionResult.SUCCESS || rightClickEvent.getUseItem() == Event.Result.DENY) && (actionResult != EnumActionResult.SUCCESS || rightClickEvent.getUseItem() != Event.Result.ALLOW))) {
                        stackHeld.onItemUse(vegetableMan, thisWorld, thisPos, vegetableMan.getActiveHand(), cursor.getFacing(), 0.5f, 0.5f, 0.5f);
                    }
                }
            }
        }

        Common.repack(vegetableMan, te, cursor);

        return new FarmCursor(cursor.getPos(), cursor.getWorld(), cursor, 1, cursor.getFacing());
    }
}