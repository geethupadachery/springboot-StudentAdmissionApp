package com.sjsu.aws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sjsu.aws.service.S3BucketService;

@RestController
@RequestMapping(value = "/studentadmissionapp")
@CrossOrigin(origins = "*")
public class StudentAdmissionAppController {

	@Autowired
	S3BucketService S3BucketService;

	@PostMapping("/addobject")
	public void createObject(@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "username") String username) throws Exception {
		this.S3BucketService.uploadFile(file, username);
	}

	@DeleteMapping("/deleteobject")
	public void deleteObject(@RequestParam(value = "fileName") String fileName,
			@RequestParam(value = "username") String username) {
		System.out.println("username " + username + "  " + "fileName " + fileName);
		this.S3BucketService.deleteFile(fileName, username);
	}

	@GetMapping("/getobject")
	public void fetchObject(@RequestParam(value = "fileName") String filename,
			@RequestParam(value = "username") String username) throws Exception {
		this.S3BucketService.downloadFile(filename, username);
	}

	@PutMapping("/updateobject")
	public void updateObject(@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "username") String username) throws Exception {
		this.S3BucketService.uploadFile(file, username);
	}
}
