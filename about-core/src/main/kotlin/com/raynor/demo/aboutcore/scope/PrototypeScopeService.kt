package com.raynor.demo.aboutcore.scope

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Instant

@Component
@Scope("prototype")
class ProtoTypeScope {
    val timeInit = Instant.now()
    val timeLazy by lazy {
        Instant.now()
    }
}


@Component
@Scope("singleton")
class SingletonScope {
    val timeInit = Instant.now()
    val timeLazy by lazy {
        Instant.now()
    }
}


@Service
class PrototypeScopeService(
    private val protoTypeScope: ProtoTypeScope,
    private val singletonScope: SingletonScope,
    private val prototypeProvider: ObjectProvider<ProtoTypeScope>,
    private val singletonProvider: ObjectProvider<SingletonScope>,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun getTimes() {
        // 쓸일이 있을까 싶다.
        /*
        ObjectProvider 를 사용하여 요청 시에 빈을 생성하여
        로그 확인시 protoTypeScopeBean 만 서로 다른 값을 가짐

        protoTypeScope: 2024-12-20T08:06:39.169703Z, 2024-12-20T08:06:48.801319Z
        singletonScope: 2024-12-20T08:06:39.170743Z, 2024-12-20T08:06:48.801609Z
        protoTypeScopeBean: 2024-12-20T08:06:52.661882Z, 2024-12-20T08:06:52.662451Z
        singletonScopeBean: 2024-12-20T08:06:39.170743Z, 2024-12-20T08:06:48.801609Z

        ------------------------------------------------------------------

        protoTypeScope: 2024-12-20T08:06:39.169703Z, 2024-12-20T08:06:48.801319Z
        singletonScope: 2024-12-20T08:06:39.170743Z, 2024-12-20T08:06:48.801609Z
        protoTypeScopeBean: 2024-12-20T08:06:56.647995Z, 2024-12-20T08:06:56.648353Z
        singletonScopeBean: 2024-12-20T08:06:39.170743Z, 2024-12-20T08:06:48.801609Z
        * */

        val protoTypeScopeBean = prototypeProvider.getObject()
        val singletonScopeBean = singletonProvider.getObject()
        logger.info("------------------------------------------------------------------")
        logger.info("protoTypeScope: ${protoTypeScope.timeInit}, ${protoTypeScope.timeLazy}")
        logger.info("singletonScope: ${singletonScope.timeInit}, ${singletonScope.timeLazy}")
        logger.info("protoTypeScopeBean: ${protoTypeScopeBean.timeInit}, ${protoTypeScopeBean.timeLazy}")
        logger.info("singletonScopeBean: ${singletonScopeBean.timeInit}, ${singletonScopeBean.timeLazy}")
    }
}