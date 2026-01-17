package com.ai.rag.controller;


import com.ai.rag.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class RagController {

    @Autowired private RagService ragService;

    @Value("classpath:static/handbook.txt")
    private Resource handbookResource;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadHandbook() {
        if(!handbookResource.exists())
            return ResponseEntity.ok("No file found");
        ragService.ingestHandbook(handbookResource);
        return ResponseEntity.ok("File Uploaded Successfully");
    }

    @PostMapping("/uploadText")
    public ResponseEntity<String> uploadHandbook(@RequestBody Resource handbookResource) {
        if(!handbookResource.exists())
            return ResponseEntity.ok("No Text found");
        ragService.ingestHandbook(handbookResource);
        return ResponseEntity.ok("Text Uploaded Successfully");
    }


    @GetMapping("/ai")
    public ResponseEntity<String> generateAnswer(@RequestParam String question) {
        return ResponseEntity.ok(ragService.generateAnswer(question));
    }
}
