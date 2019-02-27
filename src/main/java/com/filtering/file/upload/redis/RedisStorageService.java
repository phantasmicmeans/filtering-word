package com.filtering.file.upload.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;


@Service
public class RedisStorageService {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private final RedisTemplate<String, String> redisTemplate;

    public RedisStorageService(RedisTemplate<String,String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void putBWordList(List<String> bwordList) { //저장
        if(!bwordList.isEmpty()) {
            bwordList.forEach(this::putBWord);
        }
    }

    public void putBWord(String bword) { //굼칙어 저장
        if(!isExistsBWord(bword)) {
            String key = getBWordSetKey(bword);
            this.redisTemplate.opsForSet().add(key , bword);
        }
    }

    public void removeBWordList(List<String> bwordList) {
        if(!bwordList.isEmpty()) {
            bwordList.forEach(this::removeBWord);
        }
    }

    public void removeBWord(String bword) { //금칙어 삭제
        if(isExistsBWord(bword)) {
            String key = getBWordSetKey(bword);
            this.redisTemplate.opsForSet().remove(key , bword);
        }
    }

    public void putWhiteList(List<String> whiteList) {
        if(!whiteList.isEmpty()) {
            whiteList.forEach(this::putWhiteWord);
        }
    }

    public void putWhiteWord(String whiteWord) {
        if(!isExistsInWhiteList(whiteWord)) {
            this.redisTemplate.opsForSet().add("white_list", whiteWord);
        }
    }

    public void removeWhiteList(List<String> whiteList) {
        if(!whiteList.isEmpty()) {
            whiteList.forEach(this::removeWhiteWord);
        }
    }

    public void removeWhiteWord(String whiteWord) {
        if(isExistsInWhiteList(whiteWord)) {
            this.redisTemplate.opsForSet().remove("white_list", whiteWord);
        }
    }


    public boolean isExistsBWord(String bword) {
        String key = getBWordSetKey(bword);
        return this.redisTemplate.opsForSet().isMember(key, bword);
    }


    public String getBWordSetKey(String bword) {

        if(bword.length() == 2) return "bword_two";
        else if(bword.length() == 3) return "bword_three";
        else if(bword.length() == 4) return "bword_four";
        else return "bword_more";
    }


    public Set<String> getWhiteList () {
        return this.redisTemplate.opsForSet().members("white_list");
    }

    public boolean isExistsInWhiteList(String bword) {
        return this.redisTemplate.opsForSet().isMember("white_list", bword);
    }

}
