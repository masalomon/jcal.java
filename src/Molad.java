/**  A {@code Molad} object represents an exact moment in time in the system
 * traditionally used by Halacha when calculating the New Moon, or <i>molad</i>
 * (birth).  In this system, a week is divided into 7 days, counted from
 * Sunday; a day is divided into 24 hours, beginning at nightfall; and an
 * hour is divided into 1080 chalakim (parts).  Each chelek is thus equal to
 * 3 1/3 seconds.  For many calculations, only the day of the week is relevant;
 * the actual number of days is not important.  For others, the actual number
 * of days is needed.  Thus, many operations have been duplicated, allowing for
 * both sorts of calculations.  Any {@code Molad} object can be normalized to
 * drop its week count.
 * <p>The New Moon, or <b>molad</b>, occurs at the moment when the Moon is in
 * conjunction - the Moon is directly between the Sun and the Earth. and no
 * part of the Moon's disc is visible.  (A solar eclipse may take place if all
 * three bodies are on the same plane as well.)  The next Jewish month begins,
 * ideally, when the new Moon first becomes visible, several hours later.  The
 * actual time difference between one molad and the next varies; for most
 * calculations, we approximate the time difference as 29 days, 12 hours, and
 * 793 chalakim (44 1/18 minutes).  In our notation, we would write this as
 * 29'12'793.
 * @author Menachem A. Salomon
 * @see "Rambam - Ya"d Hachazaka" (Mishne Torah), Sefer III - Zmanim, Hilchos
 * Kiddush Hachodesh, Chapters 6 to 8
 */
class Molad {
	/** Supply a stand-alone version, for testing */
	public static void main(String argv[]) {
		Molad nosar = new Molad(1, 12, 793);
		nosar.print();
		nosar.add(m_origin);
		nosar.display();
	}

	/* Constants for ease of reading and use */
	/** 7 - The number of days in a week. */
	static final int DAYS = 7;
	/** 24 - The number of hours in a day. */
	static final int HOURS = 24;		
	/** 1080 - The number of chalakim in an hour. */
	static final int CHELEKS = 1080;

	/** The (hypothetical) molad of Tishrei, Year 1.  This molad did not really
	 * occur. It is found by subracting the {@link #nosar} of 1 year from the
	 * first actual molad, Tishrei of Year 2, which occurred on Friday, at the
	 * beginning of the third hour of the day, or 6'14'00 in our notation.
	 * Counting backwards, we arrive at 2'5'204 as the molad of Year 1. */
	static final Molad m_origin = new Molad(2, 5, 204);

	/** The difference (not counting weeks) between one molad and the next.
	 *	The actual time difference between molads is 29 days, 12 hours, 793
	 *	cheleks. */
	static final Molad nosar = new Molad(1, 12, 793);

	/** The <i>nosar<i> ("excess" - not counting weeks) between the beginning
	 * of 1 year and the next.  A regular, non-leap year, has 12 months.  The
	 * value given here is 12 times the monthly nosar (with the weeks dropped).
	 * The real length of a year is 354 days, 8 hours, 876 cheleks.
	 * @see #nosar*/
	static final Molad reg_year = new Molad(4, 8, 876);

	/** The nosar of a leap year.  A leap year has 13 months. The value given
	 * here is 13 times the monthly nosar (with the weeks dropped).  The real
	 * length of a leap year is 384 days, 21 hours, 589 cheleks.
	 * @see #reg_year The nosar of a regular year */
	static final Molad leap_year = new Molad(5, 21, 589);
	
	/** The nosar of a machzor.  A <i>machzor</i> is a 19 year cycle consisting
	 * of 7 leap years and 12 non-leap years in a specific order.  The nosars
	 * of these years resolve (after adjustment) to the nosar 2'16'595. */
	static final Molad machzor = new Molad(2, 16, 595);

	/** The actual length of one month, without dropping weeks: 29'12'793. */
	static final Molad m_chodesh = new Molad(29, 12, 793);

	/** The actual length of one year, including weeks: 354'8'876. */
	static final Molad m_year = new Molad(354, 8, 876);

	/** The actual length of a leap year, including weeks: 384'21'589. */
	static final Molad m_lyear = new Molad(384, 21, 589);

	/** The names of each of the days of the week. */
	public static final String weekdays[] = {
		"Shabbos", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday",
		"Friday"			// Shabbos is first because SHABBOS % DAYS = 0
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
		"Adar Sheini"					// indexed non-sequentially
	} ;

	/* Members: day, hour, and chelek of the molad */
	/** The day of the week: Shabbos is 0, Sunday - Friday are 1 - 6. */
	int days;
	/** The hour of the day: hours are 0 - 23, starting at nightfall. */
	int hours;
	/** The chalakim of the hour: values are 0 - 1079 */
	int cheleks;

	/* Constructors: no-arg, copy, or day, hour, chelek supplied */
	/** Default constructor.  The {@code Molad} is initialized to Shkia on
	 * Friday night (0'0'0). */
	public Molad() {
		days = hours = cheleks = 0;		// initialize to Shkia, Friday night
	}

	/** Initialize {@code Molad} to day, hour, and chelek given.  Only minimal
	 * checking is performed: if any argument is outside the acceptable range,
	 * it is ignored and the corresponding field is set to 0. */
	public Molad(int dy, int hr, int clk) {		/* TO-ADD: throws invalidArg */
		if (dy >= 0 && dy < DAYS) days = dy;
		if (hr >= 0 && hr < HOURS) hours = hr;
		if (clk >= 0 && clk <= CHELEKS) cheleks = clk;
	}

