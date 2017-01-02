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

import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.ui.BuffIndicator;

import java.text.DecimalFormat;
import java.util.HashSet;

public class Buff extends Actor {
	
	public Char target;

	{
		actPriority = 3; //low priority, at the end of a turn
	}

	//determines how the buff is announced when it is shown.
	//buffs that work behind the scenes, or have other visual indicators can usually be silent.
	public enum buffType {POSITIVE, NEGATIVE, NEUTRAL, SILENT};
	public buffType type = buffType.SILENT;

	public HashSet<Class<?>> resistances = new HashSet<Class<?>>();

	public HashSet<Class<?>> immunities = new HashSet<Class<?>>();

    public boolean applyProc() {
        return !target.immunities().contains( getClass() );
    }
	
	public boolean attachTo( Char target ) {

		if (target.immunities().contains( getClass() )) {
			return false;
		}
		
		this.target = target;
		target.add( this );

		if (target.buffs().contains(this)){
			if (target.sprite != null) {
                fx( true );
            }
			return true;
		} else {
            return false;
        }
	}
	
	public void detach() {
		fx( false );
		target.remove( this );
	}
	
	@Override
	public boolean act() {
		diactivate();
		return true;
	}
	
	public int icon() {
		return BuffIndicator.NONE;
	}

	public void fx(boolean on) {
		//do nothing by default
	}

	public String heroMessage(){
		return null;
	}

	public String desc(){
		return "";
	}

	//to handle the common case of showing how many turns are remaining in a buff description.
	protected String dispTurns(float input){
		return new DecimalFormat("#.##").format(input);
	}

	// Creates a fresh instance of the buff and attaches that, this allows duplication.
	public static<T extends Buff> T create(Char target, Class<T> buffClass ) {
		try {
			T buff = buffClass.newInstance();
			buff.attachTo( target );
            buff.applyProc();
			return buff;
		} catch (Exception e) {
			ShatteredPixelDungeon.reportException(e);
			return null;
		}
	}

	// Returns either the buff of type buffClass, or makes a new one
	private static<T extends Buff> T newBuff( Char target, Class<T> buffClass ) {
		T buff = target.buff( buffClass );
		if (buff != null) {
            buff.applyProc();
			return buff;
		} else {
			return create( target, buffClass );
		}
	}

    // Refreshes the current buff to a new duration if higher or creates a new buff from duration
    public static<T extends FlavourBuff> T affect( Char target, Class<T> buffClass, float duration ) {
        T buff = newBuff( target, buffClass );
        buff.postpone( duration );
        return buff;
    }

    // Adds to the duration of an active buff, or creates a new one with that duration
    public static<T extends FlavourBuff> T add(Char target, Class<T> buffClass, float duration ) {
        T buff = newBuff( target, buffClass );
        buff.spend( duration );
        return buff;
    }

    // Apply (or refresh) a standard buff
    public static<T extends Buff> T apply( Char target, Class<T> buffClass) {
        return newBuff( target, buffClass );
    }

    // When the buff is done, it detaches
	public static void detach( Buff buff ) {
		if (buff != null) {
			buff.detach();
		}
	}
	
	public static void detach( Char target, Class<? extends Buff> cl ) {
		detach( target.buff( cl ) );
	}
}
