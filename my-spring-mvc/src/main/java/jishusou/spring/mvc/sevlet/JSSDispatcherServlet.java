package jishusou.spring.mvc.sevlet;


import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class JSSDispatcherServlet extends HttpServlet {

    private Map<String, Object> ioc = new HashMap<>();
    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1. 加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        // 2. 扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        // --------------------   ioc 部分    -------------------
        // 3. 实例化相关的类，并且将实例化之后的Bean缓存到ioc容器中
        doInstance();
        // -----------------------  DI 部分  ---------------------
        // 4. 完成依赖注入
        doAnnotation();
        // ---------------------- MVC 部分 ----------------------
        // 5. 初始化HandlerMapping
        doInitHandlerMapping();

        System.out.println("JSS Spring Framework is init ...");
    }

    private void doInitHandlerMapping() {
    }

    private void doAnnotation() {
    }

    private void doInstance() {

        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {

                Class<?> clazz = Class.forName(className);

                // 1. 默认是类名首字母手写
                String beanName = toLowFirstCase(clazz.getSimpleName());
                Object instance = clazz.newInstance();

                // 2. 如果在不同的包路径下，出现相同类名，只能自定义BeanName

                ioc.put(beanName, instance);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowFirstCase(String simpleName) {

        char[] chars = simpleName.toCharArray();
        chars[0] = (char) (chars[0]+32);
        return new String(chars);

    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().
                getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classpath = new File(url.getFile());
        for (File file :
                classpath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {


                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replaceAll(".class", "");

                // Class.forName(className)
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            contextConfig.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != resourceAsStream) {
                try {
                    resourceAsStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
