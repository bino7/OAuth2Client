package com.yunpos;

/**
 * The access token.
 * 
 * @author Maycon Bordin <mayconbordin@gmail.com>
 */
public class Token {
    private final long expiresIn;
    private final long expiresAt;
    private final String tokenType;
    private final String refreshToken;
    private final String token;
    private final String scope;

    /**
     * Create an access token.
     * 
     * @param expiresIn The life expectancy of the token in seconds.
     * @param tokenType The type of token.
     * @param refreshToken The refresh token value.
     * @param token The access token value.
     */
    public Token(long expiresIn, String tokenType, String token,String refreshToken,String scope ) {
        this.expiresIn    = expiresIn;
        this.tokenType    = tokenType;
        this.refreshToken = refreshToken;
        this.token = token;
        this.expiresAt    = (expiresIn * 1000) + System.currentTimeMillis();
        this.scope=scope;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getToken() {
        return token;
    }

    /**
     * Checks if the access token might have expired, by comparing  the time the token
     * was created plus {@link #expiresIn} and the current time.
     * @return True if the token expired, false otherwise.
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() >= this.getExpiresAt());
    }



    /**
     * Refresh this token.
     * 
     * @param client The client for refreshing the token, the same used to create this token.
     * @return The refreshed token.
     * @throws OAuth2Exception 
     */
    public Token refresh(OAuth2Client client) throws OAuth2Exception {
        OAuth2Config oauthConfig = new OAuth2Config.Builder(client.getConfig())
                .grantType(OAuth2Constants.GRANT_REFRESH_TOKEN)
                .build();
        return null;
        //return OAuth2Utils.refreshAccessToken(this, oauthConfig);
    }

    @Override
    public String toString() {
        return "Token{" + "expiresIn=" + expiresIn + ", expiresAt=" + expiresAt 
                + ", tokenType=" + tokenType + ", refreshToken=" + refreshToken 
                + ", token=" + token + '}';
    }
}
