package us.drullk.vegetablecarnival;

import net.minecraft.block.Block;
import net.minecraft.block.BlockQuartz;
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
import us.drullk.vegetablecarnival.common.block.BlockVCComponent;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCCable;
import us.drullk.vegetablecarnival.common.tile.operator.*;
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
    public static final Logger logger = LogManager.getLogger(MOD_ID);

    @Mod.Instance(MOD_ID)
    public static VegetableCarnival instance;

    @SidedProxy(clientSide = CLIENT_PROXY, serverSide = COMMON_PROXY, modId = MOD_ID)
    public static CommonProxy proxy;

    public static Block autoFarmOperator;
    public static Block farmPower;
    public static Block farmCable;

    private static IdentityHashMap<IBlockState, IFarmOperator> mainOperators = new IdentityHashMap<>();

    @Nullable
    public static IFarmOperator getOperation(IBlockState blockState) {
        return mainOperators.get(blockState);
    }

    public static boolean setOperation(IBlockState state, IFarmOperator operator)
    {
        if(mainOperators.containsKey(state)) {
            System.out.println(state + " already exists in map! Use something else!");
            return false;
        }
        else mainOperators.put(state, operator);
        return true;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        autoFarmOperator = new BlockVCMachine();
        farmPower = new BlockVCCable();
        farmCable = new BlockVCComponent();

        autoFarmOperator.setUnlocalizedName("vcmachine");
        farmPower.setUnlocalizedName("vccable");
        farmCable.setUnlocalizedName("vccomponent");

        ItemBlockVC ib_vc_machine = new ItemBlockVC(autoFarmOperator, false);
        ItemBlockVC ib_vc_cable = new ItemBlockVC(farmPower, false);
        ItemBlockVC ib_vc_comp = new ItemBlockVC(farmCable, false);

        register(autoFarmOperator, "vcmachine");
        register(ib_vc_machine, "vcmachine");
        register(farmPower, "vccable");
        register(ib_vc_cable, "vccable");
        register(farmCable, "vccomponent");
        register(ib_vc_comp, "vccomponent");

        VCConfig.initProps(event.getModConfigurationDirectory());

        proxy.preInit();

        setOperation(Blocks.EMERALD_BLOCK.getDefaultState(), new HeightOperator(1));
        setOperation(Blocks.DIAMOND_BLOCK.getDefaultState(), new HeightOperator(-1));

        setOperation(Blocks.BEDROCK.getDefaultState(), new StopOperator());
        setOperation(Blocks.OBSIDIAN.getDefaultState(), new StopOperator());

        setOperation(Blocks.LAPIS_BLOCK.getDefaultState(), new UseOperator());

        setOperation(Blocks.QUARTZ_BLOCK.getDefaultState(), new BreakOperator());

        setOperation(Blocks.NETHER_BRICK.getDefaultState(), new ClickOperator());

        setOperation(Blocks.COBBLESTONE.getDefaultState(), new TillOperator());

        setOperation(Blocks.QUARTZ_BLOCK.getDefaultState().withProperty(BlockQuartz.VARIANT, BlockQuartz.EnumType.CHISELED), new HarvestOperator());

        setOperation(Blocks.WOOL.getStateFromMeta(0), new SkipOperator(1));
        setOperation(Blocks.WOOL.getStateFromMeta(1), new SkipOperator(2));
        setOperation(Blocks.WOOL.getStateFromMeta(2), new SkipOperator(3));
        setOperation(Blocks.WOOL.getStateFromMeta(3), new SkipOperator(4));

        //setOperation(Blocks.CLAY.getDefaultState(), new SetBlockOperator(Blocks.HARDENED_CLAY.getDefaultState()));

        setOperation(Blocks.SOUL_SAND.getDefaultState(), new TreeChOperator());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        GameRegistry.registerTileEntity(TileEntityVCMachine.class, "vcmachine");
        GameRegistry.registerTileEntity(TileEntityVCCable.class, "vccable");
        GameRegistry.registerTileEntity(TileEntityVCComponent.class, "vccomponent");

        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLInitializationEvent event) {
        proxy.postInit();
    }

    private static <T extends IForgeRegistryEntry<?>> T register(T thing, String name) {
        thing.setRegistryName(new ResourceLocation(MOD_ID, name));
        GameRegistry.register(thing);
        return thing;
    }
}