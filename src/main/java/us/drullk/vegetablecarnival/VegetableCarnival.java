package us.drullk.vegetablecarnival;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.drullk.vegetablecarnival.common.LibMisc;
import us.drullk.vegetablecarnival.common.VCConfig;
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;
import us.drullk.vegetablecarnival.common.item.ItemBlockVC;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

@Mod(modid = LibMisc.MOD_ID, name = LibMisc.MOD_NAME, dependencies = LibMisc.DEPENDENCIES, version = LibMisc.VERSION)
public class VegetableCarnival {
    private static final Logger logger = LogManager.getLogger(LibMisc.MOD_ID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        BlockVCMachine autoFarmOperator = new BlockVCMachine();
        autoFarmOperator.setUnlocalizedName("vegetablecarnival.machine");
        ItemBlockVC ib_vc_machine = new ItemBlockVC(autoFarmOperator, "autoFarmOperatorVC");

        GameRegistry.register(ib_vc_machine);

        VCConfig.initProps(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        GameRegistry.registerTileEntity(TileEntityVCMachine.class, "autoFarmOperatorVC");
    }
}