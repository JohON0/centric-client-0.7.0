package centric.pl.johon0.utils.player;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

import java.util.ArrayList;
import java.util.List;

public class ShulkerUtil {

    public static List<ItemStack> getItemInShulker(ItemStack s) {

        CompoundNBT compoundnbt = s.getChildTag("BlockEntityTag");

        if (compoundnbt != null) {
            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                    return nonnulllist.stream().filter(item -> item.getItem() != Items.AIR).toList();

            }
        }

        return new ArrayList<>();
    }

}
