package cn.leancloud.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpAppender<E> extends AbstractHttpAppender<E> {
  public HttpAppender() {

  }

  @Override
  protected void append(E eventObject) {
    String msg = this.layout.doLayout(eventObject);
    postToServer(msg);
  }

  private void postToServer(final String event) {
    try {
      assert endpointUrl != null;
      URL endpoint = new URL(endpointUrl);
      final HttpURLConnection connection;
      if (proxy == null) {
        connection = (HttpURLConnection) endpoint.openConnection();
      } else {
        connection = (HttpURLConnection) endpoint.openConnection(proxy);
      }
      connection.setRequestMethod("POST");
      connection.setDoOutput(true);
      connection.addRequestProperty("Content-Type", this.layout.getContentType());
      connection.connect();
      sendAndClose(event, connection.getOutputStream());
      connection.disconnect();
      final int responseCode = connection.getResponseCode();
      if (responseCode != 200) {
        final String message = readResponseBody(connection.getInputStream());
        addError("Loggly post failed (HTTP " + responseCode + ").  Response body:\n" + message);
      }
    } catch (final IOException e) {
      addError("IOException while attempting to communicate with Loggly", e);
    }
  }

  private void sendAndClose(final String event, final OutputStream output) throws IOException {
    try {
      output.write(event.getBytes("UTF-8"));
    } finally {
      output.close();
    }
  }
}
