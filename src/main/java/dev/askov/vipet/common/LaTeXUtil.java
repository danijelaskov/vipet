package dev.askov.vipet.common;

import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javax.swing.*;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LaTeXUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(LaTeXUtil.class);
  private static final int DEFAULT_FONT_SIZE = 18;

  private LaTeXUtil() {}

  public static Image createImageFromLaTeX(String laTeXString, final Color foregroundColor) {
    try {
      final var formula = new TeXFormula(laTeXString);
      final var icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, DEFAULT_FONT_SIZE);

      if (foregroundColor != null) {
        icon.setForeground(foregroundColor);
      }

      final var width = icon.getIconWidth();
      final var height = icon.getIconHeight();

      if (width <= 0 || height <= 0) {
        throw new IllegalArgumentException("Invalid icon dimensions: " + width + "x" + height);
      }

      // Create the buffered image with the correct size
      final var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

      // Draw the LaTeX formula onto the image
      final var g2 = image.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setRenderingHint(
          RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

      final var jl = new JLabel();
      jl.setForeground(new Color(0, 0, 0)); // Set the color to black
      icon.paintIcon(jl, g2, 0, 0);
      g2.dispose();

      return SwingFXUtils.toFXImage(image, null);
    } catch (Exception e) {
      LOGGER.error("Failed to render LaTeX formula to image", e);

      return null;
    }
  }

  public static Image createImageFromLaTeX(String laTeXString) {
    return createImageFromLaTeX(laTeXString, null);
  }

  public static void populateMatrix(final RealMatrix matrix, final StringBuilder builder) {
    builder.append("\\begin{bmatrix}");

    for (var i = 0; i < matrix.getRowDimension(); i++) {
      for (var j = 0; j < matrix.getColumnDimension(); j++) {
        builder.append(NumericFormatUtil.formatNumber(matrix.getEntry(i, j), true));

        if (j < matrix.getColumnDimension() - 1) {
          builder.append(" & ");
        }
      }

      if (i < matrix.getRowDimension() - 1) {
        builder.append(" \\\\ ");
      }
    }

    builder.append("\\end{bmatrix}");
  }

  public static void populateColumnVector(final RealVector vector, final StringBuilder builder) {
    builder.append("\\begin{bmatrix}");

    for (var i = 0; i < vector.getDimension(); i++) {
      builder.append(NumericFormatUtil.formatNumber(vector.getEntry(i), true));

      if (i < vector.getDimension() - 1) {
        builder.append(" \\\\ ");
      }
    }

    builder.append("\\end{bmatrix}");
  }
}
