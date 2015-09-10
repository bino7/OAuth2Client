package com.yunpos;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.IntNode;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * 功能描述：
 * <p>
 * 版权所有：小牛信息科技有限公司
 * <p>
 * 未经本公司许可，不得以任何方式复制或使用本程序任何部分
 *
 * @author bino 新增日期：2015/9/9
 * @author bino 修改日期：2015/9/9
 */
public class OAuth2Client {
    private OAuth2Config config;
    private Token token;
    private Optional<CloseableHttpClient>  httpClient;
    private Supplier<CloseableHttpClient> httpClientSupplier;
    private Map<String,OAuth2Request> oAuth2RequestMap;
    public static OAuth2Client withPasswordGrant(String username, String password,
                                                 String clientId, String clientSecret, String scope, String urlAccessToken) {
        OAuth2Config config = new OAuth2Config.Builder(clientId, clientSecret, urlAccessToken)
                .grantType(OAuth2Constants.GRANT_PASSWORD)
                .credentials(username, password)
                .scope(scope)
                .build();

        return new OAuth2Client(config);
    }
    public OAuth2Client(OAuth2Config config){
        this.config =config;
        oAuth2RequestMap=new HashMap<>();
        httpClient=Optional.empty();
        httpClientSupplier=HttpClients::createDefault;
        OAuth2Request tokenRequest=
                OAuth2Request.post(config.getUrlAccessToken())
                .header(OAuth2Constants.HEADER_ACCEPT, OAuth2Constants.JSON_CONTENT)
                .parameter(OAuth2Constants.CLIENT_ID, config.getClientId())
                .parameter(OAuth2Constants.CLIENT_SECRET, config.getClientSecret())
                .parameter(OAuth2Constants.GRANT_TYPE, OAuth2Constants.GRANT_PASSWORD)
                .parameter(OAuth2Constants.USERNAME, config.getUsername())
                .parameter(OAuth2Constants.PASSWORD, config.getPassword())
                .afterResponse(tokenResponseHandler);
        tokenRequest.setName(OAuth2Constants.TOKEN_REQUEST);
        oAuth2RequestMap.put(OAuth2Constants.TOKEN_REQUEST, tokenRequest);

        OAuth2Request reflashTokenRequest=
                OAuth2Request.post(config.getUrlAccessToken())
                        .header(OAuth2Constants.HEADER_ACCEPT,OAuth2Constants.JSON_CONTENT)
                        .parameter(OAuth2Constants.CLIENT_ID,config.getClientId())
                        .parameter(OAuth2Constants.CLIENT_SECRET,config.getClientSecret())
                        .parameter(OAuth2Constants.GRANT_TYPE,OAuth2Constants.GRANT_REFRESH_TOKEN)
                        .beforeRequest(r->r.header(OAuth2Constants.REFRESH_TOKEN,token.getRefreshToken()))
                        .afterResponse(tokenResponseHandler);
        reflashTokenRequest.setName(OAuth2Constants.REFLASH_TOKEN_REQUEST);
        oAuth2RequestMap.put(OAuth2Constants.REFLASH_TOKEN_REQUEST,reflashTokenRequest);
    }

    private JsonDeserializer<Token> tokenDeserializer=new JsonDeserializer<Token>(){
        @Override
        public Token deserialize (JsonParser jp, DeserializationContext deserializationContext)throws
        IOException, JsonProcessingException {
            JsonNode node = jp.getCodec().readTree(jp);
            long expiresIn = (Long) ((IntNode) node.get("expires_in")).longValue();
            String tokenType = node.get("token_type").asText();
            String accessToken = node.get("access_token").asText();
            String refreshToken = node.get("refresh_token").asText();
            String scope = node.get("scope").asText();
            return new Token(expiresIn, tokenType, accessToken, refreshToken, scope);
        }
    };

    private ResponseHandler tokenResponseHandler=(req,rsp)->{
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new SimpleModule().addDeserializer(Token.class,tokenDeserializer));
        token= mapper.readValue(rsp.getEntity().getContent(), Token.class);
    };

    public OAuth2Client httpClientSupplier(Supplier<CloseableHttpClient> httpClientSupplier){
        this.httpClientSupplier=httpClientSupplier;
        return this;
    }

    public OAuth2Client requests(OAuth2Request... oAuth2Requests){
        Arrays.asList(oAuth2Requests).forEach(r ->oAuth2RequestMap.put(r.getName(),r));
        return this;
    }

    public OAuth2Client addRequest(String name,OAuth2Request oAuth2Request){
        oAuth2Request.setName(name);
        oAuth2RequestMap.put(name, oAuth2Request);
        return this;
    }

    public void doCall(String name) throws IOException{
        OAuth2Request request=oAuth2RequestMap.get(name);
        if(request==null){
            return;
        }
        request.execute(this);
    }

    public CloseableHttpClient getHttpClient(){
        return httpClient.orElseGet(httpClientSupplier);
    }
    public OAuth2Config getConfig() {
        return config;
    }

    public synchronized Token getToken() throws IOException{
        if(token==null){
            doCall(OAuth2Constants.TOKEN_REQUEST);
        }else if(token.isExpired()){
            doCall(OAuth2Constants.REFLASH_TOKEN_REQUEST);
        }
        return token;
    }

}
