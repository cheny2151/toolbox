package cn.cheny.toolbox.scan;

import cn.cheny.toolbox.scan.asm.AnnotationDesc;
import cn.cheny.toolbox.scan.asm.MethodDesc;
import cn.cheny.toolbox.scan.asm.visitor.ClassFilterVisitor;
import cn.cheny.toolbox.scan.asm.visitor.MethodAnnotationFilterVisitor;
import cn.cheny.toolbox.scan.filter.FilterResult;
import cn.cheny.toolbox.scan.filter.ScanFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassReader;

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
import java.util.stream.Collectors;

/**
 * 扫描包下指定的class
 *
 * @author cheney
 * @date 2019/6/26
 */
@Slf4j
public class PathScanner {

    /**
     * 空路径
     */
    public final static String EMPTY_PATH = "";

    /**
     * package分隔符
     */
    public final static String PACKAGE_SEPARATE_CHARACTER = ".";

    /**
     * url分隔符
     */
    public final static String URL_SEPARATE_CHARACTER = "/";

    /**
     * class文件拓展名
     */
    public final static String CLASS_EXTENSION = ".class";

    /**
     * file
     */
    private static final String URL_PROTOCOL_FILE = "file";

    /**
     * jar
     */
    private static final String URL_PROTOCOL_JAR = "jar";

    /**
     * file url pre
     */
    private static final String FILE_URL_PREFIX = "file:";

    /**
     * jar url pre
     */
    private static final String JAR_URL_PREFIX = "jar:";

    /**
     * jar extension
     */
    private static final String JAR_FILE_EXTENSION = ".jar";

    /**
     * JAR ENTRY PRE
     */
    private static final String JAR_URL_ENTRY_PRE = "!/";

    /**
     * jar extension & ENTRY PRE length
     */
    private static final int JAR_TAIL_LEN = JAR_FILE_EXTENSION.length() + JAR_URL_ENTRY_PRE.length();

    /**
     * maven build jar: BOOT-INF url pre
     */
    private static final String BOOT_INF_URL_PRE = "BOOT-INF/classes/";

    /**
     * maven build jar: META-INF pre
     */
    private static final String META_INF_URL_PRE = "META-INF/classes/";

    /**
     * module-info.class
     */
    private static final String MODULE_INFO = "module-info.class";

    /**
     * .class长度
     */
    private static final int CLASS_END_LEN = 6;

    /**
     * 系统文件分隔符
     */
    private final static String PATH_SEPARATE = System.getProperty("path.separator");

    /**
     * 是否扫描第三方jar包
     */
    private boolean scanAllJar = false;

    /**
     * 判断是否加载某个jar包
     */
    private IsLoadingJar isLoadingJar;

    /**
     * 过滤器
     */
    private ScanFilter scanFilter;

    /**
     * classLoader
     */
    private ClassLoader classLoader;

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
    public List<ClassResource> scanClassResource(String scanPath) throws ScanException {
        String scanPath0 = PACKAGE_SEPARATE_CHARACTER.equals(scanPath) ? EMPTY_PATH : scanPath;
        String resourcePath = scanPath0.replaceAll("\\.", URL_SEPARATE_CHARACTER);
        if (resourcePath.startsWith(URL_SEPARATE_CHARACTER)) {
            resourcePath = resourcePath.substring(1);
        }
        String targetUrl = extractEffectiveUrl(resourcePath);

        Collection<URL> resources = getResources(resourcePath);
        if (log.isDebugEnabled()) {
            log.debug("resource path:{},result urls:{}", resourcePath, resources);
        }

        List<ClassResource> results = new ArrayList<>();
        for (URL resource : resources) {
            String protocol = resource.getProtocol();
            if (log.isDebugEnabled()) {
                log.debug("file protocol:{}", protocol);
            }

            if (URL_PROTOCOL_FILE.equals(protocol)) {
                String file = resource.getFile();
                scanClassInFile(targetUrl, new File(file), results);
            } else if (URL_PROTOCOL_JAR.equals(protocol)) {
                scanClassInJar(targetUrl, resource, results);
            }
        }

        return results;
    }

    public List<Class<?>> scanClass(String scanPath) throws ScanException {
        return scanClassResource(scanPath).stream()
                .map(ClassResource::getClazz)
                .collect(Collectors.toList());
    }

