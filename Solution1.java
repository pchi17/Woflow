package woflow.Woflow;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class Solution1 {
    private static final String path = "https://nodes-on-nodes-challenge.herokuapp.com/nodes/";
    private final Map<String, Set<String>> graph = new HashMap<>();
    private final Queue<String> queue = new LinkedList<>();

    private String getNodePath(String nodeID) {
        return path + nodeID;
    }

    private JSONArray getResponse(String nodeID) throws IOException {
        URL url = new URL(getNodePath(nodeID));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        int responseCode = con.getResponseCode();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return new JSONArray(response.toString());
    }

    private JSONArray getChildren(String nodeId) throws IOException {
        JSONObject obj = getResponse(nodeId).getJSONObject(0);
        return obj.getJSONArray("child_node_ids");
    }

    private void doBFS(String headNodeID) throws IOException {
        queue.add(headNodeID);
        while (!queue.isEmpty()) {
            String id = queue.poll();
            if (graph.containsKey(id)) continue;
            graph.put(id, new HashSet<>());
            JSONArray children = getChildren(id);
            for (int i = 0; i < children.length(); i++) {
                String child = (String) children.get(i);
                graph.get(id).add(child);
                queue.add(child);
            }
        }

        Map<String, Integer> counts = new HashMap<>();
        int maxf = 0;
        String mostCommonNode = null;
        for (Set<String> children : graph.values()) {
            for (String child : children) {
                int count = counts.getOrDefault(child, 0) + 1;
                counts.put(child, count);
                if (count > maxf) {
                    maxf = count;
                    mostCommonNode = child;
                }
            }
        }

        System.out.println("total unique nodes = " + graph.size());
        System.out.println("The most common node is " + mostCommonNode);
        System.out.println("The frequency of it is " + maxf);
        System.out.println("I assume most common means the node with most incoming edges.");
    }

    public static void main(String[] args) throws IOException {
        String head = "089ef556-dfff-4ff2-9733-654645be56fe";
        Solution1 solution = new Solution1();
        solution.doBFS(head);
        
        // total unique nodes = 30
        // The most common node is a06c90bf-e635-4812-992e-f7b1e2408a3f
        // The frequency of it is 3
        // I assume most common means the node with most incoming edges.
    }
}
