package dev.askov.vipet.localization;

import java.util.Locale;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;

public final class LocalizationManager {

  public static final Locale[] SUPPORTED_LOCALES = {
    Locale.forLanguageTag("sr"), Locale.forLanguageTag("en"),
  };
  public static final Locale DEFAULT_LOCALE = SUPPORTED_LOCALES[0];

  private static final LocalizationManager INSTANCE = new LocalizationManager();

  private final ObjectProperty<Locale> localeProperty = new SimpleObjectProperty<>();
  private final ReadOnlyObjectWrapper<ResourceBundle> resourceBundlePropertyWrapper =
      new ReadOnlyObjectWrapper<>(this, "resourceBundle");

  private LocalizationManager(final Locale locale) {
    localeProperty.set(locale);
    resourceBundlePropertyWrapper.bind(
        Bindings.createObjectBinding(
            () -> ResourceBundle.getBundle("i18n/messages", getLocale()), localeProperty));
  }

  private LocalizationManager() {
    this(DEFAULT_LOCALE);
  }

  public static LocalizationManager getInstance() {
    return INSTANCE;
  }

  public ObjectProperty<Locale> localeProperty() {
    return localeProperty;
  }

  public Locale getLocale() {
    return localeProperty.get();
  }

  public void setLocale(final Locale locale) {
    localeProperty.set(locale);
  }

  public ReadOnlyObjectProperty<ResourceBundle> resourceBundleProperty() {
    return resourceBundlePropertyWrapper.getReadOnlyProperty();
  }

  public ResourceBundle getResourceBundle() {
    return resourceBundlePropertyWrapper.get();
  }

  public StringBinding getStringBinding(final String key) {
    return Bindings.createStringBinding(
        () -> resourceBundlePropertyWrapper.get().getString(key), resourceBundlePropertyWrapper);
  }

  public StringBinding getStringBinding(final String key, final Object... arguments) {
    return Bindings.createStringBinding(
        () -> resourceBundlePropertyWrapper.get().getString(key).formatted(arguments),
        resourceBundlePropertyWrapper);
  }

  public String getString(final String key) {
    return getResourceBundle().getString(key);
  }

  public String getString(final String key, final Object... arguments) {
    return getResourceBundle().getString(key).formatted(arguments);
  }

  public static boolean isLocaleSupported(final Locale locale) {
    for (final var supportedLocale : SUPPORTED_LOCALES) {
      if (supportedLocale.equals(locale)) {
        return true;
      }
    }
    return false;
  }
}
