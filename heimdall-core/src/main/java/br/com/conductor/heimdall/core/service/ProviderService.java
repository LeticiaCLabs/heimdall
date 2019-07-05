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
import br.com.conductor.heimdall.core.dto.PageableDTO;
import br.com.conductor.heimdall.core.dto.ProviderDTO;
import br.com.conductor.heimdall.core.dto.ProviderParamsDTO;
import br.com.conductor.heimdall.core.entity.Provider;
import br.com.conductor.heimdall.core.entity.ProviderParam;
import br.com.conductor.heimdall.core.exception.ExceptionMessage;
import br.com.conductor.heimdall.core.exception.HeimdallException;
import br.com.conductor.heimdall.core.repository.ProviderRepository;
import br.com.conductor.heimdall.core.util.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.ExampleMatcher.StringMatcher;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This class provides methos to create, read, update and delete a {@link Provider}
 *
 * @author <a href="https://dijalmasilva.github.io" target="_blank">Dijalma Silva</a>
 */
@Service
public class ProviderService {

    @Autowired
    private ProviderRepository providerRepository;

    /**
     * Saves a {@link Provider} to the repository
     *
     * @param providerPersist The {@link ProviderDTO}
     * @return The saved {@link Provider}
     */
    public Provider save(ProviderDTO providerPersist) {
        Provider provider = GenericConverter.mapper(providerPersist, Provider.class);
        provider.setProviderDefault(false);
        provider.getProviderParams().forEach(p -> p.setProvider(provider));
        return this.providerRepository.save(provider);
    }

    /**
     * Edits a {@link Provider} by its Id
     *
     * @param id   The {@link Provider} Id
     * @param providerEdit The {@link ProviderDTO}
     * @return The edited {@link Provider}
     */
    @Transactional
    public Provider edit(String id, ProviderDTO providerEdit) {
        Provider found = this.find(id);

        HeimdallException.checkThrow(found.isProviderDefault(), ExceptionMessage.DEFAULT_PROVIDER_CAN_NOT_UPDATED_OR_REMOVED);

        found.setName(providerEdit.getName());
        found.setPath(providerEdit.getPath());
        if (Objects.nonNull(providerEdit.getDescription())) {
            found.setDescription(providerEdit.getDescription());
        }

        List<ProviderParam> providers = providerEdit.getProviderParams().stream()
                .map(providerParamsDTO -> getProviderParam(providerParamsDTO, found)).collect(Collectors.toList());

        found.getProviderParams().clear();
        found.getProviderParams().addAll(providers);

        return this.providerRepository.save(found);
    }

    /**
     * Generates a paged list of {@link Provider} from a request
     *
     * @param providerDTO The {@link ProviderDTO}
     * @param pageableDTO The {@link PageableDTO}
     * @return The paged {@link Provider} list
     */
    public Page<Provider> listWithPageableAndFilter(ProviderDTO providerDTO, PageableDTO pageableDTO) {

        Provider provider = GenericConverter.mapper(providerDTO, Provider.class);
        Example<Provider> example = Example.of(provider,
                ExampleMatcher.matching().withIgnorePaths("providerDefault").withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING));

        Pageable pageable = Pageable.setPageable(pageableDTO.getOffset(), pageableDTO.getLimit());

        return this.providerRepository.findAll(example, pageable);
    }

    /**
     * Generates a list of {@link Provider} from a request
     *
     * @param providerDTO The {@link ProviderDTO}
     * @return The list of {@link Provider}
     */
    public List<Provider> listWithFilter(ProviderDTO providerDTO) {

        Provider provider = GenericConverter.mapper(providerDTO, Provider.class);
        Example<Provider> example = Example.of(provider,
                ExampleMatcher.matching().withIgnorePaths("providerDefault").withIgnoreCase().withStringMatcher(StringMatcher.CONTAINING));

        return this.providerRepository.findAll(example);
    }

    /**
     * Finds a {@link Provider} by its Id
     *
     * @param id The {@link Provider} Id
     * @return The {@link Provider}
     */
    public Provider find(String id) {

        Provider provider = providerRepository.findById(id).orElse(null);

        HeimdallException.checkThrow(Objects.isNull(provider), ExceptionMessage.GLOBAL_NOT_FOUND, "Provider");

        return provider;
    }

    /**
     * Deletes a {@link Provider} by its Id
     *
     * @param id The {@link Provider} Id
     */
    public void delete(String id) {
        final Provider provider = this.find(id);

        this.providerRepository.delete(provider);
    }

    protected ProviderParam getProviderParam(ProviderParamsDTO p, Provider provider) {
        ProviderParam providerParam = new ProviderParam();
        providerParam.setProvider(provider);
        providerParam.setValue(p.getValue());
        providerParam.setLocation(p.getLocation());
        providerParam.setName(p.getName());
        return providerParam;
    }
}
