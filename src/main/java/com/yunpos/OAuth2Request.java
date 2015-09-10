package com.yunpos;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.methods.*;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.*;

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
public class OAuth2Request {

    private String name;
    private String method;
    private String uri;

    private EntityBuilder entityBuilder;

    private List<NameValuePair> headers;
    private List<NameValuePair> nameValuePairs;

    private HttpUriRequest request;
    private CloseableHttpResponse response;

    private List<RequestHandler> requestHandlers;
    private List<ResponseHandler> responseHandlers;
    private List<ErrorHandler> errorHandlers;

    private OAuth2Request(String method,String url){
        this.method=method;
        this.uri =url;

        headers=new ArrayList();
        nameValuePairs=new ArrayList();
        requestHandlers=new ArrayList<>();
        responseHandlers=new ArrayList<>();
        errorHandlers=new ArrayList();
    }

    public void execute(OAuth2Client oAuth2Client){
        try {
            requestHandlers.forEach(h -> h.handler(this));
            buildRequest();
            if(!getName().equals(OAuth2Constants.TOKEN_REQUEST) && !getName().equals(OAuth2Constants.REFLASH_TOKEN_REQUEST)){
                request.setHeader(OAuth2Constants.AUTHORIZATION, OAuth2Constants.BEARER+" "+ oAuth2Client.getToken().getToken());
            }
            response=oAuth2Client.getHttpClient().execute(request);
            responseHandlers.forEach(h -> {
                try {
                    h.handler(request, response);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorHandlers.stream().filter(eh->eh.exceptionClass().equals(e.getClass()))
                            .forEach(eh->eh.handler(request,response,e));
                }
            });
            reset();
        }catch (Exception e){
            e.printStackTrace();
            errorHandlers.stream().filter(h->h.exceptionClass().equals(e.getClass()))
                   .forEach(h->h.handler(request,response,e));
        }
    }
    private void buildRequest(){
        RequestBuilder requestBuilder=RequestBuilder.create(method);
        requestBuilder.setUri(uri);
        headers.forEach(h->requestBuilder.addParameter(h));
        EntityBuilder entityBuilder=EntityBuilder.create();
        if(nameValuePairs.isEmpty()==false){
            entityBuilder.setParameters(nameValuePairs);
            requestBuilder.setEntity(entityBuilder.build());
        }
        request=requestBuilder.build();
    }

    private void reset(){
        request=null;
    }

    public OAuth2Request header(String name,String value){
        headers.add(new BasicNameValuePair(name,value));
        return this;
    }

    public OAuth2Request parameter(String name,String value){
        nameValuePairs.add(new BasicNameValuePair(name, value));
        return this;
    }

    public OAuth2Request beforeRequest(RequestHandler... handlers){
        requestHandlers.addAll(Arrays.asList(handlers));
        return this;
    }
    public OAuth2Request afterResponse(ResponseHandler... handlers){
        responseHandlers.addAll(Arrays.asList(handlers));
        return this;
    }
    public OAuth2Request afterError(ErrorHandler... handlers){
        errorHandlers.addAll(Arrays.asList(handlers));
        return this;
    }

    public static OAuth2Request post(String uri){
        return new OAuth2Request(HttpPost.METHOD_NAME,uri);
    }
    public static OAuth2Request put(String uri){
        return new OAuth2Request(HttpPut.METHOD_NAME,uri);
    }
    public static OAuth2Request get(String uri){
        return new OAuth2Request(HttpGet.METHOD_NAME,uri);
    }
    public static OAuth2Request delete(String name,String uri){
        return new OAuth2Request(HttpDelete.METHOD_NAME,uri);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRequestHandlers(List<RequestHandler> requestHandlers) {
        this.requestHandlers = requestHandlers;
    }

    public void setResponseHandlers(List<ResponseHandler> responseHandlers) {
        this.responseHandlers = responseHandlers;
    }

    public void setErrorHandlers(List<ErrorHandler> errorHandlers) {
        this.errorHandlers = errorHandlers;
    }
}

