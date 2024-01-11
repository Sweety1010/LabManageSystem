package com.lab.labmanagesystem.factory;

import com.arcsoft.face.EngineConfiguration;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.ErrorInfo;
import com.lab.labmanagesystem.config.ArcFaceAutoConfiguration;
import com.lab.labmanagesystem.constant.MessageConstant;
import com.lab.labmanagesystem.exception.FaceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

@Slf4j
public class FaceEngineFactory extends BasePooledObjectFactory<FaceEngine> {
    private String appId;
    private String sdkKey;
    private String activeKey;
    private EngineConfiguration engineConfiguration;

    public FaceEngineFactory(String appId, String sdkKey, String activeKey, EngineConfiguration engineConfiguration) {
        this.appId = appId;
        this.sdkKey = sdkKey;
        this.activeKey = activeKey;
        this.engineConfiguration = engineConfiguration;
    }

    @Override
    public FaceEngine create() {
        FaceEngine faceEngine = new FaceEngine(ArcFaceAutoConfiguration.CACHE_LIB_FOLDER);
        int activeCode = faceEngine.activeOnline(appId, sdkKey);
        if (activeCode != ErrorInfo.MOK.getValue() && activeCode != ErrorInfo.MERR_ASF_ALREADY_ACTIVATED.getValue()) {
            log.error(MessageConstant.FACE_ENGINE_ACTIVE_FAIL + activeCode);
            throw new FaceException(MessageConstant.FACE_ENGINE_ACTIVE_FAIL + activeCode);
        }
        int initCode = faceEngine.init(engineConfiguration);
        if (initCode != ErrorInfo.MOK.getValue()) {
            log.error(MessageConstant.FACE_ENGINE_INIT_FAIL + initCode);
            throw new FaceException(MessageConstant.FACE_ENGINE_INIT_FAIL + initCode);
        }
        return faceEngine;
    }

    @Override
    public PooledObject<FaceEngine> wrap(FaceEngine faceEngine) {
        return new DefaultPooledObject<>(faceEngine);
    }


    @Override
    public void destroyObject(PooledObject<FaceEngine> p) throws Exception {
        FaceEngine faceEngine = p.getObject();
        int result = faceEngine.unInit();
        super.destroyObject(p);
    }
}
