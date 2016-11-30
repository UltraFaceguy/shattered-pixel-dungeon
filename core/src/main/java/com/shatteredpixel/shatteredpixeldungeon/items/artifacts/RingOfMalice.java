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
package com.shatteredpixel.shatteredpixeldungeon.items.artifacts;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Corruption;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs.MaliceClone;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ElmoParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.ShadowParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.Scroll;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;
import java.util.Collections;

public class RingOfMalice extends Artifact {

	{
		image = ItemSpriteSheet.ARTIFACT_RING;

        exp = 0;
        levelCap = 14;

        charge = level()+6;
        partialCharge = 0;
        chargeCap = level()+6;

        cooldown = 0;

        defaultAction = AC_SUMMON;

        bones = true;
	}

	public static final String AC_SUMMON = "SUMMON";

	private final ArrayList<Class> scrolls = new ArrayList<>();

	protected WndBag.Mode mode = WndBag.Mode.SCROLL;

	public RingOfMalice() {
		super();

		Class<?>[] scrollClasses = Generator.Category.SCROLL.classes;
		float[] probs = Generator.Category.SCROLL.probs.clone(); //array of primitives, clone gives deep copy.
		int i = Random.chances(probs);

		while (i != -1){
			scrolls.add(scrollClasses[i]);
			probs[i] = 0;

			i = Random.chances(probs);
		};
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped( hero ) && charge > 2 && !cursed)
			actions.add(AC_SUMMON);
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals( AC_SUMMON )) {

			if (!isEquipped( hero ))             GLog.i( Messages.get(Artifact.class, "need_to_equip") );
			else if (charge < 3)                     GLog.i( Messages.get(this, "no_charge") );
			else if (cursed)                          GLog.i( Messages.get(this, "cursed") );
			else {
				charge -= 3;
                ArrayList<Integer> respawnPoints = new ArrayList<Integer>();

                for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
                    int p = curUser.pos + PathFinder.NEIGHBOURS8[i];
                    if (Actor.findChar( p ) == null && (Level.passable[p] || Level.avoid[p])) {
                        respawnPoints.add( p );
                    }
                }
                if (respawnPoints.size() == 0) {
                    GLog.p( Messages.get(this, "no_space") );
                    return;
                }

                int index = Random.index( respawnPoints );
                MaliceClone clone = new MaliceClone();
                clone.duplicate( curUser );
                GameScene.add( clone );
                clone.sprite.interruptMotion();
                clone.move( respawnPoints.get( index ) );
                clone.sprite.place( respawnPoints.get( index ) );
                Sample.INSTANCE.play( Assets.SND_CURSED );
                clone.sprite.emitter().burst( ShadowParticle.CURSE, 5 );
                Buff.append(clone, Corruption.class);
                hero.HP = Math.max (hero.HP - (hero.HP / 5), 1);
                exp += 18;
                if (exp >= (level()+1)*40 && level() < levelCap) {
                    upgrade();
                    exp -= level()*40;
                    GLog.p( Messages.get(this, "levelup") );
                }

				Invisibility.dispel();
                hero.spend( 1f );
                hero.busy();
                hero.sprite.operate( hero.pos );
			}
		}
	}

	@Override
	protected ArtifactBuff passiveBuff() {
		return new ringRecharge();
	}

	@Override
	public Item upgrade() {
        chargeCap++;
        return super.upgrade();
	}

	@Override
	public String desc() {
		String desc = super.desc();

		if (cursed && isEquipped (Dungeon.hero)){
			desc += "\n\n" + Messages.get(this, "desc_cursed");
		}

		return desc;
	}

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle(bundle);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
	}

	public class ringRecharge extends ArtifactBuff{
		@Override
		public boolean act() {
			LockedFloor lock = target.buff(LockedFloor.class);
			if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
				partialCharge += 1 / (150f - (chargeCap - charge)*15f);

				if (partialCharge >= 1) {
					partialCharge --;
					charge ++;

					if (charge == chargeCap){
						partialCharge = 0;
					}
				}
			}

			updateQuickslot();

			spend( TICK );

			return true;
		}
	}
}