package com.filtering.file.upload.service;

import com.filtering.file.upload.exception.FileEmptyException;
import com.filtering.file.upload.redis.RedisStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Description : Redis 연동 불가시 작업 빈으로 사용 활성화
 */
@Component
public class BadWordService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisStorageService redisStorageService;

    private HashMap<String, String> badWordList_two;
    private HashMap<String, String> badWordList_three;
    private HashMap<String, String> badWordList_four;
    private HashMap<String, String> badWordList_more;
    private List<String> whiteList = null;
    private boolean inited = false;
    private int MAX_VALUE = 10;

    @PostConstruct
    public void init() {
        logger.info("==================== white list 생성 중 =======================");
        this.whiteList = this.redisStorageService.getWhiteList();
        if(this.whiteList.isEmpty())
            this.whiteList = this.makeWhiteListWithoutRedis();
        logger.info("==================== white list 생성 완료 =====================");
    }

    public void initMap() {
        this.badWordList_two = new HashMap<>();
        this.badWordList_three = new HashMap<>();
        this.badWordList_four = new HashMap<>();
        this.badWordList_more = new HashMap<>();
    }

    public void deleteMap() {
        this.badWordList_two = null;
        this.badWordList_four = null;
        this.badWordList_three = null;
        this.badWordList_more = null;
    }

    /**
     * if redis set(white_list) has no data:
     * 파일로부터 읽어들여 처리
     */
    public List<String> makeWhiteListWithoutRedis() {
        StringTokenizer st = Optional.ofNullable(this.getTokenizedStringFromFile("whiteList"))
                                        .orElseThrow(FileEmptyException::new);
        List<String> whiteList = new ArrayList<>();
        while(st.hasMoreTokens())
            whiteList.add(st.nextToken());
        return whiteList;
    }

    /**
     * file to redis 저장.
     */
    public void makeBWordListToRedis() {
        StringTokenizer st = Optional.ofNullable(this.getTokenizedStringFromFile("bwordList"))
                                        .orElseThrow(FileEmptyException::new);

        logger.info("==================== word list 생성 중 =======================");
        while(st.hasMoreTokens()) {
            String value = st.nextToken();
            this.redisStorageService.putBWord(value);
        }
        logger.info("==================== word list 생성 완료 ====================");
    }

    /**
     * file로부터 금칙어 읽어들여 ","로 토크나이징
     * @return StringTokenizer
     */
    public StringTokenizer getTokenizedStringFromFile(String which_file) {
        String words = this.readFromFileBWord(which_file).toString();
        return words.length() != 0 ? new StringTokenizer(words, ",") : null;
    }

    /**
     * 금칙어 글자 수에 따라 다른 HashMap에 저장
     * @param bword
     */
    public void validateWords(String bword) {
        String key = this.toBinary(bword);
        if(bword.length() == 2 )  this.badWordList_two.put(key, bword);
        else if(bword.length() == 3) this.badWordList_three.put(key, bword);
        else if(bword.length() == 4) this.badWordList_four.put(key, bword);
        else {
            this.badWordList_more.put(key, bword);
            this.MAX_VALUE = Math.max(this.MAX_VALUE, bword.length());
        }
    }

    /**
     * 파일로부터 입력길이에 따른 HashMap 매핑. redis 사용시 불필요
     * @param bword
     * @return
     */
    public boolean search(String bword) {
        if(bword.length() == 2 && this.getBadWordList_two().containsKey(toBinary(bword)))
            return true;
        else if(bword.length() == 3 && this.getBadWordList_three().containsKey(toBinary(bword)))
            return true;
        else if(bword.length() == 4 && this.getBadWordList_four().containsKey(toBinary(bword)))
            return true;
        else
            return this.getBadWordList_more().containsKey(toBinary(bword));
    }

    /**
     * file로 부터 BWord 읽어들임
     * @return sb
     */
    public StringBuilder readFromFileBWord(String which_file) {
        StringBuilder sb = new StringBuilder();
        try{
            String data = "";
            if(which_file.equals("bwordList"))
                data = "data/*";
            else if(which_file.equals("whiteList"))
                data = "whiteList/*";
            else throw new FileNotFoundException();

            PathMatchingResourcePatternResolver scanner = new PathMatchingResourcePatternResolver();
            Resource[] resources = scanner.getResources(data);

            if(resources == null || resources.length == 0)
                logger.info("cannot find any files");
            else {
                for (Resource resource : resources) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                    String line = null;
                    while ((line = bufferedReader.readLine()) != null)
                        sb.append(line + ",");
                    bufferedReader.close();
                }
            }
        }catch(Throwable e) {
            logger.info("error Message : " + e.getMessage());
        }
        return sb;
    }

    /**
     * String to binary String
     * @param bword
     * @return
     */
    public String toBinary(String bword) { //String to binary
        byte [] bytes = bword.getBytes();
        StringBuilder binary = new StringBuilder();

        for(byte b : bytes) {
            int val = b;
            for(int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
            binary.append(' ');
        }
        return binary.toString();
    }

    public int getMAX_VALUE() { return MAX_VALUE; }

    public boolean isInited() { return this.inited; }

    public void setInited(boolean inited) { this.inited = inited; }

    public HashMap<String, String> getBadWordList_two() { return badWordList_two; }

    public HashMap<String, String> getBadWordList_three() { return badWordList_three; }

    public HashMap<String, String> getBadWordList_four() { return badWordList_four; }

    public HashMap<String, String> getBadWordList_more() { return badWordList_more; }

    public List<String> getWhiteList() { return whiteList; }

    public void setWhiteList(List<String> whiteList) { this.whiteList = whiteList; }
}