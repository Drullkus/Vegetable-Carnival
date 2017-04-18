package us.drullk.vegetablecarnival.common.util;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class VCConfig {
    private static final String CATEGORY_GENERAL = "General Config";

    public static int maximumRadius;

    public static void initProps(File location)
    {
        File mainFile = new File(location + "/vegetable_carnival.cfg");

        Configuration config = new Configuration(mainFile);

        config.addCustomCategoryComment(CATEGORY_GENERAL, "General options");

        maximumRadius = config.get(CATEGORY_GENERAL, "The Maximum Square Radius of the farm. The controller is excluded.", 5).getInt(5);

        if(maximumRadius < 1)
        {
            maximumRadius = 1;
        }

        if (config.hasChanged())
            config.save();
    }
}
