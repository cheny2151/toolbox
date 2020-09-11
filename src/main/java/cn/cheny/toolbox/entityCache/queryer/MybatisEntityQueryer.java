package cn.cheny.toolbox.entityCache.queryer;

import cn.cheny.toolbox.entityCache.buffer.model.BufferInfo;
import cn.cheny.toolbox.reflect.ReflectUtils;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 实体查询器--mybatis实现
 *
 * @author cheney
 * @date 2020-09-01
 */
@Slf4j
public class MybatisEntityQueryer implements EntityQueryer {

    private final static String QUERY_METHOD_NAME = "query";

    /**
     * Mapper代理实例
     */
    private Object proxyMapper;

    /**
     * 查询方法
     */
    private Method queryMethod;

    public MybatisEntityQueryer(SqlSession sqlSession) {
        Class<?> mapperInterface = createMapperInterface();
        sqlSession.getConfiguration().addMapper(mapperInterface);
        this.proxyMapper = sqlSession.getMapper(mapperInterface);
        this.queryMethod = ReflectUtils.getMethod(mapperInterface, QUERY_METHOD_NAME, Map.class);
    }

    @Override
    public <T> List<T> query(BufferInfo<T> bufferInfo) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("fields", bufferInfo.splitFields());
        params.put("tableName", bufferInfo.getTableName());
        params.put("filters", bufferInfo.getSqlCondition());
        try {
            List<Map<String, Object>> results = (List<Map<String, Object>>) queryMethod.invoke(proxyMapper, params);
            return results.stream().map(r -> mapToEntity(r, bufferInfo.getEntityClass())).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("执行查询缓存失败", e);
        }
        return Collections.emptyList();
    }

    /**
     * 动态创建查询Mapper接口
     *
     * @return Mapper接口
     */
    private Class<?> createMapperInterface() {
        ClassPool classPool = ClassPool.getDefault();
        ClassClassPath classPath = new ClassClassPath(MybatisEntityQueryer.class);
        classPool.insertClassPath(classPath);
        CtClass cc = classPool.makeInterface(MybatisEntityQueryer.class.getName() + "$proxy");
        try {
            CtClass typeList = classPool.getCtClass(List.class.getName());
            CtClass typeMap = classPool.getCtClass(Map.class.getName());
            CtMethod queryMethod = CtNewMethod.abstractMethod(typeList, QUERY_METHOD_NAME,
                    new CtClass[]{typeMap}, null, cc);
            queryMethod.setGenericSignature(
                    SignatureAttribute.toMethodSignature("(Ljava/util/Map;)Ljava/util/List<Ljava/util/Map;>;").encode());
            // 添加注解
            ClassFile classFile = cc.getClassFile();
            ConstPool constPool = classFile.getConstPool();
            AnnotationsAttribute attribute = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            Annotation select = new Annotation("org.apache.ibatis.annotations.Select", constPool);
            StringMemberValue sqlVal = new StringMemberValue(getBaseSql(), constPool);
            ArrayMemberValue arrayVal = new ArrayMemberValue(sqlVal, constPool);
            arrayVal.setValue(new MemberValue[]{sqlVal});
            select.addMemberValue("value", arrayVal);
            attribute.addAnnotation(select);
            Annotation resultType = new Annotation("org.apache.ibatis.annotations.ResultType", constPool);
            resultType.addMemberValue("value", new ClassMemberValue(Map.class.getName(), constPool));
            attribute.addAnnotation(resultType);
            queryMethod.getMethodInfo().addAttribute(attribute);
            cc.addMethod(queryMethod);
            return cc.toClass();
        } catch (Exception e) {
            throw new RuntimeException("fail to create proxy queryer", e);
        }
    }

    public String getBaseSql() {
        return "<script> select ${fields} from ${tableName} " +
                "<if test = \"filters != null and filters != ''\">" +
                "where ${filters}" +
                "</if></script>";
    }

}
