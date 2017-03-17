package us.drullk.vegetablecarnival.common;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class VCConfig {
    private static final String CATEGORY_GENERAL = "General Config";

    public static void initProps(File location)
    {
        File mainFile = new File(location + "/vegetable_carnival.cfg");

        Configuration config = new Configuration(mainFile);

        config.addCustomCategoryComment(CATEGORY_GENERAL, "General options");

        //rfCostMultiplier = config.get(CATEGORY_TE, "The Multiplier for RF Cost for Magma Crucible recipe adaptation", 5).getInt(5);

        if (config.hasChanged())
            config.save();
    }
}
