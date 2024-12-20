package com.raynor.demo.aboutcore.scope

import org.springframework.stereotype.Service

@Service
class RequestScopeService(
    private val userContext: UserContext,
    private val tracker: Tracker,
) {

    fun getContext(): List<String?> {
        /*
        음 알겠는데... 흠... 좀...
        아 고런 느낌

        [Tracker] Bean created at: 2024-12-20T09:48:58.237083Z
        2024-12-20T18:48:58.411+09:00  INFO 75842 --- [  restartedMain] o.s.b.d.a.OptionalLiveReloadServer       : LiveReload server is running on port 35729
        2024-12-20T18:48:58.432+09:00  INFO 75842 --- [  restartedMain] o.s.b.w.embedded.tomcat.TomcatWebServer  : Tomcat started on port 8080 (http) with context path '/'
        2024-12-20T18:48:58.439+09:00  INFO 75842 --- [  restartedMain] c.raynor.demo.aboutcore.ApplicationKt    : Started ApplicationKt in 1.297 seconds (process running for 1.539)
        2024-12-20T18:49:09.323+09:00  INFO 75842 --- [nio-8080-exec-1] o.a.c.c.C.[Tomcat].[localhost].[/]       : Initializing Spring DispatcherServlet 'dispatcherServlet'
        2024-12-20T18:49:09.324+09:00  INFO 75842 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Initializing Servlet 'dispatcherServlet'
        2024-12-20T18:49:09.324+09:00  INFO 75842 --- [nio-8080-exec-1] o.s.web.servlet.DispatcherServlet        : Completed initialization in 0 ms
        [UserContext] Bean created at: 2024-12-20T09:49:09.328992Z
        [UserContext] Bean destroyed at: 2024-12-20T09:49:09.392770Z
        [UserContext] Bean created at: 2024-12-20T09:49:12.017611Z
        [UserContext] Bean destroyed at: 2024-12-20T09:49:12.019295Z
        [UserContext] Bean created at: 2024-12-20T09:49:12.955243Z
        [UserContext] Bean destroyed at: 2024-12-20T09:49:12.956736Z
        Disconnected from the target VM, address: '127.0.0.1:52888', transport: 'socket'
        2024-12-20T18:49:15.539+09:00  INFO 75842 --- [ionShutdownHook] o.s.b.w.e.tomcat.GracefulShutdown        : Commencing graceful shutdown. Waiting for active requests to complete
        2024-12-20T18:49:15.543+09:00  INFO 75842 --- [tomcat-shutdown] o.s.b.w.e.tomcat.GracefulShutdown        : Graceful shutdown complete
        [Tracker] Bean destroyed at: 2024-12-20T09:49:15.545379Z


        1. @RequestScope의 성능 관련 특징

        장점
        1.	요청별 상태 관리 간소화
        •	@RequestScope를 사용하면 요청마다 별도의 빈 인스턴스를 생성하므로 상태 관리가 간편합니다.
        •	특정 요청에만 필요한 데이터를 별도로 관리해 메모리 충돌(다른 요청 간 상태 혼동)을 방지합니다.
        2.	코드 간결화
        •	요청 데이터 전달을 위한 매개변수나 전역 상태 관리 코드가 줄어듭니다.
        •	비효율적인 데이터 전달 방식(예: 전역 맵 사용)을 제거할 수 있습니다.

        단점
        1.	빈 생성 비용
        •	@RequestScope 빈은 요청마다 생성되고, 요청 종료 시 파기됩니다.
        •	요청 빈도가 높아질수록 빈 생성 및 소멸 작업이 서버의 자원(메모리, CPU)에 부담을 줄 수 있습니다.
        2.	GC(가비지 컬렉션) 부담
        •	요청 스코프 빈은 요청이 끝나면 가비지 컬렉션 대상이 되므로, 짧은 생명주기를 가진 객체가 많아지고 GC 작업이 증가할 수 있습니다.
        3.	컨텍스트 관리 비용
        •	Spring이 요청 범위를 관리하기 위해 RequestAttributes를 설정하고 요청별 컨텍스트를 유지해야 합니다. 이로 인해 추가적인 관리 비용이 발생합니다.


        2. 성능 관점에서의 분석

        요청 처리 속도
            •	요청마다 새로운 빈 인스턴스를 생성하므로 요청당 처리 시간이 약간 증가할 수 있습니다.
            •	간단한 빈이라면 영향이 미미하지만, 빈 생성 시 복잡한 초기화 작업이 포함되어 있다면 성능 저하로 이어질 수 있습니다.

        메모리 사용량
            •	요청이 많을수록 메모리 사용량이 증가합니다.
            •	특히 대규모 트래픽 상황에서는 빈 생성/소멸의 빈도가 높아져 메모리 압박과 GC 작업 증가가 발생할 수 있습니다.

        대규모 트래픽 처리
            •	요청 범위의 빈은 요청별로 격리되어 있어, 동시 요청이 많더라도 상태 관리 문제는 발생하지 않습니다.
            •	하지만 생성 비용이 높은 객체를 @RequestScope로 관리하면, 대규모 트래픽에서 병목현상이 발생할 가능성이 높습니다.
        * * */
        return listOf(
            tracker.id,
            userContext.id,
            userContext.userAgent,
            userContext.contentType,
        )
    }
}