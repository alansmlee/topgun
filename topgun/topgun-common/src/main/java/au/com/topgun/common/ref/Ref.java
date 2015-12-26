package au.com.topgun.common.ref;

import au.com.topgun.common.exception.DataException;

public class Ref {

    public static enum RunSQLOption {
        DEFAULT, NORMAL, IGNORE_SQLEXCEPTION;
        public static void validate(String value) throws DataException {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equalsIgnoreCase(value)) {
                    return;
                }
            }
            final StringBuilder sb = new StringBuilder();
            for (RunSQLOption option : RunSQLOption.values()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(option.name());
            }
            throw new DataException(String.format("Invalid PreRunSQLOption[%s]. Valid values are [%s]", value, sb.toString()));
        }
    }

    public static enum VerifyFileLineOption {
        DEFAULT, MATCH_EXACT, MATCH_USING_INDEX;
        public static void validate(String value) throws DataException {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equalsIgnoreCase(value)) {
                    return;
                }
            }
            final StringBuilder sb = new StringBuilder();
            for (VerifyFileLineOption option : VerifyFileLineOption.values()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(option.name());
            }
            throw new DataException(String.format("Invalid VerifyFileOption[%s]. Valid values are [%s]", value, sb.toString()));
        }
    }

    public static enum VerifySQLResultSetOptions {
        MATCH_EXACT, MATCH_EXACT_IN_ORDER, MATCH_TOP_N_IN_ORDER, MATCH_ANY;
        public static void validate(String value) throws DataException {
            for (int i = 0; i < values().length; i++) {
                if (values()[i].name().equalsIgnoreCase(value)) {
                    return;
                }
            }
            final StringBuilder sb = new StringBuilder();
            for (VerifySQLResultSetOptions option : VerifySQLResultSetOptions.values()) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(option.name());
            }
            throw new DataException(String.format("Invalid VerifySQLResultSetOptions[%s]. Valid values are [%s]", value, sb.toString()));
        }
    }
}
