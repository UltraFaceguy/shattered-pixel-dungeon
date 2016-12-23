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
package com.shatteredpixel.shatteredpixeldungeon.actors.buffs;

import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

 public class Healing extends Buff {

     protected int level;
     private static final String LEVEL	= "level";

     @Override
     public int icon() {
         return BuffIndicator.HEALING;
     }

     @Override
     public void storeInBundle( Bundle bundle ) {
         super.storeInBundle( bundle );
         bundle.put( LEVEL, level );
     }

     @Override
     public void restoreFromBundle( Bundle bundle ) {
         super.restoreFromBundle( bundle );
         level = bundle.getInt( LEVEL );
     }

     public void set( int level ) {
         this.level = Math.max(this.level, level);
     }

     @Override
     public boolean act() {
         if (target.isAlive()) {
             if (target.HP == target.HT) {
                 level -= target.HT / 20;
             } else {
                 int restore = 1 + Random.Int(level / 4);

                 level -= restore;
                 target.heal(restore);
             }

             spend( TICK );

             if (level <= 0) {
                 detach();
             }
         } else {
             detach();
         }

         return true;
     }

     @Override
     public String toString() {
         return Messages.get(this, "name");
     }

     @Override
     public String desc() {
         return Messages.get(this, "desc", level);
     }
 }
