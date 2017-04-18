package us.drullk.vegetablecarnival;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.drullk.vegetablecarnival.api.IFarmOperator;
import us.drullk.vegetablecarnival.common.tile.operator.HeightOperator;
import us.drullk.vegetablecarnival.common.tile.operator.StopOperator;
import us.drullk.vegetablecarnival.common.tile.operator.UseOperator;
import us.drullk.vegetablecarnival.common.util.LibMisc;
import us.drullk.vegetablecarnival.common.util.VCConfig;
import us.drullk.vegetablecarnival.common.block.BlockVCCable;
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;
import us.drullk.vegetablecarnival.common.item.ItemBlockVC;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCComponent;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;
import us.drullk.vegetablecarnival.proxy.CommonProxy;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;

import static us.drullk.vegetablecarnival.common.util.LibMisc.CLIENT_PROXY;
import static us.drullk.vegetablecarnival.common.util.LibMisc.COMMON_PROXY;
import static us.drullk.vegetablecarnival.common.util.LibMisc.MOD_ID;

@Mod(modid = MOD_ID, name = LibMisc.MOD_NAME, dependencies = LibMisc.DEPENDENCIES, version = LibMisc.VERSION)
public class VegetableCarnival {
    private static final Logger logger = LogManager.getLogger(MOD_ID);

    @Mod.Instance(MOD_ID)
    public static VegetableCarnival instance;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY, modId = MOD_ID)
    public static CommonProxy proxy;

    public static Block autoFarmOperator;
    public static Block farmCable;

    private static IdentityHashMap<IBlockState, IFarmOperator> mainOperators = new IdentityHashMap<>();

    @Nullable
    public static IFarmOperator getOperation(IBlockState blockState)
    {
        return mainOperators.get(blockState);
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        autoFarmOperator = new BlockVCMachine();
        farmCable = new BlockVCCable();

        autoFarmOperator.setUnlocalizedName("vcmachine");
        farmCable.setUnlocalizedName("vccable");

        ItemBlockVC ib_vc_machine = new ItemBlockVC(autoFarmOperator, false);
        ItemBlockVC ib_vc_cable = new ItemBlockVC(farmCable, true);

        register(autoFarmOperator, "vcmachine");
        register(ib_vc_machine, "vcmachine");
        register(farmCable, "vccable");
        register(ib_vc_cable, "vccable");

        VCConfig.initProps(event.getModConfigurationDirectory());

        proxy.preInit();

        mainOperators.put(Blocks.EMERALD_BLOCK.getDefaultState(), new HeightOperator(new int[]{0, 1, 0}));
        mainOperators.put(Blocks.DIAMOND_BLOCK.getDefaultState(), new HeightOperator(new int[]{0, -1, 0}));

        mainOperators.put(Blocks.NETHER_BRICK.getDefaultState(), new StopOperator());

        mainOperators.put(Blocks.LAPIS_BLOCK.getDefaultState(), new UseOperator());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        GameRegistry.registerTileEntity(TileEntityVCMachine.class, "vcmachine");
        GameRegistry.registerTileEntity(TileEntityVCComponent.class, "vccable");

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLInitializationEvent event)
    {
        proxy.postInit();
    }

    private static <T extends IForgeRegistryEntry<?>> T register(T thing, String name)
    {
        thing.setRegistryName(new ResourceLocation(MOD_ID, name));
        GameRegistry.register(thing);
        return thing;
    }
}