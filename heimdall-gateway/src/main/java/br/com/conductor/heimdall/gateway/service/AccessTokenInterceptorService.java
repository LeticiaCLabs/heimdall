/*-
 * =========================LICENSE_START==================================
 * heimdall-gateway
 * ========================================================================
 * Copyright (C) 2018 Conductor Tecnologia SA
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ==========================LICENSE_END===================================
 */
package br.com.conductor.heimdall.gateway.service;

import br.com.conductor.heimdall.core.entity.AccessToken;
import br.com.conductor.heimdall.core.entity.App;
import br.com.conductor.heimdall.core.entity.Plan;
import br.com.conductor.heimdall.core.enums.Status;
import br.com.conductor.heimdall.core.repository.AccessTokenRepository;
import br.com.conductor.heimdall.core.service.AccessTokenService;
import br.com.conductor.heimdall.core.service.AppService;
import br.com.conductor.heimdall.core.service.PlanService;
import br.com.conductor.heimdall.core.util.ConstantsInterceptors;
import br.com.conductor.heimdall.core.util.DigestUtils;
import br.com.conductor.heimdall.core.trace.TraceContextHolder;
import com.netflix.zuul.context.RequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static br.com.conductor.heimdall.gateway.util.ConstantsContext.ACCESS_TOKEN;
import static br.com.conductor.heimdall.gateway.util.ConstantsContext.CLIENT_ID;

/**
 * @author Marcelo Aguiar Rodrigues
 */
@Service
public class AccessTokenInterceptorService {

    @Autowired
    private AccessTokenService accessTokenService;

    @Autowired
    private AppService appService;

    @Autowired
    private PlanService planService;

    /**
     * Validates if a access token originated
     *
     * @param apiId    Api id
     */
    public void validate(String apiId) {

        RequestContext context = RequestContext.getCurrentContext();

        String clientId = context.getZuulRequestHeaders().get(CLIENT_ID);
        String accessToken = context.getZuulRequestHeaders().get(ACCESS_TOKEN);

        if (clientId == null) clientId = context.getRequest().getHeader(CLIENT_ID);
        if (accessToken == null) accessToken = context.getRequest().getHeader(ACCESS_TOKEN);

        this.validateAccessToken(apiId, clientId, accessToken);
    }

    /**
     * Method responsible for validating access_token in interceptor
     *
     * @param apiId       Api reference id
     * @param clientId    user client_id
     * @param accessToken access token
     */
    private void validateAccessToken(String apiId, String clientId, String accessToken) {

        final String ACCESS_TOKEN = "Access Token";

        TraceContextHolder.getInstance().getActualTrace().setAccessToken(DigestUtils.digestMD5(accessToken));

        if (clientId == null || clientId.isEmpty()) {
            buildResponse(String.format(ConstantsInterceptors.GLOBAL_CLIENT_ID_OR_ACESS_TOKEN_NOT_FOUND, "Client Id"));
            return;
        }

        if (accessToken != null && !accessToken.isEmpty()) {

            final AccessToken token = accessTokenService.findByCode(accessToken);
            if (token != null) {
                final App app = appService.find(token.getApp());

                if (app != null || token.getStatus().equals(Status.ACTIVE)) {

                    Set<String> apiIds = app.getPlans().parallelStream()
                            .map(plan -> planService.find(plan))
                            .map(Plan::getApiId)
                            .collect(Collectors.toSet())
                            ;

                    if (apiIds.contains(apiId)) {

                        String cId = app.getClientId();
                        if (clientId.equals(cId)) {

                            TraceContextHolder.getInstance().getActualTrace().setApp(app.getName());

                        } else {
                            buildResponse(String.format(ConstantsInterceptors.GLOBAL_CLIENT_ID_OR_ACESS_TOKEN_NOT_FOUND, ACCESS_TOKEN));
                        }
                    } else {
                        buildResponse(ConstantsInterceptors.GLOBAL_ACCESS_NOT_ALLOWED_API);
                    }
                } else {
                    buildResponse(String.format(ConstantsInterceptors.GLOBAL_CLIENT_ID_OR_ACESS_TOKEN_NOT_FOUND, ACCESS_TOKEN));
                }
            }
        } else {
            buildResponse(String.format(ConstantsInterceptors.GLOBAL_CLIENT_ID_OR_ACESS_TOKEN_NOT_FOUND, ACCESS_TOKEN));
        }
    }

    private void buildResponse(String message) {
        RequestContext ctx = RequestContext.getCurrentContext();
        ctx.setSendZuulResponse(false);
        ctx.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        ctx.setResponseBody(message);
    }
}
