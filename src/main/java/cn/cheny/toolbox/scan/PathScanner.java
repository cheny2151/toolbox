package cn.cheny.toolbox.scan;

import cn.cheny.toolbox.scan.filter.ScanFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * 扫描包下指定的class
 *
 * @author cheney
 * @date 2019/6/26
 */
@Slf4j
public class PathScanner {

    // class文件拓展名
    private final static String CLASS_EXTENSION = ".class";

    // file
    private static final String URL_PROTOCOL_FILE = "file";

    // jar
    private static final String URL_PROTOCOL_JAR = "jar";

    // file url pre
    private static final String FILE_URL_PREFIX = "file:";

    // jar url pre
    private static final String JAR_URL_PREFIX = "jar:";

    // jar extension
    private static final String JAR_FILE_EXTENSION = ".jar";

    // JAR ENTRY PRE
    private static final String JAR_URL_ENTRY_PRE = "!/";

    // .class长度
    private static final int CLASS_END_LEN = 6;

    // 空路径
    private final static String EMPTY_PATH = "";

    // 分隔符
    private final static String SEPARATE_CHARACTER = ".";

    // 是否扫描class path声明的jar
    private boolean findInClassPathJar = false;

    // 判断是否加载某个jar包
    private IsLoadingJar isLoadingJar;

    // 过滤器
    private ScanFilter scanFilter;

    public PathScanner() {
    }

    public PathScanner(ScanFilter scanFilter) {
        this.scanFilter = scanFilter;
    }

    /**
     * 扫描项目中指定包名的所有类
     * 例如 expression.cheney
     * 使用"","."扫描根，即项目中所有类
     *
     * @param scanPath 包路径,以'.'或者'/'为分隔符
     * @return 扫描到的Class
     */
    public List<Class<?>> scanClass(String scanPath) throws ScanException {
        String scanPath0 = SEPARATE_CHARACTER.equals(scanPath) ? EMPTY_PATH : scanPath;
        String resourcePath = scanPath0.replaceAll("\\.", "/");
        if (resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }
        String parentPath = extractEffectivePath(scanPath0);
        if (parentPath.startsWith(".")) {
            parentPath = parentPath.substring(1);
        }

        Collection<URL> resources = getResources(resourcePath);
        List<Class<?>> results = new ArrayList<>();

        for (URL resource : resources) {
            String protocol = resource.getProtocol();
            if (log.isDebugEnabled()) {
                log.debug("file protocol:{}", protocol);
            }

            if (URL_PROTOCOL_FILE.equals(protocol)) {
                String file = resource.getFile();
                scanClassInFile(parentPath, new File(file), results);
            } else if (URL_PROTOCOL_JAR.equals(protocol)) {
                scanClassInJar(parentPath, resource, results);
            }
        }

        return results;
    }

    /**
     * 从本地File中扫描所有类
     *
     * @param parentPath 上级目录,以'.'为分隔符
     * @param file       文件
     * @param result     扫描结果集
     */
    private void scanClassInFile(String parentPath, File file, List<Class<?>> result) {
        // 结尾补'.'
        if (!StringUtils.isEmpty(parentPath)) {
            parentPath = parentPath + SEPARATE_CHARACTER;
        }
        List<File> effectiveFiles = new ArrayList<>();
        if (file.isFile() && file.getName().endsWith(CLASS_EXTENSION)) {
            effectiveFiles.add(file);
        }
        effectiveFiles.addAll(getEffectiveChildFiles(file));
        for (File child : effectiveFiles) {
            loadResourcesInFile(parentPath, child, result);
        }
    }

