package dev.askov.vipet.mvc;

import dev.askov.vipet.VIPET;
import dev.askov.vipet.localization.LocalizationManager;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ControllerInjector {

  private static final Logger LOGGER = LoggerFactory.getLogger(ControllerInjector.class);
  private static final String FXML_ROOT_DIRECTORY = "fxml";
  private static final String FXML_EXTENSION = "fxml";
  private static final Map<Class<?>, Callable<?>> INJECTION_METHODS = new HashMap<>();

  public static void registerControllerFactory(
      final Class<?> clazz, final Callable<?> injectionMethod) {
    INJECTION_METHODS.put(clazz, injectionMethod);
  }

  public static FXMLLoader getLoader(final String viewName) {
    LOGGER.debug("Creating FXML loader for {}", viewName);

    return new FXMLLoader(
        VIPET.class.getResource(
            "%s/%s.%s".formatted(FXML_ROOT_DIRECTORY, viewName, FXML_EXTENSION)),
        LocalizationManager.getInstance().getResourceBundle(),
        new JavaFXBuilderFactory(),
        ControllerInjector::constructController);
  }

  private static Object constructController(final Class<?> clazz) {
    LOGGER.debug("Constructing {}", clazz.getSimpleName());

    final var injectionMethod = INJECTION_METHODS.get(clazz);
    if (injectionMethod != null) {
      try {
        return injectionMethod.call();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    } else {
      try {
        return clazz.getConstructor().newInstance();
      } catch (InstantiationException
          | IllegalAccessException
          | InvocationTargetException
          | NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
