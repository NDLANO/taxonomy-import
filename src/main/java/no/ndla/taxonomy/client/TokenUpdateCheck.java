package no.ndla.taxonomy.client;

import java.time.Instant;

public class TokenUpdateCheck  {
    public static boolean shouldUpdateToken(Long last_token_update, Authentication authentication) {
        return last_token_update == null
                || authentication == null
                || ((last_token_update + ((authentication.expires_in - 300) * 1000)) <= (Instant.now().toEpochMilli()));
    }
}