	/** Copy constructor.  All fields are copied from the old {@code Molad}. */
	public Molad(final Molad from) {
		days = from.days;
		hours = from.hours;
		cheleks = from.cheleks;
	}


	/* Addition: 2 forms: plus() will allow counting weeks, add() will not.
	 * Assume both Molads (this and argument) are valid. */
	/** Add two {@code Molad} objects, without dropping the weeks.
	 * @see #add(Molad) */
	public void plus(Molad m) {
		cheleks += m.cheleks;			// Add the chalakim from argument.
		if (cheleks >= CHELEKS) {		// If we have 1080 chalakim, then ...
			cheleks -= CHELEKS;			// ... exchange them for 1 hour.
			hours++;
		}
		hours += m.hours;				// Add the hours from argument.
		if (hours >= HOURS) {			// If we have 24 hours, then ...
			hours -= HOURS;				// ...exchange them for 1 day.
			days++;
		}
		days += m.days;					// Add the days from argument.  In this
	}									// method, don't drop the weeks.

	/** Add two {@code Molad} objects, normalizing the result so the days do
	 * not add up to a week.
	 * @see #plus(Molad) */
	public void add(Molad m) {
		this.plus(m);					// First add normally.
		if (days >= DAYS)				// Do the days add up to a full week?
			days -= DAYS;				// Drop the weeks, keep only the days.
		// adjustDays() - but doing it manually is simpler.
	}

	/* Multiplication: 2 forms, as above. Use mult() to multiply and drop
	 *	the weeks, use times() to multiply and keep the weeks. */
	/** Multiply a {@code Molad} object by an integer without normalizing the
	 * days. 
	 * @see #mult(int) */
	public void times(int factor) {
		int clk = cheleks * factor;		// Multiply each field.
		int hr = hours * factor;
		int dy = days * factor;

		if (clk > CHELEKS)				// If have more than 1080 cheleks ...
			hr += (clk / CHELEKS);		// exchange each 1080 cheleks for 1 hr.
		cheleks = (clk % CHELEKS);		// Remainder remain chalakim.

		if (hr >= HOURS)				// If have more than 24 hours ...
			dy += (hr / HOURS);			// exchange each 24 hours for a day.
		hours = (hr % HOURS);			// Remainder remain hours.

		days = dy;						// Keep all days, don't drop the weeks.
	}

	/** Multiply a {@code Molad} by an integer, normalizing the result by
	 * dropping the weeks.
	 * @see #times(int) */
	public void mult(int factor) {
		this.times(factor);				// Use times() to do the work.
		this.adjustDays();				// Adjust so days < 7
		//m.days = (dy % DAYS);
	}

	/** Adjust the molad, dropping the weeks so that days < 7. */
	public void adjustDays() {
		days %= DAYS;		// Only keep days that don't add to a full week.
	}

	/* Accessor methods for days, hours, and cheleks */
	/** Get the days */		public int getDays()	{	return days;	}
	/** Get the hours */	public int getHours()	{	return hours;	}
	/** Get the cheleks */	public int getCheleks()	{	return cheleks;	}

	/* Various printing methods: toString(), print(), display() */
	/** Return String with format "x day(s), y hour(s), and z chelek(s)".
	 *	If days, hours, or cheleks are 1, the plural 's' is not printed. */
	public String toString() {
		return days + " day" + (days == 1 ? "" : "s") + ", " +
			hours + " hour" + (hours == 1 ? "" : "s") + ", and " +
			cheleks + " chelek" + (cheleks == 1 ? "" : "s");
	}

	/** Print the value of the molad, in days, hours and cheleks. */
	public void print() {
		System.out.println("Molad is " + this + ".");
	}

	/** Print the time of the molad in the modern format, with named day of
	 *	the week and the time of day in an hour:minute format. */
	 public void display() {
		int min = cheleks / 18;		// 18 cheleks to a minute
		int clk = cheleks % 18;		// Cheleks that don't add up to a minute

		int dy = days;				// Convert from hours past sunset (shkia)
		int hr = hours - 6;			//	to hours past midnight.
		if (hr < 0) {				// If hours went negative,
			hr += HOURS;			//	change to hours past midnight ...
			dy-= 1;					//	... of the previous day. But if
			if (dy < 0)				//	today was Shabbos (0), then dy just
				dy += DAYS;			//	became -1, so add 7 to change it to
		}							//	Friday.

		String s;
		if (hr >= 18 && hr < HOURS)		s = " evening";
		else if (hr >= 0 && hr < 6)		s = " predawn";
		else if (hr >= 6 && hr < 12)	s = " morning";
		else /* hr >= 12 && hr < 18 */	s = " afternoon";

//TODO: Have to get the names of the days, don't have it yet.
		System.out.println("The molad is " + weekdays[dy] + s + ", " +
			(hr % 12 != 0 ? hr % 12 : 12 ) + ":" +
			((min < 10 ) ? "0" + min : min) + " " +
			(hr < 12 ? "AM" : "PM") + " and " + clk +
			(clk == 1 ? " chelek" : " chalakim") + ".");
	}
}
