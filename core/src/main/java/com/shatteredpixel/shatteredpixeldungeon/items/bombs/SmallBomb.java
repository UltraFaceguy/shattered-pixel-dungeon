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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class SmallBomb extends Bomb {
	
	{
		image = ItemSpriteSheet.SMALL_BOMB;

		usesTargeting = true;

		stackable = false;
	}

    public int bombLevel;
	private Fuse fuse;

	@Override
	public boolean isSimilar(Item item) {
		return item instanceof SmallBomb && this.fuse == ((SmallBomb) item).fuse;
	}

	@Override
	protected void onThrow( int cell ) {
		if (!Level.pit[ cell ]) {
			Actor.addDelayed(fuse = new Fuse().ignite(this), 2.1f);
		}
		if (Actor.findChar( cell ) != null && !(Actor.findChar( cell ) instanceof Hero) ){
			ArrayList<Integer> candidates = new ArrayList<>();
			for (int i : PathFinder.NEIGHBOURS8)
				if (Level.passable[cell + i])
					candidates.add(cell + i);
			int newCell = candidates.isEmpty() ? cell : Random.element(candidates);
			Dungeon.level.drop( this, newCell ).sprite.drop( cell );
		} else {
            super.onThrow(cell);
        }
	}

	@Override
	public boolean doPickUp(Hero hero) {
        GLog.w( Messages.get(this, "no_pickup") );
		return false;
	}

    @Override
    public ItemSprite.Glowing glowing() {
        return fuse != null ? new ItemSprite.Glowing( 0xFF0000, 0.6f) : null;
    }

    @Override
	public void detonate(int cell){
		//We're blowing up, so no need for a fuse anymore.
		this.fuse = null;

		Sample.INSTANCE.play( Assets.SND_BLAST );

		if (Dungeon.visible[cell]) {
			CellEmitter.center( cell ).burst( BlastParticle.FACTORY, 24 );
		}

		boolean terrainAffected = false;
		for (int n : PathFinder.NEIGHBOURS9) {
			int c = cell + n;
			if (c >= 0 && c < Dungeon.level.length()) {
				if (Dungeon.visible[c]) {
					CellEmitter.get( c ).burst( SmokeParticle.FACTORY, 2 );
				}

				if (Level.flamable[c]) {
					Dungeon.level.destroy( c );
					GameScene.updateMap( c );
					terrainAffected = true;
				}

				//destroys items / triggers bombs caught in the blast.
				Heap heap = Dungeon.level.heaps.get( c );
				if(heap != null)
					heap.explode();

				Char ch = Actor.findChar( c );
				if (ch != null) {
					//those not at the center of the blast take damage less consistently.
					int maxDamage = 5 + bombLevel * 3 + (ch.HT / (15 - bombLevel));
                    int minDamage = c == cell ? maxDamage / 2 : maxDamage / 4;

					int dmg = Random.NormalIntRange( minDamage, maxDamage ) - ch.drRoll();
					if (dmg > 0) {
						ch.damage( dmg, this );
					}

					if (ch == Dungeon.hero && !ch.isAlive()) {
                        Dungeon.fail(getClass());
                    }
				}
			}
		}

		if (terrainAffected) {
			Dungeon.observe();
		}
	}

	@Override
	public String desc() {
		if (fuse == null)
			return super.desc();
		else
			return Messages.get(this, "desc");
	}

	private static class Fuse extends Actor{

		{
			actPriority = 1;
		}

		private SmallBomb bomb;

		private Fuse ignite(SmallBomb bomb){
			this.bomb = bomb;
			return this;
		}

		@Override
		protected boolean act() {

                //something caused our bomb to explode early, or be defused. Do nothing.
                if (bomb.fuse != this){
                    Actor.remove( this );
                    return true;
                }

                //look for our bomb, remove it from its heap, and blow it up.
                for (Heap heap : Dungeon.level.heaps.values()) {
                    if (heap.items.contains(bomb)) {
                        heap.items.remove(bomb);

                        bomb.detonate(heap.pos);

                        Actor.remove(this);
                        return true;
                    }
                }

                //can't find our bomb, something must have removed it, do nothing.
                bomb.fuse = null;
                Actor.remove( this );
                return true;
		}
	}
}
