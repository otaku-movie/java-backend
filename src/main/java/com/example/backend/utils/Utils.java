package com.example.backend.utils;
import com.example.backend.config.MinIOConfig;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Utils {
    public static File MultipartToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir") + File.separator + multipartFile.getOriginalFilename());
        multipartFile.transferTo(file);

        return file;
    }
    public static void upload (InputStream stream, String path, String mime) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        MinIOConfig minIOConfig = new MinIOConfig();


        minIOConfig.getMinioClient()
            .putObject(
              PutObjectArgs.builder()
                .bucket(minIOConfig.bucket)
                .object(path)
                .stream(stream, stream.available(), -1)
                .contentType(mime)
                .build()
            );
    }
}
