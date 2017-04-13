package us.drullk.vegetablecarnival.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCComponent;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockVCMachine extends Block implements ITileEntityProvider {
    public BlockVCMachine() {
        super(Material.IRON, Blocks.IRON_BLOCK.getMapColor(Blocks.IRON_BLOCK.getDefaultState()));
        this.isBlockContainer = true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVCMachine();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        System.out.println("breaking block");

        TileEntity te = worldIn.getTileEntity(pos);
        if (te != null && te instanceof TileEntityVCMachine)
        {
            ((TileEntityVCMachine) te).invalidateDependents();
        }

        super.breakBlock(worldIn, pos, state);

        worldIn.removeTileEntity(pos);
    }
}