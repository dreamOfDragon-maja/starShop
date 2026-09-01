package com.starshop.constant;

public class BucketConstant {

    public record BucketSign(BucketThreadType bucketThreadType, String UUID) {}

    public enum BucketThreadType{
        READ_THREAD ,
        WRITE_THREAD ;
    }


}
