package us.drullk.vegetablecarnival.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import us.drullk.vegetablecarnival.VegetableCarnival;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCCable;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCComponent;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockVCCable extends BlockVCComponent {
    public BlockVCCable() {
        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X).withProperty(VALIDATION, false));
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (player.isCreative()) {
            TileEntity te = world.getTileEntity(pos);
            if (te != null && te instanceof TileEntityVCCable) {
                TileEntityVCMachine master = ((TileEntityVCCable) te).getMaster();
                if (master != null)
                {
                    if (!world.isRemote){
                        master.update();
                    }
                    return true;
                }
            }
        }

        return false;
    }
}
