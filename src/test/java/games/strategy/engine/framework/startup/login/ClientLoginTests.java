package games.strategy.engine.framework.startup.login;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

@RunWith(Enclosed.class)
public final class ClientLoginTests {
  private static final String PASSWORD = "password";

  public static final class AddAuthenticationResponsePropertiesTest {
    @Test
    public void shouldCreateResponseProcessableByMd5CryptAuthenticatorWhenMd5CryptChallengePresent() {
      final Map<String, String> challenge = Md5CryptAuthenticator.newChallenge();
      final Map<String, String> response = Maps.newHashMap();

      ClientLogin.addAuthenticationResponseProperties(PASSWORD, challenge, response);

      assertThat(Md5CryptAuthenticator.canProcessResponse(response), is(true));
    }

    @Test
    public void shouldCreateResponseProcessableByHmacSha512AuthenticatorWhenHmacSha512ChallengePresent() {
      final Map<String, String> challenge = Maps.newHashMap(ImmutableMap.<String, String>builder()
          .putAll(Md5CryptAuthenticator.newChallenge())
          .putAll(HmacSha512Authenticator.newChallenge())
          .build());
      final Map<String, String> response = Maps.newHashMap();

      ClientLogin.addAuthenticationResponseProperties(PASSWORD, challenge, response);

      assertThat(HmacSha512Authenticator.canProcessResponse(response), is(true));
    }

    @Test
    public void shouldCreateResponseIgnoredByHmacSha512AuthenticatorWhenHmacSha512ChallengeAbsent() {
      final Map<String, String> challenge = Md5CryptAuthenticator.newChallenge();
      final Map<String, String> response = Maps.newHashMap();

      ClientLogin.addAuthenticationResponseProperties(PASSWORD, challenge, response);

      assertThat(HmacSha512Authenticator.canProcessResponse(response), is(false));
    }
  }
}
