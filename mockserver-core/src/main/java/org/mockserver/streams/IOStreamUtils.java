package org.mockserver.streams;

import org.apache.commons.io.IOUtils;
import org.mockserver.log.model.LogEntry;
import org.mockserver.logging.MockServerLogger;
import org.slf4j.event.Level;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockserver.character.Character.NEW_LINE;

/**
 * @author jamesdbloom
 */
public class IOStreamUtils {
    private static final MockServerLogger MOCK_SERVER_LOGGER = new MockServerLogger(IOStreamUtils.class);

    public static String readInputStreamToString(Socket socket) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        StringBuilder result = new StringBuilder();
        String line;
        Integer contentLength = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("content-length") || line.startsWith("Content-Length")) {
                contentLength = Integer.parseInt(line.split(":")[1].trim());
            }
            if (line.length() == 0) {

                if (contentLength != null) {
                    result.append(NEW_LINE);
                    for (int position = 0; position < contentLength; position++) {
                        result.append((char) bufferedReader.read());
                    }
                }
                break;
            }
            result.append(line).append(NEW_LINE);
        }
        return result.toString();
    }

    public static String readInputStreamToString(ServletRequest request) {
        try {
            return IOUtils.toString(request.getInputStream(), UTF_8.name());
        } catch (IOException ioe) {
            MOCK_SERVER_LOGGER.logEvent(
                new LogEntry()
                    .setType(LogEntry.LogMessageType.EXCEPTION)
                    .setLogLevel(Level.ERROR)
                    .setMessageFormat("IOException while reading HttpServletRequest input stream")
                    .setThrowable(ioe)
            );
            throw new RuntimeException("IOException while reading HttpServletRequest input stream", ioe);
        }
    }

    public static byte[] readInputStreamToByteArray(ServletRequest request) {
        try {
            return IOUtils.toByteArray(request.getInputStream());
        } catch (IOException ioe) {
            MOCK_SERVER_LOGGER.logEvent(
                new LogEntry()
                    .setType(LogEntry.LogMessageType.EXCEPTION)
                    .setLogLevel(Level.ERROR)
                    .setMessageFormat("IOException while reading HttpServletRequest input stream")
                    .setThrowable(ioe)
            );
            throw new RuntimeException("IOException while reading HttpServletRequest input stream", ioe);
        }
    }

    public static void writeToOutputStream(byte[] data, ServletResponse response) {
        try {
            OutputStream output = response.getOutputStream();
            output.write(data);
            output.close();
        } catch (IOException ioe) {
            MOCK_SERVER_LOGGER.logEvent(
                new LogEntry()
                    .setType(LogEntry.LogMessageType.EXCEPTION)
                    .setLogLevel(Level.ERROR)
                    .setMessageFormat(String.format("IOException while writing [%s] to HttpServletResponse output stream", new String(data)))
                    .setThrowable(ioe)
            );
            throw new RuntimeException(String.format("IOException while writing [%s] to HttpServletResponse output stream", new String(data)), ioe);
        }
    }

    public static ByteBuffer createBasicByteBuffer(String input) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(input.length()).put(input.getBytes(UTF_8));
        byteBuffer.flip();
        return byteBuffer;
    }
}
