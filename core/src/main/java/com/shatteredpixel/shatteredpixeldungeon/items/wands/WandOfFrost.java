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
package com.shatteredpixel.shatteredpixeldungeon.items.wands;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Chill;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Frost;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.MagicMissile;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SnowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.MagesStaff;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Callback;
import com.watabou.utils.PathFinder;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class WandOfFrost extends DamageWand {

	{
		image = ItemSpriteSheet.WAND_FROST;

        collisionProperties = Ballistica.PROJECTILE;
	}

	public int min(int lvl){
		return 2+lvl;
	}

	public int max(int lvl){
		return 6+3*lvl;
	}

	@Override
	protected void onZap(Ballistica bolt) {

		int pos = bolt.collisionPos;
		Heap heap = Dungeon.level.heaps.get(pos);

		if (heap != null) {
			heap.freeze();
		}

		for (int i : PathFinder.NEIGHBOURS9) {
			if (Level.passable[pos + i]) {
				CellEmitter.get( pos + i ).start( SnowParticle.FACTORY, 0.1f, 8 );
				Char ch = Actor.findChar(pos + i);

                if (ch == null) {
                    continue;
                }

                if (ch.buff(Chill.class) != null) {
                    if (ch.buff(Chill.class).speedFactor() <= 0.3f) {
                        if (Level.water[ch.pos]) {
                            Buff.affect(ch, Frost.class, Random.Float(3.5f, 6.0f));
                        } else {
                            Buff.affect(ch, Frost.class, Random.Float(1.0f, 2.0f));
                        }
                        continue;
                    }
                }

                if (ch.buff(Frost.class) != null) {
                    if (Level.water[ch.pos]) {
                        Buff.affect(ch, Frost.class, Random.Float(3.5f, 6.0f));
                    } else {
                        Buff.affect(ch, Frost.class, Random.Float(1.0f, 2.0f));
                    }
                    continue;
                }

                if (ch.pos == pos) {
                    Buff.add(ch, Chill.class, 3 + (float)level() / 2);
                    ch.damage(damageRoll(), this);
                } else {
                    Buff.add(ch, Chill.class, 1 + (float)level() / 3);
                }
			}
		}
	}

	@Override
	protected void fx(Ballistica bolt, Callback callback) {
		MagicMissile.blueLight(curUser.sprite.parent, bolt.sourcePos, bolt.collisionPos, callback);
		Sample.INSTANCE.play(Assets.SND_ZAP);
	}

	@Override
	public void onHit(MagesStaff staff, Char attacker, Char defender, int damage) {
		Chill chill = defender.buff(Chill.class);
		if (chill != null && Random.IntRange(2, 10) > chill.cooldown()){
			//need to delay this through an actor so that the freezing isn't broken by taking damage from the staff hit.
			new FlavourBuff(){
				{actPriority = Integer.MIN_VALUE;}
				public boolean act() {
					Buff.affect(target, Frost.class, Frost.duration(target) * Random.Float(1f, 2f));
					return super.act();
				}
			}.attachTo(defender);
		}
	}

	@Override
	public void staffFx(MagesStaff.StaffParticle particle) {
		particle.color(0x88CCFF);
		particle.am = 0.6f;
		particle.setLifespan(2f);
		float angle = Random.Float(PointF.PI2);
		particle.speed.polar( angle, 2f);
		particle.acc.set( 0f, 1f);
		particle.setSize( 0f, 1.5f);
		particle.radiateXY(Random.Float(1f));
	}

}
