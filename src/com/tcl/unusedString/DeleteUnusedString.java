
package com.tcl.unusedString;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @author pcgu
 */
public class DeleteUnusedString {
    // 字符格式
    private static String ENCODING = "UTF-8";
    // 提取出来需要删除的字符串所在txt文件的路径
    private static String UNUSED_STRING_FILE_PATH = "/data/share/new/UnusedString/";
    // txt文件名
    private static String UNUSED_STRING_FILE_NAME = "unused.txt";
    // 需要过滤的工程目录
    private static String PROJECT_RES_PATH = "/data/project/userCare/master/client/UserCare/res";
    private static ArrayList<String> ALL_UNUSED_STRING_LIST = new ArrayList<>();
    private static ArrayList<File> ALL_STRING_XML_FILE_LIST = new ArrayList<>();
    private static int REALY_DELETE_XML_STRING_LENGTH = 0;

    public static void main(String[] args) {
        try {
            initUnusedStringList();
            initStringXmlFileList();
            deleteUnusedStringInStringXml();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void initUnusedStringList() throws Exception {
        // 获取result.txt 文件 生成地址
        File file = new File(UNUSED_STRING_FILE_PATH, UNUSED_STRING_FILE_NAME);
        // 判断文件是否存在
        if (file.isFile() && file.exists()) {
            // 考虑到编码格式
            InputStreamReader read = new InputStreamReader(new FileInputStream(file), ENCODING);
            BufferedReader bufferedReader = new BufferedReader(read);
            String line = null;
            String subString = null;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.contains("R.string.")) {
                    int beginIndex = line.indexOf("R.string.");
                    int endIndex = line.indexOf("' ");
                    subString = line.substring(beginIndex, endIndex);
                    String[] strings = subString.split("\\.");
                    ALL_UNUSED_STRING_LIST.add(strings[2]);
                }
            }
            read.close();
        }
    }

    private static void initStringXmlFileList() {
        File file = new File(PROJECT_RES_PATH);
        if (file != null && file.exists() && file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (int i = 0; i < listFiles.length; i++) {
                File valuesFile = listFiles[i];
                if (valuesFile.isDirectory()) {
                    File[] valuesFiles = valuesFile.listFiles();
                    for (int j = 0; j < valuesFiles.length; j++) {
                        File stringFile = valuesFiles[j];
                        if (stringFile.isFile()
                                && stringFile.getName().equalsIgnoreCase("strings.xml")) {
                            ALL_STRING_XML_FILE_LIST.add(stringFile);
                        }
                    }
                }
            }
        }
    }

    private static void deleteUnusedStringInStringXml() {
        for (int i = 0; i < ALL_STRING_XML_FILE_LIST.size(); i++) {
            File stringFile = ALL_STRING_XML_FILE_LIST.get(i);
            parsexml(stringFile);
        }
        System.out.println("all unused string size->" + ALL_UNUSED_STRING_LIST.size());
        System.out.println("realy delete xml string length->" + REALY_DELETE_XML_STRING_LENGTH);
    }

    private static void parsexml(File file) {
        // 建立DocumentBuilderFactory对象
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder builder;
        Document doucument;
        try
        {
            // 建立DocumentBuilder对象
            builder = builderFactory.newDocumentBuilder();
            // 用DocumentBuilder对象的parse方法引入文件建立Document对象
            doucument = builder.parse(file);
            // 找出string的节点list
            NodeList allStrings = doucument.getElementsByTagName("string");
            filterUnusedXmlString(allStrings, file.getAbsolutePath());
        } catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        } catch (SAXException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            System.err.println("找不到你指定的文件！");
            e.printStackTrace();
        }
    }

    public static void filterUnusedXmlString(NodeList allStringNodeList, String filename)
    {
        Element element;
        ArrayList<Node> filterStringList = new ArrayList<>();
        ArrayList<Integer> unUsedStringIndexList = new ArrayList<>();
        // 对符合条件的所有节点进行遍历
        for (int m = 0; m < ALL_UNUSED_STRING_LIST.size(); m++)
        {
            String unUsedStringItem = ALL_UNUSED_STRING_LIST.get(m);
            for (int i = 0; i < allStringNodeList.getLength(); i++)
            {
                // 获得一个元素
                element = (Element) allStringNodeList.item(i);
                if (unUsedStringItem.equals(element.getAttribute("name"))) {
                    unUsedStringIndexList.add(i);
                    i = allStringNodeList.getLength();
                }
            }
        }
        for (int i = 0; i < allStringNodeList.getLength(); i++) {
            Node node = allStringNodeList.item(i);
            // 替换&字符为&amp;,因为在xml里&符号默认为&amp;
            if (node.getTextContent().contains("&")) {
                String replaceResultString = node.getTextContent().replaceAll("&", "&amp;");
                System.out.println("replace original string ->" + node.getTextContent());
                node.setTextContent(replaceResultString);
                System.out.println("replace result string ->" + replaceResultString);
            }
            if (!isUnusedXmlStringIndex(unUsedStringIndexList, i)) {
                filterStringList.add(node);
            }
        }
        String begin = "<?xml version=\"1.0\" encoding=\"utf-8\"?><resources>";
        writeToStringXmlFile(false, begin, filename);
        for (int i = 0; i < filterStringList.size(); i++)
        {
            element = (Element) filterStringList.get(i);
            String content = "<string name=\"" + element.getAttribute("name") + "\">"
                    + element.getTextContent() + "</string>";
            writeToStringXmlFile(true, content, filename);
        }
        String endString = "</resources>";
        writeToStringXmlFile(true, endString, filename);
    }

    private static void writeToStringXmlFile(boolean append, String content, String filename) {
        FileOutputStream outputStream = null;
        String finalString = content + "\r\n";
        try {
            byte[] bytes = finalString.getBytes(ENCODING);
            outputStream = new FileOutputStream(filename, append);
            // write a byte sequence
            outputStream.write(bytes);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private static boolean isUnusedXmlStringIndex(ArrayList<Integer> arrayList, int position) {
        for (int i = 0; i < arrayList.size(); i++) {
            if (position == arrayList.get(i)) {
                ++REALY_DELETE_XML_STRING_LENGTH;
                return true;
            }
        }
        return false;
    }
}
