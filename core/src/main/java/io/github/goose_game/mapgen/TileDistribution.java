package io.github.goose_game.mapgen;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Random;

/**
 * A weighted probability distribution over {@link TileType}s, which can be sampled using a {@link
 * Random}.
 *
 * <p>A type's chance of being drawn is its weight divided by the sum of all weights, which means
 * that weights do not need to add to 100. Types with no specified weight have a weight of 0, and
 * are therefore never drawn.
 */
public final class TileDistribution {

  /**
   * The default weights for map generation, which are 80 grass, 10 water, and 5 each of bushes and
   * buildings. The default distribution cannot contain any values of POIs.
   */
  public static final Map<TileType, Integer> DEFAULT_DISTRIBUTION =
      new EnumMap<>(
          Map.of(
              TileType.GRASS, 80,
              TileType.WATER, 10,
              TileType.BUSH, 5,
              TileType.BUILDING, 5));

  /**
   * The points of interest on the map. POIs are unique, are always surrounded by a ring of grass
   * tiles, and there always exists a path between each one. {@link TileType#SPAWN} must be one of
   * these POIs, and the map generator itself must ensure this.
   */
  public static final EnumSet<TileType> POINTS_OF_INTEREST = EnumSet.of(
      TileType.SPAWN,
      TileType.ESCAPE
  );

  private static final TileType[] VALUES = TileType.values();
  private final int[] cumulative; // Used for the distribution of each TileType
  private final int total; // Used as an upper bound for random numbers

  /**
   * Creates a distribution from the given weights.
   *
   * @param weights the weight of each tile type; types missing from the map get a weight of 0
   * @throws IllegalArgumentException if any weight is negative, or if all weights are 0
   */
  public TileDistribution(Map<TileType, Integer> weights) {
    this.cumulative = new int[VALUES.length];
    int sum = 0;

    for (int i = 0; i < VALUES.length; i++) {
      // Retrieve the weight of the given TileType, or 0 if one has not been specified
      int weight = weights.getOrDefault(VALUES[i], 0);

      if (weight < 0) {
        throw new IllegalArgumentException("Negative weight for " + VALUES[i]);
      } else if (POINTS_OF_INTEREST.contains(VALUES[i]) && weight != 0) {
        throw new IllegalArgumentException(VALUES[i] + " has a weight > 0, despite being listed as"
            + " a POI.");
      }

      sum += weight;
      cumulative[i] = sum;
    }

    // Weights must sum to at least 1 (which would mean that all tiles on the map are of whichever
    // type has the weight of 1)
    if (sum <= 0) {
      throw new IllegalArgumentException("Weights must sum to at least 1");
    }

    this.total = sum;
  }

  /**
   * Draws one tile type at random, in proportion to the weights.
   *
   * @param rng the random number generator to draw from
   * @return the sampled tile type
   */
  public TileType sample(Random rng) {
    int roll = rng.nextInt(total);

    for (int i = 0; i < VALUES.length; i++) {
      if (roll < cumulative[i]) {
        return VALUES[i];
      }
    }

    // Impossible to reach this bit of code
    throw new IllegalStateException("Random number was outside of the given distribution.");
  }

  /**
   * Draws one tile type at random, as in {@link #sample(Random)}, but with the weight of one type
   * increased.
   *
   * @param rng the random number generator to draw from
   * @param boosted the tile type whose weight is increased
   * @param percentageBoost the increase as a fraction of the weight, e.g. {@code 0.75f} multiplies
   *     the weight by 1.75
   * @return the sampled tile type
   */
  public TileType boostedSample(Random rng, TileType boosted, float percentageBoost) {

    // Firstly calculate new distribution with the boosted tile
    int[] boostedCumulative = new int[VALUES.length];
    int sum = 0;

    for (int i = 0; i < VALUES.length; i++) {
      // Retrieve the weight of the given TileType, or 0 if one has not been specified
      int weight = cumulative[i];
      if (VALUES[i].equals(boosted)) {
        weight *= 1 + percentageBoost;
      }

      sum += weight;
      boostedCumulative[i] = sum;
    }

    /*
     * Now roll and get sample as usual.
     *
     * Developers note: I would have extracted this logic out to a helper method, as it is shared
     * with sample(), however this would not be worth the faff of  having to validate each value in
     * cumulative / boostedCumulative. Due to the fact that we lean on the constructor's cumulative
     * array here, we do not need to enforce its validation again, whereas a seperate helper would
     * be independent of the constructor's validation and would need to apply its own.
     */
    int roll = rng.nextInt(sum);

    for (int i = 0; i < VALUES.length; i++) {
      if (roll < boostedCumulative[i]) {
        return VALUES[i];
      }
    }

    // Impossible to reach this bit of code
    throw new IllegalStateException("Random number was outside of the given distribution.");
  }
}
