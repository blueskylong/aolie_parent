package com.ranranx.aolie.gateway.router;

import com.ranranx.aolie.application.user.service.ILoginService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.support.NameUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author xxl
 * @version V0.0.1
 * @date 2022/5/30 0030 16:44
 **/
@Component
public class RouterGenerator implements ApplicationEventPublisherAware, ApplicationRunner {


    @Autowired
    private RouteDefinitionWriter routeDefinitionWriter;

    @DubboReference
    private ILoginService loginService;


    private ApplicationEventPublisher publisher;


    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }


    private void loadRoutes() {
        Map<String, List<Long>> dsServiceNameRelation = loginService.getDsServiceNameRelation();
        Map<String, List<Long>> viewServiceNameRelation = loginService.getViewServiceNameRelation();
        Map<String, List<Long>> fixToServiceName = loginService.getFixToServiceName();
        dsServiceNameRelation.entrySet().forEach(entry -> {
            try {
                RouteDefinition definition = new RouteDefinition();
                definition.setId("schema_" + entry.getKey());
                definition.setUri(new URI("lb://" + entry.getKey()));
                PredicateDefinition lstPredicate = toPredicate(entry.getValue(),
                        viewServiceNameRelation.get(entry.getKey()), fixToServiceName.get(entry.getKey()));
                definition.setPredicates(Arrays.asList(lstPredicate));
                System.out.println(entry.getKey());
                System.out.println(lstPredicate);
                routeDefinitionWriter.save(Mono.just(definition)).subscribe();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        publisher.publishEvent(new RefreshRoutesEvent(this));
    }

    private PredicateDefinition toPredicate(List<Long> lstDsId, List<Long> lstViewId, List<Long> lstFixId) {
        PredicateDefinition definition = new PredicateDefinition();
        definition.setName("Path");
        int preCount = 1;
        if (lstDsId != null && !lstDsId.isEmpty()) {
            for (int i = 0; i < lstDsId.size(); i++) {
                definition.addArg(NameUtils.generateName(i), "/dmdata/" + lstDsId.get(i) + "/**");
            }
            preCount += lstDsId.size();
        }
        if (lstViewId != null && !lstViewId.isEmpty()) {
            for (int i = 0; i < lstViewId.size(); i++) {
                definition.addArg(NameUtils.generateName(i + preCount), "/viewdata/" + lstViewId.get(i) + "/**");
            }
        }
        if (lstFixId != null && !lstFixId.isEmpty()) {
            for (int i = 0; i < lstFixId.size(); i++) {
                definition.addArg(NameUtils.generateName(i + preCount), "/fixrow/" + lstFixId.get(i) + "/**");
            }
        }
        return definition;

    }

//
//    public void add(RouteDefinition routeDefinition) {
//        Assert.notNull(routeDefinition.getId(), "routeDefinition is can not be null");
//        repository.save(Mono.just(routeDefinition)).subscribe();
//        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
//        publisher.publishEvent(new RefreshRoutesEvent(this));
//    }
//
//
//    public void update(RouteDefinition routeDefinition) {
//        Assert.notNull(routeDefinition.getId(), "routeDefinition is can not be null");
//        repository.delete(Mono.just(routeDefinition.getId())).subscribe();
//        routeDefinitionWriter.delete(Mono.just(routeDefinition.getId())).subscribe();
//        repository.save(Mono.just(routeDefinition)).subscribe();
//        routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
//        publisher.publishEvent(new RefreshRoutesEvent(this));
//    }
//
//
//    public void delete(String id) {
//        Assert.notNull(id, "routeDefinition is can not be null");
//        repository.delete(Mono.just(id)).subscribe();
//        routeDefinitionWriter.delete(Mono.just(id)).subscribe();
//        publisher.publishEvent(new RefreshRoutesEvent(this));
//    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        loadRoutes();

    }
}
