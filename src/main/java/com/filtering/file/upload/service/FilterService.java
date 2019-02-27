package com.filtering.file.upload.service;

import com.filtering.file.upload.redis.RedisStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

/**
 * Description:
 * Redis로부터 금칙어 체크
 * redis -> set - 2글자/3글자/4글자/그 외로 나누어 관리.
 */
@Service
public class FilterService {

    @Autowired
    private RedisStorageService redisStorageService;

    @Autowired
    private BadWordService badWordService;

    public StringBuilder Tokenize(String ipt) {

        StringTokenizer st = new StringTokenizer(ipt," ");
        StringBuilder sb = new StringBuilder();

        while(st.hasMoreTokens()) {
            String token = st.nextToken();
            sb.append(token);
        }

        return sb;
    }

    /**
     * String to Binary-String
     * @param ipt
     * @return
     */
    public String toBinary(String ipt) { //String to binary

        byte [] bytes = ipt.getBytes();
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

    /**
     * char compare
     * @param ipt
     * @param ipt2
     * @return
     */
    public boolean isMatchCharValue(char ipt, char ipt2) {

        String strA = Integer.toBinaryString(ipt);
        String strB = Integer.toBinaryString(ipt2);
        return (Integer.valueOf(strA) & Integer.valueOf(strB)) == Integer.valueOf(strA);
    }

    /**
     * 금칙어 Y/N
     * @param ipt
     * @return
     */
    public boolean isBadWord(String ipt) {

        int len  = ipt.length();
        int MAX_VALUE = this.badWordService.getMAX_VALUE(); //욕 최대 길이

        for(int i = 0; i < len; i++) {
            int n = 1;
            StringBuilder sb_kr = new StringBuilder(); //한국어 sb
            StringBuilder sb_n_kr = new StringBuilder(); //한국어를 제외한 sb

            for(int k = i; k < i + n; k++) {
                //금칙어 최대 길이를 넘어가거나, indexOutOfBound

                int end = k + 1;

                if(n > MAX_VALUE || k > len - 1) break;
                String next_ipt_c = ipt.substring(k, k+1); //추가할 다음 글자.

                if(sb_n_kr.length() != 0 && isBadWordNumber(sb_n_kr.toString())) {
                    sb_kr.append(sb_n_kr.toString());
                }

                if(isKorean(next_ipt_c)) { // 그 글자가 한글이면,
                    sb_kr.append(next_ipt_c); //기존 String에 다음글자 붙이고,
                    String compare_str = sb_kr.toString();

                    if(this.redisStorageService.isExistsBWord(compare_str)) {
                        String whole_str = ipt.substring(0, end);

                        if(!isWhiteList(whole_str)) //if ipt is not in WhiteList
                            return true;
                    }

                    if(sb_n_kr.length() != 0) sb_n_kr.delete(0, sb_n_kr.length()); //한글이 아닌 sb 비우기.

                }else { //한글이 아니고, 숫자 or 문자, 영어면 sb
                    if(isEngilish(next_ipt_c)) next_ipt_c = next_ipt_c.toLowerCase(); //영어는 소문자로

                    String compare_str = sb_n_kr.append(next_ipt_c).toString();

                    if(this.redisStorageService.isExistsBWord(compare_str)) {
                        String whole_str = ipt.substring(0, end);

                        if(!isWhiteList(whole_str))
                            return true;
                    }
                }
                n++;
            }
        }
        return false;
    }


    /**
     * better filtering method for API Test
     * @param ipt
     * @return filtering 완료된 string 리턴
     */
    public String betterFilteringForTest(String ipt) {

        char[]ipt_charArray = ipt.toCharArray();
        int len  = ipt.length();
        int MAX_VALUE = this.badWordService.getMAX_VALUE(); //금칙어 최대 길이
        int end_badword = 0;

        for(int i = 0; i < len; i++) {
            int n = 1;
            StringBuilder sb_kr = new StringBuilder(); //한국어 sb
            StringBuilder sb_n_kr = new StringBuilder(); //한국어를 제외한 sb

            for(int k = i; k < i + n; k++) {
                //금칙어 최대 길이를 넘어가거나, indexOutOfBound
                int start = i;
                int end = k + 1;

                if(n > MAX_VALUE || k > len - 1) break;
                String next_ipt_c = ipt.substring(k, k+1); //추가할 다음 글자.

                if(sb_n_kr.length() != 0 && isBadWordNumber(sb_n_kr.toString())) {
                    sb_kr.append(sb_n_kr.toString());
                }

                if(isKorean(next_ipt_c)) { // 그 글자가 한글이면,
                    sb_kr.append(next_ipt_c); //기존 String에 다음글자 붙이고,
                    String compare_str = sb_kr.toString();

                    if(this.redisStorageService.isExistsBWord(compare_str)) {
                      //금칙어이면 시작점부터 현재 인덱스까지 전부 *처리
                      //시작점은 start = i , 현재 인덱스는 end = k
                        String whole_str = ipt.substring(end_badword, end); //그 전에 완전히 * 로 변환완료된 인덱스부터 현재 인덱스까지 한번에 가져옴.
                        if(!this.isWhiteList(whole_str)) { // 화이트 리스트에 존재하는 str이면.

                            IntStream.range(start, end)
                                    .forEach(inner_idx -> {
                                        if (ipt_charArray[inner_idx] != ' ')
                                            ipt_charArray[inner_idx] = '*';
                                    });
                            end_badword = end;
                        }
                    }

                    if(sb_n_kr.length() != 0) sb_n_kr.delete(0, sb_n_kr.length()); //한글이 아닌 sb 비우기.

                }else { //한글이 아니고, 숫자 or 문자, 영어면 sb

                    if(isEngilish(next_ipt_c)) next_ipt_c = next_ipt_c.toLowerCase(); //영어는 소문자로

                    String compare_str = sb_n_kr.append(next_ipt_c).toString();

                    if(this.redisStorageService.isExistsBWord(compare_str)) {
                        String whole_str = ipt.substring(end_badword, end); //그 전에 완전히 * 로 변환완료된 인덱스부터 현재 인덱스까지 한번에 가져옴.

                        if(!this.isWhiteList(whole_str)) { // 화이트 리스트에 존재하는 str이면.
                            IntStream.range(start, end)
                                    .forEach(inner_idx -> {
                                        if (ipt_charArray[inner_idx] != ' ')
                                            ipt_charArray[inner_idx] = '*';
                                    });
                            end_badword = end;
                        }
                    }
                }
                n++;
            }
        }
        return String.valueOf(ipt_charArray);
    }


    /**
     * 특수 케이스 판단 (18, 10, 69)
     * @param ipt_sb
     * @return
     */
    public boolean isBadWordNumber(String ipt_sb) {

        if(isNumber(ipt_sb)) {
            int ipt_sb_number = Integer.valueOf(ipt_sb);
            return (ipt_sb_number == 18 || ipt_sb_number == 10 || ipt_sb_number == 69) && ipt_sb.length() == 2;
        }
        return false;
    }

    /**
     * 숫자 판단 method
     * @param ipt_sb
     * @return
     */
    public boolean isNumber(String ipt_sb) {
        return Pattern.matches("^[0-9]*$", ipt_sb);
    }

    /**
     * Eng 판단 method
     * @param ipt_c
     * @return
     */
    public boolean isEngilish(String ipt_c) {
        return Pattern.matches("^[a-zA-Z]*$", ipt_c);
    }

    /**
     * 한글 판단 method
     * @param ipt_c
     * @return
     */
    public boolean isKorean(String ipt_c) { //한글자씩 한글 여부 체크
        return Pattern.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*", ipt_c);
    }

    /**
     * white list matching method
     * @param ipt
     * @return (if ipt is in White List)
     */
    public boolean isWhiteList(String ipt) {
        return this.badWordService.getWhiteList().stream()
                                                 .anyMatch(element ->
                                                         Pattern.matches(".*("+element+").*$", ipt));
    }
}

