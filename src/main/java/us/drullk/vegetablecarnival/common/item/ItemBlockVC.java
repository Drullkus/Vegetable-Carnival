package us.drullk.vegetablecarnival.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBlockVC extends ItemBlock {
    public ItemBlockVC(Block block, boolean hasSubs) {
        super(block);
        hasSubtypes = hasSubs;
    }
}
