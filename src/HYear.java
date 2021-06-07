/** An {@code HYear} object holds information about a year in the Jewish
 * calendar.  All calculations are as described in the Rambam's Ya"d Hachazaka
 * (Mishne Torah) Sefer III - Zmanim, Hilchos Kiddush Hachodesh, Chapters 6 to
 * 8 (referenced herein an HKH). 
 * @author Menachem A. Salomon
 * @see Molad
 */
public class HYear {
	/** The year number. Constrained to be between 1 and 6000 (inclusive). */
	private int year;
	/** The molad Tishrei of that year. */
	private Molad mld;
	/** The day on which Rosh Hashana (New Year's Day) falls. */
	private int newYear;
	/** The type of year: one of {@link #CHASER}, {@link #KSEDER}, {@link
	 * #SHALEM}, or {@link INVALID}. */
	private int yearType;
	/** A valid year type.  This value can be added to the length of a standard
	 * year (354 days, or 384 in a leap years) to find the length of this year.
	 * {@code CHASER} ("lacking") is 1 day shorter, {@code KSEDER} ("normal")
	 * is standard, and {@code SHALEM} ("full") is 1 day longer. */
	public static final int
		CHASER = -1, KSEDER = 0, SHALEM = 1, INVALID = -354;


	/* Constructors: no-arg, or year number supplied */
	/** Default constructor.  Will attempt to create a new {@code HYear} object
	 * using the current year  (see {@link #getThisYear()}). */
	public HYear()
	{
		this(getThisYear());
	}

	/** Create an {@code HYear} object.  The argument is validated as by
	 * {@link #setYear(int)}.
	 * @param yr the year (from creation) to set the value to. */
	public HYear(int yr)
	{
		if (yr < 1 || yr > 6000)
			yr = getThisYear();
		setYear(yr);
	}


	/** Return the year number of the current year. Currently returns
	 *	a constant 5771 (2011 CE), hope to update. */
	public static int getThisYear()
	{
		// TODO: Calculate this year from the Date
		return 5771;
	}


	/** Return true if the year passed in is a leap year, false otherwise.  The
	 * algorithm is specified by HKH, Perek 6, Halacha 11. */
	public static boolean isLeap(int yr)
	{
		switch (yr % 19) {
			case 0: case 3: case 6: case 8: case 11: case 14: case 17:
				return true;
			}
		return false;
	}

	/** Return true if this {@code HYear} object represents a leap year, false
	 * otherwise.
	 * @see #isLeap(int) */
	public boolean isLeap()
	{
		return isLeap(year);
	}


	/* Accessor methods for the year number. */
	/** Set the current year.  The argument is validated to ensure it is in the
	 * acceptable range (1- 6000). */
	public void setYear(int yr)
	{
		if (yr >= 1 && yr <= 6000)
			year = yr;
		calcMolad();
		setYearType();
	}

	/** Get the year represented by this {@code HYear} object. */
	public int getYear()
	{
		return year;
	}


	/** Calculate the molad for the first month (Tishrei) of any year.  The
	 * calculations are described in HKH, Perek 6. */
	private static Molad calcMolad(int year)
	{
		int cycles = --year / 19;	// cycles past
		Molad mld = new Molad(Molad.machzor);
		mld.mult(cycles);			// add 19 * machzor to origin
		mld.add(Molad.m_origin);

		year %= 19;			// years past of current cycle
		for (int i = 1; i <= year; i++)
			mld.add( isLeap(i) ? Molad.leap_year : Molad.reg_year );

		return mld;
	}

	/** Calculate the molad for the first month (Tishrei) of this year.  See
	 * {@link #calcMolad(int)} for details. */
	private void calcMolad()
	{
		mld = calcMolad(year);
	}


	/** Get  the value of the {@code Molad} Tishrei of of this year.
	 * @return a copy of this year's {@code Molad} Tishrei */
	public Molad getMolad()
	{
		Molad ret = new Molad(mld);		// Use copy constructor
		return ret;			// Return copy, not original.
	}

