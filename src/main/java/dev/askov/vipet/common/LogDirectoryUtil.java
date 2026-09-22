package dev.askov.vipet.common;

public final class LogDirectoryUtil {

  private static final String LOG_DIRECTORY_NAME = "logs";
  private static final String DEVELOPMENT_LOG_DIRECTORY_PATH = "./" + LOG_DIRECTORY_NAME + "/";

  private LogDirectoryUtil() {}

  public static String getLogDirectory(final boolean isDevelopment) {
    if (isDevelopment) {
      return DEVELOPMENT_LOG_DIRECTORY_PATH;
    }

    final var appName = System.getProperty("app.name", "VIPET");
    final var os = System.getProperty("os.name").toLowerCase();
    final var userHome = System.getProperty("user.home");

    if (os.contains("win")) {
      return System.getenv("LOCALAPPDATA") + "\\" + appName + "\\" + LOG_DIRECTORY_NAME + "\\";
    } else if (os.contains("mac")) {
      return userHome
          + "/Library/"
          + (LOG_DIRECTORY_NAME.substring(0, 1).toUpperCase() + LOG_DIRECTORY_NAME.substring(1))
          + "/"
          + appName
          + "/";
    } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
      return userHome + "/." + appName.toLowerCase() + "/" + LOG_DIRECTORY_NAME + "/";
    } else {
      return DEVELOPMENT_LOG_DIRECTORY_PATH;
    }
  }
}
