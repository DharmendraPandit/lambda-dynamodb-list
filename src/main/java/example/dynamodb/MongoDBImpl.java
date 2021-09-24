package example.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.PutItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import example.Announcement;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class MongoDBImpl {
    private static final String DYNAMODB_TABLE_NAME = "ANNOUNCEMENTS";
    private static final int PAGE_LIMIT = 2;
    //private static final AmazonDynamoDB client = AmazonDynamoDBClientBuilder.defaultClient();

    private static final JSONParser parser = new JSONParser();
    private static MongoDBImpl mongoInstance = null;
    //private static final Logger logger = LoggerFactory.getLogger(MongoDBImpl.class);
    private DynamoDB dynamoDB = null;

    private MongoDBImpl() {
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().withRegion("ap-south-1").build();
        dynamoDB = new DynamoDB(client);
    }

    public static MongoDBImpl getInstance() {
        if(mongoInstance == null)
            mongoInstance = new MongoDBImpl();
        return mongoInstance;
    }

    public JSONObject create(BufferedReader reader, Context context) throws IOException {
        JSONObject responseJson = new JSONObject();
        LambdaLogger logger = context.getLogger();
        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("JSON Request for create: "+ event.toJSONString());
            if (event.get("body") != null) {
                Announcement announcement = new Announcement((String) event.get("body"));
                logger.log("JSON announcement: "+ announcement);

                dynamoDB.getTable(DYNAMODB_TABLE_NAME)
                        .putItem(new PutItemSpec().withItem(new Item()
                                .withString("id", announcement.getId())
                                .withString("title", announcement.getTitle())
                                .withString("description", announcement.getDescription())
                                .withString("date", announcement.getDate()))
                        );
            }
            JSONObject responseBody = new JSONObject();
            responseBody.put("message", "New Announcement created");
            JSONObject headerJson = new JSONObject();
            responseJson.put("statusCode", 200);
            responseJson.put("headers", headerJson);
            responseJson.put("body", responseBody.toString());
        } catch (ParseException ex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", ex.getMessage());
        }
        logger.log("JSON Response for create: "+ responseJson.toJSONString());
        return responseJson;
    }

    public JSONObject get(BufferedReader reader, Context context) throws IOException {
        JSONObject responseJson = new JSONObject();
        LambdaLogger logger = context.getLogger();
        Item result = null;
        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("JSON Request for get: "+ event.toJSONString());
            if (event.get("pathParameters") != null) {
                JSONObject pps = (JSONObject) event.get("pathParameters");
                if (pps.get("id") != null) {
                    String id = (String) pps.get("id");
                    result = dynamoDB.getTable(DYNAMODB_TABLE_NAME).getItem("id", id);
                }

                JSONObject responseBody = new JSONObject();
                if (result != null) {
                    Announcement announcement = new Announcement(result.toJSON());
                    responseBody.put("announcement", announcement);
                    responseJson.put("statusCode", 200);
                } else {
                    responseBody.put("message", "No item found");
                    responseJson.put("statusCode", 404);
                }

            }
        } catch (ParseException pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", pex);
        }
        logger.log("JSON Response for get: "+ responseJson.toJSONString());
        return responseJson;
    }

    public JSONObject getList(Context context, BufferedReader reader) throws IOException{
        JSONObject responseJson = new JSONObject();
        LambdaLogger logger = context.getLogger();
        int pageNo = 0;
        try {
            JSONObject event = (JSONObject) parser.parse(reader);
            logger.log("JSON Request for get: " + event.toJSONString());
            if (event.get("pathParameters") != null) {
                JSONObject pps = (JSONObject) event.get("pathParameters");
                if (pps.get("id") != null) {
                    String id = (String) pps.get("id");
                    QuerySpec spec = new QuerySpec().withKeyConditionExpression("id = :v_id").withValueMap(new ValueMap().withString(":v_id", id));
                    ItemCollection<QueryOutcome> dbResult = dynamoDB.getTable(DYNAMODB_TABLE_NAME).query(spec);
                    for (Page<Item, QueryOutcome> page : dbResult.pages()) {
                        responseJson.put("page", ++pageNo);
                        responseJson.put("items", page.iterator().next().toJSONPretty());
                    }
                }
            }

            if (pageNo == 0) {
                responseJson.put("page", 0);
                responseJson.put("items", "Empty list");
            }
        }catch (ParseException pex) {
            responseJson.put("statusCode", 400);
            responseJson.put("exception", pex);
        }
        logger.log("JSON Response for getList: "+ responseJson.toJSONString());
        return responseJson;
    }
}
