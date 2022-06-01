package com.ranranx.aolie.common.tools;

import com.ranranx.aolie.common.types.CommonUtils;
import com.ranranx.aolie.common.exceptions.InvalidConfigException;
import com.ranranx.aolie.common.exceptions.InvalidParamException;
import com.ranranx.aolie.common.exceptions.NotExistException;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Sql语句加载器，
 * 写SQL语句时，可以使用参数，例 #{versionCode} ，则需要在后面的参数中增加此值
 * @author xxl
 * @version V0.0.1
 * @date 2021/1/7 0007 20:39
 **/
@Component
public class SqlLoader {

    private static final String SQL_PACKAGE = "sqltemplate";
    private static Map<String, List<String>> mapSql = new HashMap<>();

    public static String getSql(String name) {
        if (CommonUtils.isEmpty(name)) {
            throw new InvalidParamException("没有指定语句名称");
        }
        List<String> sql = mapSql.get(name);
        if (CommonUtils.isEmpty(sql)) {
            throw new NotExistException("指定语句不存在:" + name);
        }

        return sql.get(0);
    }

    public static List<String> getSqls(String name) {
        if (CommonUtils.isEmpty(name)) {
            throw new InvalidParamException("没有指定语句名称");
        }
        List<String> sql = mapSql.get(name);
        if (CommonUtils.isEmpty(sql)) {
            throw new NotExistException("指定语句不存在:" + name);
        }

        return sql;
    }

    @PostConstruct
    public void scanSql() {
        System.out.println("-->scan Sql");
        ClassScannerUtils classScannerUtils = new ClassScannerUtils();
        Map<String, List<String>> map = classScannerUtils.searchClasses(SQL_PACKAGE);
        if (map != null) {
            mapSql.putAll(map);
        }
        System.out.println("-->scan Sql end");
    }
}


interface Scan {

    String SQL_SUFFIX = ".sql";

    void search(String packageName, Map<String, List<String>> sql);

