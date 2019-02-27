package com.filtering.file.upload.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    void store(MultipartFile file);

    List<String> getLines(String filename);

    Stream<Path> loadAll();

    Path load(String filename);

    List<String> excelUpload(Path path);

    Resource loadAsResource(String filename);

    void deleteAll();

}
