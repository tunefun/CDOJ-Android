package cn.edu.uestc.acm.cdoj_android.net.data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by lenovo on 2016/8/7.
 */
public class Contest {
    private int id;
    public int contestId;
    public boolean result = false;
    public String contentString = "";
    public ArrayList<Problem> problemList = new ArrayList<>(15);
    public Contest(String json){
        if (json == null) {
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(json);
            result = jsonObject.optString("result", "fail").equals("success");
            if (!result){
                return;
            }
            JSONObject contestObject = jsonObject.getJSONObject("contest");
            contestId = contestObject.getInt("contestId");
            contentString = contestObject.toString();
            JSONArray plist = jsonObject.getJSONArray("problemList");
            for (int i = 0; i < plist.length(); i++) {
                problemList.add(Problem.newInstance(plist.getJSONObject(i)));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public ArrayList<Problem> getProblemList(){
        return problemList;
    }
    public String getContentString(){
        return contentString;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
