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
package br.com.conductor.heimdall.gateway.configuration;

import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientConnectionManagerFactory;
import org.springframework.cloud.commons.httpclient.ApacheHttpClientFactory;
import org.springframework.cloud.netflix.zuul.ZuulProxyAutoConfiguration;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.post.SendErrorFilter;
import org.springframework.cloud.netflix.zuul.filters.post.SendResponseFilter;
import org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter;
import org.springframework.cloud.netflix.zuul.filters.route.SimpleHostRoutingFilter;
import org.springframework.cloud.netflix.zuul.metrics.DefaultCounterFactory;
import org.springframework.cloud.netflix.zuul.metrics.EmptyCounterFactory;
import org.springframework.cloud.netflix.zuul.metrics.EmptyTracerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.zuul.monitoring.CounterFactory;
import com.netflix.zuul.monitoring.TracerFactory;

import br.com.conductor.heimdall.core.service.EnvironmentService;
import br.com.conductor.heimdall.gateway.failsafe.CircuitBreakerManager;
import br.com.conductor.heimdall.gateway.filter.CustomHostRoutingFilter;
import br.com.conductor.heimdall.gateway.filter.CustomSendErrorFilter;
import br.com.conductor.heimdall.gateway.filter.CustomSendResponseFilter;
import br.com.conductor.heimdall.gateway.filter.HeimdallDecorationFilter;
import br.com.conductor.heimdall.gateway.listener.StartServer;
import br.com.conductor.heimdall.gateway.router.CredentialService;
import br.com.conductor.heimdall.gateway.util.RequestHelper;
import br.com.conductor.heimdall.gateway.zuul.route.ProxyRouteLocator;
import br.com.conductor.heimdall.gateway.zuul.storage.CacheZuulRouteStorage;
import br.com.conductor.heimdall.gateway.zuul.storage.ZuulRouteStorage;
import io.micrometer.core.instrument.MeterRegistry;

/**
 * {@inheritDoc}
 */
@Configuration
public class ZuulConfiguration extends ZuulProxyAutoConfiguration {

	@Autowired(required = false)
	private Registration registration;

	@Autowired
	private ZuulRouteStorage zuulRouteStore;

	@Autowired
	private DiscoveryClient discovery;

	@Autowired
	private ZuulProperties zuulProperties;

	@Autowired
	private ServerProperties server;

	@Autowired
	private EnvironmentService environmentService;

	@Autowired
	private RequestHelper requestHelper;

	@Qualifier("heimdallErrorController")
	@Autowired(required = false)
	private ErrorController errorController;

	@Autowired
	private CircuitBreakerManager circuitBreakerManager;

	@Autowired
	private CredentialService credentialService;

	@Bean
	public ProxyRouteLocator proxyRouteLocator() {

		return new ProxyRouteLocator(server.getServlet().getContextPath(), discovery, zuulProperties, registration,
				zuulRouteStore);
	}

	@Override
	public PreDecorationFilter preDecorationFilter(RouteLocator routeLocator, ProxyRequestHelper proxyRequestHelper) {

		return new HeimdallDecorationFilter(proxyRouteLocator(), this.server.getServlet().getContextPath(),
				zuulProperties, proxyRequestHelper, requestHelper, credentialService, environmentService);
	}

	@Bean
	public SendResponseFilter sendResponseFilter() {

		return new CustomSendResponseFilter(zuulProperties);
	}

	@Override
	public SendErrorFilter sendErrorFilter() {
		return new CustomSendErrorFilter();
	}

	@Bean
	@ConditionalOnMissingBean({ SimpleHostRoutingFilter.class, CloseableHttpClient.class })
	public SimpleHostRoutingFilter simpleHostRoutingFilter(ProxyRequestHelper helper, ZuulProperties zuulProperties,
			ApacheHttpClientConnectionManagerFactory connectionManagerFactory,
			ApacheHttpClientFactory httpClientFactory) {
		return new CustomHostRoutingFilter(helper, zuulProperties, connectionManagerFactory, httpClientFactory,
				circuitBreakerManager);
	}

	@Bean
	@ConditionalOnMissingBean({ SimpleHostRoutingFilter.class })
	public SimpleHostRoutingFilter simpleHostRoutingFilter(ProxyRequestHelper helper, ZuulProperties zuulProperties,
			CloseableHttpClient httpClient) {
		return new CustomHostRoutingFilter(helper, zuulProperties, httpClient, circuitBreakerManager);
	}

	@Bean
	public StartServer startServeltListenerZuul() {

		return new StartServer();
	}

	@Bean
	public ZuulRouteStorage cacheZuulRouteStore() {

		return new CacheZuulRouteStorage();
	}

	@Override
	public HeimdallHandlerMapping zuulHandlerMapping(RouteLocator routes) {

		HeimdallHandlerMapping handlerMapping = new HeimdallHandlerMapping(proxyRouteLocator(), zuulController());
		handlerMapping.setErrorController(this.errorController);
		return handlerMapping;
	}

	@Bean
	@ConditionalOnMissingBean(CounterFactory.class)
	public CounterFactory counterFactory(MeterRegistry meterRegistry) {
		return new DefaultCounterFactory(meterRegistry);
	}

	@Bean
	@ConditionalOnMissingClass("io.micrometer.core.instrument.MeterRegistry")
	@ConditionalOnMissingBean(CounterFactory.class)
	public CounterFactory counterFactory() {
		return new EmptyCounterFactory();
	}

}
