package us.drullk.vegetablecarnival;

import net.minecraft.block.Block;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import us.drullk.vegetablecarnival.common.LibMisc;
import us.drullk.vegetablecarnival.common.VCConfig;
import us.drullk.vegetablecarnival.common.block.BlockVCCable;
import us.drullk.vegetablecarnival.common.block.BlockVCMachine;
import us.drullk.vegetablecarnival.common.item.ItemBlockVC;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCComponent;
import us.drullk.vegetablecarnival.common.tile.TileEntityVCMachine;

@Mod(modid = LibMisc.MOD_ID, name = LibMisc.MOD_NAME, dependencies = LibMisc.DEPENDENCIES, version = LibMisc.VERSION)
public class VegetableCarnival {
    private static final Logger logger = LogManager.getLogger(LibMisc.MOD_ID);

    public static Block autoFarmOperator;
    public static Block farmCable;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        autoFarmOperator = new BlockVCMachine();
        farmCable = new BlockVCCable();

        autoFarmOperator.setUnlocalizedName("vegetablecarnival.machine");
        farmCable.setUnlocalizedName("vegetablecarnival.cable");

        ItemBlockVC ib_vc_machine = new ItemBlockVC(autoFarmOperator, "vegetablecarnival.machine.item", false);
        ItemBlockVC ib_vc_cable = new ItemBlockVC(farmCable, "vegetablecarnival.cable.item", true);

        GameRegistry.register(ib_vc_machine);
        GameRegistry.register(ib_vc_cable);

        VCConfig.initProps(event.getModConfigurationDirectory());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        GameRegistry.registerTileEntity(TileEntityVCMachine.class, "vegetablecarnival.machine");
        GameRegistry.registerTileEntity(TileEntityVCComponent.class, "vegetablecarnival.cable");
    }
}