package com.filtering.file.upload.controller;

import com.filtering.file.upload.redis.RedisStorageService;
import com.filtering.file.upload.storage.StorageFileNotFoundException;
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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private RedisStorageService redisStorageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String listUploadedFiles(/*Model model*/) throws IOException {


        storageService.deleteAll();
        storageService.init();
//        Stream<Path> pathStream = storageService.loadAll();
//
//        model.addAttribute("files", storageService.loadAll().map(
//                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
//                        "serveFile", path.getFileName().toString()).build().toString())
//                .collect(Collectors.toList()));

        return "uploadForm";
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        logger.info("file :" + file.getFilename());

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PostMapping("/")
    public String handleFileUpload(@RequestParam("radioWord") String whichWord, @RequestParam("file") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        logger.info("upload - start");
        logger.info(file.getOriginalFilename());

        String filename = file.getOriginalFilename();
        storageService.store(file);

        List<String> fileWords = null;
        if (filename.toUpperCase().endsWith(".XLS") || filename.toUpperCase().endsWith(".XLSX")) {

            Path path = storageService.load(filename);
            fileWords = storageService.excelUpload(path);

            if (whichWord.equals("bword"))
                this.redisStorageService.putBWordList(fileWords);
            else //whichWord = "wword"
                this.redisStorageService.putWhiteList(fileWords);

        }else {
            fileWords = this.storageService.getLines(file.getOriginalFilename());

            if (whichWord.equals("bword"))
                this.redisStorageService.putBWordList(fileWords);
            else //whichWord = "wword"
                this.redisStorageService.putWhiteList(fileWords);

        }
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

}
