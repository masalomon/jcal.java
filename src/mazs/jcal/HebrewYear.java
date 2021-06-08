package mazs.jcal;

import static mazs.jcal.HebrewYear.YearType.*;

/** A {@code HebrewYear} holds information about a year in the Jewish calendar.  All
 * calculations are as described in the Rambam's Ya"d Hachazaka (Mishne Torah), Sefer
 * III - Zmanim, Hilchos Kiddush Hachodesh, Chapters 6 to 8 (referenced herein as HKH).
 * @author Menachem A. Salomon
 * @see Molad
 */
public class HebrewYear {
	/** The year number of the Hebrew year being described.  Constrained to be between
	 * 1 and 6000 (inclusive). */
	private int year;
	/** The {@link Molad molad} of the month of Tishrei of this year. */
	private Molad molad;
	/** The day of the week on which Rosh Hashana (New Year's Day) falls. */
	private int roshHashana;
	/** The type of year of this year: one of {@link #CHASER}, {@link #KSEDER}, {@link
	 * #SHALEM}, or {@link INVALID}. */
	private YearType yearType;


	/** Create a new {@code HebrewYear} describing the current year.  The current year is
	 * found using {@link #getCurrentYear()} (which is admittedly not perfect). */
	public HebrewYear() {
		this(getCurrentYear());
	}

	/** Create a {@code HebrewYear} describing the given year.  The year number is
	 * constrained to the range [{@code 1 <= year <= 6000}]
	 * @param year the number of the year (from Creation, A.M.) to set the value to. */
	public HebrewYear(int year) {
		if (year < 1 || year > 6000)
			year = getCurrentYear();
		setYear(year);
	}

	/** Set the current year.  The year number is validated to ensure it is in the
	 * acceptable range ({@code 1 <= year <= 6000}). */
	public void setYear(int yearNum) {
		if (yearNum >= 1 && yearNum <= 6000)
			year = yearNum;
		calculateMolad();
		setYearType();
	}

	/** Return the Hebrew year number of the year represented by this {@code HebrewYear}. */
	public int getYear() {
		return year;
	}

	/** Calculate the Hebrew year number of the current year.
	 * TODO: Should return the year based on the current date.
	 * Currently returns a constant 5781 (2021 CE) (Was: 5771, 2011 CE).
	 * MUST BE UPDATED!!! */
	public static int getCurrentYear() {
		// TODO: Calculate this year from the Date
		/* Algorithm: Hebrew year is Gregorian year + 3760, but new year starts in
		 * September or October. */
		return 5781;
	}

	/** Return {@code true} if this {@code HebrewYear} represents a leap year,
	 * {@code false} otherwise.
	 * @see #isLeap(int) */
	public boolean isLeap() {
		return isLeap(year);
	}

	/** Return {@code true} if the given year is a leap year, or {@code false} if it is
	 * not.  The algorithm is specified by HKH, Perek 6, Halacha 11. */
	public static boolean isLeap(int year) {
		switch (year % 19) {
			case 0: case 3: case 6: case 8: case 11: case 14: case 17:
				return true;
			}
		return false;
	}

	/** Calculate the molad for the first month (Tishrei) of any year.  The formula for
	 * the calculation is described in HKH, Perek 6.
	 * @return the {@link Molad} of Tishrei of the given year
	 */
	private static Molad calculateMolad(int year) {
		int cycles = --year / 19;					// the number of completed cycles
		Molad molad = new Molad(Molad.machzor);
		molad.multiply(cycles);						// Add up 19 machzorim (19 * machzor)
		molad.add(Molad.bahered);					// Add them to the original (first) molad

		year %= 19;									// number of years past in current cycle
		for (int i = 1; i <= year; i++)
			molad.add(isLeap(i) ? Molad.leap_year : Molad.reg_year);

		return molad;
	}

	/** Calculate the molad for the first month (Tishrei) of this year.  See
	 * {@link #calculateMolad(int)} for details. */
	private void calculateMolad() {
		molad = calculateMolad(year);
	}

	/** Get the {@link Molad molad} of the month of Tishrei of this year.
	 * @return a copy of this year's {@code Molad molad} Tishrei */
	public Molad getMolad() {
		if (molad == null)
			calculateMolad();
		return new Molad(molad);		// Return a copy, not the original.
	}

	/** Calculate and set the type of year and the day of the week on which this year
	 * starts. */
	private void setYearType()
	{
		// The type of year depends on Rosh Hashana of this year, of the next year, and
		//	whether or not this year is a leap year.
		roshHashana = calculateRoshHashana(year, molad);			// This year's Rosh Hashana
		Molad nextTishrei = calculateMolad(year + 1);				// Calculate RH for next year
		int nextRoshHashana = calculateRoshHashana(year + 1, nextTishrei);
		yearType = calculateYearType(roshHashana, nextRoshHashana, isLeap());
	}

	/** Return the day of the week on which Rosh Hashana (New Year's Day) falls.
	 * @return the day of the week of the first day of this year */
	public int getRoshHashana() {
		return roshHashana;
	}

	/** Get the {@link YearType type} of year of this {@code HebrewYear}. */
	public YearType getYearType() {
		return yearType;
	}

