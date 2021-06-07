package mazs.jcal;

/** A {@code HebrewMonth} object represents a month in the Jewish calendar.  All
 * calculations are as described in the Rambam's Ya"d Hachazaka (Mishne Torah)
 * Sefer III - Zmanim, Hilchos Kiddush Hachodesh, Chapters 6 to 8 (referenced
 * herein an HKH).
 * <p>The Jewish calendar technically begins counting its months from Nissan;
 * thus Nissan should be 1, Tishrei should be 7, Adar should be 12, and Adar
 * Sheini (in leap years) should be 13.  However, the year begins in Tishrei,
 * and it is easier for most calculations to number the months as they occur
 * through the year, beginning with Tishrei.  This leads to a problem in leap
 * years: how do we number Adar Sheini and the following months.  One approach
 * is to follow the year: Adar Sheini is 7, Nissan to Elul are 8 - 13.  But
 * this means that {@link #NISSAN} would not always represent Nissan, and so on
 * for the rest of the year.
 * <p>Another approach is to leave the months Nissan to Elul as 7 - 12.  This
 * has the advantage of constant month values, but then Adar Sheini (13) is
 * numbered out of sequence.  I have used a somewhat hybrid approach.  For the
 * most part, the second approach is used - the month is "normalized" (see
 * {@link #normalize()}), but where the first approach is  better, the month is
 * "sequentialized" (see {@link #sequentialize()}).
 * @author Menachem A. Salomon
 * @see Molad
 * @see HebrewYear
 */
public class HebrewMonth {
	/** The number of the month - an integer between TISHREI and ELUL. For
	 *	a leap year, Adar Rishon is 6, Adar Sheini is 13. This means we must
	 *	play around some to keep things straight. */
	private int month;
	/** A constant for the name of the month.  Value is 1-based, starting at
	 * {@code Tishrei = 1}.  Adar Sheini as a value of 13. */
	public static final int
		TISHREI = 1, CHESHVAN = 2, KISLEIV = 3, TEVEIS = 4, SHEVAT = 5,
		ADAR = 6, NISSAN = 7, IYAR = 8, SIVAN = 9, TAMMUZ = 10, AV = 11,
		ELUL = 12, ADAR_II = 13, ADAR_I = 6;
	/** The molad for that month. Not needed to print a calendar. */
	private Molad mld;
	/** The day of the week on which (the 2nd day of) Rosh Chodesh falls. */
	private int roshChodesh;
	/** The month's length, either {@link #CHASER} (29) or {@link #MALEI} (30)
	 */
	private int length;
	/** A valid month length.  A month that is {@code CHASER} is 29 days long,
	 * and a month that is {@code MALEI} is 30. */
	public static final int CHASER = 29, MALEI = 30;
	/** An HebrewYear object, for the year number, type, and start. */
	private HebrewYear year;
	/** The name of the month */
	String name;


	/* Constructors: no-arg or year and month. */
	/** Default constructor.  Initialize to Tishrei of the current year (see
	 * {@link HebrewYear#getThisYear()}). */
	public HebrewMonth()
	{
		this(new HebrewYear(), 1);
	}

	/** Construct a {@code HebrewMonth} with a specific year and month.
	 * @param yr the number of the year, counting from Creation.
	 * @param mo the value of the current month; should be one of the constant
	 * month names.  It is validated as by {@link #setMonth(int). */
	public HebrewMonth(int yr, int mo)
	{
		this(new HebrewYear(yr), mo);
	}

	/** Construct a {@code HebrewMonth} object using a pre-existing {@code HebrewYear}
	 * object and a specific month.  The month should be one of the constant
	 * month names.  It is validated as by {@link #setMonth(int)}. */
	public HebrewMonth(HebrewYear yr, int mo)
	{
		year = yr;
		setMonth(mo);
	}


	/** Get the value of the current month.  This will be equal to one of the
	 * constant month names. */
	public int getMonth()	{	return month;	}

	/** Set the value of the current month.  This value should be one of the
	 * constant month names.  It is validated to ensure it is in the proper
	 * range (1 - 12, or 1 - 13 for leap years). */
	public void setMonth(int mo)
	{
		if (year == null) year = new HebrewYear();
		if (mo >= TISHREI && mo <= ELUL || year.isLeap() && mo == ADAR_II)
			month = mo;
		else month = TISHREI;

		mld = year.getMolad();
		setMolad();
		setRoshChodesh();
		setMonthLength();
		name = (year.isLeap() ? Molad.leap_months : Molad.reg_months)
			[month - 1];
	}


	/* Accessor methods for roshChodesh, length, and name */
	/** Get the day of the week upon which the month begins.  When Rosh Chodesh
	 * is 2 days, this is the second of those days. */
	public int getRoshChodesh()	{	return roshChodesh;		}

	/** Get the length of the month.  See {@link #length}. */
	public int getMonthLength()	{	return length;			}

