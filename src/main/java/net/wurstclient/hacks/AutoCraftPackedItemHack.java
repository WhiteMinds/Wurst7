/*
 * Copyright (c) 2014-2020 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.gui.screen.ingame.CraftingScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({ "auto craftPackedItem", "AutoCraftPackedItem" })
public final class AutoCraftPackedItemHack extends Hack implements UpdateListener {
  public final SliderSetting packCount = new SliderSetting("pack count", "", 4, 1, 9, 1, ValueDisplay.INTEGER);

  private int counter = 0;
  public ItemStack heldItem;

  public AutoCraftPackedItemHack() {
    super("AutoCraftPackedItem", "AutoCraftPackedItem");

    setCategory(Category.OTHER);
    addSetting(packCount);
  }

  @Override
  protected void onEnable() {
    counter = 0;
    heldItem = WurstClient.MC.player.getMainHandStack();
    // EVENTS.add(UpdateListener.class, this);
  }

  @Override
  protected void onDisable() {
    counter = 0;
    // EVENTS.remove(UpdateListener.class, this);
  }

  @Override
  public void onUpdate() {
    counter++;
    if (counter % 4 != 0)
      return;

    if (WurstClient.MC.currentScreen instanceof CraftingScreen) {
      CraftingScreen gui = (CraftingScreen) WurstClient.MC.currentScreen;
      CraftingScreenHandler handler = gui.getScreenHandler();

      int endIdx = handler.getCraftingSlotCount() - 1;
      int lackCount = packCount.getValueI();

      // 移除不需要或多出的物品，统计还差几个目标物品
      for (int i = 0; i <= endIdx; i++) {
        if (i == handler.getCraftingResultSlotIndex())
          continue;

        Slot slot = handler.getSlot(i);
        ItemStack stack = slot.getStack();
        if (stack.isEmpty())
          continue;

        if (stack.getItem().equals(heldItem.getItem()) && lackCount > 0) {
          lackCount--;
        } else {
          // 移除
          IMC.getInteractionManager().windowClick_QUICK_MOVE(slot.id);
          gui.refreshRecipeBook();
          // TODO: 如果 lackCount > 0，将换入目标物品
          // IMC.getInteractionManager().windowClick_SWAP(slot.id);
          return;
        }
      }

      for (int i = endIdx + 1; i < handler.slots.size(); i++) {
        if (lackCount <= 0)
          break;

        Slot slot = handler.getSlot(i);
        ItemStack stack = slot.getStack();
        if (stack.isEmpty())
          continue;

        if (!stack.getItem().equals(heldItem.getItem()))
          continue;

        // IMC.getInteractionManager().windowClick_QUICK_MOVE(slot.id);

        List<Slot> emptySlots = handler.slots.stream().filter(s -> s.getStack().isEmpty()).collect(Collectors.toList());
        if (emptySlots.size() <= 0)
          break;

        IMC.getInteractionManager().windowClick_PICKUP(slot.id);
        IMC.getInteractionManager().windowClick_PICKUP(emptySlots.get(0).id);
        // gui.refreshRecipeBook();
        lackCount--;
        return;
      }
    }
  }
}
