/** This class serves as a command line interface to the Hebrew calendar program,
 * {@link mazs.jcal.JCal}.  It is named in all lowercase (contrary to Java
 * conventions), for ease of use from the command line.
 * @author Menachem A. Salomon
 * @see mazs.jcal.Molad
 * @see mazs.jcal.JCal
 */

public class jcal {
	/** Run from the command line.
	 * Synopsis: java jcal [options] {@code <year>} {@code <month>}
	 * Options: [ none yet ] */
	public static void main(String... args)
	{
		mazs.jcal.JCal.main(args);
	}
}
