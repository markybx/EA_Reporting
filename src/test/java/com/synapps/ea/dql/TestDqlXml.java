package com.synapps.ea.dql;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;

import com.synapps.ea.reporting.dql.DqlQueryRenderer;
import com.synapps.ea.reporting.dql.xml.Column;
import com.synapps.ea.reporting.dql.xml.Condition;
import com.synapps.ea.reporting.dql.xml.Join;
import com.synapps.ea.reporting.dql.xml.OrderBy;
import com.synapps.ea.reporting.dql.xml.From;
import com.synapps.ea.reporting.dql.xml.OrderByColumn;
import com.synapps.ea.reporting.dql.xml.Query;
import com.synapps.ea.reporting.dql.xml.Select;
import com.synapps.ea.reporting.dql.xml.Type;
import com.synapps.ea.reporting.dql.xml.Where;


public class TestDqlXml {

	@Test
	public void testCreateTemplate() {
		Query theQuery = new Query();
		Select selectClause = new Select();
		From fromClause = new From();
		Where whereClause = new Where();
		OrderBy orderBy = new OrderBy();
		theQuery.setSelect(selectClause);
		theQuery.setFrom(fromClause);
		theQuery.setWhere(whereClause);
		theQuery.setOrderBy(orderBy);
		
		List<Column> columns = selectClause.getColumns();
		columns.add(createColumn("res", "r_object_id", "reservoir_id"));
		columns.add(createColumn("res", "reservoir_name_registered", null));
		columns.add(createColumn("pfa", "area_name", "public_area_name"));
		columns.add(createColumn("pfa", "area_type", "public_area_type"));
		columns.add(createColumn("wmr", "region_name", "water_mgmt_region_name"));
		columns.add(createColumn("wmr", "region_type", "water_mgmt_region_type"));
		columns.add(createColumn("dam", "dam_top_level", null));
		columns.add(createColumn("dam", "outlet_type_and_location", null));
		columns.add(createColumn("u", "registered_paon", null));
		columns.add(createColumn("u", "undertaker_name", null));
		columns.add(createColumn("ucon", "forename", null));
		columns.add(createColumn("ucon", "street", null));
		
		List<Type> fromTypes = fromClause.getTypes();
		fromTypes.add(createType("ea_reservoir", "res", null, null, null, null, null, null));
		fromTypes.add(createType("ea_resv_water_mgt_area", "wma_res", "LEFT OUTER", "wma_res", "parent_id", "=", "res", "r_object_id"));
		fromTypes.add(createType("ea_area", "wma", "LEFT OUTER", "wma", "r_object_id", "=", "wma_res", "child_id"));
		fromTypes.add(createType("ea_resv_pub_face_area", "pfa_res", "LEFT OUTER", "pfa_res", "parent_id", "=", "res", "r_object_id"));
		fromTypes.add(createType("ea_area", "pfa", "LEFT OUTER", "pfa", "r_object_id", "=", "pfa_res", "child_id"));
		fromTypes.add(createType("ea_res_pub_face_region", "pfr_res", "LEFT OUTER", "pfr_res", "parent_id", "=", "res", "r_object_id"));
		fromTypes.add(createType("ea_region", "pfr", "LEFT OUTER", "pfr", "r_object_id", "=", "pfr_res", "child_id"));
		fromTypes.add(createType("ea_resv_water_mgt_reg", "wmr_res", "LEFT OUTER", "wmr_res", "parent_id", "=", "res", "r_object_id"));
		fromTypes.add(createType("ea_region", "wmr", "LEFT OUTER", "wmr", "r_object_id", "=", "wmr_res", "child_id"));
		fromTypes.add(createType("ea_reservoir_dam", "dam_res", "LEFT OUTER", "dam_res", "parent_id", "=", "res", "r_object_id"));
		fromTypes.add(createType("ea_dam", "dam", "LEFT OUTER", "dam", "r_object_id", "=", "dam_res", "child_id"));
		fromTypes.add(createType("ea_undertaker_resv", "u_res", "LEFT OUTER", "res", "r_object_id", "=", "u_res", "child_id"));
		fromTypes.add(createType("ea_undertaker", "u", "LEFT OUTER", "u_res", "parent_id", "=", "u", "r_object_id"));
		fromTypes.add(createType("ea_undertaker_contacts", "u_ucon", "LEFT OUTER", "u", "r_object_id", "=", "u_ucon", "child_id"));
		fromTypes.add(createType("ea_undertaker_contact", "ucon", "LEFT OUTER", "u_ucon", "parent_id", "=", "ucon", "r_object_id"));
		
		List<Condition> conditions = whereClause.getConditons();
		conditions.add(createWhereCondition(null, "res.physical_status", "=", "'In operation'"));
		conditions.add(createWhereCondition("AND", "pfa.area_name", "=", "'Central'"));
		
		List<OrderByColumn>  orderByList = orderBy.getOrderBy();
		orderByList.add(createOrderColumn("res.r_object_id", null));
		orderByList.add(createOrderColumn("pfa.area_name", "DESC"));
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Query.class);
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
			StreamResult result = new StreamResult( new FileOutputStream("exampleTemplate.xml") );

			marshaller.marshal( theQuery, result );
			
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println();
	}
	
	private OrderByColumn createOrderColumn(String name, String ordering) {
		OrderByColumn column = new OrderByColumn();
		column.setName(name);
		column.setOrdering(ordering);
		return column;
	}

	@Test
	public void testReadAndRenderDql() {
		Query theQuery = null;
		DqlQueryRenderer renderer = new DqlQueryRenderer();
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(Query.class);
			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			theQuery = (Query) unMarshaller.unmarshal(new FileInputStream("exampleTemplate.xml"));
			renderer.renderQuery(theQuery);
		
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private Column createColumn(String type, String attribute, String asName) {
		Column column = new Column();
		column.setAttribute(attribute);
		column.setTypeAlias(type);
		column.setAsName(asName);
		return column;
	}
	
	private Type createType( 
			String typeName, 
			String typeAlias,
			String joinType,
			String joinLeftType, 
			String joinLeftAttr, 
			String joinCondition, 
			String joinRightType, 
			String joinRightAttr) {

		Type type = new Type();
		type.setName(typeName);
		type.setAlias(typeAlias);
		
		if (joinType != null && joinLeftAttr != null && joinCondition != null && joinRightAttr != null) {
			Join join = new Join();
			join.setJoinType(joinType);
			join.setCondition(joinCondition);
			Column col;
			col = new Column();
			col.setTypeAlias(joinLeftType);
			col.setAttribute(joinLeftAttr);
			join.setLeftColumn(col);
			col = new Column();
			col.setTypeAlias(joinRightType);
			col.setAttribute(joinRightAttr);
			join.setRightColumn(col);
			type.setJoinCondition(join);
		}
		
		return type;
	}
	
	private Condition createWhereCondition(String logicOp, String leftValue, String comparer, String rightValue) {
		Condition condition = new Condition();
		condition.setLogicOp(logicOp);
		condition.setLeftValue(leftValue);
		condition.setCompareOp(comparer);
		condition.setRightValue(rightValue);
		return condition;
	}
}
