package games.strategy.engine.lobby.server.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utilitiy class to create/read/delete bad words (there is no update).
 */
public class BadWordController {
  private static final Logger logger = Logger.getLogger(BadWordController.class.getName());

  public void addBadWord(final String word) {
    try (final Connection con = Database.getPostgresConnection();
        final PreparedStatement ps = con.prepareStatement("insert into bad_words (word) values (?)")) {
      ps.setString(1, word);
      ps.execute();
      con.commit();
    } catch (final SQLException sqle) {
      if (sqle.getErrorCode() == 30000) {
        // this is ok
        // the word is bad as expected
        logger.info("Tried to create duplicate banned word:" + word + " error:" + sqle.getMessage());
        return;
      }
      throw new IllegalStateException("Error inserting banned word:" + word, sqle);
    }
  }

  void removeBannedWord(final String word) {
    try (final Connection con = Database.getPostgresConnection();
        final PreparedStatement ps = con.prepareStatement("delete from bad_words where word = ?")) {
      ps.setString(1, word);
      ps.execute();
      con.commit();
    } catch (final SQLException sqle) {
      throw new IllegalStateException("Error deleting banned word:" + word, sqle);
    }
  }

  public List<String> list() {
    final String sql = "select word from bad_words";

    try (final Connection con = Database.getPostgresConnection();
        final PreparedStatement ps = con.prepareStatement(sql);
        final ResultSet rs = ps.executeQuery()) {
      final List<String> badWords = new ArrayList<>();
      while (rs.next()) {
        badWords.add(rs.getString(1));
      }
      return badWords;
    } catch (final SQLException sqle) {
      throw new IllegalStateException("Error reading bad words", sqle);
    }
  }
}