    /**
     * 从本地File中扫描所有类
     *
     * @param targetUrl 目标目录,以'/'为分隔符
     * @param file      文件
     * @param result    扫描结果集
     */
    private void scanClassInFile(String targetUrl, File file, List<ClassResource> result) throws ScanException {
        // 结尾补'/'
        String parentUrl = targetUrl;
        if (!StringUtils.isEmpty(parentUrl)) {
            parentUrl = parentUrl + URL_SEPARATE_CHARACTER;
        }
        List<File> effectiveFiles = new ArrayList<>();
        if (file.isFile() && file.getName().endsWith(CLASS_EXTENSION)) {
            effectiveFiles.add(file);
        }
        effectiveFiles.addAll(getEffectiveChildFiles(file));
        for (File child : effectiveFiles) {
            loadResourcesInFile(targetUrl, parentUrl, child, result);
        }
    }

    /**
     * 从本地File中扫描所有类
     *
     * @param targetUrl 目标目录,以'/'为分隔符
     * @param parentUrl 上级目录,以'/'为分隔符
     * @param file      文件
     * @param result    扫描结果集
     */
    private void loadResourcesInFile(String targetUrl, String parentUrl, File file, List<ClassResource> result) throws ScanException {
        if (BOOT_INF_URL_PRE.equals(parentUrl) || META_INF_URL_PRE.equals(parentUrl)) {
            return;
        }
        if (file.getName().endsWith(JAR_FILE_EXTENSION)) {
            if (isScanAllJar()) {
                try {
                    scanClassInJar(targetUrl, new URL(FILE_URL_PREFIX + file.getPath()), result);
                } catch (MalformedURLException e) {
                    // do nothing
                }
            }
        } else if (file.isFile()) {
            String ClassFileUrl = parentUrl + file.getName();
            filterClass(ClassFileUrl, result);
        } else if (file.isDirectory()) {
            List<File> childFiles = getEffectiveChildFiles(file);
            if (childFiles.size() == 0) {
                return;
            }
            String nextScanPath = getNextScanPath(parentUrl, file);
            for (File child : childFiles) {
                loadResourcesInFile(targetUrl, nextScanPath, child, result);
            }
        }
    }

    /**
     * 获取下个包路径
     *
     * @param parentPath 上级目录,以'/'为分隔符
     * @param directory  扫描的当前目录
     * @return 下一个包路径
     */
    private String getNextScanPath(String parentPath, File directory) {
        return parentPath + directory.getName() + URL_SEPARATE_CHARACTER;
    }

    /**
     * 从本地jar包中扫描所有类
     *
     * @param targetUrl 目标目录,以'/'为分隔符
     * @param url       jar包的url资源
     * @param result    扫描结果集
     */
    private void scanClassInJar(String targetUrl, URL url, List<ClassResource> result)
            throws ScanException {
        JarUrl jarUrl = extractRealJarUrl(targetUrl, url);
        if (jarUrl.getFirstJar() == null || !loadingJar(jarUrl)
                || shouldNotScan(targetUrl, jarUrl)) {
            return;
        } else if (!PackageUtils.isJar(jarUrl.getFirstJar())) {
            if (log.isDebugEnabled()) {
                log.debug("url:{} not a jar", url);
            }
            return;
        }
        try {
            loadResourcesInJar(jarUrl, result);
        } catch (IOException e) {
            throw new ScanException("加载资源异常", e);
        }
    }

    /**
     * 判断是否不应该扫描
     *
     * @param targetUrl 目标目录
     * @param jarUrl    jar url
     * @return 不需要扫描返回false
     */
    private boolean shouldNotScan(String targetUrl, JarUrl jarUrl) {
        if (StringUtils.isNotEmpty(targetUrl)) {
            return false;
        }
        // 存在二级jar并且不扫描所有jar包
        return jarUrl.getNextJar() != null && !isScanAllJar();
    }

    private boolean loadingJar(JarUrl jarUrl) {
        return this.isLoadingJar == null ||
                this.isLoadingJar.isLoading(new IsLoadingJar.JarInfo(extractJarName(jarUrl.getOrigin()), jarUrl.getOrigin()));
    }

