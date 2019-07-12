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
package br.com.conductor.heimdall.core.dto;

import br.com.conductor.heimdall.core.entity.App;
import br.com.conductor.heimdall.core.enums.Status;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Set;

/**
 * Class is a Data Transfer Object for the {@link App}.
 *
 * @author Filipe Germano
 *
 */
@Data
public class AppUpdateDTO implements Serializable {

     private static final long serialVersionUID = 4168592185106510648L;

     @NotNull
     @Size(max = 180)
     private String name;

     @Size(max = 200)
     private String description;
     
     @NotNull
     private String developerId;

     private Status status = Status.ACTIVE;
     
     @NotNull
     @Size(min = 1)
     private Set<String> plans;
}
