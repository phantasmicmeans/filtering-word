package com.filtering.file.upload.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.filtering.file.upload.data.RequestF;
import com.filtering.file.upload.data.RequestWord;
import com.filtering.file.upload.data.ResponseF;
import com.filtering.file.upload.exception.DataNullException;
import com.filtering.file.upload.exception.StorageException;
import com.filtering.file.upload.exception.StorageFileNotFoundException;
import com.filtering.file.upload.redis.RedisStorageService;
import com.filtering.file.upload.service.BadWordService;
import com.filtering.file.upload.service.FilterService;
import com.filtering.file.upload.storage.StorageService;
import com.filtering.file.upload.utils.ExcelRead;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin("*")
public class filterController {

    @Autowired
    private FilterService filterService;

    @Autowired
    private reqValidator reqValidator;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    BadWordService badWordService;

    @Autowired
    RedisStorageService redisStorageService;

    @Autowired
    private StorageService storageService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * filtering된 결과 return
     * @param request
     * @return
     */
    @GetMapping(value = "v1/filter/result/search", consumes = {"application/json"}, produces =  {"application/json"})
    @ApiOperation(value = "filtering된 결과 리턴")
    public ResponseEntity<String> getFilteredBWordForTest(@RequestBody RequestF request){

        RequestF requestF = Optional.ofNullable(request)
                                    .orElseThrow(DataNullException::new);

        String bword = this.reqValidator.validateRequest(requestF.getRequestStr());
        String filteredResult = this.filterService.betterFilteringForTest(bword);

        return new ResponseEntity<>(filteredResult, HttpStatus.OK);
    }


    /**
     * 금칙어를 request로 받아, 금칙어 여부와 filtering된 결과 return - RESTful
     * TODO -> boot tomcat URI 한글 encoding 문제
     * @param request String
     * @return
     */
    @GetMapping(value = "v1/filter/redis/{request}", produces = {"application/json"})
    @ApiOperation(value = "금칙어를 request로 받아, 금칙어 여부와 filtering된 결과 return - RESTful")
    public ResponseEntity<ResponseF<String>> getFilteredBWord(@PathVariable("request") String request) {

        String bword = reqValidator.validateRequest(request);

        String filteredResult = filterService.betterFilteringForTest(bword);
        boolean matches = filterService.isBadWord(bword);

        ResponseF<String> responseF = new ResponseF<>(matches, filteredResult);
        return new ResponseEntity<>(responseF, HttpStatus.OK);
    }


    /**
     * 금칙어를 request로 받아, 금칙어 여부와 filtering된 결과 return - Not RESTful
     * @param request RequestF
     * @return
     */
    @GetMapping(value = "v1/filter/redis/search", produces = {"application/json"})
    @ApiOperation(value = "금칙어를 request로 받아, 금칙어 여부와 filtering된 결과 return - Not RESTful")
    public ResponseEntity<ResponseF<String>> getFilteredResult(@RequestBody RequestF request) {

        RequestF requestF = Optional.ofNullable(request)
                                .orElseThrow(DataNullException::new);

        String bword = reqValidator.validateRequest(requestF.getRequestStr());
        String filteredResult = filterService.betterFilteringForTest(bword);
        boolean matches = filterService.isBadWord(bword);

        ResponseF<String> responseF = new ResponseF<>(matches, filteredResult);
        return new ResponseEntity<>(responseF, HttpStatus.OK);
    }

    /**
     * 금칙어를 request로 받아, Redis에 저장.
     * @param request
     * @return self-descriptive uri를 함께 리턴.
     */
    @PostMapping(value = "v1/filter/redis", produces = {"application/json"})
    @ApiOperation(value = "금칙어를 request로 받아, Redis에 저장")
    public ResponseEntity<?> saveBWord(@RequestBody RequestF request) {
        //TODO - Redis관련 Exception 정의 안되어있음.
        RequestF requestF = Optional.ofNullable(request)
                                .orElseThrow(DataNullException::new);
        String bword = reqValidator.validateRequest(requestF.getRequestStr());
        this.redisStorageService.putBWord(bword);
        URI location = ServletUriComponentsBuilder.fromCurrentServletMapping()
                                                    .path("v1/filter/redis/{request}")
                                                    .build()
                                                    .expand(bword)
                                                    .toUri();
        return ResponseEntity.created(location).body(requestF);
    }

    /**
     * 금칙어 리스트를 받아 Redis로 insert
     * @param requestWord
     * @return
     */
    @PostMapping(value = "v1/filter/redis/in", produces = {"application/json"})
    @ApiOperation(value = "금칙어 리스트를 받아 Redis로 insert")
    public ResponseEntity<String> saveBWordList(@RequestBody RequestWord requestWord) {

        RequestWord requestB = Optional.ofNullable(requestWord)
                                    .orElseThrow(DataNullException::new);

        List<String> bwordList = requestB.getWordList();
        if (!bwordList.isEmpty()) {
            this.redisStorageService.putBWordList(bwordList);
            return new ResponseEntity<>("successfully created", HttpStatus.CREATED);
        } else
            throw new DataNullException();
    }

