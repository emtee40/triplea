package games.strategy.util;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;

import org.junit.Test;

public final class FileNameUtilsTest {
  @Test
  public void removeIllegalCharacters_ShouldRemoveIllegalCharacters() {
    assertThat(FileNameUtils.removeIllegalCharacters(FileNameUtils.ILLEGAL_CHARACTERS), is(""));
  }

  @Test
  public void removeIllegalCharacters_ShouldNotRemoveLegalCharacters() {
    assertThat(FileNameUtils.removeIllegalCharacters("AZaz09!-"), is("AZaz09!-"));
  }

  @Test
  public void replaceIllegalCharacters_ShouldReplaceIllegalCharacters() {
    assertThat(FileNameUtils.replaceIllegalCharacters(FileNameUtils.ILLEGAL_CHARACTERS, '_'),
        is(String.join("", Collections.nCopies(FileNameUtils.ILLEGAL_CHARACTERS.length(), "_"))));
  }

  @Test
  public void replaceIllegalCharacters_ShouldNotReplaceLegalCharacters() {
    assertThat(FileNameUtils.replaceIllegalCharacters("AZaz09!-", '_'), is("AZaz09!-"));
  }
}
