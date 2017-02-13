/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015  Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2016 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.shatteredpixel.shatteredpixeldungeon.plants;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.items.food.Blandfruit;
import com.shatteredpixel.shatteredpixeldungeon.items.food.MysteryMushroom;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class MushroomCluster extends Plant {

	{
		image = 14;
	}

	@Override
	public void activate() {
		Dungeon.level.drop( new MysteryMushroom(), pos ).sprite.drop();
	}

	//seed is never dropped, only care about plant class
	public static class Seed extends Plant.Seed {
		{
			plantClass = MushroomCluster.class;
		}
	}
}
