package us.drullk.vegetablecarnival.common.block;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCComponent;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockVCCable extends BlockRotatedPillar implements ITileEntityProvider {
    public static final PropertyBool VALIDATION = PropertyBool.create("valid");

    public BlockVCCable() {
        super(Material.IRON, Blocks.IRON_BLOCK.getMapColor(Blocks.IRON_BLOCK.getDefaultState()));

        this.setDefaultState(this.blockState.getBaseState().withProperty(AXIS, EnumFacing.Axis.X).withProperty(VALIDATION, false));

        this.isBlockContainer = true;
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return (state.getValue(AXIS).ordinal() << 1) | (state.getValue(VALIDATION) ? 1 : 0);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return meta < 6 ? this.getDefaultState().withProperty(AXIS, EnumFacing.Axis.values()[meta>>>1]).withProperty(VALIDATION, (meta&1) == 1) : this.getDefaultState();
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityVCComponent();
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);

        System.out.println("Invalid? " + (te != null) + " && " + (te instanceof TileEntityVCComponent));

        if (te != null && te instanceof TileEntityVCComponent)
        {
            TileEntityVCMachine master = ((TileEntityVCComponent) te).getMaster();

            System.out.println("Invalidating is " + (master != null));

            if (master != null)
            {
                master.invalidateDependents();
            }
        }

        super.breakBlock(worldIn, pos, state);

        worldIn.removeTileEntity(pos);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, AXIS, VALIDATION);
    }
}
