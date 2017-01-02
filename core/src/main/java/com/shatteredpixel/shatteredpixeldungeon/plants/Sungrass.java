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
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShaftParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfHealing;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;

public class Sungrass extends Plant {
	
	{
		image = 4;
	}
	
	@Override
	public void activate() {
		Char ch = Actor.findChar(pos);

		if (ch == null) {
			return;
		}
		
		if (ch == Dungeon.hero) {
			Buff.apply( ch, Health.class ).boost(ch.HT / 2);
		}
		
		if (Dungeon.visible[pos]) {
			CellEmitter.get( pos ).start( ShaftParticle.FACTORY, 0.2f, 3 );
		}
	}
	
	public static class Seed extends Plant.Seed {
		{
			image = ItemSpriteSheet.SEED_SUNGRASS;

			plantClass = Sungrass.class;
			alchemyClass = PotionOfHealing.class;

			bones = true;
		}
	}
	
	public static class Health extends Buff {
		
		private static final float STEP = 1f;
		
		private int pos;
		private int healCurr = 1;
		private int count = 0;
		private int level;

		{
			type = buffType.POSITIVE;
		}
		
		@Override
		public boolean act() {
			if (target.pos != pos) {
				detach();
			}
			if (count == 2) {
                int amount = 1 + (int)(healCurr * 0.01 * target.HT);
				if (level <= amount) {
					target.heal(level);
					detach();
				} else {
					target.heal(amount);
					level -= amount;
					if (healCurr < target.HT / 10) {
						healCurr++;
					}
				}
				if (target.HP == target.HT && target instanceof Hero){
					((Hero)target).resting = false;
				}
				count = 0;
			} else {
				count++;
			}
			if (level <= 0)
				detach();
			spend( STEP );
			return true;
		}

		public int absorb( int damage ) {
			level -= damage;
			if (level <= 0)
				detach();
			return damage;
		}

		public void boost( int amount ){
			level += amount;
			pos = target.pos;
		}
		
		@Override
		public int icon() {
			return BuffIndicator.SUN_HEAL;
		}
		
		@Override
		public String toString() {
			return Messages.get(this, "name");
		}

		@Override
		public String desc() {
			return Messages.get(this, "desc", level);
		}

		private static final String POS	= "pos";
		private static final String HEALCURR = "healCurr";
		private static final String COUNT = "count";
		private static final String LEVEL = "level";

		@Override
		public void storeInBundle( Bundle bundle ) {
			super.storeInBundle( bundle );
			bundle.put( POS, pos );
			bundle.put( HEALCURR, healCurr);
			bundle.put( COUNT, count);
			bundle.put( LEVEL, level);
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
			super.restoreFromBundle( bundle );
			pos = bundle.getInt( POS );
			healCurr = bundle.getInt( HEALCURR );
			count = bundle.getInt( COUNT );
			level = bundle.getInt( LEVEL );

		}
	}
}