    default void parseSql(BufferedReader bufferedReader, Map<String, List<String>> mapSql) {
        try {
            String line = null;
            StringBuilder builder = new StringBuilder();
            String lastName = null;
            while (true) {
                line = bufferedReader.readLine();
                //如果结束了,则保存最后一个数据
                if (line == null) {
                    if (lastName != null && builder.toString().trim().length() > 0) {
                        List<String> lstSql = mapSql.get(lastName);
                        if (lstSql == null) {
                            lstSql = new ArrayList<>();
                            mapSql.put(lastName, lstSql);
                        }
                        lstSql.add(builder.toString());
                    }
                    return;
                }
                if (line.trim().startsWith("--") || line.trim().startsWith("//")) {
                    continue;
                }
                //表示名称行
                if (line.trim().endsWith(":")) {
                    //开始新的语句
                    builder = new StringBuilder();
                    line = line.substring(0, line.length() - 1).trim();
                    lastName = line;
                    if (mapSql.containsKey(line)) {
                        throw new InvalidConfigException("SQL语句名重复:" + line);
                    }
                } else if (line.trim().endsWith(";")) {
                    //如果有结束标记
                    String sql = builder.append(line).toString();

                    sql = sql.substring(0, sql.length() - 1);

                    List<String> lstSql = mapSql.get(lastName);
                    if (lstSql == null) {
                        lstSql = new ArrayList<>();
                        mapSql.put(lastName, lstSql);
                    }
                    lstSql.add(sql);
                    builder = new StringBuilder();
                } else {
                    //以下语句行
                    //如果还没有名称的,是舍弃
                    if (lastName == null) {
                        continue;
                    }
                    //这里每一行用空格 代替
                    builder.append(line)
                            .append(" ");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


class FileScanner implements Scan {

    private String defaultClassPath = FileScanner.class.getResource("/").getPath();

    public String getDefaultClassPath() {
        return defaultClassPath;
    }

    public void setDefaultClassPath(String defaultClassPath) {
        this.defaultClassPath = defaultClassPath;
    }

    public FileScanner(String defaultClassPath) {
        this.defaultClassPath = defaultClassPath;
    }

    public FileScanner() {
    }


    @Override
    public void search(String packageName, Map<String, List<String>> mapSql) {
        //先把包名转换为路径,首先得到项目的classpath
        String classpath = defaultClassPath;
        //然后把我们的包名basPack转换为路径名

        String searchPath = classpath;
        new ClassSearcher().doPath(new File(searchPath), packageName, true, mapSql);
    }

    public class ClassSearcher {
        String SQL_SUFFIX = ".sql";

        public void doPath(File file, String packageName, boolean flag, Map<String, List<String>> mapSql) {

            if (file.isDirectory()) {
                //文件夹我们就递归
                File[] files = file.listFiles();
                for (File f1 : files) {
                    doPath(f1, packageName, false, mapSql);
                }
            } else {
                if (file.getName().endsWith(SQL_SUFFIX)
                        && file.getPath().endsWith(File.separator + packageName + File.separator + file.getName())) {
                    try {
                        System.out.println("--->load:" + file.getPath());
                        FileReader fileReader = new FileReader(file);
                        BufferedReader bufferedReader = new BufferedReader(fileReader);
                        parseSql(bufferedReader, mapSql);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
            return;
        }
    }

}

class JarScanner implements Scan {

    @Override
    public void search(String packageName, Map<String, List<String>> mapSql) {
        try {
            //通过当前线程得到类加载器从而得到URL的枚举
            Enumeration<URL> urlEnumeration = Thread.currentThread().getContextClassLoader().getResources(packageName.replace(".", "/"));
            while (urlEnumeration.hasMoreElements()) {
                URL url = urlEnumeration.nextElement();//得到的结果大概是：jar:file:/C:/Users/ibm/.m2/repository/junit/junit/4.12/junit-4.12.jar!/org/junit
                String protocol = url.getProtocol();//大概是jar
                if ("jar".equalsIgnoreCase(protocol)) {
                    //转换为JarURLConnection
                    JarURLConnection connection = (JarURLConnection) url.openConnection();
                    if (connection != null) {
                        JarFile jarFile = connection.getJarFile();
                        if (jarFile != null) {
                            //得到该jar文件下面的类实体
                            Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
                            while (jarEntryEnumeration.hasMoreElements()) {
                            /*entry的结果大概是这样：
                                    org/
                                    org/junit/
                                    org/junit/rules/
                                    org/junit/runners/*/
                                JarEntry entry = jarEntryEnumeration.nextElement();
                                String jarEntryName = entry.getName();

                                //这里我们需要过滤不是class文件和不在basePack包名下的类
                                if (jarEntryName.contains(".sql") && jarEntryName.indexOf(packageName) != -1) {
                                    System.out.println("--->load:" + jarEntryName);
                                    InputStreamReader inputStreamReader;
                                    InputStream resourceAsStream = this.getClass().getResourceAsStream(jarEntryName);
                                    if (resourceAsStream == null) {
                                        inputStreamReader = new InputStreamReader(jarFile.getInputStream(entry));
                                    } else {
                                        inputStreamReader = new InputStreamReader(resourceAsStream);
                                    }
                                    BufferedReader reader = new BufferedReader(inputStreamReader);
                                    parseSql(reader, mapSql);
                                }
                            }
                        }
                    }
                } else if ("file".equalsIgnoreCase(protocol)) {
                    //从maven子项目中扫描
                    FileScanner fileScanner = new FileScanner();
                    fileScanner.setDefaultClassPath(url.getPath().replace(packageName.replace(".", "/"), ""));
                    fileScanner.search(packageName, mapSql);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ScanExecutor implements Scan {

    private volatile ScanExecutor instance;

    @Override
    public void search(String packageName, Map<String, List<String>> mapSql) {
//        Scan fileSc = new FileScanner();
//        fileSc.search(packageName, mapSql);
        Scan jarScanner = new JarScanner();
        jarScanner.search(packageName, mapSql);
    }

    public ScanExecutor() {
    }

    public ScanExecutor getInstance() {
        if (instance == null) {
            synchronized (ScanExecutor.class) {
                if (instance == null) {
                    instance = new ScanExecutor();
                }
            }
        }
        return instance;
    }

}


class ClassScannerUtils {
    public Map<String, List<String>> searchClasses(String packageName) {
        Map<String, List<String>> mapSql = new HashMap<>();
        new ScanExecutor().search(packageName, mapSql);
        return mapSql;
    }

}

