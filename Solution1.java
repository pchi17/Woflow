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
    private static final String PATH = "https://nodes-on-nodes-challenge.herokuapp.com/nodes/";

    // making HTTP get call, possibly with multiply node ids.
    private JSONArray getResponse(String nodeIDs) throws IOException {
        URL url = new URL(PATH + nodeIDs);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        return new JSONArray(response.toString());
    }

    private void doBFS(String headNodeID) throws IOException {
        String mostCommonNode = null;
        int maxf = 0;
        Queue<String> queue = new LinkedList<>();
        Set<String> visitedNodes = new HashSet<>();
        Map<String, Integer> incomingEdgesCount = new HashMap<>();

        queue.add(headNodeID);
        visitedNodes.add(headNodeID);

        while (!queue.isEmpty()) {
            // doing a batch fetch with multiple node IDs. saves the number of HTTP calls.
            // it would also make sense to limit node ids to a certain number,
            // because the graph could be huge and, we might get a large response
            // that we do not have enough bandwidth or memory to handle...
            JSONArray response = getResponse(String.join(",", queue));

            // clears the queue, child nodes will be added to the queue for the next fetch.
            queue.clear();

            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObj = response.getJSONObject(i);
                JSONArray children = jsonObj.getJSONArray("child_node_ids");
                for (int j = 0; j < children.length(); j++) {
                    String child = children.getString(j);
                    int count = incomingEdgesCount.getOrDefault(child, 0) + 1;
                    incomingEdgesCount.put(child, count);
                    if (count > maxf) {
                        maxf = count;
                        mostCommonNode = child;
                    }
                    // if visitedNodes already contains child as a key,
                    // it means we've already performed a fetch with it. Do not add it to queue.
                    if (visitedNodes.contains(child)) continue;
                    queue.add(child);
                    visitedNodes.add(child);
                }
            }
        }

        System.out.println("total unique nodes = " + visitedNodes.size());
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
