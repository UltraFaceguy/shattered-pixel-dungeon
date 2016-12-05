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

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.LockedFloor;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.SmallBomb;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.ui.QuickSlotButton;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

public class BombBag extends Artifact {

    public static final String AC_THROWBOMB = "THROWBOMB";
    public static final String AC_FILL = "FILL";

    protected WndBag.Mode mode = WndBag.Mode.BOMB;

	{
		image = ItemSpriteSheet.ARTIFACT_BOMB;

		levelCap = 10;
		exp = 0;

		charge = 2;
        partialCharge = 0;
        chargeCap = 2 + (2+level()) / 3;

		defaultAction = AC_THROWBOMB;
		usesTargeting = true;

        bones = true;
	}

    @Override
    public void restoreFromBundle(Bundle bundle) {
        super.restoreFromBundle(bundle);
        chargeCap = 2 + (2+level()) / 3;
    }

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions( hero );
		if (isEquipped(hero) && charge > 0 && !cursed)
			actions.add(AC_THROWBOMB);
        if (isEquipped(hero) && level() < 10 && !cursed)
            actions.add(AC_FILL);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_THROWBOMB)) {

			curUser = hero;

			if (!isEquipped( hero )) {
				GLog.i( Messages.get(Artifact.class, "need_to_equip") );
				QuickSlotButton.cancel();

			} else if (charge < 1) {
				GLog.i( Messages.get(this, "no_charge") );
				QuickSlotButton.cancel();

			} else if (cursed) {
				GLog.w( Messages.get(this, "cursed") );
				QuickSlotButton.cancel();

			} else {
                GameScene.selectCell(throwbomb);
			}

		} else if (action.equals(AC_FILL)){

            GameScene.selectItem(itemSelector, mode, Messages.get(this, "prompt"));
            chargeCap = 2 + (2+level()) / 3;

        }
	}

    protected CellSelector.Listener throwbomb = new  CellSelector.Listener() {

        @Override
        public void onSelect(Integer target) {

            if (target == null) return;

            Invisibility.dispel();
            charge -= 1;
            updateQuickslot();


            SmallBomb sb = new SmallBomb();
            sb.bombLevel = level();
            sb.cast( curUser, target );
            curUser.spendAndNext(1f);
        }

        @Override
        public String prompt() {
            return Messages.get(BombBag.class, "throw_prompt");
        }
    };

    @Override
    protected ArtifactBuff passiveBuff() {
        return new bombRecharge();
    }

    public class bombRecharge extends ArtifactBuff {
        @Override
        public boolean act() {
            LockedFloor lock = target.buff(LockedFloor.class);
            if (charge < chargeCap && !cursed && (lock == null || lock.regenOn())) {
                partialCharge += (1 + level() * 0.1) * (1 / (150f - (chargeCap - charge) * 15f));

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

    protected static WndBag.Listener itemSelector = new WndBag.Listener() {
        @Override
        public void onSelect( Item item ) {
            if (item != null && item instanceof Bomb) {
                if (curItem.level() >= 10) {
                    curItem.level(10);
                    GLog.p( Messages.get(BombBag.class, "maxlevel"));
                } else {
                    Hero hero = Dungeon.hero;
                    hero.sprite.operate( hero.pos );
                    hero.busy();
                    hero.spend( 1f );
                    curItem.upgrade();
                    GLog.p(Messages.get(BombBag.class, "levelup"));
                    item.detach(hero.belongings.backpack);
                }
            }
        }
    };

	@Override
	public String desc() {
		String desc = super.desc();

		if (isEquipped( Dungeon.hero )){
			if (cursed) {
                desc += "\n\n" + Messages.get(this, "desc_cursed");
            } else {
                if (level() < levelCap)
                    desc += "\n\n" + Messages.get(this, "desc_hint");
            }
		}
		return desc;
	}
}