    /**
     * 从本地File中扫描所有类
     *
     * @param parentPath 上级目录,以'.'为分隔符
     * @param file       文件
     * @param result     扫描结果集
     */
    private void loadResourcesInFile(String parentPath, File file, List<Class<?>> result) {
        if (file.isFile()) {
            String ClassFileName = parentPath + file.getName();
            filterClass(ClassFileName, result);
        } else if (file.isDirectory()) {
            List<File> childFiles = getEffectiveChildFiles(file);
            if (childFiles.size() == 0) {
                return;
            }
            String nextScanPath = getNextScanPath(parentPath, file);
            for (File child : childFiles) {
                loadResourcesInFile(nextScanPath, child, result);
            }
        }
    }

    /**
     * 获取下个包路径
     *
     * @param parentPath 上级目录,以'.'为分隔符
     * @param directory  扫描的当前目录
     * @return 下一个包路径
     */
    private String getNextScanPath(String parentPath, File directory) {
        return parentPath + directory.getName() + SEPARATE_CHARACTER;
    }

    /**
     * 从本地jar包中扫描所有类
     *
     * @param parentPath 上级目录,以'.'为分隔符
     * @param url        jar包的url资源
     * @param result     扫描结果集
     */
    private void scanClassInJar(String parentPath, URL url, List<Class<?>> result)
            throws ScanException {
        JarUrl jarUrl = extractRealJarUrl(url);
        if (jarUrl.getFirstJar() == null || !loadingJar(jarUrl)) {
            return;
        } else if (!isJar(jarUrl.getFirstJar())) {
            if (log.isDebugEnabled()) {
                log.debug("url:{} not a jar", url);
            }
            return;
        }
        try {
            loadResourcesInJar(parentPath, jarUrl, result);
        } catch (IOException e) {
            throw new ScanException("加载资源异常", e);
        }
    }

    private boolean loadingJar(JarUrl jarUrl) {
        return this.isLoadingJar == null || this.isLoadingJar.isLoading(jarUrl);
    }

