package org.example.controller;

import lombok.extern.log4j.Log4j;
import net.coobird.thumbnailator.Thumbnailator;
import org.example.domain.AttachFileDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@Log4j
public class UploadController {
    @GetMapping("/uploadForm")
    public void uploadForm(MultipartFile[] uploadFile, Model model) {
        log.info("upload form");
    }

    @PostMapping("/uploadFormAction")
    public void uploadFormPost(MultipartFile[] uploadFile, Model model) {
        String uploadFolder = "C:\\upload";

        for (MultipartFile file : uploadFile) {
            log.info("--------------------------------------------------------------");
            log.info("Upload File Name: " + file.getOriginalFilename());
            log.info("Upload File Size: " + file.getSize());

            File saveFile = new File(uploadFolder, file.getOriginalFilename());

            try {
                file.transferTo(saveFile);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    @GetMapping("/uploadAjax")
    public void uploadAjax() {
        log.info("upload ajax");
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping(value="/uploadAjaxAction", produces=MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<AttachFileDTO>> uploadAjaxPost(MultipartFile[] uploadFile) {
        log.info("update ajax post..........");

        log.info("uploadFile.length: " + uploadFile.length);

        List<AttachFileDTO> list = new ArrayList<>();
        String uploadFolder = "/Users/yuseongpyo/Desktop/ImageUpload";

        String uploadFolderPath = getFolder();

        // ?????? ??????
        File uploadPath = new File(uploadFolder, getFolder());
        log.info("upload path: " + uploadPath);

        if (uploadPath.exists() == false) {
            uploadPath.mkdirs();
        }

        for (MultipartFile file : uploadFile) {
            log.info("---------------------------------------------");
            log.info("Upload File Name: " + file.getOriginalFilename());
            log.info("Upload File Size: " + file.getSize());

            AttachFileDTO attachDTO = new AttachFileDTO();

            String uploadFileName = file.getOriginalFilename();

            uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\") + 1);

            log.info("only file name: " + uploadFileName);

            attachDTO.setFileName(uploadFileName);

            // ?????? ????????? ?????? UUID ??????
            UUID uuid = UUID.randomUUID();

            uploadFileName = uuid.toString() + "_" + uploadFileName;


            try {
                // File saveFile = new File(uploadFolder, uploadFileName);
                File saveFile = new File(uploadPath, uploadFileName);
                file.transferTo(saveFile);

                attachDTO.setUuid(uuid.toString());
                attachDTO.setUploadPath(uploadFolderPath);

                // ????????? / ?????? ??????
                if (checkImageType(saveFile)) {
                    attachDTO.setImage(true);

                    FileOutputStream thumbnail = new FileOutputStream(new File(uploadPath, "s_" + uploadFileName));

                    Thumbnailator.createThumbnail(file.getInputStream(), thumbnail, 100, 100);

                    thumbnail.close();
                }

                // add to List
                list.add(attachDTO);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }

        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/display")
    @ResponseBody
    public ResponseEntity<byte[]> getFile(String fileName) {
        log.info("fileName: " + fileName);

        File file = new File("/Users/yuseongpyo/Desktop/ImageUpload" + fileName);

        log.info("file: " + file);

        ResponseEntity<byte[]> result = null;

        try {
            HttpHeaders header = new HttpHeaders();

            System.out.println("file.toPath(): " + file.toPath());
            System.out.println("Files.probeContentType(file.toPath()): " + Files.probeContentType(file.toPath()));

            header.add("Content-Type", Files.probeContentType(file.toPath()));

            // result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), HttpStatus.OK);
            result = new ResponseEntity<>(FileCopyUtils.copyToByteArray(file), header, HttpStatus.OK);

        } catch(IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    // ?????? ???????????? ??? ??? ?????? MIME ?????? APPLICATION_OCTET_STREAM_VALUE ??????
    @GetMapping(value="/download", produces=MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    // byte[] ?????? ????????? ????????? ??? ????????? org.springframework.core.io.Resource ????????? ???????????? ??? ??? ????????? ??????
    public ResponseEntity<Resource> downloadFile(String fileName, @RequestHeader("User-Agent") String userAgent) {
        log.info("download file: " + fileName);
        Resource resource = new FileSystemResource("/Users/yuseongpyo/Desktop/ImageUpload" + fileName);

        log.info("resource: " + resource);

        if (resource.exists() == false) {
            log.info("NOT_FOUND");
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String resourceName = resource.getFilename();
        log.info("resourceName: " + resourceName);

        // remove UUID
        String resourceOriginalName = resourceName.substring(resourceName.indexOf("_") + 1);

        HttpHeaders headers = new HttpHeaders();

        try {
            String downloadName = null;

            if (userAgent.contains("Trident")) {
                log.info("IE browser");
                downloadName = URLEncoder.encode(resourceOriginalName, "UTF-8").replaceAll("\\+", " ");
            } else if (userAgent.contains("Edge")) {
                log.info("Edge browser");
                downloadName = URLEncoder.encode(resourceOriginalName, "UTF-8");
            } else {
                log.info("Chrome browser");
                downloadName = new String(resourceOriginalName.getBytes("UTF-8"), "ISO-8859-1");
            }

            log.info("downloadName: " + downloadName);

            // Content-Disposition: ??????????????? ???????????? ????????? ????????? ???????????? ??????
            // -> ?????? ????????? ?????? ????????? ????????? ?????? ????????? ????????? ?????? ????????? ??? ????????? ????????? ?????? ??????!
            // % IE????????? ?????? ?????? ????????? ????????????. -> Content-Disposition??? IE????????? ????????? ????????? ????????? ??????!
            // -> Http ?????? ??? User-Agent??? ???????????? ???????????? ??????/ pc, mobile ???????????? ????????? ??????
            // headers.add("Content-Disposition", "attachment; filename=" + new String(resourceName.getBytes("UTF-8"), "ISO-8859-1"));
            headers.add("Content-Disposition", "attachment; filename=" + downloadName);
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticateD()")
    @PostMapping("/deleteFile")
    @ResponseBody
    public ResponseEntity<String> deleteFile(String fileName, String type) {
        log.info("deleteFile: " + fileName);

        File file;

        try {
            file = new File("/Users/yuseongpyo/Desktop/ImageUpload" + URLEncoder.encode(fileName, "UTF-8"));

            log.info("file path: " + file.getAbsolutePath());


            file.delete();

            if (type.equals("image")) {
                String largeFileName = file.getAbsolutePath().replace("s_", "");

                log.info("largeFileName: " + largeFileName);

                file = new File(largeFileName);

                file.delete();
            }

        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("deleted", HttpStatus.OK);
    }


    private String getFolder() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        String str = sdf.format(date);
        // java.lang.IllegalArgumentException: character to be escaped is missing ?????? - File.separator
        // return str.replaceAll("-", File.separator);
        return str.replaceAll("-", "/");
    }

    // ?????? ?????? ????????? ??????/?????? ?????? ?????? ??????
    private boolean checkImageType(File file) {
        try {
            String contentType = Files.probeContentType(file.toPath());

            return contentType.startsWith("image");
        } catch(IOException e) {
            e.printStackTrace();
        }

        return false;
    }

}