package com.wlld.myjecs.bean;


import com.wlld.myjecs.config.FreeWord;
import com.wlld.myjecs.entity.business.AllKeyWords;
import com.wlld.myjecs.config.SysConfig;
import com.wlld.myjecs.entity.KeywordType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.dromara.easyai.config.RZ;
import org.dromara.easyai.config.SentenceConfig;
import org.dromara.easyai.config.TfConfig;
import org.dromara.easyai.naturalLanguage.TalkToTalk;
import org.dromara.easyai.naturalLanguage.languageCreator.CatchKeyWord;
import org.dromara.easyai.naturalLanguage.word.MyKeyWord;
import org.dromara.easyai.naturalLanguage.word.WordEmbedding;
import org.dromara.easyai.rnnJumpNerveCenter.CustomManager;
import org.dromara.easyai.rnnJumpNerveCenter.RRNerveManager;

import java.util.*;

/**
 * @param
 * @DATA
 * @Author LiDaPeng
 * @Description
 */
@Configuration
public class BeanMangerOnly {//需要单例的类

    @Bean
    public SentenceConfig getConfig() {//配置文件
        SentenceConfig sentenceConfig = new SentenceConfig();
        sentenceConfig.setQaWordVectorDimension(32);//维度大参数多，它就能适应更复杂的情况，速度越慢，需要样本越多
        sentenceConfig.setMaxWordLength(20);//语言长度 越长越好，但是越长需求的数据量越大，计算时间越长性能越差，也需要更多的内存。
        sentenceConfig.setTrustPowerTh(0);//语义分类可信阈值，范围0-1
        sentenceConfig.setSentenceTrustPowerTh(0.3f);//生成语句可信阈值
        sentenceConfig.setWeStudyPoint(0.005f);
        sentenceConfig.setMaxAnswerLength(50);//回复语句的最长长度
        sentenceConfig.setTimes(1);//qa模型训练增强
        sentenceConfig.setParam(0.3f);//正则抑制系数
        return sentenceConfig;
    }

    @Bean
    public TfConfig getTfConfig() {//64-128
        TfConfig config = new TfConfig();
        config.setMaxLength(25);//最大长度设置为40
        config.setMultiNumber(8);//每层编解码器设置3个多头
        config.setFeatureDimension(32);//设置词向量维度
        config.setAllDepth(1);//设置tf编解码器深度
        config.setTimes(400);//循环训练五百次
        config.setStudyRate(0.0025f);//设置tf学习率å
        config.setTimePunValue(0.5f);//0.5
        config.setShowLog(true);//对学习中的数据打印
        return config;
    }

    @Bean
    public TalkToTalk getTalkToTalk() throws Exception {
        return new TalkToTalk(getTfConfig());
    }

    @Bean
    public List<String> getFreeWord() {
        return new ArrayList<>(Arrays.asList(FreeWord.keyWord));
    }

    @Bean
    public WordEmbedding getWordEmbedding() {//词向量嵌入器（word2Vec）
        //中文语句 你，我，他，好 文字 是 离散的高维特征->离散特征连续化 并降维 [0.223,0.123,0.122334 ....]
        return new WordEmbedding();
    }

    @Bean
    public RRNerveManager getRRNerveManager() throws Exception {
        return new RRNerveManager(getWordEmbedding());
    }

    @Bean//关键词抓取类
    public Map<Integer, CatchKeyWord> catchKeyWord() {//关键词抓取
        return new HashMap<>();
    }

    @Bean
    public Map<Integer, MyKeyWord> getMyKeyWord() {//关键词敏感性嗅探
        return new HashMap<>();
    }

    @Bean
    public Map<Integer, List<KeywordType>> getKeyTypes() {//主键是语句类别，值是找个语句类别的关键词种类集合
        return new HashMap<>();
    }

    @Bean
    public AllKeyWords getAllKeyWords() {//获取所有关键词
        return new AllKeyWords();
    }

    @Bean
    public SysConfig sysConfig() {
        return new SysConfig();
    }


}
