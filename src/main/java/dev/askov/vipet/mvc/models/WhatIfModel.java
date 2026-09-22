package dev.askov.vipet.mvc.models;

import dev.askov.vipet.mvc.models.network.NetworkModel;

public record WhatIfModel(
    NetworkModel networkModel, WhatIfModel.PerformanceMeasuresProviderType type) {

  public enum PerformanceMeasuresProviderType {
    ANALYZER,
    SIMULATOR
  }
}