	/** Get the name of the month, as a String. */
	public String getName()		{	return name;			}

	
	/** Change month to "normalized" form: Nissan to Elul are 7 - 12, Adar
	 * Sheini is 13.  The internal month number is unchanged.
	 * @return the normalized number of the month
	 * @deprecated I can't figure out a good interface. */
	protected int normalize(int month) {
		if ( !year.isLeap() || month <= ADAR_I)
			return month;					// no change needed
		else if (month == NISSAN)
			return ADAR_II;
		else	/* Nissan - Elul */
			return month -1;
	}

	/** Change month to "sequentialized" form: Adar Sheini is 7, Nissan to
	 * Elul are 8 - 13.  The internal month number is unchanged.
	 * @return the sequentialized number of the month */
	protected int sequentialize() {
		if ( !year.isLeap() || month <= ADAR_I )
			return month;					// no change needed
		else if (month < ADAR_II)
			return month + 1;				// advance month by 1
		else	/* month == ADAR_II */
			return NISSAN;					// Adar II becomes Nissan
	}

	/** Calculate the molad for this month, based on Tishrei's molad. */
	public void setMolad()
	{
		int curMonth = sequentialize();
		for (int i = 1; i < curMonth; i++)		// Don't add for Tishrei
			mld.add(Molad.nosar);
	}

	/** Get  the value of the {@code Molad} of this month.
	 * @return a copy of this month's {@code Molad} */
	public Molad getMolad()
	{
		Molad ret = new Molad(mld);			// Use copy constructor
		return ret;							// Return copy, not original.
	}


	/** Calculate which day the first of the month falls. */
	private void setRoshChodesh()
	{
		int dy = year.getNewYear();
		switch (month) {		// Fall through, adding more for each month
			case ELUL:		dy += 2;	// Av is 30 days (4 weeks + 2 days)
			case AV:		dy += 1;	// Tammuz is 29 (4 weeks + 1 day)
			case TAMMUZ:	dy += 2;	// Sivan is 30
			case SIVAN:		dy += 1;	// Iyar is 29
			case IYAR:		dy += 2;	// Nissan is 30
			case NISSAN:				// Adar II adds as many days as Nissan
			case ADAR_II:	dy += 1;	// Adar (II) is 29
			case ADAR:		dy += 2;	// Shevat is 30
			case SHEVAT:	dy += 1;	// Teveis is 29
			case TEVEIS:	dy += 2;	// Kisleiv is 30 (sometimes 29)
			case KISLEIV:	dy += 1;	// Cheshvan is 29 (sometimes 30)
			case CHESHVAN:	dy += 2;	// Tishrei is 30 days
			case TISHREI:	dy += 0;	// Don't add anything, start of year
		}
		// For extra month: Adar I is 30 days (Adar II gets advanced, too)
		if (year.isLeap() && month >= NISSAN) dy += 2;
		// Adjust for year type: Cheshvan is 30 or Kisleiv is 29
		if (year.getYearType() == HebrewYear.SHALEM && month > CHESHVAN) dy++;
		else if (year.getYearType() == HebrewYear.CHASER && month > KISLEIV) dy--;

		roshChodesh = dy % Molad.DAYS;
	}

	/** Calculate the length of the month.  Depends on year type. */
	private void setMonthLength()
	{
		// Months of Tishrei, Kisleiv, Shevat, Nissan, Sivan, Av
		if (month % 2 != 0) length = MALEI;
		else	// Months of Cheshvan, Teveis, Adar, Iyar, Tammuz, Elul
			length = CHASER;
		// Exceptions: Adar I is 30, Adar II is 29, Cheshvan and Kisleiv
		//	depend on the year type (1 more or 1 less)
		if (year.isLeap()) {
			if (month == ADAR_I) length = MALEI;
			else if (month == ADAR_II) length = CHASER;
		}
		if (month == CHESHVAN && year.getYearType() == HebrewYear.SHALEM)
			length = MALEI;
		if (month == KISLEIV && year.getYearType() == HebrewYear.CHASER)
			length = CHASER;
	}


	/** Display a calendar of the month, with or without a header */
	public void display(boolean dispHdr)
	{
		if (dispHdr) {
			System.out.println("    " + name + " " + year.getYear());
			System.out.println("  S  M  T  W  T  F  S");
		}
		int dayOfMonth = roshChodesh;
		if (dayOfMonth == 0) dayOfMonth = Molad.DAYS;	// Adjust if Shabbos
		int dayOfWeek = 0;
		while ((--dayOfMonth) > 0) {		// Skip to correct day of week
			System.out.print("   ");
			dayOfWeek++;
		}
		System.out.print(" ");
		while (++dayOfMonth <= length) {
			System.out.print(
				(dayOfMonth < 10 ? " " + dayOfMonth : dayOfMonth) + " ");
			dayOfWeek++;
			if (dayOfWeek == Molad.DAYS && dayOfMonth < length) {
				System.out.print("\n ");	// Start a new week
				dayOfWeek = 0;
			}
		}
		System.out.println();
	}
}
