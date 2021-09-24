package example.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import example.dynamodb.MongoDBImpl;
import org.json.simple.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class AnnouncementGetHandler implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("### Got request in AnnouncementCreateHandler");
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = MongoDBImpl.getInstance().get(reader, context);
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer.write(responseJson.toString());
        writer.close();
    }
}
