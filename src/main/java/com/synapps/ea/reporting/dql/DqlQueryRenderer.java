package com.synapps.ea.reporting.dql;

import java.util.List;

import com.synapps.ea.reporting.dql.xml.Column;
import com.synapps.ea.reporting.dql.xml.Condition;
import com.synapps.ea.reporting.dql.xml.From;
import com.synapps.ea.reporting.dql.xml.Join;
import com.synapps.ea.reporting.dql.xml.OrderBy;
import com.synapps.ea.reporting.dql.xml.OrderByColumn;
import com.synapps.ea.reporting.dql.xml.Query;
import com.synapps.ea.reporting.dql.xml.Select;
import com.synapps.ea.reporting.dql.xml.Type;
import com.synapps.ea.reporting.dql.xml.Where;


/**
 * @author Mark Billingham
 *
 */
public class DqlQueryRenderer {
	
	/**
	 * @param query
	 * @return
	 */
	public String renderQuery(Query query) {
		StringBuilder builder = new StringBuilder();
		
		Select selectClause = query.getSelect();
		
		if (null != selectClause) {
			renderSelect(selectClause, builder);
		}
		
		From fromClause = query.getFrom();
		
		if (null != fromClause) {
			renderFrom(fromClause, builder);
		}
		
		Where whereClause = query.getWhere();
		
		if (null != whereClause) {
			renderWhere(whereClause, builder);
		}
		
		OrderBy orderBy = query.getOrderBy();

		if (null != orderBy) {
			renderOrderBy(orderBy, builder);
		}
		return builder.toString();
	}


	private void renderSelect(Select selectClause, StringBuilder builder) {
		List<Column> columns = selectClause.getColumns();
		builder.append("SELECT ");
		if (Boolean.TRUE.equals(selectClause.getDistinct())) {
			builder.append("DISTINCT ");
		}
		boolean first = true;
		for (Column column : columns) {
			if (! first) {
				builder.append(", ");
			}
			renderColumn(column, builder);
			first = false;
		}
	}

	private void renderFrom(From fromClause, StringBuilder builder) {
		List<Type> types = fromClause.getTypes();
		builder.append("\nFROM ");
		boolean first = true;
		for (Type type : types) {
			Join join = type.getJoinCondition();
			if (null != join) {
				builder.append("\n").append(join.getJoinType()).append(" JOIN ");
			} else if (! first) {
				builder.append(", ");
			}
			builder.append(type.getName());
			
			if (null != type.getAlias()) {
				builder.append(" ").append(type.getAlias());
			}
			if (null != join) {
				builder.append(" ON ");
				renderColumn(join.getLeftColumn(), builder);
				builder.append(join.getCondition());
				renderColumn(join.getRightColumn(), builder);
			}
			first = false;
		}
		
	}

	private void renderWhere(Where whereClause, StringBuilder builder) {
		List<Condition> conditions = whereClause.getConditons();
		
		boolean first = true;
		for (Condition condition : conditions) {
			if (first) {
				builder.append("\nWHERE ");
			} else {
				builder.append(" ");
			}
			
			if (null != condition.getLogicOp() && ! first) {
				builder.append(condition.getLogicOp()).append(" ");
			}
			if (Boolean.TRUE.equals(condition.getGroupBegin())) {
				builder.append("(");
			}
			if (Boolean.TRUE.equals(condition.getAnyValue())) {
				builder.append("ANY ");
			}
			builder.append(condition.getLeftValue());
			builder.append(" ").append(condition.getCompareOp()).append(" ");
			builder.append(condition.getRightValue());
			if (Boolean.TRUE.equals(condition.getGroupEnd())) {
				builder.append(")");
			}
			first = false;
		}
	}

	private void renderOrderBy(OrderBy orderBy, StringBuilder builder) {
		List<OrderByColumn> columns = orderBy.getOrderBy();
		boolean first = true;
		for (OrderByColumn column : columns) {
			if (first) {
				builder.append("\nORDER BY ");
			} else {
				builder.append(", ");
			}
			builder.append(column.getName());
			if (null != column.getOrdering()) {
				builder.append(" ").append(column.getOrdering());
			}
			first = false;
		}
		
	}

	private void renderColumn(Column column, StringBuilder builder) {
		String type = column.getTypeAlias();
		if (null != type && type.length() > 0) {
			builder.append(type).append(".");
		}
		builder.append(column.getAttribute());
		String asName = column.getAsName();
		if (null != asName) {
			builder.append(" AS ").append(asName);
		}
		
	}
}
