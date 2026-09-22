package dev.askov.vipet.mvc.models.analyzer;

import dev.askov.vipet.core.performancemeasures.PerformanceMeasureCollection;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import java.util.List;
import java.util.Map;
import javafx.util.Pair;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public record GordonNewellAnalysisModel(
    RealMatrix probabilitySubMatrix,
    RealMatrix intermediateMatrix1,
    RealMatrix intermediateMatrix2,
    RealVector probabilitySubVector,
    RealVector visitRatioSubVector,
    RealVector visitRatios,
    RealMatrix fMatrix,
    RealMatrix gMatrix,
    int numberOfServiceCenters,
    int numberOfJobs,
    PerformanceMeasureCollection performanceMeasureCollection,
    Map<ServiceCenterModel, List<Pair<Integer, Double>>> stateProbabilities) {}
