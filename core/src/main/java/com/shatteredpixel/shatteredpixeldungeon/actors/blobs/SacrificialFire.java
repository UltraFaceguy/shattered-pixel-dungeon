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
package com.shatteredpixel.shatteredpixeldungeon.actors.blobs;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.Journal;
import com.shatteredpixel.shatteredpixeldungeon.Journal.Feature;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Blindness;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.FlavourBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.BlobEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SacrificialParticle;
import com.shatteredpixel.shatteredpixeldungeon.levels.Level;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.Bundle;
import com.watabou.utils.PathFinder;

public class SacrificialFire extends Blob {

    protected int pos;
    public int tributes = 5;

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );

        if (volume > 0) {
            for (int i = 0; i < cur.length; i++) {
                if (cur[i] > 0) {
                    pos = i;
                    break;
                }
            }
        }
    }

    @Override
    protected void evolve() {
        volume = off[pos] = cur[pos];
        area.union(pos%Dungeon.level.width(), pos/Dungeon.level.width());

        Char ch = Actor.findChar( pos );
        if (ch != null) {
            if (ch.buff(Marked.class) == null) {
                Buff.append(ch, Marked.class, 1f);
            } else {
                Buff.prolong(ch, Marked.class, 1f);
            }
        }

        if (Dungeon.visible[pos]) {
            Journal.add( Feature.ALTER_OF_SACRIFICE );
        }
    }

    @Override
    public void seed( Level level, int cell, int amount ) {
        super.seed(level, cell, amount);

        cur[pos] = 0;
        pos = cell;
        volume = cur[pos] = amount;

        area.setEmpty();
        area.union(cell%level.width(), cell/level.width());
    }

    @Override
    public void use( BlobEmitter emitter ) {
        super.use( emitter );
        emitter.pour( SacrificialParticle.FACTORY, 0.04f );
    }

    @Override
    public String tileDesc() {
        return Messages.get(this, "desc");
    }

    public static void affectCell( int cell ) {
        SacrificialFire fire = (SacrificialFire) Dungeon.level.blobs.get( SacrificialFire.class );
        if (fire != null && fire.pos == cell) {
            Char ch = Actor.findChar( cell );
            if (ch == null) {
                return;
            }
            CellEmitter.get(cell).burst( SacrificialParticle.FACTORY, 20 );
            ch.sprite.flash();
            Sample.INSTANCE.play( Assets.SND_BURNING );

            if (ch.buff( Marked.class ) == null) {
                Buff.append( ch, Marked.class, 1f );
            } else {
                Buff.prolong( ch, Marked.class, 1f );
            }
        }
    }

    private void acceptTribute(Char ch) {
        if (ch instanceof Hero) {
            Dungeon.hero.HT += 5 + tributes;
            Dungeon.hero.HP += 5 + tributes;
            tributes = 0;

            Buff.append(ch, Blindness.class, 10f);

            for (Mob mob : Dungeon.level.mobs) {
                mob.beckon( pos );
            }

            tributeFlare();
            Sample.INSTANCE.play( Assets.SND_CURSED );
        } else {
            Dungeon.hero.HT += 1;
            Dungeon.hero.HP += 1;
            tributes--;

            tributeFlare();
            Sample.INSTANCE.play( Assets.SND_BURNING );
        }
        if (tributes <= 0) {
            volume = 0;
        }
    }

    private void tributeFlare() {
        for (int i = 0; i < PathFinder.NEIGHBOURS8.length; i++) {
            int p = pos + PathFinder.NEIGHBOURS8[i];
            CellEmitter.get(p).burst( SacrificialParticle.FACTORY, 5 );
        }
    }

    public static class Marked extends FlavourBuff {

        public final float DURATION	= 5f;

        @Override
        public int icon() {
            return BuffIndicator.SACRIFICE;
        }

        @Override
        public String toString() {
            return Messages.get(this, "desc");
        }

        @Override
        public String desc() {
            return Messages.get(this, "desc");
        }

        @Override
        public void detach() {
            if (!target.isAlive()) {
                checkAlterTribute();
            }
            super.detach();
        }

        public boolean checkAlterTribute() {
            SacrificialFire fire = (SacrificialFire) Dungeon.level.blobs.get( SacrificialFire.class );
            if (fire != null && fire.volume != 0 && target.pos == fire.pos) {
                fire.acceptTribute(target);
                return true;
            }
            return false;
        }
    }
}