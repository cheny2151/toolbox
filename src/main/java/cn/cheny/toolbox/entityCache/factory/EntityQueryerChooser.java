package cn.cheny.toolbox.entityCache.factory;

import cn.cheny.toolbox.entityCache.queryer.EntityQueryer;
import cn.cheny.toolbox.entityCache.queryer.JpaEntityQueryer;
import cn.cheny.toolbox.entityCache.queryer.MybatisEntityQueryer;
import cn.cheny.toolbox.reflect.ReflectUtils;
import cn.cheny.toolbox.spring.SpringUtils;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;

import java.util.Collection;

/**
 * 实体查询器的自动选择器
 *
 * @author cheney
 * @date 2020-09-01
 */
public class EntityQueryerChooser {

    private Object sqlSession;

    public EntityQueryerChooser() {
    }

    public EntityQueryerChooser(Object sqlSession) {
        this.sqlSession = sqlSession;
    }

    public EntityQueryer getEntityQueryer() {
        MybatisEntityQueryer mybatisEntityQueryer = testMybatis();
        if (mybatisEntityQueryer != null) {
            return mybatisEntityQueryer;
        }
        JpaEntityQueryer jpaEntityQueryer = testJpa();
        if (jpaEntityQueryer != null) {
            return jpaEntityQueryer;
        }
        // todo other support
        return null;
    }

    private boolean isInSprintEnv() {
        return ReflectUtils.isPresent("org.springframework.context.ApplicationContextAware", null);
    }

    /**
     * 测试是否为mybatis环境
     *
     * @return mybatis实体查询器
     */
    private MybatisEntityQueryer testMybatis() {
        if (!ReflectUtils.isPresent("org.apache.ibatis.session.SqlSessionFactory", null)) {
            return null;
        }
        // spring environment
        if (isInSprintEnv()) {
            Collection<?> sqlSessionFactoryInSpringEnv = SpringUtils.getBeansOfType(SqlSessionTemplate.class);
            if (sqlSessionFactoryInSpringEnv.size() > 0) {
                SqlSession sqlSession = (SqlSession) sqlSessionFactoryInSpringEnv.iterator().next();
                return new MybatisEntityQueryer(sqlSession);
            }
        }
        if (this.sqlSession != null && this.sqlSession instanceof SqlSession) {
            return new MybatisEntityQueryer((SqlSession) this.sqlSession);
        }
        return null;
    }

    /**
     * 测试是否为jpa环境
     *
     * @return jpa实体查询器
     */
    private JpaEntityQueryer testJpa() {
        Class<?> entityManagerClass;
        try {
            entityManagerClass = Class.forName("javax.persistence.EntityManager");
        } catch (ClassNotFoundException e) {
            return null;
        }
        return null;
    }
}
