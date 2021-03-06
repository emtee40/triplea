package games.strategy.triplea.delegate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import javax.annotation.Nullable;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Territory;
import games.strategy.engine.data.Unit;
import games.strategy.triplea.attachments.TerritoryAttachment;
import games.strategy.triplea.xml.TestMapGameData;
import games.strategy.util.Match;

@RunWith(Enclosed.class)
public final class MatchesTests {

  private static final Match<Integer> IS_ZERO_MATCH = Match.of(it -> it == 0);
  private static final Object VALUE = new Object();

  private static <T> Matcher<Match<T>> matches(final @Nullable T value) {
    return new TypeSafeDiagnosingMatcher<Match<T>>() {
      @Override
      public void describeTo(final Description description) {
        description.appendText("matcher matches using ").appendValue(value);
      }

      @Override
      public boolean matchesSafely(final Match<T> match, final Description description) {
        if (!match.match(value)) {
          description.appendText("it does not match");
          return false;
        }
        return true;
      }
    };
  }

  private static <T> Matcher<Match<T>> notMatches(final @Nullable T value) {
    return new TypeSafeDiagnosingMatcher<Match<T>>() {
      @Override
      public void describeTo(final Description description) {
        description.appendText("matcher does not match using ").appendValue(value);
      }

      @Override
      public boolean matchesSafely(final Match<T> match, final Description description) {
        if (match.match(value)) {
          description.appendText("it matches");
          return false;
        }
        return true;
      }
    };
  }

  @Test
  public void testAlways() {
    assertTrue(Matches.always().match(VALUE));
  }

  @Test
  public void testNever() {
    assertFalse(Matches.never().match(VALUE));
  }

  @Test
  public void testCountMatches() {
    assertEquals(0, Matches.countMatches(Arrays.asList(), IS_ZERO_MATCH));

    assertEquals(1, Matches.countMatches(Arrays.asList(0), IS_ZERO_MATCH));
    assertEquals(1, Matches.countMatches(Arrays.asList(-1, 0, 1), IS_ZERO_MATCH));

    assertEquals(2, Matches.countMatches(Arrays.asList(0, 0), IS_ZERO_MATCH));
    assertEquals(2, Matches.countMatches(Arrays.asList(-1, 0, 1, 0), IS_ZERO_MATCH));
  }

  @Test
  public void testGetMatches() {
    final Collection<Integer> input = Arrays.asList(-1, 0, 1);

    assertEquals("empty collection", Arrays.asList(), Matches.getMatches(Arrays.asList(), Matches.always()));
    assertEquals("none match", Arrays.asList(), Matches.getMatches(input, Matches.never()));
    assertEquals("some match", Arrays.asList(-1, 1), Matches.getMatches(input, IS_ZERO_MATCH.invert()));
    assertEquals("all match", Arrays.asList(-1, 0, 1), Matches.getMatches(input, Matches.always()));
  }

