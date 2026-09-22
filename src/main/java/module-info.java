module dev.askov.vipet {
  requires javafx.controls;
  requires javafx.fxml;
  requires javafx.swing;
  requires javafx.web;
  requires javafx.base;
  requires transitive javafx.graphics;
  requires jcommander;
  requires com.google.gson;
  requires com.opencsv;
  requires org.apache.poi.ooxml;
  requires org.apache.poi.ooxml.schemas;
  requires commons.math3;
  requires org.kordamp.ikonli.core;
  requires org.kordamp.ikonli.javafx;
  requires org.kordamp.ikonli.codicons;
  requires org.kordamp.ikonli.coreui;
  requires org.kordamp.ikonli.fluentui;
  requires org.kordamp.ikonli.materialdesign2;
  requires org.kordamp.ikonli.carbonicons;
  requires jlatexmath;
  requires org.slf4j;
  requires org.apache.logging.log4j.slf4j2.impl;
  requires org.apache.logging.log4j.core;

  opens dev.askov.vipet.mvc.controllers to
      javafx.fxml;
  opens dev.askov.vipet.mvc.controllers.networkeditor to
      javafx.fxml;
  opens dev.askov.vipet.mvc.controllers.distributions to
      javafx.fxml;
  opens dev.askov.vipet.mvc.controllers.analysis to
      javafx.fxml;
  opens dev.askov.vipet.mvc.views.components to
      javafx.fxml;
  opens dev.askov.vipet.mvc.views.formatters to
      javafx.fxml;
  opens dev.askov.vipet.configuration to
      jcommander;
  opens dev.askov.vipet to
      jcommander;

  exports dev.askov.vipet to
      javafx.graphics;
}
