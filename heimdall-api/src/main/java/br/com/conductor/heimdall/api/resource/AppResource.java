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
package br.com.conductor.heimdall.api.resource;

import br.com.conductor.heimdall.api.util.ConstantsPrivilege;
import br.com.conductor.heimdall.core.converter.GenericConverter;
import br.com.conductor.heimdall.core.dto.AppUpdateDTO;
import br.com.conductor.heimdall.core.dto.PageableDTO;
import br.com.conductor.heimdall.core.dto.AppDTO;
import br.com.conductor.heimdall.core.entity.App;
import br.com.conductor.heimdall.core.service.AppService;
import br.com.conductor.heimdall.core.util.ConstantsTag;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static br.com.conductor.heimdall.core.util.ConstantsPath.PATH_APPS;

/**
 * Uses the {@link AppService} to provide methods to create, read, update and delete a {@link App}.
 *
 * @author Filipe Germano
 * @author <a href="https://dijalmasilva.github.io" target="_blank">Dijalma Silva</a>
 */
@io.swagger.annotations.Api(
        value = PATH_APPS,
        produces = MediaType.APPLICATION_JSON_VALUE,
        tags = {ConstantsTag.TAG_APPS})
@RestController
@RequestMapping(value = PATH_APPS)
public class AppResource {

    @Autowired
    private AppService appService;

    /**
     * Finds a {@link App} by its Id.
     *
     * @param id The App Id
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Find App by id", response = App.class)
    @GetMapping(value = "/{appId}")
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_READ_APP)
    public ResponseEntity<?> findById(@PathVariable("appId") String id) {

        App app = appService.find(id);

        return ResponseEntity.ok(app);
    }

    /**
     * Finds all {@link App} from a request.
     *
     * @param pageableDTO {@link PageableDTO}
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Find all Apps", responseContainer = "List", response = App.class)
    @GetMapping
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_READ_APP)
    public ResponseEntity<?> findAll(@ModelAttribute PageableDTO pageableDTO) {

        if (!pageableDTO.isEmpty()) {

            final Pageable pageable = PageRequest.of(pageableDTO.getPage(), pageableDTO.getLimit());
            Page<App> appPage = appService.list(pageable);

            return ResponseEntity.ok(appPage);
        } else {

            List<App> apps = appService.list();

            return ResponseEntity.ok(apps);
        }
    }

    /**
     * Saves a {@link App}.
     *
     * @param appDTO {@link AppUpdateDTO}
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Save a new App")
    @PostMapping
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_CREATE_APP)
    public ResponseEntity<?> save(@RequestBody @Valid AppDTO appDTO) {

        App app = GenericConverter.mapper(appDTO, App.class);
        app = appService.save(app);

        return ResponseEntity.created(URI.create(String.format("/%s/%s", "apps", app.getId()))).build();
    }

    /**
     * Updates a {@link App}.
     *
     * @param id     The App Id
     * @param appUpdateDTO {@link AppUpdateDTO}
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Update App")
    @PutMapping(value = "/{appId}")
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_UPDATE_APP)
    public ResponseEntity<?> update(@PathVariable("appId") String id,
                                    @RequestBody AppUpdateDTO appUpdateDTO) {

        App app = GenericConverter.mapper(appUpdateDTO, App.class);
        app = appService.update(id, app);

        return ResponseEntity.ok(app);
    }

    /**
     * Deletes a {@link App}.
     *
     * @param id The App Id
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Delete App")
    @DeleteMapping(value = "/{appId}")
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_DELETE_APP)
    public ResponseEntity<?> delete(@PathVariable("appId") String id) {

        appService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
