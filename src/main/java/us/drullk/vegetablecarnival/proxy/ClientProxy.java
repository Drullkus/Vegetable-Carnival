package us.drullk.vegetablecarnival.proxy;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import us.drullk.vegetablecarnival.VegetableCarnival;

import javax.annotation.Nonnull;

public class ClientProxy extends CommonProxy {
    @Override
    public void preInit()
    {
        registerItemModel(Item.getItemFromBlock(VegetableCarnival.autoFarmOperator));
        registerItemModel(Item.getItemFromBlock(VegetableCarnival.farmCable));
    }

    private static ResourceLocation registerItemModel(Item item)
    {
        ResourceLocation itemLocation = null;
        if (item != null)
        {
            itemLocation = item.getRegistryName();
        }
        if (itemLocation != null)
        {
            itemLocation = registerIt(item, itemLocation);
        }

        return itemLocation;
    }

    private static ResourceLocation registerIt(Item item, final ResourceLocation location)
    {
        ModelLoader.setCustomMeshDefinition(item, new ItemMeshDefinition()
        {
            @Nonnull
            @Override
            public ModelResourceLocation getModelLocation(@Nonnull ItemStack stack)
            { return new ModelResourceLocation(location, "inventory"); }
        });

        //System.out.println(location);

        ModelLoader.registerItemVariants(item, location);

        return location;
    }
}
