package com.example.javacryptoweb.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.javacryptoweb.service.FileEncryptionService;

import ch.qos.logback.core.model.Model;

@Controller
public class FileController {
     @Autowired
    private FileEncryptionService fileEncryptionService;

    private Map<String, byte[]> encryptedFiles = new HashMap<>();
    private Map<String, SecretKey> fileKeys = new HashMap<>();
    private Map<String, byte[]> decryptedFiles = new HashMap<>();

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/upload")
    public String uploadFile(MultipartFile file, RedirectAttributes redirectAttributes, Model model) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Selectionner un fichier pour télécharger.");
            return "redirect:/";
        }

        try {
            SecretKey key = fileEncryptionService.generateKey();
            byte[] fileBytes = file.getBytes();
            byte[] encryptedFile = fileEncryptionService.encryptFile(fileBytes, key);
            String fileId = UUID.randomUUID().toString();
            encryptedFiles.put(fileId, encryptedFile);
            fileKeys.put(fileId, key);

            redirectAttributes.addFlashAttribute("message", "Fichier chiffré avec succès.");
            redirectAttributes.addFlashAttribute("fileId", fileId);
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            redirectAttributes.addFlashAttribute("message", "Echec du téléchargement et chiffrement du fichier: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @GetMapping("/download")
    @ResponseBody
    public byte[] downloadFile(@RequestParam("fileId") String fileId) {
        byte[] encryptedFile = encryptedFiles.get(fileId);
        if (encryptedFile == null) {
            throw new RuntimeException("Fichier non trouvé");
        }
        return encryptedFile;
    }

    @PostMapping("/decrypt")
    public String decryptFile(@RequestParam("fileId") String fileId, RedirectAttributes redirectAttributes, Model model) {
        byte[] encryptedFile = encryptedFiles.get(fileId);
        SecretKey key = fileKeys.get(fileId);
        if (encryptedFile == null || key == null) {
            redirectAttributes.addFlashAttribute("message", "Fichier ou clé non trouvé");
            return "redirect:/";
        }

        try {
            byte[] decryptedFile = fileEncryptionService.decryptFile(encryptedFile, key);
            decryptedFiles.put(fileId, decryptedFile);
            redirectAttributes.addFlashAttribute("message", "Fichier chiffré avec succès.");
            redirectAttributes.addFlashAttribute("fileId", fileId);
        } catch (IOException | IllegalBlockSizeException | BadPaddingException e) {
            redirectAttributes.addFlashAttribute("message", "Echec du decryptage du fichier: " + e.getMessage());
            e.printStackTrace();
        }

        return "redirect:/";
    }

    @GetMapping("/downloadDecrypted")
    @ResponseBody
    public byte[] downloadDecryptedFile(@RequestParam("fileId") String fileId) {
        byte[] decryptedFile = decryptedFiles.get(fileId);
        if (decryptedFile == null) {
            throw new RuntimeException("Fichier non trouvé");
        }
        return decryptedFile;
    }
}
