package com.starshop.common.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.auth.EnvironmentVariableCredentialsProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyuncs.exceptions.ClientException;
import com.starshop.properties.AliyunOSSProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


@Component
public class AliyunOSSUtils {

    @Resource
    private AliyunOSSProperties aliyunOSSProperties;

    public String upload(byte[] content ,String originalFilename) throws ClientException {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String region = aliyunOSSProperties.getRegion();
        String bucketName = aliyunOSSProperties.getBucketName();
        //从环境变量中获取ID和KEY
        EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();

        //获取当前系统日期的字符串，格式为yyyy-MM
        String dir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));

        //生成一个新的不重复文件名
        String newFileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));

        String objectName = dir +"/"+ newFileName;

        //创建OSSClient实例
        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);
        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ossClient.putObject(bucketName,objectName,new ByteArrayInputStream(content));
        } finally {
            ossClient.shutdown();
        }

        //格式：https://bucketName.endpoint/fileName
        return endpoint.split("//")[0] + "//" +bucketName + "." + endpoint.split("//")[1] + "/" +objectName;
    }



}
