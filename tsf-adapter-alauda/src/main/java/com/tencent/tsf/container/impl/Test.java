package com.tencent.tsf.container.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.text.DecimalFormat;

public class Test {
    public static void main(String []args){

        String testStr = "Bear hhahah";
        System.out.println(testStr.replace("Bear ", ""));

        if(testStr.endsWith("/")){
            System.out.println("/////");
        }else {
            System.out.println("nonono");
        }

        String val = "1.311432";
        DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
        String p =   decimalFormat.format(Float.parseFloat(val));//format 返回的是字符串
        System.out.println(p);

        JSONObject jsonObject = JSON.parseObject("");
        if (jsonObject.containsKey("aaa")){
            System.out.println(1);
        }

    }
    private static Float getFloat(String str){
        String number = str.replaceAll("[a-zA-Z]","" );
        String letter = str.replaceAll("[^a-zA-Z]","" );
        Float num = Float.parseFloat(number);
        switch (letter){
            case "Mi":
                num /= 1024;
                break;
            case "Ki":
                num /= (1024*1024);
                break;
            case "Bi":
                num /= (1024*1024*1024);
                break;
            case "m":
                num /= 1000;
                break;
            default:
                break;

        }
        return num;
    }

    private void switchT(){
        for(int i=0;i<10;i++){
            System.out.println(i);
            switch (i) {
                case 0:
                    System.out.println("match0");
                    break;
                case 1:
                    System.out.println("match1");
                    break;
                case 2:
                    System.out.println("match2");
                    break;
                case 3:
                    System.out.println("match3");
                    break;
            }
        }
    }
}