    private String extractJarName(URL url) {
        String path = url.getPath();
        String[] split = path.substring(0, path.length() - JAR_TAIL_LEN).split(URL_SEPARATE_CHARACTER);
        return split[split.length - 1];
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
        ClassLoader classLoader = getClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources(resourcePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                addIfUseful(url, urls);
            }
            if (EMPTY_PATH.equals(resourcePath) && scanAllJar) {
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
     * @param targetUrl 目标目录
     * @param url       原url
     * @return jar url
     */
    private JarUrl extractRealJarUrl(String targetUrl, URL url) {
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
        }
        jarUrl.setTargetUrl(targetUrl);
        return jarUrl;
    }

    /**
     * 从jarInputStream流中提取有效的类并添加到result中
     *
     * @param jarUrl JAR包url信息
     * @param result 结果合集
     * @throws IOException IO异常
     */
    private void loadResourcesInJar(JarUrl jarUrl, List<ClassResource> result)
            throws IOException {
        URL firstJar = jarUrl.getFirstJar();
        try (JarFile jarFile = new JarFile(new File(firstJar.getFile()))) {
            String targetUrl = jarUrl.getTargetUrl();
            if (jarUrl.getNextJar() != null) {
                JarEntry targetEntry = jarFile.getJarEntry(jarUrl.getNextJar());
                try (InputStream inputStream = jarFile.getInputStream(targetEntry);
                     JarInputStream innerJarInputStream = new JarInputStream(inputStream)) {
                    JarEntry nextJarEntry;
                    while ((nextJarEntry = innerJarInputStream.getNextJarEntry()) != null) {
                        addIfTargetCLass(nextJarEntry, targetUrl, result);
                    }
                }
            } else {
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry nextJarEntry = entries.nextElement();
                    addIfTargetCLass(nextJarEntry, targetUrl, result);
                }
            }
        } catch (Exception e) {
            log.error("加载jar包异常:{}", e.getMessage());
        }
    }

    /**
     * 检查jarEntry是否有效，是否为目标包，若是则添加到结果集合
     *
     * @param jarEntry  jar entry
     * @param targetUrl 目标目录
     * @param result    结果集合
     */
    private void addIfTargetCLass(JarEntry jarEntry, String targetUrl, List<ClassResource> result) {
        if (jarEntry.isDirectory()) {
            return;
        }
        String name = jarEntry.getName();
        if (name.startsWith(URL_SEPARATE_CHARACTER)) {
            name = name.substring(1);
        }
        if (name.startsWith(BOOT_INF_URL_PRE)) {
            name = name.substring(BOOT_INF_URL_PRE.length());
        }
        if (name.startsWith(META_INF_URL_PRE)) {
            name = name.substring(META_INF_URL_PRE.length());
        }
        if (name.endsWith(CLASS_EXTENSION) &&
                (StringUtils.isEmpty(targetUrl) || name.startsWith(targetUrl))) {
            filterClass(name, result);
        }
    }

