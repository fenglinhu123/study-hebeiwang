package com.mhyy.user.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class WorkUtils {
    public static void main(String[] args) {
        String inputFile = "F:\\IDEAworkspace\\cgb2202-se_-teacher\\note.txt"; // 输入文件路径
        String outputFile = "F:\\IDEAworkspace\\cgb2202-se_-teacher\\note1.txt"; // 输出文件路径

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            while ((line = reader.readLine()) != null) {
                // 替换"xxxx"为"yyy"
                String replacedLine = line.replace("xxxx", "yyy");
                writer.write(replacedLine);
                writer.newLine();
            }

            System.out.println("替换完成！");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}