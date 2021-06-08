package mazs.jcal;

/**  A {@code Molad} represents a moment in time, in the system traditionally used by
 * Halacha when calculating the New Moon, or <i>molad</i> (lit. birth).  In this system,
 * a week is divided into 7 days, counted from Sunday; a day is divided into 24 hours,
 * beginning at nightfall; and an hour is divided into 1080 chalakim (parts).  (A chelek
 * is equal to 3 1/3 seconds.)  For some calculations, only the day of the week is
 * important, and days that add up to a whole week can be ignored.  For others, the actual
 * number of days is significant.  Thus, many operations have been duplicated, allowing
 * for both sorts of calculations.  Any {@code Molad} object can be normalized to drop its
 * week count.
 * <p>
 * The New Moon, or <b>molad</b>, occurs at the moment when the Moon is in conjunction -
 * the Moon is (directly) between the Sun and the Earth, and no part of the Moon's disc
 * is visible.  (A solar eclipse may take place if all three celestial bodies are in the
 * same plane as well.)  The new Jewish month begins, ideally, when the new Moon first
 * becomes visible, several hours later.  The actual time difference between one molad and
 * the next varies.  For most calculations, we approximate the time difference as 29 days,
 * 12 hours, and 793 chalakim (44 1/18 minutes).  In our notation, we would write this as
 * 29'12'793, or 29d 12h 793c.
 * @author Menachem A. Salomon
 * @see "Rambam - Ya"d Hachazaka (Mishne Torah), Sefer III - Zmanim, Hilchos Kiddush
 * Hachodesh (Laws of the New Moon), Chapters 6 to 8"
 */
public class Molad {
	/** Supply a stand-alone version, for testing */
	public static void main(String argv[]) {
		Molad l_nosar = new Molad(1, 12, 793);
		l_nosar.print();
		l_nosar.add(bahered);
		l_nosar.display();
	}

	/** The number of days in a week - 7. */
	static final int DAYS = 7;
	/** The number of hours in a day - 24. */
	static final int HOURS = 24;
	/** The number of chalakim (plural of chelek) in an hour - 1080. */
	static final int CHELEKS = 1080;

	/** The (hypothetical) molad of Tishrei, Year 1.  This molad did not actually occur,
	 * as it is (nearly) a year before Creation.  It is found by subtracting the
	 * {@link #nosar} of 1 year from the first actual molad, Tishrei of Year 2, which
	 * occurred on Friday, at the beginning of the third hour of the day (when Adam
	 * Harishon was created), or 6'14'00 in our notation.  Counting backwards, we arrive
	 * at 2'5'204 as the molad of Year 1, a.k.a. Molad BaHeRed. */
	static final Molad bahered = new Molad(2, 5, 204);

	/** The <i>nosar</i> ("excess" - not counting weeks) of the (average) time difference
	 * between one molad and the next.  The actual time difference between molads varies,
	 * but the accepted average is 29 days, 12 hours, and 793 chalakim. */
	static final Molad nosar = new Molad(1, 12, 793);

	/** The <i>nosar</i> ("excess" - not counting weeks) of the time difference between
	 * the molad at the beginning of a regular (non-leap) year and the molad at the
	 * beginning of the following year.  A regular year has 12 months, so the nosar of a
	 * year is 12 times the nosar of a single month, normalized by dropping the weeks.
	 * The non-normalized length of the year is 354 days, 8 hours, and 876 chalakim.
	 * @see #nosar */
	static final Molad reg_year = new Molad(4, 8, 876);

	/** The <i>nosar</i> ("excess" - not counting weeks) of the time difference between
	 * the molad at the beginning of a leap year and the molad at the beginning of the
	 * following year.  A leap year has 13 months, so the nosar of a leap year is 13 times
	 * the nosar of a single month, normalized by dropping the weeks.  The non-normalized
	 * length of the (average) leap year is 384 days, 21 hours, and 589 chalakim.
	 * @see #reg_year The <i>nosar</i> of a regular year */
	static final Molad leap_year = new Molad(5, 21, 589);

	/** The <i>nosar</i> of a <i>machzor</i>, the 19-year (Metonic) cycle consisting of 7
	 * leap years and 12 regular (non-leap) years, in a specific order.  The nosars of
	 * these 19 years add up to 2'16'595 after normalization.  (The actual number of days
	 * is ~7000.) */
	static final Molad machzor = new Molad(2, 16, 595);

	/** The actual length of one month, without dropping weeks: 29'12'793. */
	static final Molad m_chodesh = new Molad(29, 12, 793);

