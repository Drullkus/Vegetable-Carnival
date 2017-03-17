package us.drullk.vegetablecarnival.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockVCMachine extends Block implements ITileEntityProvider {
    public BlockVCMachine() {
        super(Material.IRON, Blocks.IRON_BLOCK.getMapColor(Blocks.IRON_BLOCK.getDefaultState()));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVCMachine();
    }
}