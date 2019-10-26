package com.sjsu.aws.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({ "com.sjsu.aws.*" })
public class StudentAdmissionAppClient
{
    public static void main(String[] args)
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(StudentAdmissionAppClient.class, args);
    }
}
