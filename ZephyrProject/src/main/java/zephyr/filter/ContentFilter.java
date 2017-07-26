package zephyr.filter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;

public class ContentFilter {
    private static Logger logger = Logger.getLogger(ContentFilter.class);
    private ArrayList<String> filterStrArray;
    private ArrayList<String> regexStrArray;
    private HashSet<String> fromUserArray;
    private ArrayList<String> fromDomainArray;
    private ArrayList<String> subjectArray;
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    // filter array를 초기화 시킴 총 4가지 이며, 본문에 적용할 필터 2개(cntentFilter, regexFilter),
    // 발신자(fromUser), 제목(subject)으로 구성
    public ContentFilter() {
        loadFilterFiles(path);
    }

    public ContentFilter(String path) {
        loadFilterFiles(path);
    }

    public void loadFilterFiles(String path) {
        try {
            filterStrArray = this.loadFilterTxtFile(path + "/contentFilter.txt");
        } catch (IOException e) {
            filterStrArray = new ArrayList<>();
            logger.warn(path + " file not found or can't read");
            e.printStackTrace();
        }
        try {
            regexStrArray = this.loadFilterTxtFile(path + "/regexFilter.txt");
        } catch (IOException e) {
            regexStrArray = new ArrayList<>();
            logger.warn(path + " file not found or can't read");
            e.printStackTrace();
        }
        try {
            fromUserArray = this.loadFilterSetTxtFile(path + "/fromUserFilter.txt");
        } catch (IOException e) {
            fromUserArray = new HashSet<>();
            logger.warn(path + " file not found or can't read");
            e.printStackTrace();
        }
        try {
            subjectArray = this.loadFilterTxtFile(path + "/subjectFilter.txt");
        } catch (IOException e) {
            subjectArray = new ArrayList<>();
            logger.warn(path + " file not found or can't read");
            e.printStackTrace();
        }
        try {
            fromDomainArray = this.loadFilterTxtFile(path + "/fromDomainFilter.txt");
        } catch (IOException e) {
            fromDomainArray = new ArrayList<>();
            logger.warn(path + " file not found or can't read");
            e.printStackTrace();
        }
    }

    // filter로 적용할 파일을 읽어옴. classpath에 있어야 함. 테스트용
    public HashSet<String> loadFilterSetTxtFile(String filterFile) throws IOException {
        HashSet<String> filterArray = new HashSet<>();
        loadFilterFile(filterArray, filterFile, true);
        return filterArray;
    }

    // filter로 적용할 파일을 읽어옴. classpath에 있어야 함.
    public ArrayList<String> loadFilterTxtFile(String filterFile) throws IOException {
        ArrayList<String> filterArray = new ArrayList<>();
        loadFilterFile(filterArray, filterFile, false);
        return filterArray;
    }

    // filter로 적용할 파일을 읽어옴. classpath에 있어야 함.
    public void loadFilterFile(Collection<String> filterArray, String filterFile, boolean isLowerCase)
            throws IOException {
        //InputStreamReader isr = new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filterFile),"utf-8");
        InputStreamReader isr = new InputStreamReader(new FileInputStream(filterFile), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String filterStr = null;
        while ((filterStr = br.readLine()) != null) {
            filterStr = filterStr.replace("\\r\\n", "\r\n");
            if (isLowerCase) {
                filterStr = filterStr.toLowerCase();
            }
            filterArray.add(filterStr);
        }
        br.close();
        isr.close();
    }

    // 본문에서 필터에 적용할 문구를 제외한 문자열을 리턴. 정규식을 적용한 문구와 완전히 동일한 문구를 나눠서 제외시킨다.
    public String filteredString(String oriStr) {
        if (oriStr != null && oriStr.length() > 0) {
            for (String regexStr : regexStrArray) {
                oriStr = oriStr.replaceAll(regexStr, "");
            }
            for (String filterStr : filterStrArray) {
                oriStr = oriStr.replace(filterStr, "");
            }
        }
        return oriStr;
    }

    // 발신자 필터에 적용되는 메일인지 확인, true일 경우 필터에 걸린 발신자
    public boolean filterFromUser(String fromuser) throws UnsupportedEncodingException {
        boolean filterResult = false;
        if (fromuser != null) {
            fromuser = fromuser.toLowerCase();
            filterResult = fromUserArray.contains(fromuser);
            if (!filterResult) {
                for (String fromDomain : fromDomainArray) {
                    if (fromuser.endsWith(fromDomain)) {
                        filterResult = true;
                        break;
                    }
                }
            }
        }
        return filterResult;
    }

    // 제목 필터에 걸린 메일인지 확인, true일 경우 필터에 걸린 제목
    public boolean filterSubject(String subject) throws UnsupportedEncodingException {
        boolean filterResult = false;
        if (subject != null) {
            for (String filter : subjectArray) {
                if (subject.indexOf(filter) != -1) {
                    filterResult = true;
                    break;
                }
            }
        }
        return filterResult;
    }

}
