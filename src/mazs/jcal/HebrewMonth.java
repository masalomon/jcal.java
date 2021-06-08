package mazs.jcal;

/** A {@code HebrewMonth} represents a month (of a specific year) in the Jewish calendar.
 * All calculations are as described in the Rambam's Ya"d Hachazaka (Mishne Torah), Sefer
 * III - Zmanim, Hilchos Kiddush Hachodesh, Chapters 6 to 8 (referenced herein as HKH).
 * <p>
 * The Jewish calendar technically begins counting its months from Nissan, so ideally
 * Nissan would be numbered as 1, Tishrei would be 7, Adar would be 12, and Adar Sheini
 * (in leap years) would be 13.  The Jewish year, however, begins in Tishrei, and for most
 * of our calculations, it will prove easier to number the months sequentially, as they
 * occur on the calendar, beginning with Tishrei.  On the other hand, this raises the
 * question of how to number Adar Sheini and the following months in a leap year.
 * <p>
 * One way to approach this issue is to follow the year: Adar Sheini is numbered as 7, and
 * Nissan to Elul are numbered 8 to 13.  This has the drawback of the constants for the
 * months {@link #NISSAN} to {@link #ELUL} either not being constant or not corresponding
 * to their respective months.  Another approach is to leave the months of Nissan to Elul
 * numbered 7 to 12.  This has the advantage of the constants for the month values being
 * constant, but Adar Sheini, numbered as 13, is out of sequence.
 * <p>
 * The second approach is primarily used here, i.e. the month number is stored in
 * "normalized" form (see {@link #getNormalizedMonthNumber(int)}).  But for those
 * situations where the first approach might be more convenient, the month number can be
 * converted to the "sequentialized" form (see {@link #getSequentializedMonthNumber()}).
 * @author Menachem A. Salomon
 * @see Molad
 * @see HebrewYear
 */
public class HebrewMonth {
	/** The number of the month - an integer between TISHREI and ELUL. For
	 *	a leap year, Adar Rishon is 6, Adar Sheini is 13. This means we must
	 *	play around some to keep things straight. */
	private int monthNum;

	/** A {@link HebrewYear} representing the year of which this month is a part,
	 * containing the year number, type, and the day of the week on which it starts. */
	private HebrewYear year;

	/** The {@link Molad} for this {@code HebrewMonth}.  If the first day of the month is
	 * set manually, then the molad might not be needed just to print a calendar. */
	private Molad molad;

	/** The day of the week on which (the 2nd day of) Rosh Chodesh falls. */
	private int roshChodesh;

	/** The number of days in the month, either {@link #CHASER} (29) or {@link #MALEI} (30). */
	private int length;

	/** The name of the month */
	String name;

	/** A constant for month lengths.  A month that is {@code CHASER} has 29 days, and a
	 * month that is {@code MALEI} has 30 days. */
	public static final int CHASER = 29, MALEI = 30;


	/** Create a {@code HebrewMonth} initialized by default to Tishrei of the current year.
	 * @see HebrewYear#getCurrentYear() */
	public HebrewMonth() {
		this(new HebrewYear(), TISHREI);
	}

	/** Create a {@code HebrewMonth} with a specific year and month.
	 * @param year the number of the year, counting from Creation (Anno Mundi, A.M.)
	 * @param month the number of the current month; should be one of the constant month
	 * names.  It is validated as by {@link #setMonth(int)}. */
	public HebrewMonth(int year, int month) {
		this(new HebrewYear(year), month);
	}

	/** Create a {@code HebrewMonth} with a pre-existing {@link HebrewYear} and a specific
	 * month.  The month should be one of the constant month names.  It is validated as if
	 * by {@link #setMonth(int)}. */
	public HebrewMonth(HebrewYear year, int month) {
		this.year = year;
		setMonth(month);
	}

	/** Get the value of the current month.  This will be equal to one of the
	 * constant month names. */
	public int getMonth() {
		return monthNum;
	}

	/** Set the value of the current month.  This value should be one of the constant
	 * month names.  It is validated to ensure it is in the proper range (1 - 12, or 1 -
	 * 13 for leap years). */
	public void setMonth(int monthNum) {
		if (year == null)
			year = new HebrewYear();
		if (monthNum >= TISHREI && monthNum <= ELUL ||
				year.isLeap() && monthNum == ADAR_II)
			this.monthNum = monthNum;
		else
			this.monthNum = monthNum = TISHREI;

		this.molad = year.getMolad();
		setMolad();
		setRoshChodesh();
		setMonthLength();
		name = (year.isLeap() ? Molad.leap_months : Molad.reg_months)
			[monthNum - 1];
	}

	/** Get the day of the week upon which the month begins.  When Rosh Chodesh
	 * is 2 days, this is the second of those days. */
	public int getRoshChodesh() {
		return roshChodesh;
	}

	/** Get the length of the month.  See {@link #length}. */
	public int getMonthLength() {
		return length;
	}

	/** Get the name of the month, as a String. */
	public String getName() {
		return name;
	}

