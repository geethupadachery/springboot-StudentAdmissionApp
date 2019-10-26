package com.sjsu.aws.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sjsu.aws.data.DataObject;
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
		System.out.println("username "+username +"  "+"fileName "+ fileName);
		this.S3BucketService.deleteFile(fileName, username);
	}
	  
	  @GetMapping("/getobject/{filename}")
		public void fetchObject(@PathVariable String filename) throws Exception {
			DataObject dataObject = new DataObject();
			dataObject.setName("filename");
			this.S3BucketService.downloadFile(dataObject);
		}
		
		@GetMapping("/listobjects")
		public List<String> listObjects() throws Exception {
			return this.S3BucketService.listObjects();
		}
		
		
		@PutMapping("/updateobject")
		public void updateObject(@RequestBody DataObject dataObject) throws Exception {
			this.S3BucketService.uploadFile(dataObject);
		}
		
	
		
		@PostMapping("/addbucket")
		public DataObject createBucket(@RequestBody DataObject dataObject) {
			return this.S3BucketService.addBucket(dataObject);
		}
		
		@GetMapping("/listbuckets")
		public List<String> listBuckets() {
			return this.S3BucketService.listBuckets();
		}
		
		@DeleteMapping("/deletebucket") 
		public void deleteBucket(@RequestBody DataObject dataObject) {
			this.S3BucketService.deleteBucket(dataObject.getName());
		}
		
		@DeleteMapping("/deletallbuckets")
		public void deleteAllBuckets() {
			this.S3BucketService.deleteAllBuckets();
		}

}
