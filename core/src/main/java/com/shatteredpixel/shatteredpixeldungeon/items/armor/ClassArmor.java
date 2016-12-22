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
package com.shatteredpixel.shatteredpixeldungeon.items.armor;

import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;

import java.util.ArrayList;

abstract public class ClassArmor extends Armor {

	private static final String AC_SPECIAL = "SPECIAL";
	
	{
		levelKnown = true;
		cursedKnown = true;
		defaultAction = AC_SPECIAL;

		bones = false;
	}

	private int armorTier;
    private int armorType;
	
	public ClassArmor(int type, int tier) {
		super( type, tier );
	}
	
	public static ClassArmor upgrade ( Hero owner, Armor armor ) {
		
		ClassArmor classArmor = null;
		
		switch (owner.heroClass) {
		case WARRIOR:
			classArmor = new WarriorArmor(armor.type, armor.tier);
			BrokenSeal seal = armor.checkSeal();
			if (seal != null) {
				classArmor.affixSeal(seal);
			}
			break;
		case ROGUE:
			classArmor = new RogueArmor(armor.type, armor.tier);
			break;
		case MAGE:
			classArmor = new MageArmor(armor.type, armor.tier);
			break;
		case HUNTRESS:
			classArmor = new HuntressArmor(armor.type, armor.tier);
			break;
		}

		classArmor.level(armor.level());
		classArmor.inscribe( armor.glyph );
		
		return classArmor;
	}

    private static final String ARMOR_TIER	= "armortier";
    private static final String ARMOR_TYPE	= "armortype";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
        bundle.put( ARMOR_TIER, armorTier );
        bundle.put( ARMOR_TYPE, armorType );
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle( bundle );
		//logic for pre-0.4.0 saves
		if (bundle.contains( "DR" )){
			//we just assume tier-4 or tier-5 armor was used.
			int DR = bundle.getInt( "DR" );
			if (DR % 5 == 0){
				level((DR - 10)/5);
				armorTier = 5;
			} else {
				level((DR - 8)/4);
				armorTier = 4;
			}
		} else {
            armorTier = bundle.getInt( ARMOR_TIER );
            armorType = bundle.getInt( ARMOR_TYPE );
		}
	}
	
	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.remove( AC_DETACH );
		if (hero.HP >= 3 && isEquipped( hero )) {
			actions.add( AC_SPECIAL );
		}
		return actions;
	}
	
	@Override
	public void execute( Hero hero, String action ) {

		super.execute( hero, action );

		if (action.equals(AC_SPECIAL)) {
			
			if (hero.HP < 3) {
				GLog.w( Messages.get(this, "low_hp") );
			} else if (!isEquipped( hero )) {
				GLog.w( Messages.get(this, "not_equipped") );
			} else {
				curUser = hero;
				Invisibility.dispel();
				doSpecial();
			}
			
		}
	}

	abstract public void doSpecial();
	
	@Override
	public boolean isIdentified() {
		return true;
	}
	
	@Override
	public int price() {
		return 0;
	}

}
