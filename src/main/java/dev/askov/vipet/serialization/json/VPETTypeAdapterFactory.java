package dev.askov.vipet.serialization.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import dev.askov.vipet.mvc.models.distributions.*;
import dev.askov.vipet.mvc.models.network.NetworkModel;
import dev.askov.vipet.mvc.models.network.ServiceCenterModel;
import dev.askov.vipet.mvc.models.network.SinkModel;
import dev.askov.vipet.mvc.models.network.SourceModel;
import dev.askov.vipet.mvc.models.network.queue.QueueModel;
import dev.askov.vipet.mvc.models.network.routing.ProbabilityRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RandomRoutingStrategyModel;
import dev.askov.vipet.mvc.models.network.routing.RoundRobinRoutingStrategyModel;
import dev.askov.vipet.serialization.json.distributions.*;
import dev.askov.vipet.serialization.json.network.NetworkTypeAdapter;
import dev.askov.vipet.serialization.json.network.ServiceCenterTypeAdapter;
import dev.askov.vipet.serialization.json.network.SinkTypeAdapter;
import dev.askov.vipet.serialization.json.network.SourceTypeAdapter;
import dev.askov.vipet.serialization.json.network.queue.QueueTypeAdapter;
import dev.askov.vipet.serialization.json.network.routing.ProbabilityRoutingStrategyAdapter;
import dev.askov.vipet.serialization.json.network.routing.RandomRoutingStrategyTypeAdapter;
import dev.askov.vipet.serialization.json.network.routing.RoundRobinRoutingStrategyTypeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VPETTypeAdapterFactory implements TypeAdapterFactory {

  private static final Logger LOGGER = LoggerFactory.getLogger(VPETTypeAdapterFactory.class);

  @Override
  @SuppressWarnings("unchecked")
  public <T> TypeAdapter<T> create(final Gson gson, final TypeToken<T> typeToken) {
    final var rawClass = typeToken.getRawType();

    // Network and network elements
    if (NetworkModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) NetworkTypeAdapter.getInstance(gson);
    } else if (ServiceCenterModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) ServiceCenterTypeAdapter.getInstance(gson);
    } else if (SourceModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) SourceTypeAdapter.getInstance(gson);
    } else if (SinkModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) SinkTypeAdapter.getInstance();
    } else if (QueueModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) QueueTypeAdapter.getInstance();
    }

    // Time distributions
    if (UniformTimeDistributionModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) UniformTimeDistributionTypeAdapter.getInstance();
    } else if (ExponentialTimeDistributionModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) ExponentialTimeDistributionTypeAdapter.getInstance();
    } else if (DeterministicTimeDistributionModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) DeterministicTimeDistributionTypeAdapter.getInstance();
    } else if (ErlangTimeDistributionModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) ErlangTimeDistributionTypeAdapter.getInstance();
    } else if (ParetoTimeDistributionModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) ParetoTimeDistributionTypeAdapter.getInstance();
    } else if (HyperexponentialTimeDistributionModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) HyperexponentialTimeDistributionTypeAdapter.getInstance();
    }

    // Routing strategies
    if (RandomRoutingStrategyModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) RandomRoutingStrategyTypeAdapter.getInstance();
    } else if (RoundRobinRoutingStrategyModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) RoundRobinRoutingStrategyTypeAdapter.getInstance();
    } else if (ProbabilityRoutingStrategyModel.class.isAssignableFrom(rawClass)) {
      return (TypeAdapter<T>) ProbabilityRoutingStrategyAdapter.getInstance();
    }

    LOGGER.error("No type adapter found for class {}", rawClass.getName());

    return null;
  }
}