  @Test
  public void testGetNMatches() {
    final Collection<Integer> input = Arrays.asList(-1, 0, 1);

    assertEquals("empty collection", Arrays.asList(), Matches.getNMatches(Arrays.asList(), 999, Matches.always()));
    assertEquals("max = 0", Arrays.asList(), Matches.getNMatches(input, 0, Matches.never()));
    assertEquals("none match", Arrays.asList(), Matches.getNMatches(input, input.size(), Matches.never()));
    assertEquals("some match; max < count",
        Arrays.asList(0),
        Matches.getNMatches(Arrays.asList(-1, 0, 0, 1), 1, IS_ZERO_MATCH));
    assertEquals("some match; max = count",
        Arrays.asList(0, 0),
        Matches.getNMatches(Arrays.asList(-1, 0, 0, 1), 2, IS_ZERO_MATCH));
    assertEquals("some match; max > count",
        Arrays.asList(0, 0),
        Matches.getNMatches(Arrays.asList(-1, 0, 0, 1), 3, IS_ZERO_MATCH));
    assertEquals("all match; max < count",
        Arrays.asList(-1, 0),
        Matches.getNMatches(input, input.size() - 1, Matches.always()));
    assertEquals("all match; max = count",
        Arrays.asList(-1, 0, 1),
        Matches.getNMatches(input, input.size(), Matches.always()));
    assertEquals("all match; max > count",
        Arrays.asList(-1, 0, 1),
        Matches.getNMatches(input, input.size() + 1, Matches.always()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetNMatches_ShouldThrowExceptionWhenMaxIsNegative() {
    Matches.getNMatches(Arrays.asList(-1, 0, 1), -1, Matches.always());
  }

  public static final class TerritoryHasEnemyUnitsThatCanCaptureItAndIsOwnedByTheirEnemyTest {
    private GameData gameData;
    private PlayerID player;
    private PlayerID alliedPlayer;
    private PlayerID enemyPlayer;
    private Territory territory;

    private Match<Territory> newMatch() {
      return Matches.territoryHasEnemyUnitsThatCanCaptureItAndIsOwnedByTheirEnemy(player, gameData);
    }

    private Unit newAirUnitFor(final PlayerID player) {
      return GameDataTestUtil.fighter(gameData).create(player);
    }

    private Unit newInfrastructureUnitFor(final PlayerID player) {
      return GameDataTestUtil.aaGun(gameData).create(player);
    }

    private Unit newLandUnitFor(final PlayerID player) {
      return GameDataTestUtil.infantry(gameData).create(player);
    }

    private Unit newSeaUnitFor(final PlayerID player) {
      return GameDataTestUtil.battleship(gameData).create(player);
    }

    @Before
    public void setUp() throws Exception {
      gameData = TestMapGameData.DELEGATE_TEST.getGameData();

      player = GameDataTestUtil.germans(gameData);
      alliedPlayer = GameDataTestUtil.japanese(gameData);
      assertThat(gameData.getRelationshipTracker().isAtWar(player, alliedPlayer), is(false));
      enemyPlayer = GameDataTestUtil.russians(gameData);
      assertThat(gameData.getRelationshipTracker().isAtWar(player, enemyPlayer), is(true));

      territory = gameData.getMap().getTerritory("Germany");
      territory.setOwner(player);
      territory.getUnits().clear();
    }

    @Test
    public void shouldNotMatchWhenTerritoryContainsOnlyAlliedLandUnits() {
      territory.getUnits().add(newLandUnitFor(alliedPlayer));

      assertThat(newMatch(), notMatches(territory));
    }

    @Test
    public void shouldMatchWhenTerritoryContainsEnemyLandUnits() {
      territory.getUnits().addAll(Arrays.asList(
          newLandUnitFor(player),
          newLandUnitFor(enemyPlayer),
          newAirUnitFor(enemyPlayer),
          newInfrastructureUnitFor(enemyPlayer)));

      assertThat(newMatch(), matches(territory));
    }

    @Test
    public void shouldMatchWhenTerritoryContainsEnemySeaUnits() {
      territory.getUnits().addAll(Arrays.asList(
          newSeaUnitFor(player),
          newSeaUnitFor(enemyPlayer),
          newAirUnitFor(enemyPlayer),
          newInfrastructureUnitFor(enemyPlayer)));

      assertThat(newMatch(), matches(territory));
    }

    @Test
    public void shouldNotMatchWhenTerritoryContainsOnlyEnemyAirUnits() {
      territory.getUnits().add(newAirUnitFor(enemyPlayer));

      assertThat(newMatch(), notMatches(territory));
    }

    @Test
    public void shouldNotMatchWhenTerritoryContainsOnlyEnemyInfrastructureUnits() {
      territory.getUnits().add(newInfrastructureUnitFor(enemyPlayer));

      assertThat(newMatch(), notMatches(territory));
    }
  }

  public static final class TerritoryIsNotUnownedWaterTest {
    private GameData gameData;
    private PlayerID player;
    private Territory landTerritory;
    private Territory seaTerritory;

    private static Match<Territory> newMatch() {
      return Matches.territoryIsNotUnownedWater();
    }

    @Before
    public void setUp() throws Exception {
      gameData = TestMapGameData.DELEGATE_TEST.getGameData();

      player = GameDataTestUtil.germans(gameData);

      landTerritory = gameData.getMap().getTerritory("Germany");
      landTerritory.setOwner(player);
      assertThat(TerritoryAttachment.get(landTerritory), is(notNullValue()));

      seaTerritory = gameData.getMap().getTerritory("Baltic Sea Zone");
      seaTerritory.setOwner(player);
      assertThat(TerritoryAttachment.get(seaTerritory), is(nullValue()));
      TerritoryAttachment.add(seaTerritory, new TerritoryAttachment("name", seaTerritory, gameData));
      assertThat(TerritoryAttachment.get(seaTerritory), is(notNullValue()));
    }

    @Test
    public void shouldMatchWhenLandTerritoryIsOwnedAndHasTerritoryAttachment() {
      assertThat(newMatch(), matches(landTerritory));
    }

    @Test
    public void shouldMatchWhenLandTerritoryIsOwnedAndDoesNotHaveTerritoryAttachment() {
      TerritoryAttachment.remove(landTerritory);

      assertThat(newMatch(), matches(landTerritory));
    }

    @Test
    public void shouldMatchWhenLandTerritoryIsUnownedAndHasTerritoryAttachment() {
      landTerritory.setOwner(PlayerID.NULL_PLAYERID);

      assertThat(newMatch(), matches(landTerritory));
    }

    @Test
    public void shouldMatchWhenLandTerritoryIsUnownedAndDoesNotHaveTerritoryAttachment() {
      landTerritory.setOwner(PlayerID.NULL_PLAYERID);
      TerritoryAttachment.remove(landTerritory);

      assertThat(newMatch(), matches(landTerritory));
    }

    @Test
    public void shouldMatchWhenSeaTerritoryIsOwnedAndHasTerritoryAttachment() {
      assertThat(newMatch(), matches(seaTerritory));
    }

    @Test
    public void shouldMatchWhenSeaTerritoryIsOwnedAndDoesNotHaveTerritoryAttachment() {
      TerritoryAttachment.remove(seaTerritory);

      assertThat(newMatch(), matches(seaTerritory));
    }

    @Test
    public void shouldMatchWhenSeaTerritoryIsUnownedAndHasTerritoryAttachment() {
      seaTerritory.setOwner(PlayerID.NULL_PLAYERID);

      assertThat(newMatch(), matches(seaTerritory));
    }

    @Test
    public void shouldNotMatchWhenSeaTerritoryIsUnownedAndDoesNotHaveTerritoryAttachment() {
      seaTerritory.setOwner(PlayerID.NULL_PLAYERID);
      TerritoryAttachment.remove(seaTerritory);

      assertThat(newMatch(), notMatches(seaTerritory));
    }
  }
}
