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

import com.shatteredpixel.shatteredpixeldungeon.Badges;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.ShatteredPixelDungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.effects.Speck;
import com.shatteredpixel.shatteredpixeldungeon.items.BrokenSeal;
import com.shatteredpixel.shatteredpixeldungeon.items.EquipableItem;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.AntiEntropy;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Corrosion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Displacement;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Metabolism;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Multiplicity;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.curses.Stench;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Affection;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.AntiMagic;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Brimstone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Camouflage;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Entanglement;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Flow;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Obfuscation;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Potential;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Repulsion;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Stone;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Swiftness;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Thorns;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.glyphs.Viscosity;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.sprites.HeroSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.particles.Emitter;
import com.watabou.utils.Bundlable;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class Armor extends EquipableItem {

	private static final int HITS_TO_KNOW    = 10;

	protected static final String AC_DETACH       = "DETACH";

    public int type;
	public int tier;
	
	private int hitsToKnow = HITS_TO_KNOW;
	
	public Glyph glyph;
	private BrokenSeal seal;
	
	public Armor( int type, int tier ) {
		this.type = type;
        this.tier = tier;
	}

    public Armor() {
        this.type = -1;
        this.tier = 1;
    }

	private static final String ARMOR_TIER	    = "armortier";
	private static final String ARMOR_TYPE	    = "armortype";
	private static final String UNFAMILIRIARITY	= "unfamiliarity";
	private static final String GLYPH			= "glyph";
	private static final String SEAL            = "seal";

	@Override
	public void storeInBundle( Bundle bundle ) {
		super.storeInBundle( bundle );
		bundle.put( ARMOR_TIER, tier );
		bundle.put( ARMOR_TYPE, type );
		bundle.put( UNFAMILIRIARITY, hitsToKnow );
		bundle.put( GLYPH, glyph );
		bundle.put( SEAL, seal);
	}

	@Override
	public void restoreFromBundle( Bundle bundle ) {
		super.restoreFromBundle(bundle);
		if ((hitsToKnow = bundle.getInt( UNFAMILIRIARITY )) == 0) {
			hitsToKnow = HITS_TO_KNOW;
		}
		inscribe((Glyph) bundle.get(GLYPH));
		seal = (BrokenSeal)bundle.get(SEAL);
		tier = bundle.getInt(ARMOR_TIER);
		type = bundle.getInt(ARMOR_TYPE);
	}

	@Override
	public void reset() {
		super.reset();
		//armor can be kept in bones between runs, the seal cannot.
		seal = null;
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		if (seal != null) actions.add(AC_DETACH);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {

		super.execute(hero, action);

		if (action.equals(AC_DETACH) && seal != null){
			BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
			if (sealBuff != null) sealBuff.setArmor(null);

			if (seal.level() > 0){
				degrade();
			}
			GLog.i( Messages.get(Armor.class, "detach_seal") );
			hero.sprite.operate(hero.pos);
			if (!seal.collect()){
				Dungeon.level.drop(seal, hero.pos);
			}
			seal = null;
		}
	}

	@Override
	public boolean doEquip( Hero hero ) {
		
		detach(hero.belongings.backpack);

		if (hero.belongings.armor == null || hero.belongings.armor.doUnequip( hero, true, false )) {
			
			hero.belongings.armor = this;
			
			cursedKnown = true;
			if (cursed) {
				equipCursed( hero );
				GLog.n( Messages.get(Armor.class, "equip_cursed") );
			}
			
			((HeroSprite)hero.sprite).updateArmor();
			activate(hero);

			hero.spendAndNext( time2equip( hero ) );
			return true;
			
		} else {
			
			collect( hero.belongings.backpack );
			return false;
			
		}
	}

	@Override
	public void activate(Char ch) {
		if (seal != null) Buff.apply(ch, BrokenSeal.WarriorShield.class).setArmor(this);
	}

	public void affixSeal(BrokenSeal seal){
		this.seal = seal;
		if (seal.level() > 0){
			//doesn't trigger upgrading logic such as affecting curses/glyphs
			level(level()+1);
			Badges.validateItemLevelAquired(this);
		}
		if (isEquipped(Dungeon.hero)){
			Buff.apply(Dungeon.hero, BrokenSeal.WarriorShield.class).setArmor(this);
		}
	}

	public BrokenSeal checkSeal(){
		return seal;
	}

	@Override
	protected float time2equip( Hero hero ) {
		return 2 / hero.speed();
	}

	@Override
	public boolean doUnequip( Hero hero, boolean collect, boolean single ) {
		if (super.doUnequip( hero, collect, single )) {

			hero.belongings.armor = null;
			((HeroSprite)hero.sprite).updateArmor();

			BrokenSeal.WarriorShield sealBuff = hero.buff(BrokenSeal.WarriorShield.class);
			if (sealBuff != null) sealBuff.setArmor(null);

			return true;

		} else {

			return false;

		}
	}
	
	@Override
	public boolean isEquipped( Hero hero ) {
		return hero.belongings != null && hero.belongings.armor == this;
	}

	public final int DRMax(){
		return DRMax(level());
	}
    public final int DRMin(){
        return DRMin(level());
    }

	public int DRMax(int lvl){
        int effectiveTier = tier;
        if (glyph != null && this.hasGlyph(Swiftness.class)) {
            effectiveTier -= 1;
        }
        switch (type) {
            default:
                return effectiveTier * (lvl+2);
            case 1:
                return effectiveTier * (lvl+1);
        }

	}

	public int DRMin(int lvl) {
        int bonus = 0;
        if (glyph != null && this.hasGlyph(Stone.class)) {
            bonus = tier;
        }
        switch (type) {
            default:
                return lvl + bonus;
            case 1:
                return bonus;
        }
	}

	@Override
	public Item upgrade() {
		return upgrade( false );
	}
	
	public Item upgrade( boolean inscribe ) {

		if (inscribe && (glyph == null || glyph.curse())){
			inscribe( Glyph.random() );
		} else if (!inscribe && Random.Float() > Math.pow(0.9, Math.max(0 , level() - 2))){
			inscribe(null);
		}

		if (seal != null && seal.level() == 0)
			seal.upgrade();

		return super.upgrade();
	}
	
	public int proc( Char attacker, Char defender, int damage ) {
		
		if (glyph != null) {
			damage = glyph.proc( this, attacker, defender, damage );
		}
		
		if (!levelKnown) {
			if (--hitsToKnow <= 0) {
				levelKnown = true;
				GLog.w( Messages.get(Armor.class, "identify") );
				Badges.validateItemLevelAquired( this );
			}
		}
		
		return damage;
	}


	@Override
	public String name() {
		return glyph != null && (cursedKnown || !glyph.curse()) ? glyph.name( super.name() ) : super.name();
	}
	
	@Override
	public String info() {
		String info = desc();

        switch (type) {
            default:
                break;
            case 0:
                info += "\n\n" + Messages.get(Armor.class, "heavy");
                break;
            case 1:
                info += "\n\n" + Messages.get(Armor.class, "light");
                break;
        }

		if (levelKnown) {
			info += "\n\n" + Messages.get(Armor.class, "curr_absorb", tier, DRMin(), DRMax(), STRReq());
			
			if (STRReq() > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "too_heavy");
			} else if (Dungeon.hero.heroClass == HeroClass.ROGUE && Dungeon.hero.STR() > STRReq()){
				info += " " + Messages.get(Armor.class, "excess_str");
			}
		} else {
			info += "\n\n" + Messages.get(Armor.class, "avg_absorb", tier, DRMin(0), DRMax(0), STRReq(0));

			if (STRReq(0) > Dungeon.hero.STR()) {
				info += " " + Messages.get(Armor.class, "probably_too_heavy");
			}
		}
		
		if (glyph != null  && (cursedKnown || !glyph.curse())) {
			info += "\n\n" +  Messages.get(Armor.class, "inscribed", glyph.name());
			info += " " + glyph.desc();
		}
		
		if (cursed && isEquipped( Dungeon.hero )) {
			info += "\n\n" + Messages.get(Armor.class, "cursed_worn");
		} else if (cursedKnown && cursed) {
			info += "\n\n" + Messages.get(Armor.class, "cursed");
		} else if (seal != null) {
			info += "\n\n" + Messages.get(Armor.class, "seal_attached");
		}
		
		return info;
	}

	@Override
	public Emitter emitter() {
		if (seal == null) return super.emitter();
		Emitter emitter = new Emitter();
		emitter.pos(10f, 6f);
		emitter.fillTarget = false;
		emitter.pour(Speck.factory( Speck.RED_LIGHT ), 0.6f);
		return emitter;
	}

	@Override
	public Item random() {
		float roll = Random.Float();
        // 75% to be +0
		if (roll < 0.75f){

		} else {
            // 20% chance to be +1
            // 4% chance to be +2
            // 0.8% chance to be +3
            // 0.016% chance to be +4...
			upgrade(1);
			while (Random.Float() < 0.2f) {
				upgrade(1);
			}
		}

        // 33% chance of curse, if not cursed, 16.67% chance of glyph'd
		if (Random.Int(3) == 0) {
			inscribe(Glyph.randomCurse());
			cursed = true;
			return this;
		} else if (Random.Int(6) == 0) {
            inscribe();
        }

		return this;
	}

	public int STRReq(){
		return STRReq(level());
	}

	public int STRReq(int lvl){
		lvl = Math.max(0, lvl);
		float effectiveTier = tier;
		if (glyph != null) effectiveTier += glyph.tierSTRAdjust();
		effectiveTier = Math.max(0, effectiveTier);

		//strength req decreases at +1,+3,+6,+10,etc.
		return (8 + Math.round(effectiveTier * 2)) - (int)(Math.sqrt(8 * lvl + 1) - 1)/2;
	}
	
	@Override
	public int price() {
		if (seal != null) return 0;

		int price = 20 * tier;
		if (hasGoodGlyph()) {
			price *= 1.5;
		}
		if (cursedKnown && (cursed || hasCurseGlyph())) {
			price /= 2;
		}
		if (levelKnown && level() > 0) {
			price *= (level() + 1);
		}
		if (price < 1) {
			price = 1;
		}
		return price;
	}

	public Armor inscribe( Glyph glyph ) {
		this.glyph = glyph;

		return this;
	}

	public Armor inscribe() {

		Class<? extends Glyph> oldGlyphClass = glyph != null ? glyph.getClass() : null;
		Glyph gl = Glyph.random();
		while (gl.getClass() == oldGlyphClass) {
			gl = Armor.Glyph.random();
		}

		return inscribe( gl );
	}

	public boolean hasGlyph(Class<?extends Glyph> type) {
		return glyph != null && glyph.getClass() == type;
	}

	public boolean hasGoodGlyph(){
		return glyph != null && !glyph.curse();
	}

	public boolean hasCurseGlyph(){
		return glyph != null && glyph.curse();
	}
	
	@Override
	public ItemSprite.Glowing glowing() {
		return glyph != null && (cursedKnown || !glyph.curse()) ? glyph.glowing() : null;
	}
	
	public static abstract class Glyph implements Bundlable {
		
		private static final Class<?>[] glyphs = new Class<?>[]{
				Obfuscation.class, Swiftness.class, Stone.class, Potential.class,
				Brimstone.class, Viscosity.class, Entanglement.class, Repulsion.class, Camouflage.class, Flow.class,
				Affection.class, AntiMagic.class, Thorns.class };
		private static final float[] chances= new float[]{
				10, 10, 10, 10,
				5, 5, 5, 5, 5, 5,
				2, 2, 2 };

		private static final Class<?>[] curses = new Class<?>[]{
				AntiEntropy.class, Corrosion.class, Displacement.class, Metabolism.class, Multiplicity.class, Stench.class
		};
			
		public abstract int proc( Armor armor, Char attacker, Char defender, int damage );
		
		public String name() {
			if (!curse())
				return name( Messages.get(this, "glyph") );
			else
				return name( Messages.get(Item.class, "curse"));
		}
		
		public String name( String armorName ) {
			return Messages.get(this, "name", armorName);
		}

		public String desc() {
			return Messages.get(this, "desc");
		}

		public boolean curse() {
			return false;
		}
		
		@Override
		public void restoreFromBundle( Bundle bundle ) {
		}

		@Override
		public void storeInBundle( Bundle bundle ) {
		}
		
		public abstract ItemSprite.Glowing glowing();

		public int tierDRAdjust(){
			return 0;
		}

		public float tierSTRAdjust(){
			return 0;
		}

		public boolean checkOwner( Char owner ) {
			if (!owner.isAlive() && owner instanceof Hero) {

				Dungeon.fail( getClass() );
				GLog.n( Messages.get(this, "killed", name()) );

				Badges.validateDeathFromGlyph();
				return true;
				
			} else {
				return false;
			}
		}

		@SuppressWarnings("unchecked")
		public static Glyph random() {
			try {
				return ((Class<Glyph>)glyphs[ Random.chances( chances ) ]).newInstance();
			} catch (Exception e) {
				ShatteredPixelDungeon.reportException(e);
				return null;
			}
		}

		@SuppressWarnings("unchecked")
		public static Glyph randomCurse(){
			try {
				return ((Class<Glyph>)Random.oneOf(curses)).newInstance();
			} catch (Exception e) {
				ShatteredPixelDungeon.reportException(e);
				return null;
			}
		}
		
	}
}
