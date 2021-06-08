package mazs.jcal;

import mazs.jcal.HebrewYear.YearType;
import static mazs.jcal.HebrewMonth.HebrewMonths.*;

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
	 * a leap year, Adar Rishon is 6, Adar Sheini is 13. This means we must
	 * play around some to keep things straight.
	 * @deprecated use {@link #getMonth()}.{@link HebrewMonths#number number} or
	 * {@link #getSequentializedMonthNumber()} instead */
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

	/** The {everything} about this {@code HebrewMonth}. */
	private HebrewMonths month;

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
	public HebrewMonth(HebrewYear year, int monthNum) {
		this.year = year;
		int normalizedMonth = getNormalizedMonthNumber(monthNum);
		setMonth(normalizedMonth, false);
	}

	/** Create a {@link HebrewMonth} for a specific {@link HebrewYear} and
	 * {@link HebrewMonths}.
	 * @param year a {@link HebrewYear} representing the desired year
	 * @param month the requested {@link HebrewMonths month}
	 * @throws ArrayIndexOutOfBoundsException if the given year does not contain the
	 * specified leap month (Adar II), i.e. it is not a leap year */
	public HebrewMonth(HebrewYear year, HebrewMonths month) {
		this.year = year;
		setMonth(month);
	}

	/** Get the value of the current month.  This will be equal to one of the
	 * constant month values. */
	public HebrewMonths getMonth() {
		return month;
	}

	/** Set the value of the current month, by (normalized) month number.  The month
	 * is loosely validated, so all Adars become Adar in a non-leap year, and Adar
	 * becomes Adar I in a leap year.  Out of range month numbers become Tishrei. */
	public void setMonth(int monthNum) {
		if (monthNum >= TISHREI.number && monthNum < HebrewMonths.values().length)
			setMonth(monthNum, false);
		else
			setMonth(TISHREI);
	}

	/** Set the value of the current month, by month number.  Validation can be strict or
	 * loose.  For loose validation, all values for Adar (Adar, Adar I, and Adar II)
	 * become Adar in a non-leap year, and Adar becomes Adar I in a leap year.  For
	 * strict validation, specifying Adar I or Adar II in a non-leap year generates an
	 * exception.
	 * @param monthNum the number of the requested month.  This must match the ordinal of
	 * one of the {@link HebrewMonths}.
	 * @param strict specify whether validation should be strict or loose
	 * @throws ArrayIndexOutOfBoundsException if the month is not a valid month number, or
	 * is not valid for the current year and strict validation was requested
	 */
	public void setMonth(int monthNum, boolean strict) {
		if (!strict || year == null) {
			setMonth(HebrewMonths.valueOf(monthNum));
		} else {
			setMonth(HebrewMonths.valueOf(monthNum, year.isLeap()));
		}
	}

	/** Set the value of the current month.  This value should be one of the enumerated
	 * constant month values.  (See {@link HebrewMonths}.)  It is validated to ensure it
	 * is in the proper range (Tishrei to Elul, and Adar II in a leap year only). */
	public void setMonth(HebrewMonths month) {
		if (year == null)
			year = new HebrewYear();
		if (!year.isLeap() && (month == ADAR_I || month == ADAR_II))
			month = ADAR;
		else if (year.isLeap() && month == ADAR)	// Assume asking for Month #6, which
			month = ADAR_I;							//	is the first Adar (Adar I).
		this.month = month;

		this.molad = year.getMolad();
		setMolad();
		setRoshChodesh();
		setMonthLength();
		name = (year.isLeap() ? Molad.leap_months : Molad.reg_months)[month.number - 1];
		/* Assert */
		if (this.name != month.monthName) {
			System.err.printf("Assertion FAILED: %s != %s. (Equality check: %B.)%n",
					this.name, this.month.monthName, this.name.equals(month.monthName));
			assert this.name == month.monthName;
		}
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
		if (!year.isLeap() || monthNumber <= ADAR_I.number)
			return monthNumber;				// Before Adar, month numbers are the same.
		else if (monthNumber == NISSAN.number)
			return ADAR_II.number;			// Correct Adar Sheini to 13.
		else								// Adjust Nissan to Elul.
			return monthNumber - 1;
	}

	/** Get the "sequentialized" month number of this {@link HebrewMonth}.
	 * <i>Sequentialized</i> means that the months are numbered as they fall through the
	 * year, with Adar Sheini as 7, and Nissan to Elul being numbered 8 to 13 in a leap
	 * year.  The internal month number is unchanged.
	 * @return the sequentialized number of the month */
	protected int getSequentializedMonthNumber() {
		if (!year.isLeap() || month.number <= ADAR_I.number)
			return month.number;			// Before Adar, month numbers are the same.
		else if (month.number < ADAR_II.number)
			return month.number + 1;		// From Nissan to Elul, adjust month by 1.
		else								// Month is Adar Sheini
			return NISSAN.number;			// Adar II takes the place of Nissan (7).
	}

	/** Calculate the molad for this month, based on Tishrei's molad. */
	private /* public */ void setMolad() {
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
		switch (month) {		// Fall through, adding more for each month
			case ELUL:		day += 2;	// Av has 30 days (4 weeks + 2 days)
			case AV:		day += 1;	// Tammuz has 29 days (4 weeks + 1 day)
			case TAMMUZ:	day += 2;	// Sivan has 30 days
			case SIVAN:		day += 1;	// Iyar has 29 days
			case IYAR:		day += 2;	// Nissan has 30 days
			case NISSAN:	day += 1;	// Adar (II) has 29 days
			case ADAR_I:		// Adar I and Adar II add as many days as Adar (Adar stam).
			case ADAR_II:		// Adar II is accounted for below.
			case ADAR:		day += 2;	// Shevat has 30 days
			case SHEVAT:	day += 1;	// Teveis has 29 days
			case TEVEIS:	day += 2;	// Kisleiv has 30 days (sometimes 29)
			case KISLEIV:	day += 1;	// Cheshvan has 29 days (sometimes 30)
			case CHESHVAN:	day += 2;	// Tishrei has 30 days
			case TISHREI:	day += 0;	// Don't add anything, start of year
		}
		// For extra month: Adar I is 30 days (Adar II gets advanced, too)
		if (year.isLeap() && month.number >= NISSAN.number)
			day += 2;
		// Adjust for year type: Cheshvan is 30 or Kisleiv is 29
		if (year.getYearType() == YearType.SHALEM && month.number > CHESHVAN.number)
			day++;
		else if (year.getYearType() == YearType.CHASER && month.number > KISLEIV.number)
			day--;

		roshChodesh = day % Molad.DAYS;
	}

	/** Calculate the length of the month.  Depends on year type. */
	private void setMonthLength() {
		// Months of Tishrei, Kisleiv, Shevat, Nissan, Sivan, Av
		if (month.number % 2 != 0)
			length = MALEI;
		else	// Months of Cheshvan, Teveis, Adar, Iyar, Tammuz, Elul
			length = CHASER;
		// Exceptions: a) Adar I is 30, Adar II is 29
		if (year.isLeap()) {
			if (month == ADAR_I) length = MALEI;
			else if (month == ADAR_II) length = CHASER;
		}
		// b) Cheshvan and Kisleiv depend on the year type (1 more or 1 less)
		if (month == CHESHVAN && year.getYearType() == YearType.SHALEM)
			length = MALEI;
		if (month == KISLEIV && year.getYearType() == YearType.CHASER)
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


	/** Constants for the names and numbers of the months.  The month numbers are 1-based,
	 * starting at {@link #Tishrei} {@code = 1}.  {@link #ADAR_II Adar Sheini} has a value
	 * of {@code 13}. */
	public enum HebrewMonths {
		TISHREI(1, "Tishrei"), CHESHVAN(2, "Marcheshvan"), KISLEIV(3, "Kisleiv"),
		TEVEIS(4, "Teveis"), SHEVAT(5, "Shevat"), ADAR(6, "Adar"), NISSAN(7, "Nissan"),
		IYAR(8, "Iyar"), SIVAN(9, "Sivan"), TAMMUZ(10, "Tammuz"), AV(11, "Av"),
		ELUL(12, "Elul"), ADAR_II(13, "Adar Sheini"), ADAR_I(6, "Adar Rishon");
		/** The (normalized) ordinal of each month.  <i>Normalized</i> means that the
		 * ordinal of each month is the same for leap and non-leap years, with Adar II
		 * tacked on to the end (13), out of sequence. */
		final int number;
		/** The proper name of the month, with Artscroll-style spelling. */
		final String monthName;
		HebrewMonths(int number, String monthName) {
			this.number = number;
			this.monthName = monthName;
		}

		static HebrewMonths valueOf(int monthNumber) {
			return values()[monthNumber - 1];
		}

		static HebrewMonths valueOf(int monthNumber, boolean isLeap) {
			if (isLeap && monthNumber == 6)
				return ADAR_I;
			if (!isLeap && monthNumber == 13)
				throw new ArrayIndexOutOfBoundsException("Regular years only have 12 months");
			return values()[monthNumber - 1];
		}
	}
}