	/** Calculate the day of the week on which Rosh Hashana (the first day of the year)
	 * falls for a given year.  The day of the week depends on the day and time of the
	 * {@link Molad} as well as whether this year, or the previous one, is a leap year.
	 * To calculate the year type (see {@link YearType}, we need to determine Rosh
	 * Hashana of both this year and the next, so this method is static to avoid infinite
	 * recursion.
	 * @see "Rambam: Ya"D HaChazaka, Zmanim, Hilchos Kiddush HaChodesh, Chapters 6-8"
	 * @return the day of the week on which the given year starts
	 */
	private static int calculateRoshHashana(int year, Molad molad) {
		// First, get the values of the Molad object:
		int days = molad.getDays(), hours = molad.getHours(), cheleks = molad.getCheleks();
		// General rule: Rosh Hashana is the same day as the molad ...
		int roshHashana = days;
		// ... unless one of the following four delays (dichuyim) applies:
		if (hours >= 18) {				// 1) Molad Yashan: Molad after Chatzos Hayom
			roshHashana++;				//    (12 noon, or 18h) - delay to next day.
		} else if (!isLeap(year) && days == 3 &&
				(hours > 9 || (hours == 9 && cheleks >= 204))) {
			roshHashana++;				// 2) G"T R"D in a non-leap year - delay to Thursday.
		} else if (isLeap(year - 1) && days == 2 &&
				(hours > 15 || (hours == 15 && cheleks >= 589))) {
			roshHashana++;				// 3) BT"U TKP"T following a leap year - delay to Tuesday.
		}
		switch (roshHashana) {			// 4) Lo AD"U Rosh: Rosh Hashana cannot fall on a Sunday,
			case 1: case 4: case 6:		//    on a Wednesday, or on a Friday.  If it would, delay
				roshHashana++;			//    it to the next day (Monday, Thursday, or Shabbos).
		}

		if (roshHashana >= Molad.DAYS)
			roshHashana -= Molad.DAYS;			// Normalize, Shabbos is 0 instead of 7.
		return roshHashana;
	}

	/** Calculate the {@link YearType year type} of a given year - {@link YearType#KSEDER
	 * KSEDER} (regular), {@link YearType#SHALEM SHALEM} (full), or {@link YearType#CHASER
	 * CHASER} (deficient) - based on the first and last days of the year (the first day
	 * of the next year), as well as whether it is a leap year.
	 * @param first the day of the week on which Rosh Hashana falls this year
	 * @param next the day of the week on which Rosh Hashana falls <i>next</i> year
	 * @param isLeap whether or not this year is a leap year */
	private static YearType calculateYearType(int first, int next, boolean isLeap) {
		YearType yearType = INVALID;
		switch (first) {			// On which day of the week is Rosh Hashana of this year?
		case 2:						// On a Monday: 2..5-0-2 (R/L: C, S)
			if (next == 5 && !isLeap)
				yearType = CHASER;
			else if	(next == 0)
				yearType = isLeap ? CHASER : SHALEM;
			else if	(next == 2 && isLeap)
				yearType = SHALEM;
			break;
		case 3:						// On a Tuesday: 3..-0-2- (R/L: K)
			if ((next == 0 && !isLeap) || (next == 2 && isLeap))
				yearType = KSEDER;
			break;
		case 5:						// On a Thursday: 5..-23-5 (R: K,S; L: C,S)
			if	(next == 2 && !isLeap)
				yearType = KSEDER;
			else if (next == 3)
				yearType = isLeap ? CHASER : SHALEM;
			else if	(next == 5 && isLeap)
				yearType = SHALEM;
			break;
		case 0:						// On a Shabbos: 0..3-5-0	(R/L: C,S)
			if	(next == 3 && !isLeap)
				yearType = CHASER;
			else if	(next == 5)
				yearType = isLeap ? CHASER : SHALEM;
			else if (next == 0 && isLeap)
				yearType = SHALEM;
			break;
		// Any other combinations are impossible and invalid, because of the dichuyim.
		default:
			yearType = INVALID;
		}
		return yearType;
	}


	/** The type of year determines the number of days in the year, particularly the
	 * number of days in the months of Cheshvan and Kisleiv. */
	public enum YearType {
		/** A {@code CHASER} ("lacking" or deficient) year is one day shorter than a
		 * normal {@link #KSEDER} year.  The month of Kisleiv has only 29 days instead of
		 * the usual 30. */
		CHASER(-1),
		/** A {@code KSEDER} ("normal" or regular) year is one in which the number of days
		 * per month alternate regularly.  Cheshvan has 29 days, and Kisleiv has 30. */
		KSEDER(0),
		/** A {@code SHALEM} ("full") year is one day longer than a normal {@link #KSEDER}
		 * year.  The month of Cheshvan has 30 days instead of only the usual 29. */
		SHALEM(1),
		/** A placeholder for invalid year input, or a {@link HebrewYear} whose
		 * {@link YearType} has not yet been calculated. */
		INVALID(-354);
		/** The difference in year length between this year type and that of a regular
		 * ({@link #KSEDER}) year.  This value can be added to the length of a regular
		 * year (354 days, or 384 if it is a leap year) to find the number of days in
		 * this year. */
		final int difference;
		YearType(int difference) {
			this.difference = difference;
		}
	}
}
