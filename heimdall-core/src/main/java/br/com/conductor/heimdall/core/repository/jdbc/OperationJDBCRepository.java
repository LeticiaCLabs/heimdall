///*
// * Copyright (C) 2018 Conductor Tecnologia SA
// *
// * Licensed under the Apache License, Version 2.0 (the "License")
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package br.com.conductor.heimdall.core.repository.jdbc;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.sql.DataSource;
//
//import org.springframework.jdbc.core.BeanPropertyRowMapper;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
//import org.springframework.stereotype.Repository;
//
//import br.com.conductor.heimdall.core.entity.Operation;
//
//@Repository
//public class OperationJDBCRepository {
//
//	private JdbcTemplate jdbcTemplate;
//
//	public OperationJDBCRepository(DataSource dataSource) {
//		this.jdbcTemplate = new JdbcTemplate(dataSource);
//	}
//
//	public List<String> findOperationsFromAllApis(List<Long> apiIds) {
//
//		Map<String, Object> params = new HashMap<>();
//		params.put("ids", apiIds);
//
//		StringBuilder sql = new StringBuilder(190);
//		sql.append("SELECT CONCAT(API.BASE_PATH, OP.PATH) ");
//		sql.append("FROM OPERATIONS OP ");
//		sql.append("INNER JOIN RESOURCES RES ON OP.RESOURCE_ID = RES.ID ");
//		sql.append("INNER JOIN APIS API ON RES.API_ID = API.ID ");
//		sql.append("WHERE API.ID IN (:ids) ");
//
//		return new NamedParameterJdbcTemplate(jdbcTemplate).queryForList(sql.toString(), params, String.class);
//	}
//
//}
