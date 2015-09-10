import com.yunpos.OAuth2Client;
import com.yunpos.OAuth2Constants;
import com.yunpos.OAuth2Request;
import com.yunpos.Token;
import org.apache.http.client.HttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

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
public class TestOAuth2Client {

    @Before
    public  void setUp(){

    }

    @Test
    public void testGetToken(){
        OAuth2Client client = OAuth2Client.withPasswordGrant(
                "admin", "admin",
                "client1", "secret1","openid",
                "http://localhost:8081/oauth/token");
        try {
            Token token=client.getToken();
            System.out.println(token);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetReflashToken(){
        OAuth2Client client = OAuth2Client.withPasswordGrant(
                "admin", "admin",
                "client1", "secret1", "openid",
                "http://localhost:8081/oauth/token");
        try {
            Token token=client.getToken();
            String token1=token.getToken();
            client.doCall(OAuth2Constants.REFLASH_TOKEN_REQUEST);
            String token2=token.getToken();
            assertNotNull(token1);
            assertNotNull(token2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testGetResource(){
        OAuth2Client client = OAuth2Client.withPasswordGrant(
                "admin", "admin",
                "client1", "secret1", "openid",
                "http://localhost:8081/oauth/token");
        client.addRequest("user", OAuth2Request.get("http://localhost:8082/resource")
            .afterResponse(((request, response) ->{
                String body = EntityUtils.toString(response.getEntity());
                System.out.println(body);
            }))
        );
        try {
            client.doCall("user");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
