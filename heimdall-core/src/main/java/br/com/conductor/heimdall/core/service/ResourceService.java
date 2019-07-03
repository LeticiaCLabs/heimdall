/*
 * Copyright (C) 2018 Conductor Tecnologia SA
 *
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
 */
package br.com.conductor.heimdall.core.service;

import br.com.conductor.heimdall.core.converter.GenericConverter;
import br.com.conductor.heimdall.core.dto.ResourceDTO;
import br.com.conductor.heimdall.core.entity.Api;
import br.com.conductor.heimdall.core.entity.Operation;
import br.com.conductor.heimdall.core.entity.Resource;
import br.com.conductor.heimdall.core.exception.HeimdallException;
import br.com.conductor.heimdall.core.repository.ResourceRepository;
import br.com.conductor.heimdall.core.service.amqp.AMQPRouteService;
import br.com.conductor.heimdall.core.util.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static br.com.conductor.heimdall.core.exception.ExceptionMessage.GLOBAL_NOT_FOUND;
import static br.com.conductor.heimdall.core.exception.ExceptionMessage.ONLY_ONE_RESOURCE_PER_API;

/**
 * This class provides methos to create, read, update and delete a {@link Resource} resource.
 *
 * @author Filipe Germano
 * @author Marcelo Aguiar Rodrigues
 */
@Service
public class ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private ApiService apiService;

    @Autowired
    private OperationService operationService;

    @Autowired
    private InterceptorService interceptorService;

    @Autowired
    private AMQPRouteService amqpRoute;


    /**
     * Finds a {@link Resource} by its Id and {@link Api} Id.
     *
     * @param apiId      The {@link Api} Id
     * @param resourceId The {@link Resource} Id
     * @return The {@link Resource} found
     */
    @Transactional(readOnly = true)
    public Resource find(final String apiId, final String resourceId) {

        apiService.find(apiId);

        return this.find(resourceId);
    }

    public Resource find(final String id) {
        Resource resource = resourceRepository.findOne(id);
        HeimdallException.checkThrow(resource == null, GLOBAL_NOT_FOUND, "Resource");

        return resource;
    }

    /**
     * Generates a paged list of {@link Resource} from a request.
     *
     * @param apiId    The {@link Api} Id
     * @param pageable The {@link Pageable}
     * @return The paged {@link Resource} list
     */
    public Page<Resource> list(final String apiId, final Pageable pageable) {

        final List<Resource> resources = this.list(apiId);

        return new PageImpl<>(resources, pageable, resources.size());
    }

    /**
     * Generates a list of {@link Resource} from a request.
     *
     * @param apiId The {@link Api} Id
     * @return The List of {@link Resource}
     */
    public List<Resource> list(String apiId) {

        apiService.find(apiId);

        final List<Resource> resources = resourceRepository.findAll();

        return resources.stream()
                .filter(resource -> apiId.equals(resource.getApiId()))
                .collect(Collectors.toList());
    }

    /**
     * Saves a {@link Resource} to the repository.
     *
     * @param apiId    The {@link Api} Id
     * @param resource The {@link Resource}
     * @return The saved {@link Resource}
     */
    public Resource save(final String apiId, final Resource resource) {

        final Api api = apiService.find(apiId);

        final boolean anyMatch = this.list(apiId).stream()
                .anyMatch(res -> resource.getName().equals(res.getName()));

        HeimdallException.checkThrow(anyMatch, ONLY_ONE_RESOURCE_PER_API);

        resource.setApiId(apiId);

        final Resource savedResource = resourceRepository.save(resource);

        api.addResource(savedResource.getId());

        apiService.update(api.getId(), api);

        amqpRoute.dispatchRoutes();

        return savedResource;
    }

    /**
     * Updates a {@link Resource} by its Id and {@link Api} Id
     *
     * @param apiId           The {@link Api} Id
     * @param resourceId      The {@link Resource} Id
     * @param resourcePersist The {@link ResourceDTO}
     * @return The updated {@link Resource}
     */
    public Resource update(String apiId, String resourceId, Resource resourcePersist) {

        final Resource resource = this.find(apiId, resourceId);

        final Resource resData = this.list(apiId).stream()
                .filter(res -> resourcePersist.getName().equals(res.getName()))
                .findAny()
                .orElse(null);
        HeimdallException.checkThrow(resData != null &&
                !Objects.equals(resData.getId(), resource.getId()), ONLY_ONE_RESOURCE_PER_API);

        final Resource updatedResource = GenericConverter.mapper(resourcePersist, resource);

        final Resource savedResource = resourceRepository.save(updatedResource);

        amqpRoute.dispatchRoutes();

        return savedResource;
    }

    public Resource update(Resource resource) {
        return this.update(resource.getApiId(), resource.getId(), resource);
    }

    /**
     * TODO
     * Deletes a {@link Resource} by its Id.
     *
     * @param apiId      The {@link Api} Id
     * @param resourceId The {@link Resource} Id
     */
    public void delete(String apiId, String resourceId) {

        Resource resource = this.find(apiId, resourceId);

        // Deletes all operations attached to the Resource
        operationService.deleteAllfromResource(apiId, resourceId);
//
//        // Deletes all interceptors attached to the Resource
// TODO       interceptorService.deleteAllfromResource(resourceId);

        resourceRepository.delete(resource.getId());

        apiService.removeResource(resource);

        amqpRoute.dispatchRoutes();
    }

    /**
     * Deletes all Resources from a Api TODO
     *
     * @param apiId Api with the Resources
     */
    public void deleteAllFromApi(String apiId) {
        List<Resource> resources = this.list(apiId);
        resources.forEach(resource -> this.delete(apiId, resource.getId()));
    }

    public void removeOperation(Operation operation) {
        Resource resource = this.find(operation.getResourceId());

        resource.removeOperation(operation.getId());

        this.update(resource);
    }
}
