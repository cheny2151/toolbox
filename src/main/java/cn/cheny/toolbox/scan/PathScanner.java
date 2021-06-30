package cn.cheny.toolbox.scan;

import cn.cheny.toolbox.scan.filter.ScanFilter;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
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

    /** class文件拓展名 */
    private final static String CLASS_EXTENSION = ".class";

    /** file */
    private static final String URL_PROTOCOL_FILE = "file";

    /** jar */
    private static final String URL_PROTOCOL_JAR = "jar";

    /** file url pre */
    private static final String FILE_URL_PREFIX = "file:";

    /** jar url pre */
    private static final String JAR_URL_PREFIX = "jar:";

    /** jar extension */
    private static final String JAR_FILE_EXTENSION = ".jar";

    /** JAR ENTRY PRE */
    private static final String JAR_URL_ENTRY_PRE = "!/";

    /** maven build jar: BOOT-INF url pre */
    private static final String BOOT_INF_URL_PRE = "BOOT-INF/classes/";

    /** maven build jar: META-INF pre */
    private static final String META_INF_URL_PRE = "META-INF/classes/";

    /** module-info.class */
    private static final String MODULE_INFO = "module-info.class";

    /** .class长度 */
    private static final int CLASS_END_LEN = 6;

    /** 空路径 */
    private final static String EMPTY_PATH = "";

    /** package分隔符 */
    private final static String PACKAGE_SEPARATE_CHARACTER = ".";

    /** url分隔符 */
    private final static String URL_SEPARATE_CHARACTER = "/";

    /** 系统文件分隔符 */
    private final static String PATH_SEPARATE = System.getProperty("path.separator");

    /** 是否扫描第三方jar包 */
    private boolean scanAllJar = false;

    /** 判断是否加载某个jar包 */
    private IsLoadingJar isLoadingJar;

    /** 过滤器 */
    private ScanFilter scanFilter;

    /** classLoader */
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
    public List<Class<?>> scanClass(String scanPath) throws ScanException {
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

        List<Class<?>> results = new ArrayList<>();
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

    /**
     * 从本地File中扫描所有类
     *
     * @param targetUrl 目标目录,以'/'为分隔符
     * @param file      文件
     * @param result    扫描结果集
     */
    private void scanClassInFile(String targetUrl, File file, List<Class<?>> result) throws ScanException {
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
    private void loadResourcesInFile(String targetUrl, String parentUrl, File file, List<Class<?>> result) throws ScanException {
        if (BOOT_INF_URL_PRE.equals(parentUrl) || META_INF_URL_PRE.equals(parentUrl)) {
            return;
        }
        if (file.getName().endsWith(JAR_FILE_EXTENSION) && isScanAllJar()) {
            try {
                scanClassInJar(targetUrl, new URL(FILE_URL_PREFIX + file.getPath()), result);
            } catch (MalformedURLException e) {
                // do nothing
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
    private void scanClassInJar(String targetUrl, URL url, List<Class<?>> result)
            throws ScanException {
        JarUrl jarUrl = extractRealJarUrl(targetUrl, url);
        if (jarUrl.getFirstJar() == null || !loadingJar(jarUrl)
                || shouldNotScan(targetUrl, jarUrl)) {
            return;
        } else if (!isJar(jarUrl.getFirstJar())) {
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
    private void loadResourcesInJar(JarUrl jarUrl, List<Class<?>> result)
            throws IOException {
        URL firstJar = jarUrl.getFirstJar();
        JarFile jarFile;
        try {
            jarFile = new JarFile(new File(firstJar.getFile()));
        } catch (Exception e) {
            log.error("加载jar包异常:{}", e.getMessage());
            return;
        }
        String targetUrl = jarUrl.getTargetUrl();
        if (jarUrl.getNextJar() != null) {
            JarEntry targetEntry = jarFile.getJarEntry(jarUrl.getNextJar());
            InputStream inputStream = jarFile.getInputStream(targetEntry);
            JarInputStream innerJarInputStream = new JarInputStream(inputStream);
            JarEntry nextJarEntry;
            while ((nextJarEntry = innerJarInputStream.getNextJarEntry()) != null) {
                addIfTargetCLass(nextJarEntry, targetUrl, result);
            }
        } else {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry nextJarEntry = entries.nextElement();
                addIfTargetCLass(nextJarEntry, targetUrl, result);
            }
        }
    }

    /**
     * 检查jarEntry是否有效，是否为目标包，若是则添加到结果集合
     *
     * @param jarEntry  jar entry
     * @param targetUrl 目标目录
     * @param result    结果集合
     */
    private void addIfTargetCLass(JarEntry jarEntry, String targetUrl, List<Class<?>> result) {
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
    private void filterClass(String ClassFileUrl, List<Class<?>> result) {
        if (ClassFileUrl.endsWith(MODULE_INFO)) {
            return;
        }
        ScanFilter scanFilter = this.scanFilter;
        if (scanFilter != null && !filterByAsm(ClassFileUrl)) {
            return;
        }
        Class<?> target;
        try {
            String fullClassName = replaceUrlToPackage(ClassFileUrl.substring(0, ClassFileUrl.length() - CLASS_END_LEN));
            target = loadClass(fullClassName);
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
     * 通过asm检测Filter，若不匹配返回false
     * 注意：无法校验是否是通过ClassLoader加载
     *
     * @param classFileUrl class文件url
     * @return 是否匹配
     */
    private boolean filterByAsm(String classFileUrl) {
        try {
            ClassFilterVisitor classVisitor = new ClassFilterVisitor(this.scanFilter);
            URL resource = getClassLoader().getResource(classFileUrl);
            if (resource != null) {
                new ClassReader(resource.openStream()).accept(classVisitor, ClassReader.SKIP_CODE);
                return classVisitor.isPass();
            } else if (log.isDebugEnabled()) {
                log.debug("can not filter By Asm,name:{},cause:url resource is null", classFileUrl);
            }
        } catch (Throwable t) {
            if (log.isDebugEnabled()) {
                log.debug("can not filter By Asm,name:{},cause:{}", classFileUrl, t.getClass().getName());
            }
        }
        return false;
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

    private static String replaceUrlToPackage(String url) {
        return url.replaceAll(URL_SEPARATE_CHARACTER, PACKAGE_SEPARATE_CHARACTER);
    }

    private static String replacePackageToUrl(String url) {
        return url.replaceAll("\\.", URL_SEPARATE_CHARACTER);
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

    /**
     * asm ClassVisitor实现类，根据字节码文件过滤类，避免直接Load Class
     */
    public static class ClassFilterVisitor extends ClassVisitor {

        /** 字节码类描述符前缀: 'L' */
        private final static String CLASS_SIGNATURE_PRE = "L";

        /** 字节码类描述符后缀: ';' */
        private final static String CLASS_SIGNATURE_TAIL = ";";

        private boolean passVisit;

        private int passVisitAnnotation;

        private String superClass;

        private List<String> annotations;

        public ClassFilterVisitor(ScanFilter filter) {
            super(Opcodes.ASM5);
            this.passVisit = false;
            this.passVisitAnnotation = 0;
            List<Class<? extends Annotation>> hasAnnotations = filter.getHasAnnotations();
            if (CollectionUtils.isNotEmpty(hasAnnotations)) {
                this.annotations = hasAnnotations.stream()
                        .map(annotation -> CLASS_SIGNATURE_PRE + replacePackageToUrl(annotation.getName()) + CLASS_SIGNATURE_TAIL)
                        .collect(Collectors.toList());
            } else {
                this.annotations = Collections.emptyList();
            }
            Class<?> superClass = filter.getSuperClass();
            if (superClass != null) {
                this.superClass = superClass.getName();
            }
        }

        @Override
        public void visit(int i, int access, String className, String signature, String superClass, String[] interfaces) {
            boolean accessPass = Modifier.isPublic(access);
            boolean superClassFilter = this.superClass == null;
            if (!superClassFilter) {
                String superClassUrl = replacePackageToUrl(this.superClass);
                if (superClass != null &&
                        superClass.equals(superClassUrl)) {
                    superClassFilter = true;
                } else if (interfaces.length > 0 && ArrayUtils.contains(interfaces, superClassUrl)) {
                    superClassFilter = true;
                }
            }
            this.passVisit = accessPass && superClassFilter;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String annotationName, boolean b) {
            if (annotations.contains(annotationName)) {
                this.passVisitAnnotation++;
            }
            return null;
        }

        public boolean isPass() {
            return passVisit && passVisitAnnotation == annotations.size();
        }
    }

}
