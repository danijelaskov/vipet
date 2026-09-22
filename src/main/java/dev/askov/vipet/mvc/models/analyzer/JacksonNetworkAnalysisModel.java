package dev.askov.vipet.mvc.models.analyzer;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public record JacksonNetworkAnalysisModel(
    RealMatrix probabilities,
    RealMatrix intermediateMatrix1,
    RealMatrix intermediateMatrix2,
    RealVector externalArrivalRates,
    RealVector averageArrivalRates,
    RealVector rhoVector,
    PerformanceMeasureCollection performanceMeasureCollection,
    Map<ServiceCenterModel, List<Pair<Integer, Double>>> stateProbabilities) {}
