package com.mby.qn;


import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

public class BatchDownload {


    private static  final String plate_url="http://assets.themobiyun.com//prechecks/plate/%s";
    private static  final String frame_url="http://assets.themobiyun.com//prechecks/frame/%s";
    private static  final String mileage_url="http://assets.themobiyun.com//prechecks/mileage/%s";
    private static  final String attachment_url="http://assets.themobiyun.com//prechecks/attachment/%s";
    private static  final String body_1_url="http://assets.themobiyun.com//prechecks/body/%s-1";
    private static  final String body_2_url="http://assets.themobiyun.com//prechecks/body/%s-2";
    private static  final String body_3_url="http://assets.themobiyun.com//prechecks/body/%s-3";
    private static  final String body_4_url="http://assets.themobiyun.com//prechecks/body/%s-4";
    private static  final String body_5_url="http://assets.themobiyun.com//prechecks/body/%s-5";

    public static void main(String[] args) throws IOException {
        System.out.println("开始，请不要关闭窗口...");
        if (args.length == 0){
            throw new RuntimeException("需要传入参数");
        }
        String file = args[0];
        File inputFile = new File(file);
        List<String> list = FileUtils.readLines(inputFile, Charset.defaultCharset());

        String type = args[1];
        switch (type){
            case "plate":
                exportByList(list, plate_url, getOutputFile(inputFile, type));
                break;
            case "frame":
                exportByList(list, frame_url, getOutputFile(inputFile, type));
                break;
            case "mileage":
                exportByList(list, mileage_url, getOutputFile(inputFile, type));
                break;
            case "attachment":
                exportByList(list, attachment_url, getOutputFile(inputFile, type));
                break;
            case "body":
                list.parallelStream().forEach(x -> {
                    try {
                        exportBody(x, getOutputFile(inputFile, type));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                break;
            default: throw new RuntimeException("第二个参数请传入: plate 或 frame 或 mileage 或 attachment 或 body");
        }
        System.out.println("结束，导出成功...");
    }

    private static String getOutputFile(File inputFile, String type) {
        String parentPath = inputFile.getParent();
        File outDic = new File(String.format("%s/prechecks/%s",parentPath, type));
        if (!outDic.exists()){
            outDic.mkdirs();
        }
        String filePath = outDic.getPath()+ "/%s.png";
        return filePath;
    }

    public static void exportByList(List<String> list, String type, String filePath) throws IOException {
        list.parallelStream().forEach(x -> {
            try {
                exportWithParam(x,type,filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void exportBody(String precheckId, String filePath) throws IOException {
        exportWithParam(precheckId, body_1_url, filePath);
        exportWithParam(precheckId, body_2_url, filePath);
        exportWithParam(precheckId, body_3_url, filePath);
        exportWithParam(precheckId, body_4_url, filePath);
        exportWithParam(precheckId, body_5_url, filePath);
    }

    public static void exportWithParam(String precheckId, String type, String filePath) throws IOException {
        String replace = precheckId.replace("\"", "");
        String url = String.format(type, replace);
        String pngName = url.substring(url.lastIndexOf("/") + 1);
        String path = String.format(filePath, pngName);
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).get().build();
        Response response = client.newCall(request).execute();
        if (response.code() == 200){
            ResponseBody body = response.body();
            OutputStream outputStream = new FileOutputStream(new File(path));
            IOUtils.copy(body.byteStream(), outputStream);
            outputStream.flush();
            outputStream.close();
        }
    }
}
