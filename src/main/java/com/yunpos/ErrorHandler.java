package com.yunpos;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

/**
 * 功能描述：
 * <p>
 * 版权所有：小牛信息科技有限公司
 * <p>
 * 未经本公司许可，不得以任何方式复制或使用本程序任何部分
 *
 * @author bino 新增日期：2015/9/10
 * @author bino 修改日期：2015/9/10
 */
public interface ErrorHandler{
    void handler(HttpUriRequest request, CloseableHttpResponse response, Exception e);
    Class exceptionClass();
}
