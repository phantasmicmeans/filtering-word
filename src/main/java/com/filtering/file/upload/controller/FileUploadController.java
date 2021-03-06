package com.filtering.file.upload.controller;

import com.filtering.file.upload.redis.RedisStorageService;
import com.filtering.file.upload.exception.StorageException;
import com.filtering.file.upload.exception.StorageFileNotFoundException;
import com.filtering.file.upload.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.file.Path;
import java.util.List;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisStorageService redisStorageService;

    @Autowired
    private reqValidator reqValidator;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(/*Model model*/) throws IOException {
        this.storageService.deleteAll();
        this.storageService.init();
        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = this.storageService.loadAsResource(filename);
        logger.info("file :" + file.getFilename());

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("radioWord") String radioWord, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        String filename = file.getOriginalFilename();
        this.storageService.store(file);

        List<String> fileContents = null;

        if (filename.toUpperCase().endsWith(".XLS") || filename.toUpperCase().endsWith(".XLSX")) {
            Path path = this.storageService.load(filename);
            fileContents = this.storageService.excelUpload(path);
        }
        else if(filename.endsWith("TXT"))
            fileContents = this.storageService.getLines(file.getOriginalFilename());
        else
            throw new StorageException("지원하지 않는 파일 형식입니다.");

        if (radioWord.equals("bword")) this.redisStorageService.putBWordList(fileContents);
        else if(radioWord.equals("wword")) this.redisStorageService.putWhiteList(fileContents);

        redirectAttributes.addFlashAttribute("message",
                 file.getOriginalFilename() + "업로드에 성공하였습니다. " + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