    /**
     * 过滤有效类添加到result中
     *
     * @param ClassFileUrl class文件url
     * @param result       扫描结果集合
     */
    private void filterClass(String ClassFileUrl, List<ClassResource> result) {
        if (ClassFileUrl.endsWith(MODULE_INFO)) {
            return;
        }
        ScanFilter scanFilter = this.scanFilter;
        FilterResult filterResult = null;
        if (scanFilter != null && !(filterResult = filterByAsm(ClassFileUrl)).isPass()) {
            return;
        }
        Class<?> targetClass;
        ClassResource target;
        try {
            String fullClassName = PackageUtils.replaceUrlToPackage(ClassFileUrl.substring(0, ClassFileUrl.length() - CLASS_END_LEN));
            targetClass = loadClass(fullClassName);
            target = new ClassResource(targetClass);
            if (filterResult != null) {
                Map<MethodDesc, List<AnnotationDesc>> annotationDescMap = filterResult.getAnnotationDescMap();
                target.setAnnotationDesc(annotationDescMap);
            }
        } catch (NoClassDefFoundError e) {
            if (log.isDebugEnabled()) {
                log.debug("can not find class def,name:{}", ClassFileUrl);
            }
            return;
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("can not find class,name:{}", ClassFileUrl);
            }
            return;
        } catch (Throwable e) {
            return;
        }
        if (scanFilter != null) {
            // 二次校验
            Class<?> superClass = scanFilter.getSuperClass();
            if (superClass != null &&
                    (!superClass.isAssignableFrom(targetClass) ||
                            superClass.equals(targetClass))) {
                return;
            }
            List<Class<? extends Annotation>> hasAnnotations = scanFilter.getHasAnnotations();
            if (hasAnnotations != null && !hasAnnotations.isEmpty()) {
                boolean hasAll = hasAnnotations.stream().allMatch(a -> targetClass.getAnnotation(a) != null);
                if (!hasAll) {
                    return;
                }
            }
        }
        result.add(target);
    }

    /**
     * 通过asm检测Filter，若不匹配返回false
     * 注意：无法校验是否是通过ClassLoader加载
     *
     * @param classFileUrl class文件url
     * @return 是否匹配
     */
    private FilterResult filterByAsm(String classFileUrl) {
        try {
            URL resource = getClassLoader().getResource(classFileUrl);
            if (resource != null) {
                ClassFilterVisitor classVisitor = getClassVisitor();
                new ClassReader(resource.openStream()).accept(classVisitor, ClassReader.SKIP_CODE);
                return classVisitor.getFilterResult();
            } else if (log.isDebugEnabled()) {
                log.debug("can not filter By Asm,name:{},cause:url resource is null", classFileUrl);
            }
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("can not filter By Asm,name:{},cause:{}", classFileUrl, t.getClass().getName());
            }
        }
        return new FilterResult(false);
    }

    private ClassFilterVisitor getClassVisitor() {
        ScanFilter scanFilter = this.scanFilter;
        return CollectionUtils.isNotEmpty(scanFilter.getExistMethodAnnotations()) ?
                new MethodAnnotationFilterVisitor(scanFilter) :
                new ClassFilterVisitor(scanFilter);
    }

    /**
     * 返回有效的子目录或文件或jar包
     *
     * @param cur 当前目录
     * @return 有效的文件实体集合
     */
    private List<File> getEffectiveChildFiles(File cur) {
        File[] files = cur.listFiles((childFile) ->
                childFile.isDirectory() || childFile.getName().endsWith(CLASS_EXTENSION)
                        || childFile.getName().endsWith(JAR_FILE_EXTENSION)
        );
        return files == null ? Collections.emptyList() : Arrays.asList(files);
    }

    /**
     * 提取有效路径，若最终结尾存在'/'则删除
     *
     * @param scanPath 扫描的路径
     * @return 合法的扫描路径
     */
    private String extractEffectiveUrl(String scanPath) {
        int length = scanPath.length();
        if (length > 0 && scanPath.endsWith(URL_SEPARATE_CHARACTER)) {
            return scanPath.substring(0, length - 1);
        }
        return scanPath;
    }

    private Class<?> loadClass(String fullClassName) throws ClassNotFoundException {
        return Thread.currentThread().getContextClassLoader().loadClass(fullClassName);
    }

    /**
     * 获取jar urls
     *
     * @throws MalformedURLException the exception while new a URL
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
                    addIfUseful(url, results);
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

    private void addIfUseful(URL url, Set<URL> results) {
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
     * @throws MalformedURLException the exception while new a URL
     */
    private void getJarURLInClassPath(Set<URL> results) throws MalformedURLException {
        String classPath = System.getProperty("java.class.path");
        if (StringUtils.isEmpty(classPath)) {
            return;
        }
        String[] classPaths = classPath.split(PATH_SEPARATE);
        for (String path : classPaths) {
            if (path.endsWith(URL_PROTOCOL_JAR)) {
                URL url = new URL(JAR_URL_PREFIX + FILE_URL_PREFIX + path + JAR_URL_ENTRY_PRE);
                results.add(url);
            }
        }
    }


    public boolean isScanAllJar() {
        return scanAllJar;
    }

    public PathScanner scanAllJar(boolean scanAllJar) {
        this.scanAllJar = scanAllJar;
        return this;
    }

    public PathScanner isLoadingJar(IsLoadingJar isLoadingJar) {
        this.isLoadingJar = isLoadingJar;
        return this;
    }

    private ClassLoader getClassLoader() {
        return this.classLoader == null ?
                Thread.currentThread().getContextClassLoader() :
                this.classLoader;
    }

    public PathScanner classloader(ClassLoader classLoader) {
        this.classLoader = classLoader;
        return this;
    }

    @Data
    public static class JarUrl {
        private URL origin;
        private URL firstJar;
        private String nextJar;
        private String targetUrl;

        public JarUrl(URL origin) {
            this.origin = origin;
        }
    }

}
