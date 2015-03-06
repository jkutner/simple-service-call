import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Main extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    String urlStr = System.getenv("SFDC_URL");

    CloseableHttpClient httpClient = HttpClients.createDefault();
    HttpGet request = new HttpGet(urlStr);
    request.setHeader("Authorization", "Bearer " + System.getenv("SFDC_AUTH_CODE"));

    long startTime = System.currentTimeMillis();
    CloseableHttpResponse response = httpClient.execute(request);
    try {
      resp.getWriter().print("Response " + handleResponse(response));
    } catch (Exception e) {
      resp.getWriter().print(e.getMessage());
      e.printStackTrace();
    }
    long endTime = System.currentTimeMillis();

    System.out.println("TIME: " + new Long(endTime-startTime));
  }

  private static String handleResponse(CloseableHttpResponse response) throws IOException {
    StatusLine statusLine = response.getStatusLine();
    HttpEntity entity = response.getEntity();
    if (statusLine.getStatusCode() >= 300) {
      throw new HttpResponseException(
          statusLine.getStatusCode(),
          statusLine.getReasonPhrase());
    }
    if (entity == null) {
      throw new ClientProtocolException("Response contains no content");
    }
    return readStream(entity.getContent());
  }

  private static String readStream(InputStream is) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String output = "";
    String tmp = reader.readLine();
    while (tmp != null) {
      output += tmp;
      tmp = reader.readLine();
    }
    return output;
  }
}