	/* Accessor methods for newYear and yearType */
	/** Get the day of the week on which Rosh Hashana (New Year) falls. */
	public int getNewYear()		{	return newYear;		}
	/** Get the type of year (see {@link #yearType}). */
	public int getYearType()	{	return yearType;	}

	/** Set the properties newYear and yearType.   */
	private void setYearType()
	{
		// Year type depends on Rosh Hashana of this year, next year, and
		//	whether or not this year is a leap year.
		newYear = calcRosh(mld, year);			// This year's Rosh Hashana
		Molad next = calcMolad(year + 1);		// Calculate RH for next year
		int nextYear = calcRosh(next, year + 1);
		yearType = calcYearType(newYear, nextYear, isLeap());
	}


	/** Calculate the day on which Rosh Hashana falls. This depends on the
	 *	Molad and whether this year (or the next) is a leap year. To
	 *	calculate the year type we need Rosh Hashana of both this year and
	 * 	the next; to avoid infinite recursion, this method is static.
	 *	<i>The calculations herein are based on Ya"D HaChazaka, Zmanim,
	 *	Hilchos Kiddush HaChodesh, Chapters 6-8.</i> */
	private static int calcRosh(Molad mld, int year)
	{
		// First, get the values of the Molad object:
		int dy = mld.getDays(), hr = mld.getHours(), clk = mld.getCheleks();
		// General rule: Rosh Hashana is the day of the molad ...
		int rosh = dy;
		// ... unless one of the following four delays (dichuyim) applies:
		if (hr > 18)		// 1) Molad Yashan: Molad after Chatzos Hayom
			rosh++;			//	(12PM, or 18h) - delay to next day.
		else if ( !isLeap(year) && dy == 3 &&
				(hr > 9 || (hr == 9 && clk >= 204)) )
			rosh++;		// 2) G"T R"D in a non-leap year - delay to Thursday.
		else if ( isLeap(year - 1) && dy == 2 &&
				(hr > 15 || (hr == 15 && clk >= 589)) )
			rosh++;				// 3) BT"U TKP"T following a leap year - delay.
		switch (rosh) {			// 4) Lo AD"U Rosh: Rosh Hashana can't be on
			case 1: case 4: case 6:			// Sunday, Wednesday, or Friday
				rosh++;
		}

		if (rosh >= Molad.DAYS)
			rosh -= Molad.DAYS;		// Normalize to 0 - 7
		return rosh;
	}


	/** Calculate the type of year - KSEDER (regular), SHALEM (full), or
	 *	CHASER (deficient) - based on the first and last days of the year,
	 *	as well as whether it is a leap year.
	 *	@param beg The day on which Rosh Hashana falls this year.
	 *	@param end The day on which Rosh Hashana falls <i>next</i> year.
	 *	@param leap Is this year a leap year? */
	private static int calcYearType(int beg, int end, boolean leap)
	{
		int yearType = INVALID;
		switch (beg) {			// On which day is Rosh Hashana of this year?
			case 2:				// On a Monday: 2..5-0-2 (R/L: C, S)
				if	(end == 5 && !leap)	yearType = CHASER;
				else if	(end == 0)
					if	(leap)	yearType = CHASER;
					else		yearType = SHALEM;
				else if	(end == 2 && leap)	yearType = SHALEM;
				break;
			case 3:				// On a Tuesday: 3..-0-2- (R/L: K)
				if ( (end == 0 && !leap) || (end == 2 && leap) )
					yearType = KSEDER;
				break;
			case 5:				// On a Thursday: 5..-23-5 (R: K,S; L: C,S)
				if	(end == 2 && !leap)	yearType = KSEDER;
				else if (end == 3)
					if	(leap)	yearType = CHASER;
					else		yearType = SHALEM;
				else if	(end == 5 && leap)	yearType = SHALEM;
				break;
			case 0:				// On a Shabbos: 0..3-5-0	(R/L: C,S)
				if	(end == 3 && !leap)	yearType = CHASER;
				else if	(end == 5)
					if	(leap)	yearType = CHASER;
					else		yearType = SHALEM;
				else if (end == 0 && leap)	yearType = SHALEM;
				break;
			// default: return INVALID
		}
		return yearType;
	}
}
