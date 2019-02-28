package com.filtering.file.upload.storage;

import com.filtering.file.upload.utils.ExcelRead;
import com.filtering.file.upload.utils.ExcelReadOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file " + filename);
            }
            if (filename.contains("..")) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file with relative path outside current directory " + filename);
            }

            if (filename.toUpperCase().endsWith(".XLS") || filename.toUpperCase().endsWith(".XLSX")) {
                logger.info("excel");
                try {
                    FileInputStream inputStream = (FileInputStream)file.getInputStream();
                    Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);

                }catch(IOException e){
                    System.out.println(e.getMessage() );
                }
            } else {
                try (InputStream inputStream = file.getInputStream()) {
                    Files.copy(inputStream, this.rootLocation.resolve(filename),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new StorageException("Failed to store file " + filename, e);
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<String> excelUpload(Path path) {

        File destFile = path.toFile();
        ExcelReadOption excelReadOption = new ExcelReadOption();
        excelReadOption.setFilePath(destFile.getAbsolutePath());
        excelReadOption.setOutputColumns("A","B");
        excelReadOption.setStartRow(1);

        List<Map<String,String>> excelContents = ExcelRead.read(excelReadOption);
        List<String> excelContent = new ArrayList<>();

        for(Map<String,String> article : excelContents)
            excelContent.add(article.get("A"));

        return excelContent;

    }


    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.rootLocation, 1)
                .filter(path -> !path.equals(this.rootLocation))
                .map(this.rootLocation::relativize);
        }
        catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    @Override
    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public List<String> getLines(String filename) {

        List<String> retData = null;
        Path file = load(filename);
        try {
            Stream<String> lines = Files.lines(file);
            retData = lines.collect(Collectors.toList());
        }catch(Throwable e) {
            throw new StorageException(e.getMessage(), e.getCause());
        }
        return retData;
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(this.rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