    /**
     * 从资源路径中获取URL实例
     *
     * @param resourcePath 资源路径
     * @return 资源URL
     * @throws ScanException 扫描异常
     */
    private Collection<URL> getResources(String resourcePath) throws ScanException {
        Set<URL> urls = new LinkedHashSet<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            if (EMPTY_PATH.equals(resourcePath)) {
                Enumeration<URL> resources = classLoader.getResources(resourcePath);
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    addIfUsefulRoot(url, urls);
                }
            } else {
                URL url = classLoader.getResource(resourcePath);
                if (url != null) {
                    urls.add(url);
                }
            }
            if (EMPTY_PATH.equals(resourcePath) && findInClassPathJar) {
                getJarUrls(classLoader, urls);
            }
        } catch (Exception e) {
            throw new ScanException("获取资源失败", e);
        }
        return urls;
    }

    /**
     * 提取有效jar包URL
     *
     * @param url 原url
     * @return jar url
     */
    private JarUrl extractRealJarUrl(URL url) {
        String fullUrl = url.toExternalForm();
        int startIndex = fullUrl.startsWith(JAR_URL_PREFIX) ? 4 : 0;
        int firstJarIdx = fullUrl.indexOf(JAR_FILE_EXTENSION) + JAR_FILE_EXTENSION.length();
        JarUrl jarUrl = new JarUrl(url);
        try {
            String firstUrl = fullUrl.substring(startIndex, firstJarIdx);
            jarUrl.setFirstJar(new URL(firstUrl));
        } catch (MalformedURLException e) {
            return jarUrl;
        }
        String nextUrl = fullUrl.substring(firstJarIdx);
        int nextJarIdx = nextUrl.indexOf(JAR_FILE_EXTENSION);
        if (nextJarIdx > 0) {
            int nextJarEnd = nextJarIdx + JAR_FILE_EXTENSION.length();
            String nextJar = nextUrl.substring(0, nextJarEnd);
            if (nextJar.startsWith(JAR_URL_ENTRY_PRE)) {
                nextJar = nextJar.substring(2);
            }
            jarUrl.setNextJar(nextJar);
            nextUrl = nextUrl.substring(nextJarEnd);
        }
        if (nextUrl.startsWith(JAR_URL_ENTRY_PRE)) {
            nextUrl = nextUrl.substring(2);
        }
        jarUrl.setPath(nextUrl);
        return jarUrl;
    }

    /**
     * 从jarInputStream流中提取有效的类并添加到result中
     *
     * @param parentPath 上级目录,以'.'为分隔符
     * @param jarUrl     JAR包url信息
     * @param result     结果合集
     * @throws IOException IO异常
     */
    private void loadResourcesInJar(String parentPath, JarUrl jarUrl, List<Class<?>> result)
            throws IOException {
        URL firstJar = jarUrl.getFirstJar();
        JarFile jarFile = new JarFile(new File(firstJar.getFile()));
        String jarUrlPath = jarUrl.getPath();
        if (jarUrl.getNextJar() != null) {
            JarEntry targetEntry = jarFile.getJarEntry(jarUrl.getNextJar());
            InputStream inputStream = jarFile.getInputStream(targetEntry);
            JarInputStream innerJarInputStream = new JarInputStream(inputStream);
            JarEntry nextJarEntry;
            while ((nextJarEntry = innerJarInputStream.getNextJarEntry()) != null) {
                addIfTargetCLass(nextJarEntry, jarUrlPath, parentPath, result);
            }
        } else {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry nextJarEntry = entries.nextElement();
                addIfTargetCLass(nextJarEntry, jarUrlPath, parentPath, result);
            }
        }
    }

    /**
     * 检查jarEntry是否有效，是否为目标包，若是则添加到结果集合
     *
     * @param jarEntry   jar entry
     * @param jarUrlPath 目标jar url的结尾包path
     * @param parentPath 上级目录
     * @param result     结果集合
     */
    private void addIfTargetCLass(JarEntry jarEntry, String jarUrlPath, String parentPath, List<Class<?>> result) {
        String name = jarEntry.getName();
        if (!jarEntry.isDirectory() && (StringUtils.isEmpty(jarUrlPath) || name.startsWith(jarUrlPath))) {
            String classFileName = name.replaceAll("/", ".");
            if (classFileName.startsWith(parentPath) && classFileName.endsWith(CLASS_EXTENSION)) {
                filterClass(classFileName, result);
            }
        }
    }

    /**
     * 过滤有效类添加到result中
     *
     * @param ClassFileName 文件名
     * @param result        扫描结果集合
     */
    private void filterClass(String ClassFileName, List<Class<?>> result) {
        Class<?> target;
        try {
            String fullClassName = ClassFileName.substring(0, ClassFileName.length() - CLASS_END_LEN);
            target = loadClass(fullClassName);
        } catch (NoClassDefFoundError e) {
            if (log.isDebugEnabled()) {
                log.debug("can not find class def,name:{}", ClassFileName);
            }
            return;
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("can not find class,name:{}", ClassFileName);
            }
            return;
        } catch (Throwable e) {
            return;
        }
        if (scanFilter != null) {
            Class<?> superClass = scanFilter.getSuperClass();
            if (superClass != null &&
                    (!superClass.isAssignableFrom(target) ||
                            superClass.equals(target))) {
                return;
            }
            List<Class<? extends Annotation>> hasAnnotations = scanFilter.getHasAnnotations();
            if (hasAnnotations != null && !hasAnnotations.isEmpty()) {
                boolean hasAll = hasAnnotations.stream().allMatch(a -> target.getAnnotation(a) != null);
                if (!hasAll) {
                    return;
                }
            }
        }
        result.add(target);
    }

    /**
     * 返回有效的子目录或文件
     *
     * @param cur 当前目录
     * @return 有效的文件实体集合
     */
    private List<File> getEffectiveChildFiles(File cur) {
        File[] files = cur.listFiles((childFile) ->
                childFile.isDirectory() || childFile.getName().endsWith(CLASS_EXTENSION)
        );
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    /**
     * 提取有效路径，若最终结尾存在'.'则删除
     *
     * @param scanPath 扫描的路径
     * @return 合法的扫描路径
     */
    private String extractEffectivePath(String scanPath) {
        StringBuilder pathBuilder = new StringBuilder(scanPath.replaceAll("/", "."));
        // 剔除最后一个有效'.'之后的path
        int length = pathBuilder.length();
        if (length > 0 && SEPARATE_CHARACTER.getBytes()[0] == pathBuilder.charAt(length - 1)) {
            pathBuilder.setLength(length - 1);
        }
        return pathBuilder.toString();
    }

    private Class<?> loadClass(String fullClassName) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(fullClassName);
    }

    /**
     * 获取jar urls
     *
     * @throws MalformedURLException
     */
    private void getJarUrls(ClassLoader classLoader, Set<URL> results) throws MalformedURLException {
        if (classLoader instanceof URLClassLoader) {
            URLClassLoader urlClassLoader = (URLClassLoader) classLoader;
            URL[] urLs = urlClassLoader.getURLs();
            for (URL url : urLs) {
                if (url.getProtocol().equals(URL_PROTOCOL_JAR)) {
                    results.add(url);
                } else if (url.getPath().endsWith(URL_PROTOCOL_JAR)) {
                    results.add(new URL(JAR_URL_PREFIX + url + JAR_URL_ENTRY_PRE));
                } else {
                    addIfUsefulRoot(url, results);
                }
            }
        }
        if (classLoader == ClassLoader.getSystemClassLoader()) {
            getJarURLInClassPath(results);
        }
        if (classLoader.getParent() != null) {
            getJarUrls(classLoader.getParent(), results);
        }
    }

    private void addIfUsefulRoot(URL url, Set<URL> results) {
        /*String path = url.getPath();
        String[] split = path.split(File.separator);
        if (split[split.length - 1].equals("classes")) {
        }*/
        results.add(url);
    }

    /**
     * 获取class path声明的jar
     *
     * @param results 扫描结果存放集合
     * @throws MalformedURLException
     */
    private void getJarURLInClassPath(Set<URL> results) throws MalformedURLException {
        String classPath = System.getProperty("java.class.path");
        if (StringUtils.isEmpty(classPath)) {
            return;
        }
        for (String path : classPath.split(":")) {
            if (path.endsWith(URL_PROTOCOL_JAR)) {
                URL url = new URL(JAR_URL_PREFIX + FILE_URL_PREFIX + path + JAR_URL_ENTRY_PRE);
                results.add(url);
            }
        }
    }

    /**
     * jar文件的magic头
     */
    private static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};

    /**
     * 判断url所对应的资源是否为jar包
     */
    protected boolean isJar(URL url) {
        return isJar(url, new byte[JAR_MAGIC.length]);
    }

    protected boolean isJar(URL url, byte[] buffer) {
        InputStream is = null;
        try {
            is = url.openStream();
            is.read(buffer, 0, JAR_MAGIC.length);
            if (Arrays.equals(buffer, JAR_MAGIC)) {
                if (log.isDebugEnabled()) {
                    log.debug("Found JAR: " + url);
                }
                return true;
            }
        } catch (Exception e) {
            // Failure to read the stream means this is not a JAR
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    // Ignore
                }
            }
        }

        return false;
    }

    public boolean isFindInClassPathJar() {
        return findInClassPathJar;
    }

    public PathScanner findInClassPathJar(boolean findInClassPathJar) {
        this.findInClassPathJar = findInClassPathJar;
        return this;
    }

    public PathScanner isLoadingJar(IsLoadingJar isLoadingJar) {
        this.isLoadingJar = isLoadingJar;
        return this;
    }

    @Data
    public static class JarUrl {
        private URL origin;
        private URL firstJar;
        private String nextJar;
        private String path;

        public JarUrl(URL origin) {
            this.origin = origin;
        }
    }
}
