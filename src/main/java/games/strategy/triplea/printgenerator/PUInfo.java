package games.strategy.triplea.printgenerator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import games.strategy.debug.ClientLogger;
import games.strategy.engine.data.GameData;
import games.strategy.engine.data.PlayerID;
import games.strategy.engine.data.Resource;

public class PUInfo {
  private final Map<PlayerID, Map<Resource, Integer>> infoMap = new HashMap<>();

  protected void saveToFile(final PrintGenerationData printData) {
    final GameData gameData = printData.getData();
    Iterator<PlayerID> playerIterator = gameData.getPlayerList().iterator();
    while (playerIterator.hasNext()) {
      final PlayerID currentPlayer = playerIterator.next();
      final Iterator<Resource> resourceIterator = gameData.getResourceList().getResources().iterator();
      final Map<Resource, Integer> resourceMap = new HashMap<>();
      while (resourceIterator.hasNext()) {
        final Resource currentResource = resourceIterator.next();
        final int amountOfResource = currentPlayer.getResources().getQuantity(currentResource);
        resourceMap.put(currentResource, amountOfResource);
      }
      infoMap.put(currentPlayer, resourceMap);
    }
    FileWriter resourceWriter = null;
    try {
      final File outFile = new File(printData.getOutDir(), "General Information.csv");
      resourceWriter = new FileWriter(outFile, true);
      // Print Title
      final int numResources = gameData.getResourceList().size();
      for (int i = 0; i < numResources / 2 - 1 + numResources % 2; i++) {
        resourceWriter.write(",");
      }
      resourceWriter.write("Resource Chart");
      for (int i = 0; i < numResources / 2 - numResources % 2; i++) {
        resourceWriter.write(",");
      }
      resourceWriter.write("\r\n");
      // Print Resources
      final Iterator<Resource> resourceIterator = gameData.getResourceList().getResources().iterator();
      resourceWriter.write(",");
      while (resourceIterator.hasNext()) {
        final Resource currentResource = resourceIterator.next();
        resourceWriter.write(currentResource.getName() + ",");
      }
      resourceWriter.write("\r\n");
      // Print Player's and Resource Amount's
      playerIterator = gameData.getPlayerList().iterator();
      while (playerIterator.hasNext()) {
        final PlayerID currentPlayer = playerIterator.next();
        resourceWriter.write(currentPlayer.getName());
        final Map<Resource, Integer> resourceMap = infoMap.get(currentPlayer);
        final Iterator<Resource> resIterator = resourceMap.keySet().iterator();
        while (resIterator.hasNext()) {
          final Resource currentResource = resIterator.next();
          final int amountResource = resourceMap.get(currentResource);
          resourceWriter.write("," + amountResource);
        }
        resourceWriter.write("\r\n");
      }
      resourceWriter.write("\r\n");
      resourceWriter.close();
    } catch (final IOException e) {
      ClientLogger.logQuietly(e);
    }
  }
}