	/** Get the "normalized" form of a "sequentialized" month number.  <i>Normalized</i>
	 * means that Nissan to Elul are numbered 7 to 12, and Adar Sheini is 13.  The
	 * internal month number is unchanged.
	 * @return the normalized month number of the given sequentialized month
	 * @deprecated I can't figure out a good interface. */
	protected int getNormalizedMonthNumber(int monthNumber) {
		if (!year.isLeap() || monthNumber <= ADAR_I)
			return monthNumber;				// Before Adar, month numbers are the same.
		else if (monthNumber == NISSAN)
			return ADAR_II;					// Correct Adar Sheini to 13.
		else								// Adjust Nissan to Elul.
			return monthNumber - 1;
	}

	/** Get the "sequentialized" month number of this {@link HebrewMonth}.
	 * <i>Sequentialized</i> means that the months are numbered as they fall through the
	 * year, with Adar Sheini as 7, and Nissan to Elul being numbered 8 to 13 in a leap
	 * year.  The internal month number is unchanged.
	 * @return the sequentialized number of the month */
	protected int getSequentializedMonthNumber() {
		if (!year.isLeap() || monthNum <= ADAR_I)
			return monthNum;					// Before Adar, months are always the same
		else if (monthNum < ADAR_II)
			return monthNum + 1;				// From Nissan to Elul, adjust month by 1.
		else									// Month is Adar Sheini
			return NISSAN;						// Adar II takes the place of Nissan (7).
	}

	/** Calculate the molad for this month, based on Tishrei's molad. */
	public void setMolad() {
		int curMonth = getSequentializedMonthNumber();
		for (int i = 1; i < curMonth; i++)		// Don't add for Tishrei
			molad.add(Molad.nosar);
	}

	/** Get the value of the {@link Molad} of this month.
	 * @return a copy of this month's {@code Molad} */
	public Molad getMolad() {
		return new Molad(molad);				// Return a copy, not the original.
	}

	/** Calculate which day the first of the month falls. */
	private void setRoshChodesh() {
		int day = year.getRoshHashana();
		switch (monthNum) {		// Fall through, adding more for each month
			case ELUL:		day += 2;	// Av is 30 days (4 weeks + 2 days)
			case AV:		day += 1;	// Tammuz is 29 (4 weeks + 1 day)
			case TAMMUZ:	day += 2;	// Sivan is 30
			case SIVAN:		day += 1;	// Iyar is 29
			case IYAR:		day += 2;	// Nissan is 30
			case NISSAN:				// Adar II adds as many days as Nissan
			case ADAR_II:	day += 1;	// Adar (II) is 29
			case ADAR:		day += 2;	// Shevat is 30
			case SHEVAT:	day += 1;	// Teveis is 29
			case TEVEIS:	day += 2;	// Kisleiv is 30 (sometimes 29)
			case KISLEIV:	day += 1;	// Cheshvan is 29 (sometimes 30)
			case CHESHVAN:	day += 2;	// Tishrei is 30 days
			case TISHREI:	day += 0;	// Don't add anything, start of year
		}
		// For extra month: Adar I is 30 days (Adar II gets advanced, too)
		if (year.isLeap() && monthNum >= NISSAN)
			day += 2;
		// Adjust for year type: Cheshvan is 30 or Kisleiv is 29
		if (year.getYearType() == HebrewYear.SHALEM && monthNum > CHESHVAN)
			day++;
		else if (year.getYearType() == HebrewYear.CHASER && monthNum > KISLEIV)
			day--;

		roshChodesh = day % Molad.DAYS;
	}

	/** Calculate the length of the month.  Depends on year type. */
	private void setMonthLength() {
		// Months of Tishrei, Kisleiv, Shevat, Nissan, Sivan, Av
		if (monthNum % 2 != 0)
			length = MALEI;
		else	// Months of Cheshvan, Teveis, Adar, Iyar, Tammuz, Elul
			length = CHASER;
		// Exceptions: a) Adar I is 30, Adar II is 29
		if (year.isLeap()) {
			if (monthNum == ADAR_I) length = MALEI;
			else if (monthNum == ADAR_II) length = CHASER;
		}
		// b) Cheshvan and Kisleiv depend on the year type (1 more or 1 less)
		if (monthNum == CHESHVAN && year.getYearType() == HebrewYear.SHALEM)
			length = MALEI;
		if (monthNum == KISLEIV && year.getYearType() == HebrewYear.CHASER)
			length = CHASER;
	}

	// TODO: display() should display to a PrintWriter or PrintStream, and there should
	//	be another method that defaults to System.out.

	/** Display a calendar of the month, with or without a header */
	public void display(boolean dispHdr) {
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

	/** A constant for the name of the month.  Value is 1-based, starting at
	 * {@code Tishrei = 1}.  Adar Sheini as a value of 13. */
	public static final int
		TISHREI = 1, CHESHVAN = 2, KISLEIV = 3, TEVEIS = 4, SHEVAT = 5,
		ADAR = 6, NISSAN = 7, IYAR = 8, SIVAN = 9, TAMMUZ = 10, AV = 11,
		ELUL = 12, ADAR_II = 13, ADAR_I = 6;
}
