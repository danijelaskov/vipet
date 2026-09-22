package dev.askov.vipet;

import dev.askov.vipet.common.LogDirectoryUtil;

public final class Main {

  public static void main(String[] args) {
    System.setProperty(
        "log.dir",
        LogDirectoryUtil.getLogDirectory(Boolean.parseBoolean(System.getProperty("app.dev"))));
    VIPET.main(args);
  }
}