	/** The actual length of one (regular) year, including weeks: 354'8'876. */
	static final Molad m_year = new Molad(354, 8, 876);

	/** The actual length of one leap year, including weeks: 384'21'589. */
	static final Molad m_lyear = new Molad(384, 21, 589);

	/** The names of each of the days of the week. */
	public static final String weekdays[] = {
		// Shabbos is first because SHABBOS % DAYS = 0
		"Shabbos", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"
	} ;

	/** The names of each of the months in a regular, non-leap year. */
	public static final String reg_months[] = {
		"Tishrei", "Marcheshvan", "Kisleiv", "Teveis", "Shevat", "Adar",
		"Nissan", "Iyar", "Sivan", "Tammuz", "Av", "Elul"
	} ;

	/** The names of each of the months in a leap year. */
	public static final String leap_months[] = {
		"Tishrei", "Marcheshvan", "Kisleiv", "Teveis", "Shevat",
		"Adar Rishon", "Nissan", "Iyar", "Sivan", "Tammuz", "Av", "Elul",
		// Note: Adar Sheini is indexed non-sequentially (out of place) here.
		"Adar Sheini"
	} ;


	/** The day of the week on which this {@code Molad} occurs.  When normalized, this is
	 * constrained to be between 0 and 6, with 0 representing Shabbos and 1 to 6
	 * representing Sunday to Friday.  When not normalized, {@code days} can be any number
	 * of days, including negative numbers. */
	private int days;
	/** The hour of the day when this {@code Molad} occurs.  This is generally constrained
	 * to be between 0 and 23, representing hours of the day, counting from nightfall. */
	private int hours;
	/** The chalakim of the hour when this {@code Molad} occurs.  (<i>Chalakim</i> is the
	 * Hebrew plural form of <i>chelek</i>.)  This is generally constrained to be between
	 * 0 and 1079, with 1080 chalakim adding up to a full hour. */
	private int cheleks;


	/** Create a {@link Molad} initialized to a default value.  The {@code Molad} is
	 * initialized to nightfall (Shkia, or sunset, 6 PM) on Friday evening (0'0'0). */
	public Molad() {
		days = hours = cheleks = 0;					// Initialize to Shkia on Friday night.
	}

	/** Create a {@link Molad} initialized with a given {@link #days day}, {@link #hours
	 * hour}, and number of {@link #cheleks chalakim}.  Only minimal checking is
	 * performed; if any argument falls outside the acceptable range, it is ignored, the
	 * corresponding field being set to 0.
	 * @throws IllegalArgumentException if any of the arguments are outside their
	 * respective acceptable ranges */
	public Molad(int day, int hour, int chelek) {
		// TODO: throw IllegalArgumentException on values outside of range.
		if (day >= 0 && day < DAYS)
			days = day;
		if (hour >= 0 && hour < HOURS)
			hours = hour;
		if (chelek >= 0 && chelek <= CHELEKS)
			cheleks = chelek;
	}

	/** Create a new {@link Molad} with the {@link #days}, {@link #hours}, and number of
	 * {@link #cheleks chalakim} copied from another {@code Molad} object. */
	public Molad(final Molad from) {
		days = from.days;
		hours = from.hours;
		cheleks = from.cheleks;
	}

	/** Return the {@link #days}, either the day of the week or the number of days. */
	public int getDays() {
		return days;
	}

	/** Return the number of {@link #hours}. */
	public int getHours() {
		return hours;
	}

	/** Return the number of {@link #cheleks chalakim} (parts of an hour). */
	public int getCheleks() {
		return cheleks;
	}

	/* Arithmetic operations come in two forms: one that discards days that amount to
	 * whole weeks, and another that retains them.  All operations assume that the Molad
	 * objects are, on entry, valid. */

	/** Add two {@link Molad} objects, retaining the days that amount to whole weeks.
	 * @see #add(Molad) */
	public void plus(final Molad other) {
		cheleks += other.cheleks;				// Add the chalakim from the other Molad.
		if (cheleks >= CHELEKS) {				// If we have 1080 chalakim (a whole hour),
			cheleks -= CHELEKS;					// ... then exchange them for an hour.
			hours++;
		}
		hours += other.hours;					// Add the hours from the other Molad.
		if (hours >= HOURS) {					// If we have 24 hours (a whole day),
			hours -= HOURS;						// ... then exchange them for a day.
			days++;
		}
		days += other.days;						// Add the days from the other Molad.
								// Note: In this method, we don't drop the whole weeks.
	}

