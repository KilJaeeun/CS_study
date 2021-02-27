import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.kohsuke.github.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CheckIssueReplyUser {
    static final String my_personal_token = "TOKEN PRIVATE";
    static final String targetUrl = "https://api.github.com/repos/KilJaeeun/CS_study/contents/README.md";
    static final String repoName = "KilJaeeun/CS_study";
    static final int start = 15;
    static final int end = 22;

    public static void main(String[] args) throws IOException {
        //사용자 이름 및 암호를 통해 연결
        GitHub github = new GitHubBuilder().withOAuthToken(my_personal_token).build();
        // 깃헙 레포 연결
        GHRepository ghRepository = github.getRepository(repoName);

        // 참여자 저장소 생성
        Map<String, int[]> participantHashMap = new HashMap();
        // 참여여부 체크

        ArrayList<Date> dateList = new ArrayList<>();
        for (int issueNum = start; issueNum < end; issueNum++) {

            GHIssue ghIssue = ghRepository.getIssue(issueNum);
            dateList.add(ghIssue.getClosedAt());
            PagedIterable<GHIssueComment> ghIssueComments = ghIssue.listComments();
            for (GHIssueComment ghIssueComment : ghIssueComments) {
                if (ghIssueComment.getCreatedAt().compareTo(ghIssue.getClosedAt()) > 0) {
                    continue;
                }

                String participant = ghIssueComment.getUser().getLogin();
                if (participantHashMap.containsKey(participant)) {
                    int[] assignments = participantHashMap.get(participant);
                    assignments[issueNum - start] = 1;
                } else {
                    int[] assignments = new int[end - start];// 초기값 0
                    assignments[issueNum - start] = 1;
                    participantHashMap.put(participant, assignments);


                }
            }

        }
        String readmeString = "";
        // 참여자 전체 정보 출력
        readmeString += "# CS_study\n" +
                " 30000원의  예치금을 미리 선납해주셔야합니다.  \n" +
                " 예치금은 스터디  중도탈퇴시,  벌금 제외한 나머지를 돌려드리는 형태로 진행되고,  \n" +
                " 하루 안제출 시, 5천원입니다. 벌금으로 다 까이시면 다시 충전을 하는 형태로 진행됩니다. \n" +
                " \n" +
                "## cs 스케줄링:\n" +
                "https://docs.google.com/spreadsheets/d/1kqbR59STucS3d9-A71N9q4aUeUeeR28bimKIsYOb4nM/edit?usp=sharing\n";
        readmeString += "## 정시 제출 현황 \n";
        readmeString += "|참여자명 |";
        DateFormat format1 = DateFormat.getDateInstance(DateFormat.SHORT);
        for (Date dateString : dateList) {
            readmeString += format1.format(dateString) + "|";
        }
        readmeString += "벌금|";
        readmeString += "\n";
        readmeString += "|";
        for (int i = start; i < end + 2; i++) {
            readmeString += "---|";
        }
        readmeString += "\n";
        for (Map.Entry<String, int[]> entry : participantHashMap.entrySet()) {
            String key = entry.getKey();
            int[] value = entry.getValue();
            readmeString += key + "";
            int charge = 0;
            for (int d : value) {
                readmeString += "|";
                if (d == 1) {
                    readmeString += "✅";
                } else {
                    readmeString += "  ";
                    charge += 5000;
                }
            }
            readmeString += "|" + charge;
            readmeString += "|\n";
        }
        if (!sendGet().isEmpty()) {
            String sha = sendGet();
            GHContent readme = ghRepository.getReadme();
            GHContentBuilder ghContentBuilder = ghRepository.createContent();
            ghContentBuilder.sha(sha);
            ghContentBuilder.message("readme update");
            ghContentBuilder.path("README.md");
            ghContentBuilder.content(readmeString);
            ghContentBuilder.commit();
            System.out.println("success");

        } else {
            throw new RuntimeException();
        }

    }

    // HTTP GET request
    private static String sendGet() {
        try {
            String answer = "";
            URL url = new URL(targetUrl);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            String USER_AGENT = "Mozilla/5.0";

            con.setRequestProperty("User-Agent", USER_AGENT);
            con.setRequestProperty("Authorization", "token" + my_personal_token);
            int responseCode = con.getResponseCode();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            // scriptengin을 이용하여 Json 파싱하기
            ScriptEngineManager sem = new ScriptEngineManager();
            ScriptEngine engine = sem.getEngineByName("javascript");
            JSONParser jsonParser1 = new JSONParser();
            JSONObject jsonObject1;
            jsonObject1 = (JSONObject) jsonParser1.parse(response.toString());
            return (String) jsonObject1.get("sha");
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


}
