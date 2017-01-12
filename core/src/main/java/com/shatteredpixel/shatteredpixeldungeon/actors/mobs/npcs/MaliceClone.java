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
package com.shatteredpixel.shatteredpixeldungeon.actors.mobs.npcs;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.sprites.MaliceSprite;
import com.watabou.utils.Bundle;

public class MaliceClone extends NPC {

    {
        spriteClass = MaliceSprite.class;

        state = HUNTING;
    }

    public int tier;

    private static final String TIER	    = "tier";

    public void duplicate( Hero hero, int level ) {
        // (20% of hero's HP) plus (5 per artifact level, scaled based on percent hero HP)
        float health = hero.HP / 5 + (hero.HP / hero.HT) * (level * 5);
        HT = HP = Math.max((int)health, hero.HT / 10);
        tier = hero.tier();
    }

    @Override
    public void storeInBundle( Bundle bundle ) {
        super.storeInBundle( bundle );
        bundle.put( TIER, tier );
    }

    @Override
    public void restoreFromBundle( Bundle bundle ) {
        super.restoreFromBundle( bundle );
        tier = bundle.getInt( TIER );
    }

    @Override
    public int attackSkill( Char target ) {
        return Dungeon.hero.attackSkill;
    }

    @Override
    public int damageRoll() {
        return Dungeon.hero.damageRoll();
    }

    @Override
    public int defenseSkill( Char enemy ) {
        return Dungeon.hero.defenseSkill( enemy );
    }

    @Override
    public int drRoll() {
        return Dungeon.hero.drRoll();
    }

    @Override
    public boolean interact() {

        int curPos = pos;

        moveSprite( pos, Dungeon.hero.pos );
        move( Dungeon.hero.pos );

        Dungeon.hero.sprite.move( Dungeon.hero.pos, curPos );
        Dungeon.hero.move( curPos );

        Dungeon.hero.spend( 1 / Dungeon.hero.speed() );
        Dungeon.hero.busy();

        return true;
    }
}