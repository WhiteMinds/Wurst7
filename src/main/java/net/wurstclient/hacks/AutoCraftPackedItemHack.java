/*
 * Copyright (c) 2014-2020 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.item.ItemStack;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.WurstClient;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({ "auto craftPackedItem", "AutoCraftPackedItem" })
public final class AutoCraftPackedItemHack extends Hack {
  public final SliderSetting packCount = new SliderSetting("每次打包所需数量", "", 9, 1, 9, 1, ValueDisplay.INTEGER);

  public ItemStack heldItem;

  public AutoCraftPackedItemHack() {
    super("自动合成打包物品", "手持要打包的物品，然后启用该功能，再去工作台点击 Pack 就会开始");

    setCategory(Category.OTHER);
    addSetting(packCount);
  }

  @Override
  protected void onEnable() {
    heldItem = WurstClient.MC.player.getMainHandStack();
  }

  @Override
  protected void onDisable() {
	  heldItem = null;
  }
}
