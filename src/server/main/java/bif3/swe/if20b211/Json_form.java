package bif3.swe.if20b211;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Json_form {

    private static ObjectMapper objectMapper = getDefaultObjectMapper();

    private static ObjectMapper getDefaultObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper;
    }
    public static JsonNode parse(String s) throws IOException {
        return objectMapper.readTree(s);
    }
    public static JsonNode parse(File f) throws IOException{
        return objectMapper.readTree(f);
    }

    /**
     * Necesarry to get all Values or to check if the body contains also a number or not
     * @param node
     * @return
     */
    public static String[] getFieldNames(JsonNode node){
        List<String> res = new ArrayList<String>();
        node.fieldNames().forEachRemaining(name -> res.add(name));
        return res.toArray(new String[res.size()]);
    }

    /**
     * In order to filter for a specific messanger
     * @param node
     * @param attribute
     * @param value
     * @return
     */
    public static HashMap<String,JsonNode> getParentsByAttributeEquals(JsonNode node, String attribute, String value){
        HashMap<String,JsonNode> res = new HashMap<String, JsonNode>();
        node.fields().forEachRemaining(field -> {
            if(field.getValue().get(attribute).asText().equals(value)){
                res.put(field.getKey(), field.getValue());
            }
        });
        return res;
    }


    public static String getValueByTitle(JsonNode node, String title){
        return node.get(title.toString()).asText();
    }

    public static <A> A fromJson(JsonNode node, Class<A> aClass) throws JsonProcessingException {
        return objectMapper.treeToValue(node,aClass);
    }

    public static JsonNode toJson(Object a) { return  objectMapper.valueToTree(a); }

    public static String stringify(JsonNode node) throws JsonProcessingException { return objectMapper.writer().writeValueAsString(node);}

    public static String toString(JsonNode node) throws JsonProcessingException { return objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(node);}

    public static int write(String path, JsonNode node){
        try {
            objectMapper.writer().writeValue(new File(path),node);
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }
}
