package us.drullk.vegetablecarnival.common.util;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import us.drullk.vegetablecarnival.api.FarmCursor;

import javax.annotation.Nullable;

public class Common {
    public static void unpack(EntityPlayer vegetableMan, IInventory inventoryTE)
    {
        for(int i = 0; i < inventoryTE.getSizeInventory() && i < vegetableMan.inventory.getSizeInventory(); i++)
        {
            ItemStack stackIn = inventoryTE.removeStackFromSlot(i);

            if (!vegetableMan.inventory.addItemStackToInventory(stackIn))
            {
                inventoryTE.setInventorySlotContents(i, stackIn);
            }
        }
    }

    public static void repack(EntityPlayer vegetableMan, IInventory inventoryTE, FarmCursor cursor)
    {
        for(int i = 0; i < vegetableMan.inventory.getSizeInventory(); i++)
        {
            ItemStack stackOut = vegetableMan.inventory.getStackInSlot(i);

            if(!isStackNull(stackOut))
            {
                stackOut = vegetableMan.inventory.removeStackFromSlot(i);

                boolean mustDeposit = !isStackNull(stackOut);

                for (int j = 0; mustDeposit && j < vegetableMan.inventory.getSizeInventory() && j < inventoryTE.getSizeInventory(); j++)
                {
                    ItemStack stackTarget = inventoryTE.getStackInSlot(j);

                    if( isStackNull(stackTarget) && inventoryTE.isItemValidForSlot(j, stackOut) )
                    {
                        inventoryTE.setInventorySlotContents(j, stackOut);
                        mustDeposit = false;
                        break;
                    }
                }

                if(mustDeposit)
                {
                    cursor.getWorld().spawnEntityInWorld(new EntityItem(cursor.getWorld(), 0.5, 0.5, 0.5, stackOut));
                }
            }
        }
    }

    public static boolean isStackNull(@Nullable ItemStack stack)
    {
        return stack == null;
    }
}
