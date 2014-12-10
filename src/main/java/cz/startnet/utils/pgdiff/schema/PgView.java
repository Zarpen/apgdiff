/**
 * Copyright 2006 StartNet s.r.o.
 *
 * Distributed under MIT license
 */
package cz.startnet.utils.pgdiff.schema;

import cz.startnet.utils.pgdiff.PgDiffUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores view information.
 *
 * @author fordfrog
 */
public class PgView extends PgRelation {

    /**
     * Were column names explicitly declared as part of the view?
     */
    private boolean declareColumnNames = false;
    /**
     * SQL query of the view.
     */
    private String query;

    /**
     * Creates a new PgView object.
     *
     * @param name {@link #name}
     */
    public PgView(final String name) {
        setName(name);
    }

    /**
     * Sets the list of declared column names for the view.
     *
     * @param columnNames list of column names
     */
    public void setDeclaredColumnNames(final List<String> columnNames) {
        // Can only be set once for a view, before defaults/comments are set
        assert !declareColumnNames;
        assert columns.isEmpty();

        if (columnNames == null || columnNames.isEmpty())
            return;

        declareColumnNames = true;

        for (final String colName: columnNames) {
            addColumn(new PgColumn(colName));
        }
    }

    /**
     * Returns a list of column names if the names were declared along with the view, null otherwise.
     *
     * @return list of column names or null
     */
    public List<String> getDeclaredColumnNames() {
        @SuppressWarnings("CollectionWithoutInitialCapacity")
        final List<String> list = new ArrayList<String>();

        if (!declareColumnNames)
            return null;

        for (PgColumn column : columns) {
            list.add(column.getName());
        }

        return list;
    }

    /**
     * Creates and returns SQL for creation of the view.
     *
     * @return created SQL statement
     */
    public String getCreationSQL() {
        final StringBuilder sbSQL = new StringBuilder(query.length() * 2);
        sbSQL.append("CREATE VIEW ");
        sbSQL.append(PgDiffUtils.getQuotedName(name));

        if (declareColumnNames) {
            assert columns != null && !columns.isEmpty();

            sbSQL.append(" (");

            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    sbSQL.append(", ");
                }

                sbSQL.append(PgDiffUtils.getQuotedName(columns.get(i).getName()));
            }
            sbSQL.append(')');
        }

        sbSQL.append(" AS\n\t");
        sbSQL.append(query);
        sbSQL.append(';');

        /* Column default values */
        for (final PgColumn col : getColumns()) {
            String defaultValue = col.getDefaultValue();

            if (defaultValue != null && !defaultValue.isEmpty()) {
                sbSQL.append("\n\nALTER VIEW ");
                sbSQL.append(PgDiffUtils.getQuotedName(name));
                sbSQL.append(" ALTER COLUMN ");
                sbSQL.append(PgDiffUtils.getQuotedName(col.getName()));
                sbSQL.append(" SET DEFAULT ");
                sbSQL.append(defaultValue);
                sbSQL.append(';');
            }
        }

        if (comment != null && !comment.isEmpty()) {
            sbSQL.append("\n\nCOMMENT ON VIEW ");
            sbSQL.append(PgDiffUtils.getQuotedName(name));
            sbSQL.append(" IS ");
            sbSQL.append(comment);
            sbSQL.append(';');
        }

        sbSQL.append(getColumnCommentDefinition());

        return sbSQL.toString();
    }

    /**
     * Creates and returns SQL statement for dropping the view.
     *
     * @return created SQL statement
     */
    public String getDropSQL() {
        return "DROP VIEW " + PgDiffUtils.getQuotedName(getName()) + ";";
    }

    /**
     * Setter for {@link #query}.
     *
     * @param query {@link #query}
     */
    public void setQuery(final String query) {
        this.query = query;
    }

    /**
     * Getter for {@link #query}.
     *
     * @return {@link #query}
     */
    public String getQuery() {
        return query;
    }

    /**
     * Adds/replaces column default value specification.
     *
     * @param columnName   column name
     * @param defaultValue default value
     */
    public void addColumnDefaultValue(final String columnName,
            final String defaultValue) {
        PgColumn col = getColumn(columnName);
        if (col == null) {
            // If names were declared, we should already know it
            assert !declareColumnNames;
            col = new PgColumn(columnName);
            addColumn(col);
        }
        col.setDefaultValue(defaultValue);
    }

    /**
     * Removes column default value if present.
     *
     * @param columnName column name
     */
    public void removeColumnDefaultValue(final String columnName) {
        addColumnDefaultValue(columnName, null);
    }

    /**
     * Adds/replaces column comment.
     *
     * @param columnName column name
     * @param comment    comment
     */
    public void addColumnComment(final String columnName,
            final String comment) {
        PgColumn col = getColumn(columnName);
        if (col == null) {
            // If names were declared, we should already know it
            assert !declareColumnNames;
            col = new PgColumn(columnName);
            addColumn(col);
        }
        col.setComment(comment);
    }

    /**
     * Removes column comment if present.
     *
     * @param columnName column name
     */
    public void removeColumnComment(final String columnName) {
        addColumnComment(columnName, null);
    }
}