    /**
     * 금칙어 리스트를 받아 Redis로부터 delete
     * @param requestWord
     * @return
     */
    @PostMapping(value = "v1/filter/redis/out", produces = {"application/json"})
    @ApiOperation(value = "금칙어 리스트를 받아 Redis로 delete")
    public ResponseEntity<String> deleteBWord(@RequestBody RequestWord requestWord) {

        RequestWord requestB = Optional.ofNullable(requestWord)
                                    .orElseThrow(DataNullException::new);

        List<String> bwordList = requestB.getWordList();
        if(!bwordList.isEmpty()) {
            this.redisStorageService.removeBWordList(bwordList);
            return new ResponseEntity<>("successfully deleted", HttpStatus.OK);
        }else {
            throw new DataNullException();
        }
    }

    /**
     * whiteList to Redis insert
     * @param requestWhiteWord
     * @return
     */
    @PostMapping(value = "v1/filter/whitelist/redis/in", produces = {"application/json"})
    @ApiOperation(value = "whiteList to Redis insert")
    public ResponseEntity<String> saveWhiteWordList(@RequestBody RequestWord requestWhiteWord) {

        RequestWord requestB = Optional.ofNullable(requestWhiteWord)
                                    .orElseThrow(DataNullException::new);

        List<String> whiteList = requestB.getWordList();
        if (!whiteList.isEmpty()) {
            this.redisStorageService.putWhiteList(whiteList);
            return new ResponseEntity<>("successfully created", HttpStatus.CREATED);
        } else
            throw new DataNullException();

    }


    /**
     * delete white Word in WhiteList from redis
     * @param requestWord
     * @return
     */
    @PostMapping(value = "v1/filter/whitelist/redis/out", produces = {"application/json"})
    @ApiOperation(value = "delete white Word in WhiteList from redis")
    public ResponseEntity<String> deleteWhiteList(@RequestBody RequestWord requestWord) {

        RequestWord requestB = Optional.ofNullable(requestWord)
                                .orElseThrow(DataNullException::new);

        List<String> whiteList = requestB.getWordList();
        if(!whiteList.isEmpty()) {
            this.redisStorageService.removeWhiteList(whiteList);
            return new ResponseEntity<>("successfully deleted", HttpStatus.OK);
        }else {
            throw new DataNullException();
        }
    }

    /**
     * 금칙어/화이트 리스트를 type으로 받아, redis 데이터 ->  엑셀파일로 다운로드.
     * @param type = bword(badword) | wword(whiteword)
     * @return
     * @throws IOException
     */
    @GetMapping("/download/{type}")
    public ResponseEntity getExcelFile(@PathVariable("type") String type) throws IOException {

        String wordType = this.reqValidator.validateType(type);
        List<String> wordList = this.redisStorageService.getWordList(wordType);

        File excelFile = ExcelRead.downloadFile(wordList, wordType);

        MediaType mediatype= MediaType.parseMediaType("application/vnd.ms-excel");
        InputStreamSource resource = new InputStreamResource(new FileInputStream(excelFile));
        Files.delete(excelFile.toPath());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + wordType +".xlsx")
                .contentType(mediatype)
                .body(resource);
    }

    /**
     * 금칙어 to Redis default 값들 저장.
     * @return
     */
    @GetMapping(value = "v1/filter/bword/redis/set")
    public ResponseEntity makeSet() {
        try {
            this.badWordService.makeBWordListToRedis();
        }catch(Exception e){
            logger.info(e.getMessage());
        }
        return new ResponseEntity(HttpStatus.CREATED);
    }

    @ApiOperation(value = "ex. http://192.168.1.30:8080", notes = "ATC서버, 이미지를 처리 후 response 반환")
    @PostMapping("/upload/server/images")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("str") String key) {

        Optional.ofNullable(file).orElseThrow(()
                -> new StorageFileNotFoundException("파일을 로드할 수 없습니다."));
        logger.info(key);

        String fileNameUpper = file.getOriginalFilename().toUpperCase(); //fileName
        if(fileNameUpper.endsWith("PNG") || fileNameUpper.endsWith("JPG") || fileNameUpper.endsWith("JPEG")) {
            this.storageService.store(file); //upload-dir storage에 저장 ->
        }else
            throw new StorageException("지원하지 않는 파일 형식입니다");

        File imageFile = this.storageService.load(file.getOriginalFilename()).toFile(); //사용할 수 있게 변환
        //TODO -> do something
        String json_result = doSomething(imageFile, key);
        return ResponseEntity.ok(json_result); //response 전송
    }


    private String doSomething(File file, String key) {
        return file.getName() + key;
    }

}