	/** Add two {@link Molad} objects, normalizing the result by dropping those days that
	 * add up to a whole week.
	 * @see #plus(Molad) */
	public void add(Molad other) {
		this.plus(other);						// First add without normalizing.
		if (days >= DAYS)						// Do the days add up to a full week?
			days -= DAYS;						// Drop the weeks, keep only the days.
		// Could use this.normalizeDays(), but doing it manually is simpler.
	}

	/** Multiply a {@link Molad} by an integer without normalizing the days. 
	 * @see #multiply(int) */
	public void times(int factor) {
		int t_chalakim = cheleks * factor;			// Multiply each field by the factor.
		int t_hours = hours * factor;
		int t_days = days * factor;

		if (t_chalakim >= CHELEKS)					// If we have 1080 or more cheleks, ...
			t_hours += (t_chalakim / CHELEKS);		// exchange each 1080 cheleks for 1 hour.
		cheleks = (t_chalakim % CHELEKS);			// And the remainder remain chalakim.

		if (t_hours >= HOURS)						// If we have 24 or more hours, then ...
			t_days += (t_hours / HOURS);			// ... exchange each 24 hours for 1 day.
		hours = (t_hours % HOURS);					// And the remainder remain hours.

		days = t_days;							// Keep all days, don't drop the weeks.
	}

	/** Multiply a {@code Molad} by an integer, normalizing the result by dropping those
	 * days that add up to whole weeks.
	 * @see #times(int) */
	public void multiply(int factor) {
		this.times(factor);					// Use times() to do most of the work.
		this.normalizeDays();				// Normalize days so they are less than a week.
		// Normalize days: this.days = this.days % DAYS
	}

	/** Normalize the {@link Molad} by dropping the weeks, retaining only those days that
	 * don't add up to a whole week.  {@link #days} is left in the range 0 to 6. */
	public void normalizeDays() {
		this.days %= DAYS;				// Only keep days that don't amount to a whole week.
	}

	/** Return a {@link String} representation of this {@link Molad}.  The string is of
	 * the simple format, "x day(s), y hour(s), and z chelek(s)".  The plural 's' is
	 * omitted for any field with a value of 1. */
	@Override
	public String toString() {
		return new StringBuilder(35)
				.append(days).append(" day").append(days == 1 ? "" : "s").append(", ")
				.append(hours).append(" hour").append(hours == 1 ? "" : "s").append(", and ")
				.append(cheleks).append(cheleks == 1 ? " chelek" : " chalakim")
				.toString();
	}

	/** Print the value of the molad, in days, hours and cheleks. */
	public void print() {
		System.out.println("Molad is " + this + ".");
	}

	/** Print the day and time of the {@link Molad} in the modern format, naming the day
	 * of the week and displaying the time of day in an hour:minute format. */
	 public void display() {
		int minutes = cheleks / 18;				// There are 18 cheleks in a minute.
		int chalakim = cheleks % 18;			// Cheleks that don't add up to a minute

		int day = days;							// Convert from hours past sunset (Shkia)
		int hour = hours - 6;					//	to hours past midnight.
		if (hour < 0) {							// If hours went negative, change to hours past
			hour += HOURS;						//	midnight of the previous day.
			day -= 1;
			if (day < 0)						// If days went negative (i.e. today was Shabbos),
				day += DAYS;					// ... then add 7 to correct it to Friday.
		}

		// TODO in JDK 14: This is a good spot for a switch() expression.
		String timeOfDay = hour >= 18 && hour < HOURS ?	"evening"
				: hour >= 0 && hour < 6		?	"predawn"
				: hour >= 6 && hour < 12	?	"morning"
				: "afternoon";					// hour >= 12 && hour < 18

		// System.out.println("The molad is " + weekdays[day] + " " + timeOfDay + ", " +
		//	(hour % 12 != 0 ? hour % 12 : 12 ) + ":" +
		//	((minutes < 10 ) ? "0" + minutes : minutes) + " " +
		//	(hour < 12 ? "AM" : "PM") + " and " + chalakim +
		//	(chalakim == 1 ? " chelek" : " chalakim") + ".");
		System.out.printf("The molad is %s %s, %d:%02d %s and %d %s.%n",
				weekdays[day], timeOfDay, hour % 12 != 0 ? hour % 12 : 12, minutes,
				hour < 12 ? "AM" : "PM", chalakim, chalakim == 1 ? "chelek" : "chalakim");
	}
}
