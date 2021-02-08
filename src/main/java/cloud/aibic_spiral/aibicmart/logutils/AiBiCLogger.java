package cloud.aibic_spiral.aibicmart.logutils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AiBiCLogger {

	private static Logger LOG = LogManager.getLogger();

	public static void info(String message, String uid, int token, Object... o) {
		LOG.info("AS" + uid + " Shop " + String.format("%5d", token) + " " + message, o);
	}
}