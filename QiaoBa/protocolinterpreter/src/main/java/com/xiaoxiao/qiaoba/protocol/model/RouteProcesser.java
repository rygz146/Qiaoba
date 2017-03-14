package com.xiaoxiao.qiaoba.protocol.model;

import com.google.auto.service.AutoService;
import com.xiaoxiao.qiaoba.annotation.router.FragmentLinkUri;
import com.xiaoxiao.qiaoba.annotation.router.RouterLinkUri;
import com.xiaoxiao.qiaoba.protocol.utils.Constant;
import com.xiaoxiao.qiaoba.protocol.utils.Logger;
import com.xiaoxiao.qiaoba.protocol.utils.ProcessUtils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

/**
 * Created by wangfei on 2017/1/11.
 */
@AutoService(Processor.class)//使用 Google 的 auto-service 库可以自动生成 META-INF/services/javax.annotation.processing.Processor 文件
public class RouteProcesser extends AbstractProcessor {

    private Filer mFiler;//文件相关的辅助类
    private Elements mElementUtils;//元素相关的辅助类
    private Messager mMessager;//日志相关的辅助类
    private Logger mLogger;
    private String moduleName;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler = processingEnv.getFiler();
        mElementUtils = processingEnv.getElementUtils();
        mMessager = processingEnv.getMessager();
        mLogger = new Logger(mMessager);
        Map<String, String> options = processingEnv.getOptions();
        if(options != null && options.size() > 0){
            moduleName = options.get(Constant.KEY_MODULE_NAME);
        }
        mLogger.info("module name  is : " + moduleName);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        types.add(RouterLinkUri.class.getCanonicalName());
        types.add(FragmentLinkUri.class.getCanonicalName());
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        DataClassCreator classCreator = new DataClassCreator(mLogger);
        Map<String, ElementHolder> routerLinkUriMap = ProcessUtils.collectClassInfo(roundEnv, RouterLinkUri.class, ElementKind.CLASS, mLogger);
        if(routerLinkUriMap.keySet().size() > 0){
            classCreator.generateRouterLinkCode(moduleName, mElementUtils, mFiler, routerLinkUriMap.values());
        }else {
            mLogger.info("The size of Activity using RouterLinkUri annotation is 0.");
        }
        Map<String, ElementHolder> fragmentLinkUriMap = ProcessUtils.collectClassInfo(roundEnv, FragmentLinkUri.class, ElementKind.CLASS, mLogger);
        if(fragmentLinkUriMap.keySet().size() > 0){
            classCreator.generateFragmentLinkCode(moduleName, mElementUtils, mFiler, fragmentLinkUriMap.values());
        }else {
            mLogger.info("The size of Fragment using FragmentLinkUri annotation is 0.");
        }
        return true;
    }
}
