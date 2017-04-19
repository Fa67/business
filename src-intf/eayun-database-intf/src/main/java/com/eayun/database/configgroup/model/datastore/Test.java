package com.eayun.database.configgroup.model.datastore;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.List;

/**
 * Created by Administrator on 2017/2/24.
 */
public class Test {

    public static void main(String[] args) {
        String json = "{\"datastores\":[{\"id\":\"18382798-5bf1-4115-82a7-b9d0cf299556\",\"versions\":[{\"id\":\"67e93968-2d67-4ed4-81e4-4688d539d3c2\",\"packages\":\"\",\"name\":\"5.5\",\"active\":1,\"image\":\"79e771fa-2cfc-477d-8489-f44412d395dd\",\"links\":[{\"rel\":\"self\",\"href\":\"https://25.0.0.179:8779/v1.0/0bdd890d46c94ced8f35bd2af125b7a6/datastores/versions/67e93968-2d67-4ed4-81e4-4688d539d3c2\"},{\"rel\":\"bookmark\",\"href\":\"https://25.0.0.179:8779/datastores/versions/67e93968-2d67-4ed4-81e4-4688d539d3c2\"}]},{\"id\":\"ac3b88b1-d349-4b23-9b50-6edb3b55dcbc\",\"packages\":\"\",\"name\":\"5.5-zc\",\"active\":0,\"image\":\"\",\"links\":[{\"rel\":\"self\",\"href\":\"https://25.0.0.179:8779/v1.0/0bdd890d46c94ced8f35bd2af125b7a6/datastores/versions/ac3b88b1-d349-4b23-9b50-6edb3b55dcbc\"},{\"rel\":\"bookmark\",\"href\":\"https://25.0.0.179:8779/datastores/versions/ac3b88b1-d349-4b23-9b50-6edb3b55dcbc\"}]},{\"id\":\"e5ce5c14-5682-4598-84e1-118fe38a95b3\",\"packages\":\"\",\"name\":\"5.5-ly\",\"active\":0,\"image\":\"\",\"links\":[{\"rel\":\"self\",\"href\":\"https://25.0.0.179:8779/v1.0/0bdd890d46c94ced8f35bd2af125b7a6/datastores/versions/e5ce5c14-5682-4598-84e1-118fe38a95b3\"},{\"rel\":\"bookmark\",\"href\":\"https://25.0.0.179:8779/datastores/versions/e5ce5c14-5682-4598-84e1-118fe38a95b3\"}]}],\"name\":\"mysql\",\"links\":[{\"rel\":\"self\",\"href\":\"https://25.0.0.179:8779/v1.0/0bdd890d46c94ced8f35bd2af125b7a6/datastores/18382798-5bf1-4115-82a7-b9d0cf299556\"},{\"rel\":\"bookmark\",\"href\":\"https://25.0.0.179:8779/datastores/18382798-5bf1-4115-82a7-b9d0cf299556\"}]}]}" ;
        Datastores datastores = JSONObject.parseObject(json, Datastores.class) ;
        System.out.println(datastores);
    }

}
