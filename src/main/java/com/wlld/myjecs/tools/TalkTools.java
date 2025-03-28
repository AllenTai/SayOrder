package com.wlld.myjecs.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wlld.myjecs.bean.BeanMangerOnly;
import com.wlld.myjecs.config.Config;
import org.dromara.easyai.entity.CreatorModel;
import org.dromara.easyai.entity.SentenceModel;
import org.dromara.easyai.entity.TalkBody;
import org.dromara.easyai.entity.WordTwoVectorModel;
import org.dromara.easyai.naturalLanguage.TalkToTalk;
import org.dromara.easyai.naturalLanguage.Tokenizer;
import org.dromara.easyai.naturalLanguage.WordTemple;
import org.dromara.easyai.naturalLanguage.word.WordEmbedding;
import org.dromara.easyai.rnnJumpNerveCenter.CustomManager;
import org.dromara.easyai.transFormer.model.TransFormerModel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TalkTools {
    public void initSemantics(BeanMangerOnly beanMangerOnly, List<TalkBody> sentences) throws Exception {
        List<TalkBody> sen = null;
        if (sentences != null) {
            sen = anySort(sentences);
        }
        boolean isStudy = initWordEmbedding(beanMangerOnly, sen);//初始化词向量嵌入
        if (Config.QA_MODEL == 1) {
            initCustomServer(isStudy, beanMangerOnly, sen);
        } else {
            initTalkToTalk(isStudy, beanMangerOnly, sen);
        }
    }

    private List<TalkBody> anySort(List<TalkBody> sentences) {//做乱序
        Random random = new Random();
        List<TalkBody> sent = new ArrayList<>();
        int time = sentences.size();
        for (int i = 0; i < time; i++) {
            int size = sentences.size();
            int index = random.nextInt(size);
            sent.add(sentences.get(index));
            sentences.remove(index);
        }
        return sent;
    }

    private void initTalkToTalk(boolean isStudy, BeanMangerOnly beanMangerOnly, List<TalkBody> sentences) throws Exception {
        File file = new File(Config.longTalkUrl); //创建文件
        TalkToTalk talkToTalk = beanMangerOnly.getTalkToTalk();
        talkToTalk.init();
        if (file.exists() && !isStudy) {
            talkToTalk.insertModel(readCreatorModel2());
        } else {
            long startTime = System.currentTimeMillis() / 1000;
            TransFormerModel transFormerModel = talkToTalk.study(sentences);
            long endTime = System.currentTimeMillis() / 1000 - startTime;
            System.out.println("qa训练总耗时:" + endTime);
            String model = JSON.toJSONString(transFormerModel);
            writeModel(model, Config.longTalkUrl);
        }
    }

    private void initCustomServer(boolean isStudy, BeanMangerOnly beanMangerOnly, List<TalkBody> sentences) throws Exception {
        File file = new File(Config.shortTalkUrl); //创建文件
        CustomManager customManager = beanMangerOnly.getCustomManager();
        customManager.init();
        if (file.exists() && !isStudy) {//读模型
            customManager.insertModel(readCreatorModel());
        } else if (sentences != null && !sentences.isEmpty()) {//训练
            CreatorModel creatorModel = customManager.study(sentences);
            String model = JSON.toJSONString(creatorModel);
            writeModel(model, Config.shortTalkUrl);
        }
    }

    private boolean initWordEmbedding(BeanMangerOnly beanMangerOnly, List<TalkBody> sentences) throws Exception {//初始化词嵌入模型
        File file = new File(Config.wordUrl); //创建文件
        WordEmbedding wordEmbedding = beanMangerOnly.getEmbedding();
        wordEmbedding.setConfig(beanMangerOnly.getConfig());
        boolean isStudy = false;
        if (file.exists()) {//模型文件存在
            wordEmbedding.insertModel(readWord2VecModel(), beanMangerOnly.getConfig().getQaWordVectorDimension());//初始化word2Vec编码器
        } else if (sentences != null && !sentences.isEmpty()) {//模型文件不存在
            isStudy = true;
            SentenceModel sentenceModel = new SentenceModel();
            for (TalkBody sentence : sentences) {
                sentenceModel.setSentence(sentence.getQuestion());
                sentenceModel.setSentence(sentence.getAnswer());
            }
            wordEmbedding.init(sentenceModel, beanMangerOnly.getConfig().getQaWordVectorDimension());
            WordTwoVectorModel wordTwoVectorModel = wordEmbedding.start();//词向量开始学习
            String model = JSON.toJSONString(wordTwoVectorModel);
            writeModel(model, Config.wordUrl);
        }
        return isStudy;
    }


    private WordTwoVectorModel readWord2VecModel() {
        File file = new File(Config.wordUrl);
        String a = readPaper(file);
        return JSONObject.parseObject(a, WordTwoVectorModel.class);
    }

    private TransFormerModel readCreatorModel2() {
        File file = new File(Config.longTalkUrl);
        String a = readPaper(file);
        return JSONObject.parseObject(a, TransFormerModel.class);
    }

    private CreatorModel readCreatorModel() {
        File file = new File(Config.shortTalkUrl);
        String a = readPaper(file);
        return JSONObject.parseObject(a, CreatorModel.class);
    }

    private void writeModel(String model, String url) throws IOException {//写出模型与 激活参数
        OutputStreamWriter out = new OutputStreamWriter(Files.newOutputStream(Paths.get(url)), StandardCharsets.UTF_8);
        out.write(model);
        out.close();
    }

    private String readPaper(File file) {
        InputStream read = null;
        String context = null;
        try {
            read = Files.newInputStream(file.toPath());
            byte[] bt = new byte[read.available()];
            read.read(bt);
            context = new String(bt, StandardCharsets.UTF_8);
            read.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (read != null) {
                try {
                    read.close(); //确保关闭
                } catch (IOException el) {
                }
            }
        }
        return context;
    }
}
