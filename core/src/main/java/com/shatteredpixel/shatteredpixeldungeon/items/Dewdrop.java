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
package com.shatteredpixel.shatteredpixeldungeon.items;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Hunger;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfFuror;
import com.shatteredpixel.shatteredpixeldungeon.items.rings.RingOfTranquility;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

public class Dewdrop extends Item {
	
	{
		image = ItemSpriteSheet.DEWDROP;
		
		stackable = true;
	}
	
	@Override
	public boolean doPickUp( Hero hero ) {
		
		DewVial vial = hero.belongings.getItem( DewVial.class );

        int value = 1 + (Dungeon.depth - 1) / 5;
        if (hero.subClass == HeroSubClass.WARDEN) {
            value += 2;
        }

        int bonus = RingOfTranquility.getBonus(hero, RingOfTranquility.DewBonus.class);

        value = value + bonus;

        if (hero.HP < hero.HT) {
            if (value >= 0) {
                Sample.INSTANCE.play( Assets.SND_DEWDROP );
                hero.heal(value);
                if (bonus > 0 && !Dungeon.isChallenged(Challenges.NO_FOOD)) {
                    hero.buff(Hunger.class).satisfy(bonus);
                }
            } else {
                Sample.INSTANCE.play( Assets.SND_HIT );
                hero.damage(value, null);
                hero.sprite.emitter().burst(Speck.factory(Speck.DUST), 1);
            }
        } else {
            if (vial == null || vial.isFull()) {
                GLog.i(Messages.get(this, "already_full"));
                return false;
            } else {
                Sample.INSTANCE.play( Assets.SND_DEWDROP );
                vial.collectDew(this);
            }
        }
		hero.spendAndNext( TIME_TO_PICK_UP );
		
		return true;
	}

	@Override
	//max of one dew in a stack
	public Item quantity(int value) {
		quantity = Math.min( value, 1);
		return this;
	}

}
