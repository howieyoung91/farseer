package com.github.howieyoung91.farseer.data;

import com.github.howieyoung91.farseer.core.pojo.DocumentVo;
import com.github.howieyoung91.farseer.data.remote.RemoteIndexController;
import com.github.howieyoung91.farseer.data.source.DocumentSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * 轮询 DocumentSource，一旦有数据就向 RemoteIndexController 中添加数据
 *
 * @author Howie Young
 * @version 1.0
 * @since 1.0 [2022/08/14 16:36]
 */
@Component
@Slf4j
public class IndexListener<S> implements ApplicationListener<ContextRefreshedEvent> {
    @Autowired(required = false)
    private DocumentSource<S>     source;
    @Autowired
    private RemoteIndexController remoteIndexController;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (source == null) {
            return;
        }
        while (true) {
            Collection<DocumentVo> documentVos = source.getDocuments();
            if (!documentVos.isEmpty()) {
                for (DocumentVo documentVo : documentVos) {
                    remoteIndexController.index(documentVo);
                    log.info("{}", documentVo);
                }
            }
            else {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
