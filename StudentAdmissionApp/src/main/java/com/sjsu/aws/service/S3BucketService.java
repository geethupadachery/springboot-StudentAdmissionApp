package com.sjsu.aws.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.sjsu.aws.data.DataObject;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsRequest;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
public class S3BucketService {

	@Value("${cloud.aws.credentials.accessKey}")
	private String key;

	@Value("${cloud.aws.credentials.secretKey}")
	private String secretKey;

	private S3Client s3Client;
	
	private AmazonS3 s3;

	@PostConstruct
	public void initialize() {

		AwsBasicCredentials awsBasicCredentials = AwsBasicCredentials.create(key, secretKey);

		s3Client = S3Client.builder().credentialsProvider(StaticCredentialsProvider.create(awsBasicCredentials))
				.region(Region.US_WEST_2).build();
		
		AWSCredentials cred = new BasicAWSCredentials(key, secretKey);
		s3 = new AmazonS3Client(cred);
	}

	public void uploadFile(DataObject dataObject)
			throws S3Exception, AwsServiceException, SdkClientException, URISyntaxException, FileNotFoundException {

		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder().bucket("student-admission-app-bucket")
					.key(dataObject.getName()).acl(ObjectCannedACL.PUBLIC_READ).build();

			File file = new File(getClass().getClassLoader().getResource(dataObject.getName()).getFile());

			s3Client.putObject(putObjectRequest, RequestBody.fromFile(file));
		} catch (AmazonServiceException ex) {
			System.out.println("Amazon Service Exception");
		}
	}

	public void downloadFile(DataObject dataObject)
			throws NoSuchKeyException, S3Exception, AwsServiceException, SdkClientException, IOException {
		GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket("student-admission-app-bucket")
				.key("sample.png").build();
		Resource resource = new ClassPathResource(".");

		s3Client.getObject(getObjectRequest, Paths.get(resource.getURL().getPath() + "/test.png"));

	}

	public List<String> listObjects() {
		return this.listObjects("student-admission-app-bucket");
	}

	public List<String> listObjects(String name) {
		List<String> names = new ArrayList<>();
		ListObjectsRequest listObjectsRequest = ListObjectsRequest.builder().bucket(name).build();
		ListObjectsResponse listObjectsResponse = s3Client.listObjects(listObjectsRequest);
		listObjectsResponse.contents().stream().forEach(x -> names.add(x.key()));
		return names;
	}

	public void deleteFile(DataObject dataObject) {
		this.deleteFile("student-admission-app-bucket", dataObject.getName());
	}

	/*
	 * public void deleteFile(String bucketName, String fileName) {
	 * DeleteObjectRequest deleteObjectRequest =
	 * DeleteObjectRequest.builder().bucket(bucketName).key(fileName) .build();
	 * s3Client.deleteObject(deleteObjectRequest); }
	 */

	public void deleteBucket(String bucket) {

		List<String> keys = this.listObjects(bucket);
		List<ObjectIdentifier> identifiers = new ArrayList<>();
		int iteration = 0;
		for (String key : keys) {

			ObjectIdentifier objIdentifier = ObjectIdentifier.builder().key(key).build();
			identifiers.add(objIdentifier);
			iteration++;

			if (iteration == 3) {
				iteration = 0;
				DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder().bucket(bucket)
						.delete(Delete.builder().objects(identifiers).build()).build();
				s3Client.deleteObjects(deleteObjectsRequest);
				identifiers.clear();
			}

		}

		if (identifiers.size() > 0) {
			DeleteObjectsRequest deleteObjectsRequest = DeleteObjectsRequest.builder().bucket(bucket)
					.delete(Delete.builder().objects(identifiers).build()).build();
			s3Client.deleteObjects(deleteObjectsRequest);

		}

		DeleteBucketRequest deleteBucketRequest = DeleteBucketRequest.builder().bucket(bucket).build();
		s3Client.deleteBucket(deleteBucketRequest);
	}

	public void deleteAllBuckets() {
		List<String> buckets = this.listBuckets();
		buckets.parallelStream().forEach(x -> this.deleteBucket(x));
	}

	public DataObject addBucket(DataObject dataObject) {
		dataObject.setName(dataObject.getName() + System.currentTimeMillis());
		CreateBucketRequest createBucketRequest = CreateBucketRequest.builder().bucket(dataObject.getName()).build();

		s3Client.createBucket(createBucketRequest);

		return dataObject;

	}

	public List<String> listBuckets() {
		List<String> names = new ArrayList<>();
		ListBucketsRequest listBucketsRequest = ListBucketsRequest.builder().build();
		ListBucketsResponse listBucketsResponse = s3Client.listBuckets(listBucketsRequest);
		listBucketsResponse.buckets().stream().forEach(x -> names.add(x.name()));
		return names;
	}
	

	public String uploadFile(MultipartFile multipartFile, String username) {
		String fileUrl = "";
		try {
			System.out.println("inside uploadFile");
			System.out.println("username is " + username);
			File file = convertMultiPartToFile(multipartFile);
			String fileName = generateFileName(multipartFile);
			fileUrl = "https://cloud-project-studentapp.s3-us-west-1.amazonaws.com" + "/"
					+ "cloud-project-studentapp" + "/" + fileName;
			uploadFileTos3bucket(fileName, file, username);
			file.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileUrl;

	}

	private File convertMultiPartToFile(MultipartFile file) throws IOException {
		File convFile = new File(file.getOriginalFilename());
		FileOutputStream fos = new FileOutputStream(convFile);
		fos.write(file.getBytes());
		fos.close();
		return convFile;
	}

	private String generateFileName(MultipartFile multiPart) {
		return multiPart.getOriginalFilename();
	}

	private void uploadFileTos3bucket(String fileName, File file, String username) {
		PutObjectResult putObject = s3.putObject("cloud-project-studentapp", username + "/" + fileName, file);
		System.out.println("uploadFileTos3bucket");
		System.out.println(putObject.getETag());
		System.out.println(putObject.getVersionId());
	}

	public void deleteFile(String fileName, String username) {
		System.out.println("Inside delete file");
        try {
        	//s3.deleteObject(new DeleteObjectRequest("student-admission-app-bucket", fileName));
        	//s3.deleteObject("cloud-project-studentapp", username+"/"+fileName);
        	DeleteObjectRequest deleteObjectRequest =
        			  DeleteObjectRequest.builder().bucket("cloud-project-studentapp").key(username+"/"+fileName) .build();
        			  s3Client.deleteObject(deleteObjectRequest);
        	System.out.println("delete completed");
        } catch (AmazonServiceException ex) {
            //logger.error("error [" + ex.getMessage() + "] occurred while removing [" + fileName + "] ");
        	ex.printStackTrace();
        }
		
	}
}
