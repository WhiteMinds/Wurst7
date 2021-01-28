/*
 * Copyright (c) 2014-2020 Wurst-Imperium and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package net.wurstclient.hacks;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.wurstclient.Category;
import net.wurstclient.SearchTags;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.hack.Hack;
import net.wurstclient.settings.SliderSetting;
import net.wurstclient.settings.SliderSetting.ValueDisplay;

@SearchTags({ "auto sell", "AutoSell" })
public final class AutoSellHack extends Hack implements UpdateListener {
	private final SliderSetting intervalSec = new SliderSetting("调用间隔（秒）", "", 60, 1, 360, 1, ValueDisplay.INTEGER);

	private int cooldown = 0;

	public AutoSellHack() {
		super("自动出售", "自动调用 /sell all");

		setCategory(Category.CHAT);
		addSetting(intervalSec);
	}

	@Override
	public void onEnable() {
		cooldown = 0;
		EVENTS.add(UpdateListener.class, this);
	}

	@Override
	public void onDisable() {
		EVENTS.remove(UpdateListener.class, this);
	}

	@Override
	public void onUpdate() {
		if (cooldown > 0) {
			cooldown--;
			return;
		}

		cooldown = 20 * intervalSec.getValueI();
		// say
		String message = "/sell all";
		ChatMessageC2SPacket packet = new ChatMessageC2SPacket(message);
		MC.getNetworkHandler().sendPacket(packet);
	}
}
