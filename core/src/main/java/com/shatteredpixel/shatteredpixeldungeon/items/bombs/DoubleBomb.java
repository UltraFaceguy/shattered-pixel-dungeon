/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
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
package com.shatteredpixel.shatteredpixeldungeon.items.bombs;

import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;

public class DoubleBomb extends Bomb {

	{
		image = ItemSpriteSheet.DBL_BOMB;
		stackable = false;
	}

	@Override
	public boolean doPickUp(Hero hero) {
		Bomb bomb = new Bomb();
		bomb.quantity(2);
		if (bomb.doPickUp(hero)) {
			//isaaaaac.... (don't bother doing this when not in english)
			if (Messages.get(this, "name").equals("two bombs"))
				hero.sprite.showStatus(CharSprite.NEUTRAL, "1+1 free!");
			return true;
		}
		return false;
	}
}