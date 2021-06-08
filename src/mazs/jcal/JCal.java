package mazs.jcal;

/** This class contains the command-line interface to the Hebrew calendar program.
 * @author Menachem A. Salomon
 * @see Molad
 * @see JCalendarFrame
 */
public class JCal {
	/** Run from the command line.
	 * Synopsis: java {@link jcal} [options] {@code <year>} {@code <month>}
	 * Options: [ none yet ] */
	public static void main(String... args) {
		// TO DO: Set options here.
		if (args.length != 2) {
			System.err.println(usage_str);
			System.exit(1);
		}

		// acquire year value:
		int yr = Integer.parseInt(args[0]);
		HebrewYear year = new HebrewYear(yr);

		// acquire month value
		// Future Versions: read month as word, get leap months right
		int mo = Integer.parseInt(args[1]);
		HebrewMonth month = new HebrewMonth(year, mo);

		// Do output:
		System.out.println("MS's Jewish Calendar, " + version_str);
		System.out.printf("Calendar for %s, %d%n", month.getName(), year.getYear());
		month.getMolad().display();
		month.display(true);
	}

	private static String version_str = "version 0.3";
	private static String usage_str = "Usage: java jcal year month";
}
