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
import br.com.conductor.heimdall.core.dto.PageableDTO;
import br.com.conductor.heimdall.core.dto.persist.AccessTokenPersist;
import br.com.conductor.heimdall.core.entity.AccessToken;
import br.com.conductor.heimdall.core.service.AccessTokenService;
import br.com.conductor.heimdall.core.util.ConstantsTag;
import br.com.conductor.heimdall.core.util.Pageable;
import io.swagger.annotations.ApiOperation;
import org.modelmapper.PropertyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

import static br.com.conductor.heimdall.core.util.ConstantsPath.PATH_ACCESS_TOKENS;

/**
 * Uses a {@link AccessTokenService} to provide methods to create, read, update and delete a {@link AccessToken}.
 *
 * @author Filipe Germano
 */
@io.swagger.annotations.Api(
        value = PATH_ACCESS_TOKENS,
        produces = MediaType.APPLICATION_JSON_VALUE,
        tags = {ConstantsTag.TAG_ACCESS_TOKENS})
@RestController
@RequestMapping(value = PATH_ACCESS_TOKENS)
public class AccessTokenResource {

    @Autowired
    private AccessTokenService accessTokenService;

    /**
     * Finds a {@link AccessToken} by its Id.
     *
     * @param id The AccessToken Id
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Find AccessToken by id", response = AccessToken.class)
    @GetMapping(value = "/{accessTokenId}")
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_READ_ACCESSTOKEN)
    public ResponseEntity<?> findById(@PathVariable("accessTokenId") String id) {

        AccessToken accessToken = accessTokenService.find(id);

        return ResponseEntity.ok(accessToken);
    }

    /**
     * Finds all {@link AccessToken}
     *
     * @param pageableDTO {@link PageableDTO}
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Find all AccessTokens", responseContainer = "List", response = AccessToken.class)
    @GetMapping
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_READ_ACCESSTOKEN)
    public ResponseEntity<?> findAll(@ModelAttribute PageableDTO pageableDTO) {

        if (!pageableDTO.isEmpty()) {
            Pageable pageable = Pageable.setPageable(pageableDTO.getOffset(), pageableDTO.getLimit());
            Page<AccessToken> accessTokenPage = accessTokenService.list(pageable);

            return ResponseEntity.ok(accessTokenPage);
        } else {
            List<AccessToken> accessTokens = accessTokenService.list();

            return ResponseEntity.ok(accessTokens);
        }
    }

    /**
     * Saves a {@link AccessToken}.
     *
     * @param accessTokenPersist {@link AccessTokenPersist}
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Save a new AccessToken")
    @PostMapping
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_CREATE_ACCESSTOKEN)
    public ResponseEntity<?> save(@RequestBody @Valid AccessTokenPersist accessTokenPersist) {

        AccessToken accessToken = GenericConverter.mapper(accessTokenPersist, AccessToken.class);
        accessToken = accessTokenService.save(accessToken);

        return ResponseEntity.created(URI.create(String.format("/%s/%s", "access-tokens", accessToken.getId()))).build();
    }

    /**
     * Updates a {@link AccessToken}.
     *
     * @param id                 The AccessToken Id
     * @param accessTokenPersist {@link AccessTokenPersist}
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Update AccessToken")
    @PutMapping(value = "/{accessTokenId}")
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_UPDATE_ACCESSTOKEN)
    public ResponseEntity<?> update(@PathVariable("accessTokenId") String id,
                                    @RequestBody AccessTokenPersist accessTokenPersist) {

        PropertyMap<AccessTokenPersist, AccessToken> propertyMap = new PropertyMap<AccessTokenPersist, AccessToken>() {
            @Override
            protected void configure() {
                skip(destination.getCode());
            }
        };

        AccessToken accessToken = GenericConverter.convertWithMapping(accessTokenPersist, AccessToken.class, propertyMap);
        accessToken = accessTokenService.update(id, accessToken);

        return ResponseEntity.ok(accessToken);
    }

    /**
     * Deletes a {@link AccessToken}.
     *
     * @param id The AccessToken Id
     * @return {@link ResponseEntity}
     */
    @ResponseBody
    @ApiOperation(value = "Delete AccessToken")
    @DeleteMapping(value = "/{accessTokenId}")
    @PreAuthorize(ConstantsPrivilege.PRIVILEGE_DELETE_ACCESSTOKEN)
    public ResponseEntity<?> delete(@PathVariable("accessTokenId") String id) {

        accessTokenService.delete(id);

        return ResponseEntity.noContent().build();
    }

}
